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
package sernet.verinice.interfaces.bpm;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface to handle tasks of the jBPM process engine.
 * 
 * Task service is accessible from verinice client by Spring remoting, configured in
 * springDispatcher-servlet.xml and veriniceclient.xml
 * 
 * You can wire instances of this interface to your Spring beans, 
 * see veriniceclient.xml for examples. Outside of the Spring context
 * use this method: ServiceFactory.lookupTaskService()
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ITaskService {

    String DESCRIPTION_SUFFIX = ".description";
    
    String VAR_READ_STATUS = "TASK_READ_STATUS";
    
    String VAR_READ = "TASK_READ";
    
    String VAR_UNREAD = "TASK_UNREAD";
    
    /**
     * Returns the task list for currently logged in user.
     * If no tasks exists or current user cannot be determined an empty list is returned.
     * 
     * @return the task list for current user
     */
    List<ITask> getCurrentUserTaskList();
    
    /**
     * Returns the task list for currently logged in user. If no tasks exists or
     * current user cannot be determined an empty list is returned.
     *
     * @param parameter
     *            If the parameter contains a user information it is overwritten
     *            by this method with the current user name.
     *
     * @return the task list of the currently logged in user.
     */
    List<ITask> getCurrentUserTaskList(ITaskParameter parameter);

    /**
     * Returns tasks created after a date for user with name username.
     * If no tasks exists an empty list is returned.
     * 
     */
    List<ITask> getTaskList(ITaskParameter parameter);
    
    /**
     * Returns uuids of all elements which are related to a task.
     * 
     * @return a list with uuids
     */
    List<String> getElementList();
    
    void completeTask(String taskId);
    
    void completeTask(String taskId, String outcomeId);
    
    void completeTask(String taskId, Map<String, Object> parameter);
    
    void completeTask(String taskId, String outcomeId, Map<String, Object> parameter);
    
    void markAsRead(String taskId);
    
    /**
     * Returns true id this process is really active.
     * Used to determine if it is dummy implementation.
     * 
     * @return true id this process is really active
     */
    boolean isActive();
    
    /**
     * Cancel and deleted a task
     * 
     * @param taskId The database id of an task
     */
    void cancelTask(String taskId);

    /**
     * Set the assignee of a task.
     * 
     * @param taskIdSet A set with the database ids of tasks
     * @param username The login name of an user
     */
    void setAssignee(Set<String> taskIdSet, String username);
    
    /**
     * @param taskIdset
     * @param duedate
     */
    void setDuedate(Set<String> taskIdset, Date duedate);
    
    /**
     * Set the assignee variable of a task.
     * Name of the var.: IGenericProcess.VAR_ASSIGNEE_NAME
     * 
     * @param taskIdSet A set with the database ids of tasks
     * @param username The login name of an user
     */
    void setAssigneeVar(Set<String> taskIdSet, String username);

    Map<String, Object> getVariables(String taskId);
    
    /**
     * @param taskId
     * @param param
     */
    void setVariables(String taskId, Map<String, Object> param);
    
    String loadTaskDescription(String taskId, Map<String, Object> varMap);
    
    String loadTaskTitle(String taskId, Map<String, Object> varMap);

    Set<String> getTaskReminderBlacklist();
    
    /**
     * @param taskId
     * @param changedElementProperties
     * 
     * Set changed element properties to tasks.
     * 
     */
    void updateChangedElementProperties(String taskId, Map<String, String> changedElementProperties);
    
    /**
     * @param taskId
     * 
     * Returns changed element properties from tasks.
     * If no tasks exists an empty map is returned.
     * 
     */
    Map<String, String> loadChangedElementProperties(String taskId);
    
    /**
     * @param taskId
     * @param uuid
     * 
     * Save changed element properties to CnATreeElement.
     * 
     */
    void saveChangedElementPropertiesToCnATreeElement(String taskId, String uuid);
}
