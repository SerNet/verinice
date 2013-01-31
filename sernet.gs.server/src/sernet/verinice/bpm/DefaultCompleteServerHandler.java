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

import java.util.Map;
import java.util.Map.Entry;

import sernet.verinice.interfaces.bpm.ICompleteServerHandler;
import sernet.verinice.interfaces.bpm.ITaskService;

/**
 * Default ICompleteServerHandler which is active if parameter are set in
 * ITaskService.completeTask but no ICompleteServerHandler is set
 * for this task/outcome in veriniceserver-jbpm.xml.
 * 
 * DefaultCompleteServerHandler set all params passed from the client
 * as jBPM process variables.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DefaultCompleteServerHandler implements ICompleteServerHandler {

    private ITaskService taskService;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#getTaskType()
     */
    @Override
    public String getTaskType() {
        return TASK_TYPE_DEFAULT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#getOutcomeId()
     */
    @Override
    public String getOutcomeId() {
        return OUTCOME_ID_DEFAULT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#execute(java.lang.String, java.util.Map)
     */
    @Override
    public void execute(String taskId, Map<String, Object> parameter) {
        if(parameter!=null) {        
            Map<String, Object> taskParameter = getTaskService().getVariables(taskId);
            for (Entry<String, Object> entry : parameter.entrySet()) {
                Object value = entry.getKey();
                if(value instanceof String) {
                    String s = (String) value;
                    if(s.length()>254) {
                        taskParameter.put(entry.getKey(), s.toCharArray());
                    } else {
                        taskParameter.put(entry.getKey(), s);
                    }
                    
                }
            }
            getTaskService().setVariables(taskId, taskParameter);
        }
    }
    
    public ITaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

}
