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

import java.util.Map;

import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.ITaskDescriptionHandler;
import sernet.verinice.model.bpm.Messages;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndividualTaskDescriptionHandler implements ITaskDescriptionHandler {

    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskDescriptionHandler#loadDescription(org.jbpm.api.task.Task)
     */
    @Override
    public String loadDescription(String taskId, Map<String, Object> varMap) {
        Object value = varMap.get(getDescriptionVar());
        String description = "emtpy";
        if(value instanceof char[]) {
            description = new String((char[])value);
        } else if(value!=null) {
            description = (String) value;
        }
        if(getDescriptionKey()!=null) {
            return Messages.getString(getDescriptionKey(), description); 
        } else {
            return description;
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskDescriptionHandler#loadTitle(org.jbpm.api.task.Task)
     */
    @Override
    public String loadTitle(String taskId, Map<String, Object> varMap) {
        Object value = varMap.get(getTitleVar());
        String title = "emtpy";
        if(value instanceof char[]) {
            title = new String((char[])value);
        } else if(value!=null) {
            title = (String) value;
        }

        if(getTitleKey()!=null) {
            return Messages.getString(getTitleKey(), title); 
        } else {
            return title;
        }
    }
    
    protected String getDescriptionVar() {
        return IIndividualProcess.VAR_DESCRIPTION;
    }
    
    protected String getTitleVar() {
        return IIndividualProcess.VAR_TITLE;
    }
    
    protected String getDescriptionKey() {
        return null;
    }
    
    protected String getTitleKey() {
        return null;
    }

}
