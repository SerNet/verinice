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
package sernet.verinice.interfaces.bpm;

import java.util.Map;

import org.jbpm.api.TaskService;

/**
 * Task complete server handler are executed before a jBPM task is completed.
 * Every handler is registered to a task and a outcome name.
 * 
 * Parameter are passed from {@link ITaskService} completeTask methods.
 * 
 * ICompleteServerHandler are configured in veriniceserver-jbpm.xml as
 * a property of Spring bean taskService:
 * 
 * Example:
 * 
 * <bean id="myCompleteHandler" class="sernet.verinice.bpm.MyCompleteServerHandler">
 *   <property name="taskService" ref="taskService" />
 * </bean>
 * 
 * <bean id="taskService" class="sernet.verinice.bpm.TaskService">
 *   ..
 *   <property name="completeHandler">
 *     <map>
 *       <entry key="[JBPM_TASK_NAME].[JBPM_TRANSITION_NAME]" value-ref="myCompleteHandler"/>
 *     </map>
 *   </property>
 *   ..
 * </bean>
 * 
 * myCompleteHandler is executed before jBPM transition [JBPM_TRANSITION_NAME] of task [JBPM_TASK_NAME].
 *
 * @see {@link ITaskService}, {@link TaskService}
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ICompleteServerHandler {
 
    public static final String TASK_TYPE_DEFAULT = "default";
    
    public static final String OUTCOME_ID_DEFAULT = "default_outcome";
    
    /**
     * @return A jBPM task name
     */
    String getTaskType();
    
    /**
     * @return A jBPM transition / outcome name
     */
    String getOutcomeId();
    
    /**
     * Called before task with taskId is completed.
     * Param parameter is passed from {@link ITaskService} completeTask methods.
     * 
     * @param taskId The DB-id of a jBPM task
     * @param parameter Complete task parameter
     */
    void execute(String taskId, Map<String, Object> parameter);

}
