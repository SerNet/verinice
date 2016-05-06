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
package sernet.verinice.bpm.indi;

import java.util.HashMap;
import java.util.Map;

import sernet.verinice.interfaces.bpm.ICompleteServerHandler;
import sernet.verinice.interfaces.bpm.IIndividualProcess;

/**
 * This task complete server handler reads param
 * IIndividualProcess.VAR_EXTENSION_JUSTIFICATION and adds it's
 * value as process/task variable IIndividualProcess.VAR_EXTENSION_JUSTIFICATION.
 * 
 * This handler is configured in veriniceserver-jbpm.xml 
 * as a property of Spring bean taskService.
 * 
 * This handler is part of the business process individual-task
 * defined in individual-task.jpdl.xml.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndiExtensionHandler implements ICompleteServerHandler {

    private Map<String, Object> taskParameter;
    
    /**
     * Value of param IIsaQmProcess.VAR_IQM_ASSIGNEE is added
     * to task as var IGenericProcess.VAR_ASSIGNEE_NAME.
     *  
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#execute(java.util.Map)
     */
    @Override
    public void execute(String taskId, Map<String, Object> parameter) {
        if(parameter!=null) {
            String justification = (String) parameter.get(IIndividualProcess.VAR_EXTENSION_JUSTIFICATION);
            if(justification!=null) {
                getTaskParameter().put(IIndividualProcess.VAR_EXTENSION_JUSTIFICATION, justification);
            }
        }
        

    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#getTaskType()
     */
    @Override
    public String getTaskType() {
        return IIndividualProcess.TASK_EXECUTE;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ICompleteServerHandler#getOutcomeId()
     */
    @Override
    public String getOutcomeId() {
        return IIndividualProcess.TRANS_EXTENSION;
    }

    public Map<String, Object> getTaskParameter() {
        if(taskParameter==null) {
            taskParameter = new HashMap<String, Object>();
        }
        return taskParameter;
    }

    public void setTaskParameter(Map<String, Object> taskParameter) {
        this.taskParameter = taskParameter;
    }
}
