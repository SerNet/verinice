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
package sernet.verinice.bpm.qm;

import java.util.HashMap;
import java.util.Map;

import sernet.verinice.interfaces.bpm.ICompleteServerHandler;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IIsaQmProcess;
import sernet.verinice.interfaces.bpm.ITaskService;

/**
 * This task complete server handler reads param
 * IIsaQmProcess.VAR_IQM_ASSIGNEE and adds it's
 * value as process/task var IIsaQmProcess.VAR_IQM_ASSIGNEE
 * to task with id taskId.
 * 
 * Configured in veriniceserver-jbpm.xml as a property of Spring bean taskService.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IsaQmSetAssigneeHandler implements ICompleteServerHandler {

    private ITaskService taskService;
    
    /**
     * Value of param IIsaQmProcess.VAR_IQM_ASSIGNEE is added
     * to task as var IGenericProcess.VAR_ASSIGNEE_NAME.
     *  
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#execute(java.util.Map)
     */
    @Override
    public void execute(String taskId, Map<String, Object> parameter) {
        if(parameter!=null) {
            String assignee = (String) parameter.get(IIsaQmProcess.VAR_IQM_ASSIGNEE);
            if(assignee!=null) {
                Map<String, Object> param = new HashMap<String, Object>();
                param.put(IGenericProcess.VAR_ASSIGNEE_NAME, assignee);
                getTaskService().setVariables(taskId, param);
            }
        }
        

    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#getTaskType()
     */
    @Override
    public String getTaskType() {
        return IIsaQmProcess.TASK_IQM_SET_ASSIGNEE;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#getOutcomeId()
     */
    @Override
    public String getOutcomeId() {
        return IIsaQmProcess.TRANS_IQM_SET_ASSIGNEE;
    }

    public ITaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

}
