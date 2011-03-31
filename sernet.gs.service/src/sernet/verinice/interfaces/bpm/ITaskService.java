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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sernet.verinice.model.iso27k.Audit;

/**
 * Interface to tasks of the jBPM process engine.
 * Is used on the client site as a service interface
 * of a Spring HttpInvokerProxyFactoryBean and for the remote implementation
 * of the service.
 * 
 * You can wire instances of this interface to your Spring beans, 
 * see veriniceclient.xml for examples. Outside of the Spring context
 * use this method: ServiceFactory.lookupTaskService()
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ITaskService {

    
    String VAR_READ_STATUS = "TASK_READ_STATUS";
    
    String VAR_READ = "TASK_READ";
    
    String VAR_UNREAD = "TASK_UNREAD";
    
    String TASK_SET_ASSIGNEE = "isa.task.setAssignee";
    String OUTCOME_COMPLETE = "isa.transition.complete";
    String TASK_IMPLEMENT = "isa.task.implement";
    String OUTCOME_ESCALATE = "isa.transition.escalate";
    String TASK_ESCALATE = "isa.task.escalate";
    String TASK_CHECK_IMPLEMENTATION = "isa.task.checkImplementation";
    String OUTCOME_ACCEPT = "isa.transition.accepted";
    
    /**
     * Returns the task list for currently logged in user.
     * If no tasks exists or current user cannot be determined an empty list is returned.
     * 
     * @return the task list for current user
     */
    List<ITask> getTaskList();
    
    /**
     * Returns tasks created after a date for user with name username.
     * If no tasks exists an empty list is returned.
     * 
     * @param username a username
     * @param since Tasks created after this date are returned, if null all tasks are returned
     * @return task list for an user
     */
    List<ITask> getTaskList(ITaskParameter parameter);
    
    /**
     * Returns all audits in a list which are related to a task.
     * 
     * @return a list with audits
     */
    List<Audit> getAuditList();
    
    void completeTask(String taskId);
    
    void completeTask(String taskId, String outcomeId);
    
    void markAsRead(String taskId);
    
    /**
     * Returns true id this process is really active.
     * Used to determine if it is dummy implementation.
     * 
     * @return true id this process is really active
     */
    boolean isActive();
    
}
