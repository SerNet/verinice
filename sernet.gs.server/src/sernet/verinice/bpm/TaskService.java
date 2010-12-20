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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.task.Task;

import sernet.gs.server.ServerInitializer;
import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IControlExecutionProcess;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.TaskInformation;
import sernet.verinice.model.iso27k.Control;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class TaskService implements ITaskService{

    private ProcessEngine processEngine;
    
    private IAuthService authService;
    
    private IBaseDao<Control,Integer> controlDao;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#getTaskList()
     */
    @Override
    public List<ITask> getTaskList() {
        return getTaskList(getAuthService().getUsername());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#getTaskList(java.lang.String)
     */
    @Override
    public List<ITask> getTaskList(String username) {
        ServerInitializer.inheritVeriniceContextState();
        List<ITask> taskList = Collections.emptyList();
        if(username!=null) {
            List<Task> jbpmTaskList = getTaskService().findPersonalTasks(username);
            if(jbpmTaskList!=null && !jbpmTaskList.isEmpty()) {
                taskList = new ArrayList<ITask>(jbpmTaskList.size());
                for (Task task : jbpmTaskList) {                          
                    taskList.add(map(task));
                }                
            }
        }    
        return taskList;
    }
    
    /**
     * @param task
     * @param taskInformation
     */
    private TaskInformation map(Task task) {
        TaskInformation taskInformation = new TaskInformation();
        taskInformation.setId(task.getId());
        taskInformation.setName(task.getName());
        taskInformation.setCreateDate(task.getCreateTime());    
        String executionId = task.getExecutionId();   
        String uuidControl = (String) getExecutionService().getVariable(executionId,IControlExecutionProcess.VAR_CONTROL_UUID);      
        Control control = getControlDao().findByUuid(uuidControl, RetrieveInfo.getPropertyInstance());
        taskInformation.setControlTitle(control.getTitle());
        taskInformation.setControlUuid(uuidControl);
        return taskInformation;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskService#completeTask(java.lang.String)
     */
    @Override
    public void completeTask(String taskId) {
        getTaskService().completeTask(taskId);
    }

    public org.jbpm.api.TaskService getTaskService() {
        return getProcessEngine().getTaskService();
    }
    
    public ExecutionService getExecutionService() {
        return getProcessEngine().getExecutionService();
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

    public IBaseDao<Control, Integer> getControlDao() {
        return controlDao;
    }

    public void setControlDao(IBaseDao<Control, Integer> controlDao) {
        this.controlDao = controlDao;
    }

}
