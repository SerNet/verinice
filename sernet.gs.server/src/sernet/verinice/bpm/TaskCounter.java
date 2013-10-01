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

import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskCounter implements AssignmentHandler {

    private static final String COUNTER_SUFFIX = "_COUNTER";
    
    private String taskType;
    
    /* (non-Javadoc)
     * @see org.jbpm.api.task.AssignmentHandler#assign(org.jbpm.api.task.Assignable, org.jbpm.api.model.OpenExecution)
     */
    @Override
    public void assign(Assignable assignable, OpenExecution execution) throws Exception {
        Object value = execution.getVariable(taskType + COUNTER_SUFFIX);
        Integer n = 0;
        if(value!=null) {
            n = (Integer) value;
            n++;
        } 
        execution.setVariable(taskType + COUNTER_SUFFIX, n);
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

}
