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
import java.util.Date;
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

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.bpm.IControlExecutionProcess;
import sernet.verinice.interfaces.bpm.IExecutionProcess;
import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;
import sernet.verinice.interfaces.bpm.IProcessService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.bpm.ProcessInformation;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.samt.SamtTopic;

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
      
    private IBaseDao<Audit, Integer> auditDao;
      
    private IBaseDao<ControlGroup, Integer> controlGroupDao;
      
    private IBaseDao<SamtTopic, Integer> samtTopicDao;
    
    private IBaseDao<CnATreeElement,Integer> elementDao;
    
    private boolean wasInitCalled = false;
    
    private static VeriniceContext.State state;

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
     * @see sernet.verinice.interfaces.bpm.IProcessService#startProcessForIsa(java.lang.String)
     */
    @Override
    public IProcessStartInformation startProcessForIsa(String uuidAudit) {
        if(VeriniceContext.getState()==null) {
            log.warn("Context is not set for this thread. Will now set the context...");
            VeriniceContext.setState(ProcessService.state);
        }
        Audit isaAudit = getAuditDao().findByUuid(uuidAudit, RetrieveInfo.getChildrenInstance().setParent(true));  
        IsaProcessContext context = new IsaProcessContext();
        context.setNumberOfProcesses(0);
        context.setUuidAudit(uuidAudit);
        context.setUuidOrganization(loadOrganization(isaAudit.getParent()).getUuid());
        context.setControlGroup(isaAudit.getControlGroup());
        context=startProcessForControlGroup(context);
        return new ProcessInformation(context.getNumberOfProcesses());
    }
    
    /**
     * @param isaAudit
     * @return
     */
    private CnATreeElement loadOrganization(CnATreeElement element) {
        if(Organization.TYPE_ID.equals(element.getTypeId())) {
            return element;
        } else {
            element = getElementDao().findByUuid(element.getUuid(), RetrieveInfo.getPropertyInstance().setParent(true));
            return loadOrganization(element.getParent());
        }
    }

    private IsaProcessContext startProcessForControlGroup(IsaProcessContext context) {
        ControlGroup controlGroup = context.getControlGroup();
        controlGroup = getControlGroupDao().findByUuid(controlGroup.getUuid(), RetrieveInfo.getChildrenInstance());
        for (CnATreeElement element : controlGroup.getChildren()) {
            if(SamtTopic.TYPE_ID.equals(element.getTypeId())) {
                RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
                ri.setLinksDown(true);
                SamtTopic samtTopic = getSamtTopicDao().findByUuid(element.getUuid(), ri);
                context.setSamtTopic(samtTopic);
                context = handleSamtTopic(context);
            }
            if(ControlGroup.TYPE_ID.equals(element.getTypeId())) {
                context.setControlGroup((ControlGroup) element);
                context = startProcessForControlGroup(context);
            }        
        }
        return context;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.IProcessService#handleControl(sernet.verinice.model.iso27k.Control)
     */
    public void handleSamtTopic(SamtTopic topic) {
        IsaProcessContext context = new IsaProcessContext();
        context.setSamtTopic(topic);
        handleSamtTopic(context);
    }
       
    private IsaProcessContext handleSamtTopic(IsaProcessContext context) {
        try {
            SamtTopic samtTopic = context.getSamtTopic();
            String uuid = samtTopic.getUuid();
            List<ExecutionImpl> executionList = findIsaExecution(uuid);
            if(executionList==null || executionList.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No process for isa topic: " + uuid);
                }
                // start process if control is not implemented
                if(SamtTopic.IMPLEMENTED_NOTEDITED_NUMERIC == samtTopic.getMaturity()) {
                    startIsaExecution(context);
                    context.increaseProcessNumber();
                }
                if (log.isInfoEnabled()) {
                    log.info("Process started for isa topic: " + uuid);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Process found for isa topic: " + uuid);
                for (ExecutionImpl executionImpl : executionList) {
                    log.debug("Process execution id: " + executionImpl.getId());
                }            
            }
            return context;
        } catch (RuntimeException re) {
            log.error("RuntimeException while handling isa topic", re);
            throw re;
        } catch (Throwable t) {
            log.error("Error while handling isa topic", t);
            throw new RuntimeException(t);
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
        props.put(IExecutionProcess.VAR_UUID, control.getUuid());
        props.put(IExecutionProcess.VAR_TYPE_ID, control.getTypeId());    
        props.put(IControlExecutionProcess.VAR_OWNER_NAME, getOwnerName(control));
        props.put(IControlExecutionProcess.VAR_IMPLEMENTATION, control.getImplementation());
        Date duedate = control.getDueDate();
        Date now = new Date(System.currentTimeMillis());
        if(duedate!=null && now.before(duedate)) {
            props.put(IControlExecutionProcess.VAR_DUEDATE, duedate);
        } else {
            props.put(IControlExecutionProcess.VAR_DUEDATE, IControlExecutionProcess.DEFAULT_DUEDATE);
        }
        
        
        startProcess(IControlExecutionProcess.KEY, props);     
    }
    
    /**
     * @param topic
     * @throws CommandException 
     */
    private void startIsaExecution(IsaProcessContext context) throws CommandException {   
        SamtTopic topic = context.getSamtTopic();
        Map<String, Object> props = new HashMap<String, Object>();      
        String username = getProcessDao().getAssignee(topic);  
        props.put(IIsaExecutionProcess.VAR_ASSIGNEE_NAME, username);
        props.put(IExecutionProcess.VAR_UUID, topic.getUuid());
        props.put(IExecutionProcess.VAR_TYPE_ID, topic.getTypeId());
        props.put(IIsaExecutionProcess.VAR_OWNER_NAME, getOwnerName(topic));
        props.put(IIsaExecutionProcess.VAR_IMPLEMENTATION, topic.getMaturity());
        if(context.getUuidAudit()!=null) {
            props.put(IIsaExecutionProcess.VAR_AUDIT_UUID, context.getUuidAudit()); 
        }
        Date duedate = topic.getCompleteUntil();
        Date now = new Date(System.currentTimeMillis());
        if(duedate!=null && now.before(duedate)) {
            props.put(IIsaExecutionProcess.VAR_DUEDATE, duedate);
        } else {
            props.put(IIsaExecutionProcess.VAR_DUEDATE, IControlExecutionProcess.DEFAULT_DUEDATE);
        }
             
        startProcess(IIsaExecutionProcess.KEY, props);     
    }
    
    /**
     * @param element
     */
    private String getOwnerName(CnATreeElement element) {
        String owner = IControlExecutionProcess.DEFAULT_OWNER_NAME;
        DetachedCriteria crit = DetachedCriteria.forClass(ChangeLogEntry.class);
        crit.add(Restrictions.eq("elementId", element.getDbId()));
        crit.add(Restrictions.eq("change", ChangeLogEntry.TYPE_INSERT));
        crit.addOrder(Order.asc("changetime"));
        List<ChangeLogEntry> result = getChangeLogEntryDao().findByCriteria(crit);
        if(result!=null && !result.isEmpty() && result.get(0).getUsername()!=null) {
            owner = result.get(0).getUsername();
            if (log.isDebugEnabled()) {
                log.debug("Owner of control: " + element.getUuid() + " is: " + owner);
            }
        } else {
            log.warn("Can not find owner of control: " + element.getUuid() + ", using default owner name: " + owner);
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
        variableCrit.add(Restrictions.eq("key", IExecutionProcess.VAR_UUID));
        variableCrit.add(Restrictions.eq("string", uuidControl));
        return getJbpmExecutionDao().findByCriteria(executionCrit);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IProcessService#findControlExecution(java.lang.String)
     */
    public List<ExecutionImpl> findIsaExecution(final String uuid) {
        DetachedCriteria executionCrit = DetachedCriteria.forClass(ExecutionImpl.class);
        String processDefinitionId = findProcessDefinitionId(IIsaExecutionProcess.KEY);
        if (log.isDebugEnabled()) {
            log.debug("Latest processDefinitionId: " + processDefinitionId);
        }
        executionCrit.add(Restrictions.eq("processDefinitionId", processDefinitionId));
        DetachedCriteria variableCrit = executionCrit.createCriteria("variables");
        variableCrit.add(Restrictions.eq("key", IExecutionProcess.VAR_UUID));
        variableCrit.add(Restrictions.eq("string", uuid));
        return getJbpmExecutionDao().findByCriteria(executionCrit);
    }
    
    @Override
    public void deleteProcess(String id) {
        getExecutionService().deleteProcessInstance(id);
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

    public IBaseDao<Audit, Integer> getAuditDao() {
        return auditDao;
    }

    public void setAuditDao(IBaseDao<Audit, Integer> auditDao) {
        this.auditDao = auditDao;
    }

    public IBaseDao<ControlGroup, Integer> getControlGroupDao() {
        return controlGroupDao;
    }

    public void setControlGroupDao(IBaseDao<ControlGroup, Integer> controlGroupDao) {
        this.controlGroupDao = controlGroupDao;
    }

    public IBaseDao<SamtTopic, Integer> getSamtTopicDao() {
        return samtTopicDao;
    }

    public void setSamtTopicDao(IBaseDao<SamtTopic, Integer> samtTopicDao) {
        this.samtTopicDao = samtTopicDao;
    }
    
    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }

    public void setWorkObjects(VeriniceContext.State workObjects) {
        ProcessService.state = workObjects;
    }

    public VeriniceContext.State getWorkObjects() {
        return ProcessService.state;
    }

    /**
     * True: This is a real implementation.
     * 
     * @see sernet.verinice.interfaces.bpm.IProcessService#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
    }
}
