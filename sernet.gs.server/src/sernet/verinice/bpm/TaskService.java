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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ManagementService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.type.Variable;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import sernet.gs.server.ServerInitializer;
import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.bpm.IExecutionProcess;
import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.KeyValue;
import sernet.verinice.model.bpm.TaskInformation;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;

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
public class TaskService implements ITaskService{

    private final Logger log = Logger.getLogger(TaskService.class);
    
    public static final Map<String, String> DEFAULT_OUTCOMES;
    
    static {
        DEFAULT_OUTCOMES = new HashMap<String, String>();
        DEFAULT_OUTCOMES.put(TASK_SET_ASSIGNEE,OUTCOME_COMPLETE);
        DEFAULT_OUTCOMES.put(TASK_IMPLEMENT,OUTCOME_COMPLETE);
        DEFAULT_OUTCOMES.put(TASK_ESCALATE,OUTCOME_COMPLETE);
        DEFAULT_OUTCOMES.put(TASK_CHECK_IMPLEMENTATION,OUTCOME_ACCEPT);  
    }
    
    private ProcessEngine processEngine;
    
    private IAuthService authService;
    
    private IBaseDao<CnATreeElement,Integer> elementDao;

    private IDao<TaskImpl, Long> jbpmTaskDao;
    
    private IDao<Variable, Long> jbpmVariableDao;
    
    private IBaseDao<Audit, Integer> auditDao;
    
    
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
            log.debug("getTaskList called...");
        }
        ServerInitializer.inheritVeriniceContextState();      
        if(!parameter.getAllUser() && parameter.getUsername()==null) {
            parameter.setUsername(getAuthService().getUsername());          
        }
        List<ITask> taskList = Collections.emptyList();
        if(doSearch(parameter)) {      
            List<Object> paramList = new LinkedList<Object>();
            StringBuilder sb = new StringBuilder("from org.jbpm.pvm.internal.task.TaskImpl as task ");
                
            if(parameter.getAuditUuid()!=null) {
                sb.append("inner join task.execution.processInstance.variables as auditVar ");
            }    
            // create (un)read query if one is false:
            if((parameter.getRead()!=null && !parameter.getRead())
               || (parameter.getUnread()!=null && !parameter.getUnread())) {
                sb.append("inner join task.execution.processInstance.variables as readVar "); 
            }
            
            boolean where = false;
            if(!parameter.getAllUser() && parameter.getUsername()!=null) {
                where = concat(sb,where);
                sb.append("task.assignee=? ");
                paramList.add(parameter.getUsername());
            } 
            
            if(parameter.getSince()!=null) {
                where = concat(sb,where);
                sb.append("task.createTime>=? ");
                paramList.add(parameter.getSince());
            }
            
            if(parameter.getAuditUuid()!=null) {
                where = concat(sb,where);
                sb.append("auditVar.key=? ");
                paramList.add(IIsaExecutionProcess.VAR_AUDIT_UUID);
                sb.append("and auditVar.string=? ");
                paramList.add(parameter.getAuditUuid());
            }
            
            if(parameter.getRead()!=null && parameter.getRead() && parameter.getUnread()!=null && !parameter.getUnread()) { 
                where = concat(sb,where);           
                sb.append("readVar.key=? ");
                paramList.add(ITaskService.VAR_READ_STATUS);
                sb.append("and readVar.string=? ");
                paramList.add(ITaskService.VAR_READ);
            }
            if(parameter.getUnread()!=null && parameter.getUnread() && parameter.getRead()!=null && !parameter.getRead()) {
                where = concat(sb,where);
                sb.append("readVar.key=? ");
                paramList.add(ITaskService.VAR_READ_STATUS);
                sb.append("and readVar.string=? ");
                paramList.add(ITaskService.VAR_UNREAD);
            }

            final String hql = sb.toString();
            if (log.isDebugEnabled()) {
                log.debug("getTaskList, hql: " + hql);
            }
            List jbpmTaskList = getJbpmTaskDao().findByQuery(hql,paramList.toArray());
            if (log.isDebugEnabled()) {
                log.debug("getTaskList, number of tasks: " + jbpmTaskList.size());
            }
            
            if(jbpmTaskList!=null && !jbpmTaskList.isEmpty()) {
                taskList = new ArrayList<ITask>();
                Task task=null;
                for (Iterator iterator = jbpmTaskList.iterator(); iterator.hasNext();) {
                    Object object = (Object) iterator.next();
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
                            outcomeList.add(new KeyValue(id, Messages.getString(id)));                          
                        }
                        taskInfo.setOutcomes(outcomeList);
                        taskList.add(taskInfo);  
                    }
                }                
            }
        }    
        if (log.isDebugEnabled()) {
            log.debug("getTaskList finished");
        }
        return taskList;
    }
    
    /**
     * @param hql
     * @param where
     */
    private boolean concat(/*not final*/StringBuilder hql,/*not final*/boolean where) {
        if(!where) {
            hql.append("where ");
            where = true;
        } else {
            hql.append("and ");
        }
        return where;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#getAuditList()
     */
    @Override
    public List<Audit> getAuditList() {
        ServerInitializer.inheritVeriniceContextState();
        String hql = "select distinct var.string from Variable var where var.key = ?";
        String[] param = new String[]{IIsaExecutionProcess.VAR_AUDIT_UUID};
        List<String> uuidAuditList = getJbpmVariableDao().findByQuery(hql, param);
        List<Audit> auditList = new ArrayList<Audit>();
        for (String uuid : uuidAuditList) {
            Audit audit = getAuditDao().findByUuid(uuid, RetrieveInfo.getPropertyInstance());
            if(audit!=null) {
                auditList.add(audit);
            }
        }
        return auditList;
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
        taskInformation.setName(Messages.getString(task.getName()));
        taskInformation.setCreateDate(task.getCreateTime()); 
        taskInformation.setAssignee(task.getAssignee());
        if (log.isDebugEnabled()) {
            log.debug("map, setting read status...");
        }
        String readStatus = (String) getTaskService().getVariable(task.getId(), ITaskService.VAR_READ_STATUS);
        taskInformation.setIsRead(ITaskService.VAR_READ.equals(readStatus));
        taskInformation.setStyle((taskInformation.getIsRead()) ? ITask.STYLE_READ : ITask.STYLE_UNREAD);
              
        String executionId = task.getExecutionId();   
        
        if (log.isDebugEnabled()) {
            log.debug("map, loading element...");
        }
        Set<String> varNameSet = new HashSet<String>();
        varNameSet.add(IExecutionProcess.VAR_UUID);
        varNameSet.add(IIsaExecutionProcess.VAR_AUDIT_UUID);
        varNameSet.add(IExecutionProcess.VAR_TYPE_ID);
        Map<String, Object> varMap = getExecutionService().getVariables(executionId,varNameSet);
        
        String uuidControl = (String) varMap.get(IExecutionProcess.VAR_UUID);            
        taskInformation.setUuid(uuidControl);  
        RetrieveInfo ri = new RetrieveInfo();
        ri.setProperties(true);
        CnATreeElement element = getElementDao().findByUuid(uuidControl, ri);
        if(element!=null) {
            taskInformation.setControlTitle(element.getTitle());
            taskInformation.setSortValue(createSortableString(taskInformation.getControlTitle()));
        }
        
        if (log.isDebugEnabled()) {
            log.debug("map, loading audit...");
        }
        String uuidAudit = (String) varMap.get(IIsaExecutionProcess.VAR_AUDIT_UUID);     
        if(uuidAudit!=null) {
            taskInformation.setUuidAudit(uuidAudit);       
            CnATreeElement audit = getElementDao().findByUuid(uuidAudit, ri);
            if(audit!=null) {
                taskInformation.setAuditTitle(audit.getTitle());
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("map, loading type...");
        }
        String typeId = (String) varMap.get(IExecutionProcess.VAR_TYPE_ID); 
        taskInformation.setType(typeId);
        taskInformation.setDueDate(task.getDuedate());   
        
        if (log.isDebugEnabled()) {
            log.debug("map finished");
        }
        return taskInformation;
    }
    
    private String createSortableString(String text) {
        String sortable = text;
        if(sortable!=null && sortable.length()>0 && isNumber(sortable.substring(0,1)) ) {
            if(sortable.length()==1 || !isNumber(sortable.substring(1,2))) {
                sortable = new StringBuilder("0").append(sortable).toString();
            }
            if(sortable.indexOf(".")==2 && sortable.length()>3) {
                sortable = new StringBuilder(sortable.substring(0, 2))
                .append(".")
                .append(createSortableString(sortable.substring(3)))
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
        Task task = getTaskService().getTask(taskId);
        if(task!=null) {
            String name = task.getName();
            if(DEFAULT_OUTCOMES.get(name)!=null) {
                completeTask(taskId,DEFAULT_OUTCOMES.get(name));
            } else {
                log.warn("No default outcome set for task: " + name);
                getTaskService().completeTask(taskId);
            }
        }
    }
    

    @Override
    public void completeTask(String taskId, String outcomeId) {
        getTaskService().completeTask(taskId,outcomeId);
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

    /**
     * True: This is a real implementation.
     * 
     * @see sernet.verinice.interfaces.bpm.ITaskService#isActive()
     */
    @Override
    public boolean isActive() {
        return true;
    }

}
