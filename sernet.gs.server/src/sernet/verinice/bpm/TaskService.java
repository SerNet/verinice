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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ManagementService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.type.Variable;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.bpm.ICompleteServerHandler;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IGsmIsmExecuteProzess;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.IIsaControlFlowProcess;
import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;
import sernet.verinice.interfaces.bpm.IIsaQmProcess;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskDescriptionHandler;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.KeyValue;
import sernet.verinice.model.bpm.Messages;
import sernet.verinice.model.bpm.TaskInformation;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.IConfigurationService;

/**
 * JBoss jBPM implementation of {@link ITaskService}.
 * Clients access the service by Springs 
 * {@link HttpInvokerProxyFactoryBean}.
 * 
 * See sernet/gs/server/spring/veriniceserver-jbpm.xml
 * for Spring configuration of this service.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskService implements ITaskService {

    private final Logger log = Logger.getLogger(TaskService.class);
    
    public static final Map<String, String> DEFAULT_OUTCOMES;
    
    static {
        DEFAULT_OUTCOMES = new HashMap<String, String>();
        DEFAULT_OUTCOMES.put(IIsaExecutionProcess.TASK_SET_ASSIGNEE,IIsaExecutionProcess.TRANS_COMPLETE);
        DEFAULT_OUTCOMES.put(IIsaExecutionProcess.TASK_IMPLEMENT,IIsaExecutionProcess.TRANS_COMPLETE);
        DEFAULT_OUTCOMES.put(IIsaExecutionProcess.TASK_ESCALATE,IIsaExecutionProcess.TRANS_COMPLETE);
        DEFAULT_OUTCOMES.put(IIsaExecutionProcess.TASK_CHECK_IMPLEMENTATION,IIsaExecutionProcess.TRANS_ACCEPT);  
        DEFAULT_OUTCOMES.put(IIsaExecutionProcess.TASK_WRITE_PERMISSION, IIsaExecutionProcess.TRANS_COMPLETE);
        
        DEFAULT_OUTCOMES.put(IIsaQmProcess.TASK_IQM_CHECK, IIsaQmProcess.TRANS_IQM_FIX);
        DEFAULT_OUTCOMES.put(IIsaQmProcess.TASK_IQM_SET_ASSIGNEE, IIsaQmProcess.TRANS_IQM_FIX);
        
        DEFAULT_OUTCOMES.put(IIsaControlFlowProcess.TASK_ASSIGN,IIsaControlFlowProcess.TRANS_ASSIGNED);
        DEFAULT_OUTCOMES.put(IIsaControlFlowProcess.TASK_ASSIGN_AUDITOR,IIsaControlFlowProcess.TRANS_WAIT);
        DEFAULT_OUTCOMES.put(IIsaControlFlowProcess.TASK_ASSIGN_DEADLINE,IIsaControlFlowProcess.TRANS_ASSIGNED);
        DEFAULT_OUTCOMES.put(IIsaControlFlowProcess.TASK_ASSIGN_NOT_RESPONSIBLE,IIsaControlFlowProcess.TRANS_ASSIGNED);
        DEFAULT_OUTCOMES.put(IIsaControlFlowProcess.TASK_CHECK,IIsaControlFlowProcess.TRANS_OK);
        DEFAULT_OUTCOMES.put(IIsaControlFlowProcess.TASK_COMMENT,IIsaControlFlowProcess.TRANS_COMPLETE);
        DEFAULT_OUTCOMES.put(IIsaControlFlowProcess.TASK_EXECUTE,IIsaControlFlowProcess.TRANS_CHECK);
        DEFAULT_OUTCOMES.put(IIsaControlFlowProcess.TASK_OBTAIN_ADVISE,IIsaControlFlowProcess.TRANS_FINISH);
        
        DEFAULT_OUTCOMES.put(IIndividualProcess.TASK_ASSIGN,IIndividualProcess.TRANS_COMPLETE);
        DEFAULT_OUTCOMES.put(IIndividualProcess.TASK_CHECK,IIndividualProcess.TRANS_ACCEPT);
        DEFAULT_OUTCOMES.put(IIndividualProcess.TASK_DEADLINE,IIndividualProcess.TRANS_ASSIGNED);
        DEFAULT_OUTCOMES.put(IIndividualProcess.TASK_EXECUTE,IIndividualProcess.TRANS_COMPLETE);
        DEFAULT_OUTCOMES.put(IIndividualProcess.TASK_NOT_RESPOSIBLE,IIndividualProcess.TRANS_ASSIGNED);
        
        DEFAULT_OUTCOMES.put(IGsmIsmExecuteProzess.TASK_EXECUTE,IGsmIsmExecuteProzess.TRANS_COMPLETE);
    }
    
    private ProcessEngine processEngine;
    
    private IAuthService authService;
    
    private IConfigurationService configurationService;
    
    private IBaseDao<CnATreeElement,Integer> elementDao;

    private IDao<TaskImpl, Long> jbpmTaskDao;
    
    private IDao<Variable, Long> jbpmVariableDao;
    
    private IBaseDao<Audit, Integer> auditDao;
    
    private Set<String> taskOutcomeBlacklist;
    
    private Map<String, ICompleteServerHandler> completeHandler;
    
    private ICompleteServerHandler defaultCompleteServerHandler;

    private Map<String, ITaskDescriptionHandler> descriptionHandler;
    
    private ITaskDescriptionHandler defaultDescriptionHandler;
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#getTaskList()
     */
    @Override
    public List<ITask> getTaskList() {
        return getTaskList(new TaskParameter(getAuthService().getUsername()));
    }
    
    /**
     * Returns tasks created after a date for user with name username.
     * If no tasks exists an empty list is returned. If date is null
     * all tasks are returned.
     * 
     * Filtering by date is done after loading all tasks since jBPM provides no
     * date filter for tasks.
     * 
     * @see sernet.verinice.interfaces.bpm.ITaskService#getTaskList(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ITask> getTaskList(ITaskParameter parameter) {
        if (log.isDebugEnabled()) {
            log.debug("getTaskList called..."); //$NON-NLS-1$
        }
        ServerInitializer.inheritVeriniceContextState();      
        if(!parameter.getAllUser() && parameter.getUsername()==null) {
            parameter.setUsername(getAuthService().getUsername());          
        }
        List<ITask> taskList = Collections.emptyList();
        if(doSearch(parameter)) {      
            Object[] queryElements = prepareSearchQuery(parameter);
            List<Object> paramList = (List<Object>)queryElements[0];
            final String hql = (String)queryElements[1];
            if (log.isDebugEnabled()) {
                log.debug("getTaskList, hql: " + hql); //$NON-NLS-1$
            }
            List jbpmTaskList = getJbpmTaskDao().findByQuery(hql,paramList.toArray());
            if (log.isDebugEnabled()) {
                log.debug("getTaskList, number of tasks: " + jbpmTaskList.size()); //$NON-NLS-1$
            }
            
            if(jbpmTaskList!=null && !jbpmTaskList.isEmpty()) {
                taskList = populateTaskList(jbpmTaskList);                
            }
        }    
        if (log.isDebugEnabled()) {
            log.debug("getTaskList finished"); //$NON-NLS-1$
        }
        return taskList;
    }

    private List<ITask> populateTaskList(List jbpmTaskList) {
        List<ITask> taskList;
        taskList = new ArrayList<ITask>();
        Task task=null;
        for (Iterator iterator = jbpmTaskList.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if(object instanceof Task ) {
                task = (Task) object;
            }
            if(object instanceof Object[] ) {
                task = (Task)((Object[])object)[0];
            }       
            if(task!=null) {
                ITask taskInfo = map(task);
                Set<String> outcomeSet = getTaskService().getOutcomes(task.getId());
                List<KeyValue> outcomeList = new ArrayList<KeyValue>(outcomeSet.size());
                for (String id : outcomeSet) {
                    if(!getTaskOutcomeBlacklist().contains(id)) {
                        outcomeList.add(new KeyValue(id, Messages.getString(id)));
                    }
                }
                taskInfo.setOutcomes(outcomeList);
                taskList.add(taskInfo);  
            }
        }
        return taskList;
    }

    private Object[] prepareSearchQuery(ITaskParameter parameter) {
        Object[] retValues = new Object[2];
        StringBuilder sb = new StringBuilder("from org.jbpm.pvm.internal.task.TaskImpl as task "); //$NON-NLS-1$
        List<Object> paramList = new LinkedList<Object>();
            
        if(parameter.getAuditUuid()!=null) {
            sb.append("inner join task.execution.processInstance.variables as auditVar "); //$NON-NLS-1$
        }    
        // create (un)read query if one is false:
        if((parameter.getRead()!=null && !parameter.getRead())
           || (parameter.getUnread()!=null && !parameter.getUnread())) {
            sb.append("inner join task.execution.processInstance.variables as readVar ");  //$NON-NLS-1$
        }
        
        boolean where = false;
        if(!parameter.getAllUser() && parameter.getUsername()!=null) {
            where = concat(sb,where);
            sb.append("task.assignee=? "); //$NON-NLS-1$
            paramList.add(parameter.getUsername());
        } 
        
        if(parameter.getProcessKey()!=null) {
            where = concat(sb,where);
            sb.append("task.executionId like ? "); //$NON-NLS-1$
            paramList.add(parameter.getProcessKey() + "%"); //$NON-NLS-1$
        }
        
        if(parameter.getTaskId()!=null) {
            where = concat(sb,where);
            sb.append("task.name=? "); //$NON-NLS-1$
            paramList.add(parameter.getTaskId()); 
        }
        
        if(parameter.getSince()!=null) {
            where = concat(sb,where);
            sb.append("task.createTime>=? "); //$NON-NLS-1$
            paramList.add(parameter.getSince());
        }
        
        if(parameter.getAuditUuid()!=null) {
            where = concat(sb,where);
            sb.append("auditVar.key=? "); //$NON-NLS-1$
            paramList.add(IIsaExecutionProcess.VAR_AUDIT_UUID);
            sb.append("and auditVar.string=? "); //$NON-NLS-1$
            paramList.add(parameter.getAuditUuid());
        }
        
        if(parameter.getRead()!=null && parameter.getRead() && parameter.getUnread()!=null && !parameter.getUnread()) { 
            where = concat(sb,where);           
            sb.append("readVar.key=? "); //$NON-NLS-1$
            paramList.add(ITaskService.VAR_READ_STATUS);
            sb.append("and readVar.string=? "); //$NON-NLS-1$
            paramList.add(ITaskService.VAR_READ);
        }
        if(parameter.getUnread()!=null && parameter.getUnread() && parameter.getRead()!=null && !parameter.getRead()) {
            where = concat(sb,where);
            sb.append("readVar.key=? "); //$NON-NLS-1$
            paramList.add(ITaskService.VAR_READ_STATUS);
            sb.append("and readVar.string=? "); //$NON-NLS-1$
            paramList.add(ITaskService.VAR_UNREAD);
        }
        retValues[0] = paramList;
        retValues[1] = sb.toString();
        return retValues;
    }
    
    /**
     * @param hql
     * @param where
     */
    private boolean concat(/*not final*/StringBuilder hql,/*not final*/boolean where) {
        boolean where0 = where;
        if(!where0) {
            hql.append("where "); //$NON-NLS-1$
            where0 = true;
        } else {
            hql.append("and "); //$NON-NLS-1$
        }
        return where0;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#getAuditList()
     */
    @Override
    public List<String> getElementList() {
        ServerInitializer.inheritVeriniceContextState();
        String hql = "select distinct var.string from Variable var where var.key = ?"; //$NON-NLS-1$
        String[] param = new String[]{IIsaExecutionProcess.VAR_AUDIT_UUID};
        return getJbpmVariableDao().findByQuery(hql, param);
    }

    private boolean doSearch(ITaskParameter parameter) {
        return parameter!=null
           && ((parameter.getRead()==null && parameter.getUnread()==null) || (parameter.getRead() || parameter.getUnread()));
    }
    
    /**
     * @param task
     * @param taskInformation
     */
    private TaskInformation map(Task task) {
        TaskInformation taskInformation = new TaskInformation();
        taskInformation.setId(task.getId());
        taskInformation.setType(task.getName());
        taskInformation.setCreateDate(task.getCreateTime()); 
        taskInformation.setAssignee(getConfigurationService().getName(task.getAssignee()));
        if (log.isDebugEnabled()) {
            log.debug("map, setting read status..."); //$NON-NLS-1$
        }          
        
        Map<String, Object> varMap = loadVariables(task);  
        taskInformation.setName(loadTaskTitle(task.getName(), varMap));
        taskInformation.setDescription(loadTaskDescription(task.getName(), varMap));
        
        
        taskInformation.setIsRead(ITaskService.VAR_READ.equals(varMap.get(ITaskService.VAR_READ_STATUS)));       
        String priority =  (String) varMap.get(IGenericProcess.VAR_PRIORITY);
        if(priority==null) {
            priority = ITask.PRIO_NORMAL;
        }
        taskInformation.setPriority(priority);
        
        Object value = varMap.get(IGenericProcess.VAR_PROPERTY_TYPES);
        if(value instanceof Set<?>) {
            taskInformation.setProperties((Set<String>) value);
        }
        
        mapControl(taskInformation, varMap);       
        mapAudit(taskInformation, varMap);
        
        if (log.isDebugEnabled()) {
            log.debug("map, loading type..."); //$NON-NLS-1$
        }
        String typeId = (String) varMap.get(IGenericProcess.VAR_TYPE_ID); 
        taskInformation.setElementType(typeId);
        taskInformation.setDueDate(task.getDuedate());   
        taskInformation.setProcessName(getProcessName(task));      
        if (log.isDebugEnabled()) {
            log.debug("map finished"); //$NON-NLS-1$
        }
        return taskInformation;
    }

    /**
     * @param task
     * @return
     */
    private String getProcessName(Task task) {
        String executionId = task.getExecutionId();
        String processKey = executionId.substring(0,executionId.indexOf('.'));        
        return Messages.getString(processKey);
    }

    @Override
    public String loadTaskDescription(String taskId, Map<String, Object> processVars) {
        ITaskDescriptionHandler handler = getDescriptionHandler().get(taskId);
        if(handler==null) {
            handler = getDefaultDescriptionHandler();
        }
        return handler.loadDescription(taskId, processVars);
    }
    
    @Override
    public String loadTaskTitle(String taskId, Map<String, Object> varMap) {
        ITaskDescriptionHandler handler = getDescriptionHandler().get(taskId);
        if(handler==null) {
            handler = getDefaultDescriptionHandler();
        }
        return handler.loadTitle(taskId, varMap);
    }

    /**
     * @param task
     * @return
     */
    private Map<String, Object> loadVariables(Task task) {
        if (log.isDebugEnabled()) {
            log.debug("map, loading element..."); //$NON-NLS-1$
        }
        String executionId = task.getExecutionId();
        return loadVariablesForProcess(executionId);
    }

    /**
     * @param executionId
     * @return
     */
    private Map<String, Object> loadVariablesForProcess(String executionId) {
        Set<String> varNameSet = getExecutionService().getVariableNames(executionId);      
        return getExecutionService().getVariables(executionId,varNameSet);
    }

    /**
     * @param taskInformation
     * @param varMap
     */
    private void mapAudit(TaskInformation taskInformation, Map<String, Object> varMap) {
        if (log.isDebugEnabled()) {
            log.debug("map, loading audit..."); //$NON-NLS-1$
        }
        CnATreeElement audit = null;
        String uuidAudit = (String) varMap.get(IIsaExecutionProcess.VAR_AUDIT_UUID);     
        if(uuidAudit!=null) {
            taskInformation.setUuidAudit(uuidAudit);
            RetrieveInfo ri = new RetrieveInfo();
            ri.setProperties(true);
            audit = getElementDao().findByUuid(uuidAudit, ri);           
        } 
        if(audit!=null) {
            taskInformation.setAuditTitle(audit.getTitle());
        } else {
            taskInformation.setAuditTitle(Messages.getString("TaskService.0")); //$NON-NLS-1$
        }
    }

    /**
     * @param taskInformation
     * @param varMap
     * @return
     */
    private void mapControl(TaskInformation taskInformation, Map<String, Object> varMap) {
        String uuidControl = (String) varMap.get(IGenericProcess.VAR_UUID);       
        taskInformation.setUuid(uuidControl);
        taskInformation.setControlTitle("no object");
        if(uuidControl==null) {
            return;
        }
        RetrieveInfo ri = new RetrieveInfo();
        ri.setProperties(true);
        CnATreeElement element = getElementDao().findByUuid(uuidControl, ri);
        if(element!=null) {
            taskInformation.setControlTitle(element.getTitle());
            taskInformation.setSortValue(createSortableString(taskInformation.getControlTitle()));
            if(element instanceof SamtTopic) {
                taskInformation.setIsProcessed(((SamtTopic)element).getMaturity()!=SamtTopic.IMPLEMENTED_NOTEDITED_NUMERIC);
            }          
        }
    }
    
    private String createSortableString(String text) {
        String sortable = text;
        final int minimumLength = 3;
        if(sortable!=null && sortable.length()>0 && isNumber(sortable.substring(0,1)) ) {
            if(sortable.length()==1 || !isNumber(sortable.substring(1,2))) {
                sortable = new StringBuilder("0").append(sortable).toString(); //$NON-NLS-1$
            }
            if(sortable.indexOf('.')==2 && sortable.length()>minimumLength) { //$NON-NLS-1$
                sortable = new StringBuilder(sortable.substring(0, 2))
                .append(".") //$NON-NLS-1$
                .append(createSortableString(sortable.substring(minimumLength)))
                .toString();
            }
        }
        return sortable;
    }
    
    private boolean isNumber(String s) {
        try {
            Integer.valueOf(s);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#completeTask(java.lang.String)
     */
    @Override
    public void completeTask(String taskId) {
        completeTask(taskId,(Map<String, Object>)null);
    }
    
    @Override
    public void completeTask(String taskId, Map<String, Object> parameter) {
        Task task = getTaskService().getTask(taskId);
        if(task!=null) {
            String name = task.getName();
            if(DEFAULT_OUTCOMES.get(name)!=null) {
                completeTask(task,DEFAULT_OUTCOMES.get(name),parameter);
            } else {
                log.warn("No default outcome set for task: " + name); //$NON-NLS-1$
                getTaskService().completeTask(taskId);
            }
        }
    }
    
    @Override
    public void completeTask(String taskId, String outcomeId) {
        completeTask(taskId,outcomeId,null);
    }
    

    @Override
    public void completeTask(String taskId, String outcomeId, Map<String, Object> parameter) {
        Task task = getTaskService().getTask(taskId);
        if(task!=null) {
            completeTask(task,outcomeId,parameter);
        }
    }
    
    private void completeTask(Task task, String outcomeId, Map<String, Object> parameter) {     
        ICompleteServerHandler handler = getHandler(task.getName(),outcomeId);
        if(handler!=null) {
            handler.execute(task.getId(),parameter);
        }
        getTaskService().completeTask(task.getId(),outcomeId);
    }
    
    /**
     * @param name
     * @param outcomeId
     * @return
     */
    private ICompleteServerHandler getHandler(String name, String outcomeId) {
        ICompleteServerHandler handler = getCompleteHandler().get(new StringBuilder(name).append(".").append(outcomeId).toString());
        if(handler==null) {
            handler = getDefaultCompleteServerHandler();
        }
        return handler;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#markAsRead(java.lang.String)
     */
    @Override
    public void markAsRead(String taskId) {
        Map<String, String> varMap = new HashMap<String, String>(1);
        varMap.put(ITaskService.VAR_READ_STATUS, ITaskService.VAR_READ);
        getTaskService().setVariables(taskId, varMap);     
    }
    
    /**
     * True: This is a real implementation.
     * 
     * @see sernet.verinice.interfaces.bpm.ITaskService#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
    }

    /**
     * This implementation cancel and deletes a task by deleting
     * the owning process of the task.
     * 
     * @param taskId The database id of an task
     * @see sernet.verinice.interfaces.bpm.ITaskService#cancelTask(java.lang.String)
     */
    @Override
    public void cancelTask(String taskId) {
        // Some task belongs to a subprocess of the main process
        // HQL query to find the main process
        String hql = "select execution.parent from org.jbpm.pvm.internal.task.TaskImpl t where t.id = ?"; //$NON-NLS-1$
        List<Execution> executionList = getJbpmTaskDao().findByQuery(hql,new Long[]{Long.valueOf(taskId)});         
        if(!executionList.isEmpty()) {
            for (Execution process : executionList) {
                if(process!=null && process.getId()!=null) {
                    getExecutionService().deleteProcessInstance(process.getId());
                }
            }
        }
        // HQL query to find the  process
        hql = "select execution from org.jbpm.pvm.internal.task.TaskImpl t where t.id = ?";
        executionList = getJbpmTaskDao().findByQuery(hql,new Long[]{Long.valueOf(taskId)});         
        if(!executionList.isEmpty()) {
            for (Execution process : executionList) {
                if(process!=null && process.getId()!=null) {
                    getExecutionService().deleteProcessInstance(process.getId());
                }
            }
        }    
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#setAssignee(java.lang.String, java.lang.String)
     */
    @Override
    public void setAssignee(Set<String> taskIdset, String username) {
        if(taskIdset!=null && !taskIdset.isEmpty() && username!=null) {
            for (String taskId : taskIdset) {
                getTaskService().assignTask(taskId, username);            
            }
            
        }
    }
    
    @Override
    public void setAssigneeVar(Set<String> taskIdset, String username) {
        if(taskIdset!=null && !taskIdset.isEmpty() && username!=null) {
            for (String taskId : taskIdset) {
                Map<String, String> param = new HashMap<String, String>();
                param.put(IGenericProcess.VAR_ASSIGNEE_NAME, username);
                getTaskService().setVariables(taskId, param);
            }
            
        }
    }
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#setVariables(java.lang.String, java.util.Map)
     */
    @Override
    public void setVariables(String taskId, Map<String, Object> param) {
        getTaskService().setVariables(taskId, param);
    }
    
    @Override
    public Map<String, Object> getVariables(String taskId) {
        Map<String, Object> variables = new HashMap<String, Object>();
        Set<String> names = getTaskService().getVariableNames(taskId);
        for (String name : names) {          
            variables.put(name, getTaskService().getVariable(taskId, name));
        }
        return variables;
    }

    public org.jbpm.api.TaskService getTaskService() {
        return getProcessEngine().getTaskService();
    }
    
    public ExecutionService getExecutionService() {
        return getProcessEngine().getExecutionService();
    }
    
    public ManagementService getManagementService() {
        return getProcessEngine().getManagementService();
    }

    public ProcessEngine getProcessEngine() {
        return processEngine;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

    public IConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(IConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    public IBaseDao<CnATreeElement, Integer> getElementDao() {
        return elementDao;
    }

    public void setElementDao(IBaseDao<CnATreeElement, Integer> elementDao) {
        this.elementDao = elementDao;
    }
    
    public IDao<TaskImpl, Long> getJbpmTaskDao() {
        return jbpmTaskDao;
    }

    public void setJbpmTaskDao(IDao<TaskImpl, Long> jbpmTaskDao) {
        this.jbpmTaskDao = jbpmTaskDao;
    }

    public IDao<Variable, Long> getJbpmVariableDao() {
        return jbpmVariableDao;
    }

    public void setJbpmVariableDao(IDao<Variable, Long> jbpmVariableDao) {
        this.jbpmVariableDao = jbpmVariableDao;
    }

    public IBaseDao<Audit, Integer> getAuditDao() {
        return auditDao;
    }

    public void setAuditDao(IBaseDao<Audit, Integer> auditDao) {
        this.auditDao = auditDao;
    }

    public Set<String> getTaskOutcomeBlacklist() {
        if(taskOutcomeBlacklist==null) {
            taskOutcomeBlacklist = Collections.emptySet();
        }
        return taskOutcomeBlacklist;
    }

    public void setTaskOutcomeBlacklist(Set<String> processDefinitions) {
        this.taskOutcomeBlacklist = processDefinitions;
    }

    public Map<String, ICompleteServerHandler> getCompleteHandler() {
        return completeHandler;
    }

    public void setCompleteHandler(Map<String, ICompleteServerHandler> completeHandler) {
        this.completeHandler = completeHandler;
    }

    public ICompleteServerHandler getDefaultCompleteServerHandler() {
        return defaultCompleteServerHandler;
    }

    public void setDefaultCompleteServerHandler(ICompleteServerHandler defaultCompleteServerHandler) {
        this.defaultCompleteServerHandler = defaultCompleteServerHandler;
    }

    public Map<String, ITaskDescriptionHandler> getDescriptionHandler() {
        return descriptionHandler;
    }

    public void setDescriptionHandler(Map<String, ITaskDescriptionHandler> descriptionHandler) {
        this.descriptionHandler = descriptionHandler;
    }

    public ITaskDescriptionHandler getDefaultDescriptionHandler() {
        return defaultDescriptionHandler;
    }

    public void setDefaultDescriptionHandler(ITaskDescriptionHandler defaultDescriptionHandler) {
        this.defaultDescriptionHandler = defaultDescriptionHandler;
    }

}
