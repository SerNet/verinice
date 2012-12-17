/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.jbpm.jpdl.internal.xml.JpdlParser;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;
import org.jbpm.pvm.internal.stream.ResourceStreamInput;
import org.jbpm.pvm.internal.xml.Parse;

import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IProcessServiceGeneric;

/**
 * ProcessServiceGeneric implements all generic methods to handle jBPM processes. 
 * jBPM is an open source workflow engine: http://www.jboss.org/jbpm/
 * 
 * This class has (almost) no dependencies to verinice classes.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessServiceGeneric implements IProcessServiceGeneric {

    private static final Logger LOG = Logger.getLogger(ProcessServiceGeneric.class);
    
    private ProcessEngine processEngine;
    private Set<String> processDefinitions;
    private boolean wasInitCalled = false;

    private IDao<ExecutionImpl, Long> jbpmExecutionDao;

    private ProcessDao processDao;

    /**
     * Spring init method configured in sernet/gs/server/spring/veriniceserver-jbpm.xml
     * 
     * Initializes the process service. See: doInit()
     */
    public void init() {
        synchronized (this) {
            if(!wasInitCalled) {
                doInit();
            }
        }
    }

    /**
     * Initializes the process service.
     * 
     * Deploys process definitions defined in Spring configuration (veriniceserver-jbpm.xml)
     * if they are not already deployed.
     * 
     * Dont call this unsyncronised method, Call: init()
     */
    private void doInit() {
        if(!wasInitCalled) {
            try {
                for (String resource : getProcessDefinitions()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Loading process definition from resource: " + resource + "...");
                    }                 
                    List<ProcessDefinitionImpl> definitions = parseProcessDefinitions(resource);          
                            
                    if(definitions!=null && definitions.size()>1) {
                        throwException("Process definition from resource: " + resource + " contains more than one process");
                    }
                    String processId = null;
                    RepositoryService aRepositoryService = null;
                    boolean doDeploy = false;
                    if(definitions!=null && !definitions.isEmpty()) {
                        for (ProcessDefinitionImpl definition : definitions) {
                            String key = definition.getKey(); 
                            if(key==null) {
                                throwException("Process definition from resource: " + resource + " contains no key.");                
                            }
                            int version = definition.getVersion();
                            if(version<1) {
                                throwException("Process definition from resource: " + resource + " contains no version > 0.");
                            }
                            processId = new StringBuilder(key).append("-").append(version).toString();
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Query process repository for id: " + processId);
                            }
                            if(processEngine==null) {
                                throw new RuntimeException("Init failed. ProcessEngine is null");
                            }
                            if(processEngine.getRepositoryService()==null) {
                                throw new RuntimeException("Init failed. RepositoryService is null");
                            }
                            // don't call this.getRepositoryService() here: endless loop!
                            aRepositoryService = processEngine.getRepositoryService();
                            doDeploy = aRepositoryService.createProcessDefinitionQuery().processDefinitionId(processId).count() == 0;              
                        }  
                        if (doDeploy) {
                            processId = aRepositoryService.createDeployment()
                            .addResourceFromClasspath(resource)           
                            .deploy();
                            if (LOG.isInfoEnabled()) {
                                LOG.info("Process definition deployed, Id: " + processId + ", loaded from resource: " + resource);
                            }
                        } else if (LOG.isDebugEnabled()) {
                            LOG.debug("Process definition exitsts, Id: " + processId + ", loaded from resource: " + resource);
                        }
                    } else {
                        LOG.warn("Resource contains no process definitions: " + resource);
                    }              
                }           
            } catch(RuntimeException re) {
                LOG.error("RuntimeException while initializing", re);
                throw re;
            } catch(Exception t) {
                LOG.error("Error while initializing", t);
                throw new RuntimeException("Error while initializing", t);
            }
            finally {
                wasInitCalled=true;
            }
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceGeneric#startProcess(java.lang.String, java.util.Map)
     */
    @Override
    public void startProcess(String processDefinitionKey, Map<String, ?> variables) {
        ProcessInstance processInstance = null;
        if(variables==null) {
            processInstance = getExecutionService().startProcessInstanceByKey(processDefinitionKey);
        } else {
            processInstance = getExecutionService().startProcessInstanceByKey(processDefinitionKey,variables);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Process started, key: " + processDefinitionKey + ", id:" + processInstance.getId());
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceGeneric#findProcessDefinitionId(java.lang.String)
     */
    @Override
    public String findProcessDefinitionId(String processDefinitionKey) {
        String id = null;
        List<ProcessDefinition> processDefinitionList = getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION).list();
        if(processDefinitionList!=null && !processDefinitionList.isEmpty()) {
            id = processDefinitionList.get(0).getId();
        }
        return id;
    }
    
    public List<ExecutionImpl> findExecutionForElement(String key, String uuid) {    
        String processDefinitionId = findProcessDefinitionId(key);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Latest processDefinitionId: " + processDefinitionId);
        }
        DetachedCriteria executionCrit = DetachedCriteria.forClass(ExecutionImpl.class);
        executionCrit.add(Restrictions.eq("processDefinitionId", processDefinitionId));
        DetachedCriteria variableCrit = executionCrit.createCriteria("variables");
        variableCrit.add(Restrictions.eq("key", IGenericProcess.VAR_UUID));
        variableCrit.add(Restrictions.eq("string", uuid));
        return getJbpmExecutionDao().findByCriteria(executionCrit);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessServiceGeneric#deleteProcess(java.lang.String)
     */
    @Override
    public void deleteProcess(String id) {
        getExecutionService().deleteProcessInstance(id);
    }
    
    /**
     * True: This is a real implementation.
     * 
     * @see sernet.verinice.interfaces.bpm.IProcessServiceIsa#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * @param resource
     * @return
     */
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
        return (List<ProcessDefinitionImpl>) parse.getDocumentObject();
    }
    
    protected boolean isWasInitCalled() {
        synchronized (this) {
            return wasInitCalled;
        }
    }

    protected void setWasInitCalled(boolean wasInitCalled) {
        synchronized (this) {
            this.wasInitCalled = wasInitCalled;
        }
    }

    public ExecutionService getExecutionService() {
        return getProcessEngine().getExecutionService();
    }

    public RepositoryService getRepositoryService() {
        return getProcessEngine().getRepositoryService();
    }

    public ProcessEngine getProcessEngine() {
        if(!isWasInitCalled()) {
            init(); 
        }
        return processEngine;
    }

    public Set<String> getProcessDefinitions() {
        return processDefinitions;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public void setProcessDefinitions(Set<String> processDefinitionSet) {
        this.processDefinitions = processDefinitionSet;
    }

    public IDao<ExecutionImpl, Long> getJbpmExecutionDao() {
        return jbpmExecutionDao;
    }

    public void setJbpmExecutionDao(IDao<ExecutionImpl, Long> jbpmExecutionDao) {
        this.jbpmExecutionDao = jbpmExecutionDao;
    }

    public ProcessDao getProcessDao() {
        return processDao;
    }

    public void setProcessDao(ProcessDao processDao) {
        this.processDao = processDao;
    }
    
    private void throwException(final String message) {
        LOG.error(message);
        throw new RuntimeException(message);
    }

}
