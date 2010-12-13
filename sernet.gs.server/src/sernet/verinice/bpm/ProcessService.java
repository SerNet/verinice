/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.stream.ResourceStreamInput;
import org.jbpm.pvm.internal.xml.Parse;

import sernet.verinice.interfaces.IProcessService;
import sernet.verinice.model.iso27k.Control;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessService implements IProcessService {

    private final Logger log = Logger.getLogger(ProcessService.class);

    private ProcessEngine processEngine;

    protected Set<String> processDefinitions;
    
    private boolean wasInitCalled = false;

    public void init() {
        synchronized (this) {
            if(!wasInitCalled) {
                doInit();
            }
        }
    }
    
    /**
     * Dont call this unsyncronised method, Call: init()
     */
    private void doInit() {
        if(!wasInitCalled) {
            try {
                for (String resource : getProcessDefinitions()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Loading process definition from resource: " + resource + "...");
                    }                 
                    List<ProcessDefinitionImpl> processDefinitions = parseProcessDefinitions(resource);          
                            
                    if(processDefinitions!=null && processDefinitions.size()>1) {
                        throwException("Process definition from resource: " + resource + " contains more than one process");
                    }
                    String processId = null;
                    RepositoryService aRepositoryService = null;
                    boolean doDeploy = false;
                    if(processDefinitions!=null && !processDefinitions.isEmpty()) {
                        for (ProcessDefinitionImpl definition : processDefinitions) {
                            String key = definition.getKey(); 
                            if(key==null) {
                                throwException("Process definition from resource: " + resource + " contains no key.");                
                            }
                            int version = definition.getVersion();
                            if(version<1) {
                                throwException("Process definition from resource: " + resource + " contains no version > 0.");
                            }
                            processId = new StringBuilder(key).append("-").append(version).toString();
                            if (log.isDebugEnabled()) {
                                log.debug("Query process repository for id: " + processId);
                            }
                            if(processEngine==null) {
                                throw new RuntimeException("Init failed. ProcessEngine is null");
                            }
                            if(processEngine.getRepositoryService()==null) {
                                throw new RuntimeException("Init failed. RepositoryService is null");
                            }
                            // don't call getRepositoryService() here: endless loop!
                            aRepositoryService = processEngine.getRepositoryService();
                            doDeploy = aRepositoryService.createProcessDefinitionQuery().processDefinitionId(processId).count() == 0;              
                        }  
                        if (doDeploy) {
                            processId = aRepositoryService.createDeployment()
                            .addResourceFromClasspath(resource)           
                            .deploy();
                            if (log.isInfoEnabled()) {
                                log.info("Process definition deployed, Id: " + processId + ", loaded from resource: " + resource);
                            }
                        } else if (log.isDebugEnabled()) {
                            log.debug("Process definition exitsts, Id: " + processId + ", loaded from resource: " + resource);
                        }
                    } else {
                        log.warn("Resource contains no process definitions: " + resource);
                    }              
                }           
            } catch(RuntimeException re) {
                log.error("RuntimeException while initializing", re);
                throw re;
            } catch(Throwable t) {
                log.error("Error while initializing", t);
                throw new RuntimeException("Error while initializing", t);
            }
            finally {
                wasInitCalled=true;
            }
        }
    }
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessService#startProcess(java.lang.String, java.util.Map)
     */
    @Override
    public void startProcess(String processDefinitionKey, Map<String, ?> variables) {
        if(variables==null) {
            getExecutionService().startProcessInstanceByKey(processDefinitionKey);
        } else {
            Control control = (Control) variables.get("control");
            ControlExecutionContext context = new ControlExecutionContext("assignee",control.getUuid() );
            Map<String, ControlExecutionContext> props = new HashMap<String, ControlExecutionContext>();
            props.put("context", context);
            getExecutionService().startProcessInstanceByKey(processDefinitionKey,props);
        }
        if (log.isInfoEnabled()) {
            log.info("Process started, key: " + processDefinitionKey);
        }
    }


    private void throwException(final String message) {
        log.error(message);
        throw new RuntimeException(message);
    }

    private List<ProcessDefinitionImpl> parseProcessDefinitions(String resource) {
        JpdlParser jpdlParser = new JpdlParser();
        Parse parse = jpdlParser.createParse();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classLoader.getResourceAsStream(resource);
        if (stream == null) {
            stream = ResourceStreamInput.class.getClassLoader().getResourceAsStream(resource);
        }
        parse.setInputStream(stream);
        parse.execute();
        List<ProcessDefinitionImpl> processDefinitions = (List<ProcessDefinitionImpl>) parse.getDocumentObject();
        return processDefinitions;
    }
    
    /**
     * @return
     */
    public ExecutionService getExecutionService() {
        return getProcessEngine().getExecutionService();
    }
    
    /**
     * @return
     */
    public RepositoryService getRepositoryService() {
        return getProcessEngine().getRepositoryService();
    }
    
    public ProcessEngine getProcessEngine() {
        if(!wasInitCalled) {
            init(); 
        }
        return processEngine;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

   

    public Set<String> getProcessDefinitions() {
        return processDefinitions;
    }

    public void setProcessDefinitions(Set<String> processDefinitionSet) {
        this.processDefinitions = processDefinitionSet;
    }
}
