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
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
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

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.bpm.IControlExecutionProcess;
import sernet.verinice.interfaces.bpm.IProcessService;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.service.commands.LoadUsername;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessService implements IProcessService {

    private final Logger log = Logger.getLogger(ProcessService.class);

    private ProcessEngine processEngine;

    protected Set<String> processDefinitions;

    private IDao<ExecutionImpl, Long> jbpmExecutionDao;
    
    private IDao<ChangeLogEntry, Integer> changeLogEntryDao;
    
    private ProcessDao processDao;
      
    private IBaseDao<Control, Integer> controlDao;
    
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
        ProcessInstance processInstance = null;
        if(variables==null) {
            processInstance = getExecutionService().startProcessInstanceByKey(processDefinitionKey);
        } else {
            processInstance = getExecutionService().startProcessInstanceByKey(processDefinitionKey,variables);
        }
        if (log.isInfoEnabled()) {
            log.info("Process started, key: " + processDefinitionKey + ", id:" + processInstance.getId());
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessService#findProcessDefinitionId(java.lang.String)
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
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessService#handleControl(sernet.verinice.model.iso27k.Control)
     */
    public void handleControl(Control control) {
        try {
            String uuidControl = control.getUuid();
            List<ExecutionImpl> executionList = findControlExecution(uuidControl);
            if(executionList==null || executionList.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No process for control: " + uuidControl);
                }
                // start process if control is not implemented
                if(!Control.IMPLEMENTED_YES.equals(control.getImplementation())) {
                    startControlExecution(control);
                }
                if (log.isInfoEnabled()) {
                    log.info("Process started for control: " + uuidControl);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Process found for control: " + uuidControl);
                for (ExecutionImpl executionImpl : executionList) {
                    log.debug("Process execution id: " + executionImpl.getId());
                }            
            }
        } catch (RuntimeException re) {
            log.error("RuntimeException while handling control", re);
            throw re;
        } catch (Throwable t) {
            log.error("Error while handling control", t);
            throw new RuntimeException(t);
        }
    }
    
    /**
     * @param control
     * @throws CommandException 
     */
    private void startControlExecution(Control control) throws CommandException {      
        Map<String, Object> props = new HashMap<String, Object>();
        
        String username = getProcessDao().getAssignee(control);
        
        props.put(IControlExecutionProcess.VAR_ASSIGNEE_NAME, username);
        props.put(IControlExecutionProcess.VAR_CONTROL_UUID, control.getUuid());
        props.put(IControlExecutionProcess.VAR_OWNER_NAME, getOwnerName(control));
        props.put(IControlExecutionProcess.VAR_IMPLEMENTATION, control.getImplementation());
        startProcess(IControlExecutionProcess.KEY, props);     
    }
    
    /**
     * @param control
     */
    private String getOwnerName(Control control) {
        String owner = IControlExecutionProcess.DEFAULT_OWNER_NAME;
        DetachedCriteria crit = DetachedCriteria.forClass(ChangeLogEntry.class);
        crit.add(Restrictions.eq("elementId", control.getDbId()));
        crit.add(Restrictions.eq("elementClass", Control.class.getName()));
        crit.add(Restrictions.eq("change", ChangeLogEntry.TYPE_INSERT));
        crit.addOrder(Order.asc("changetime"));
        List<ChangeLogEntry> result = getChangeLogEntryDao().findByCriteria(crit);
        if(result!=null && !result.isEmpty() && result.get(0).getUsername()!=null) {
            owner = result.get(0).getUsername();
            if (log.isDebugEnabled()) {
                log.debug("Owner of control: " + control.getUuid() + " is: " + owner);
            }
        } else {
            log.warn("Can not find owner of control: " + control.getUuid() + ", using default owner name: " + owner);
        }
        return owner;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessService#findControlExecution(java.lang.String)
     */
    public List<ExecutionImpl> findControlExecution(final String uuidControl) {
        DetachedCriteria executionCrit = DetachedCriteria.forClass(ExecutionImpl.class);
        String processDefinitionId = findProcessDefinitionId(IControlExecutionProcess.KEY);
        if (log.isDebugEnabled()) {
            log.debug("Latest processDefinitionId: " + processDefinitionId);
        }
        executionCrit.add(Restrictions.eq("processDefinitionId", processDefinitionId));
        DetachedCriteria variableCrit = executionCrit.createCriteria("variables");
        variableCrit.add(Restrictions.eq("key", IControlExecutionProcess.VAR_CONTROL_UUID));
        variableCrit.add(Restrictions.eq("string", uuidControl));
        return getJbpmExecutionDao().findByCriteria(executionCrit);
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
    
    public IDao<ExecutionImpl, Long> getJbpmExecutionDao() {
        return jbpmExecutionDao;
    }

    public void setJbpmExecutionDao(IDao<ExecutionImpl, Long> jbpmExecutionDao) {
        this.jbpmExecutionDao = jbpmExecutionDao;
    }

    public IDao<ChangeLogEntry, Integer> getChangeLogEntryDao() {
        return changeLogEntryDao;
    }

    public void setChangeLogEntryDao(IDao<ChangeLogEntry, Integer> changeLogEntryDao) {
        this.changeLogEntryDao = changeLogEntryDao;
    }

    public ProcessDao getProcessDao() {
        return processDao;
    }

    public void setProcessDao(ProcessDao processDao) {
        this.processDao = processDao;
    }

    public IBaseDao<Control, Integer> getControlDao() {
        return controlDao;
    }

    public void setControlDao(IBaseDao<Control, Integer> controlDao) {
        this.controlDao = controlDao;
    }
}
