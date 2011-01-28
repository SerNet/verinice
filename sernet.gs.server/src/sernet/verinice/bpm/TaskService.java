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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.JobQuery;
import org.jbpm.api.ManagementService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.TaskQuery;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import sernet.gs.server.ServerInitializer;
import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.bpm.IControlExecutionProcess;
import sernet.verinice.interfaces.bpm.IExecutionProcess;
import sernet.verinice.interfaces.bpm.IIsaExecutionProcess;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.ITask.KeyValue;
import sernet.verinice.model.bpm.TaskInformation;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;

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

    private ProcessEngine processEngine;
    
    private IAuthService authService;
    
    private IBaseDao<CnATreeElement,Integer> elementDao;

    private IDao<TaskImpl, Long> jbpmTaskDao;
    
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
    @Override
    public List<ITask> getTaskList(ITaskParameter parameter) {
        ServerInitializer.inheritVeriniceContextState();
        if(parameter.getUsername()==null) {
            parameter.setUsername(getAuthService().getUsername());          
        }
        List<ITask> taskList = Collections.emptyList();
        if(doSearch(parameter)) {      
                
            DetachedCriteria taskCrit = DetachedCriteria.forClass(TaskImpl.class);
            taskCrit.add(Restrictions.eq("assignee", parameter.getUsername()));
            if(parameter.getSince()!=null) {
                taskCrit.add(Restrictions.ge("createTime",parameter.getSince()));
            }
            // create (un)read query if one is false:
            if((parameter.getRead()!=null && !parameter.getRead())
               || (parameter.getUnread()!=null && !parameter.getUnread())) {
                DetachedCriteria varCrit = taskCrit.createCriteria("execution").createCriteria("variables");
                varCrit.add(Restrictions.eq("key", ITaskService.VAR_READ_STATUS));
                if(parameter.getRead()!=null && parameter.getRead() && parameter.getUnread()!=null && !parameter.getUnread()) {            
                    varCrit.add(Restrictions.eq("string", ITaskService.VAR_READ));
                }
                if(parameter.getUnread()!=null && parameter.getUnread() && parameter.getRead()!=null && !parameter.getRead()) {
                    varCrit.add(Restrictions.eq("string", ITaskService.VAR_UNREAD));
                } 
                taskCrit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
            }
            List<Task> jbpmTaskList = getJbpmTaskDao().findByCriteria(taskCrit);
            //List<Task> jbpmTaskList = getTaskService().createTaskQuery().assignee(parameter.getUsername()).orderDesc(TaskQuery.PROPERTY_CREATEDATE).list();
            
            if(jbpmTaskList!=null && !jbpmTaskList.isEmpty()) {
                taskList = new ArrayList<ITask>();
                for (Task task : jbpmTaskList) { 
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
        return taskList;
    }

    private boolean doSearch(ITaskParameter parameter) {
        return parameter!=null 
           && parameter.getUsername()!=null 
           && (parameter.getUnread()==null && parameter.getRead()==null) ||
               (parameter.getUnread()!=null && parameter.getUnread()) || (parameter.getRead()!=null && parameter.getRead());
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
        
        String readStatus = (String) getTaskService().getVariable(task.getId(), ITaskService.VAR_READ_STATUS);
        taskInformation.setIsRead(ITaskService.VAR_READ.equals(readStatus));
        taskInformation.setStyle((taskInformation.getIsRead()) ? ITask.STYLE_READ : ITask.STYLE_UNREAD);
              
        String executionId = task.getExecutionId();   
        String uuidControl = (String) getExecutionService().getVariable(executionId,IExecutionProcess.VAR_UUID);      
        String typeId = (String) getExecutionService().getVariable(executionId,IExecutionProcess.VAR_TYPE_ID);      
        
        CnATreeElement element = getElementDao().findByUuid(uuidControl, RetrieveInfo.getPropertyInstance());
        taskInformation.setControlTitle(element.getTitle());
        
        taskInformation.setUuid(uuidControl); 
        taskInformation.setType(typeId);
        taskInformation.setDueDate(task.getDuedate());      
        return taskInformation;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#completeTask(java.lang.String)
     */
    @Override
    public void completeTask(String taskId) {
        getTaskService().completeTask(taskId);
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
}
