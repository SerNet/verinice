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

import java.util.Map;

import sernet.verinice.bpm.DefaultTaskDescriptionHandler;
import sernet.verinice.interfaces.bpm.IIsaQmProcess;
import sernet.verinice.interfaces.bpm.ITaskDescriptionHandler;
import sernet.verinice.model.bpm.Messages;

/**
 * Reads value of process/task variable with name
 * IIsaQmProcess.VAR_FEEDBACK) and adds this to the description of a task
 * from message.properties.
 * 
 * To extend thid class override method getMessageKey to select the right message key
 * for sernet/verinice/bpm/message[_de].properties
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class QmDescriptionHandler extends DefaultTaskDescriptionHandler implements ITaskDescriptionHandler {
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskDescriptionHandler#loadDescription(org.jbpm.api.task.Task)
     */
    @Override
    public String loadDescription(String taskId, Map<String, Object> varMap) {
        Object value = varMap.get(IIsaQmProcess.VAR_FEEDBACK);
        String feedback = "emtpy";
        if(value instanceof char[]) {
            feedback = new String((char[])value);
        } else if(value!=null) {
            feedback = (String) value;
        }
        return Messages.getString(getMessageKey(), feedback);   
    }
    
    protected abstract String getMessageKey();

  

}
