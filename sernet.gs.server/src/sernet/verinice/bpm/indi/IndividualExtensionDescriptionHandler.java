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

import org.apache.log4j.Logger;

import sernet.verinice.bpm.IRemindService;
import sernet.verinice.bpm.TaskService;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.ITaskDescriptionHandler;
import sernet.verinice.model.bpm.Messages;
import sernet.verinice.model.bpm.MissingParameterException;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndividualExtensionDescriptionHandler extends IndividualTaskDescriptionHandler implements ITaskDescriptionHandler {

    private static final Logger LOG = Logger.getLogger(IndividualExtensionDescriptionHandler.class);
    
    private IRemindService remindService;
    
    @Override
    public String loadTitle(String taskId, Map<String, Object> varMap) {
        return Messages.getString(getTitleKey());
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.indi.IndividualTaskDescriptionHandler#loadDescription(java.lang.String, java.util.Map)
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
        
        String assignee = (String) varMap.get(IGenericProcess.VAR_ASSIGNEE_NAME);
        String address = "";
        String name = "";
        
        Map<String, String> assigneeData;
        try {
            assigneeData = getRemindService().loadUserData(assignee);
            address = assigneeData.get(IRemindService.TEMPLATE_ADDRESS);
            name = assigneeData.get(IRemindService.TEMPLATE_NAME);
        } catch (MissingParameterException e) {
            LOG.error("Error while loading data for assignee: " + assignee, e);
        }
        
        Object justification = varMap.get(IIndividualProcess.VAR_EXTENSION_JUSTIFICATION);
        
        if(getDescriptionKey()!=null) {
            return Messages.getString(getDescriptionKey(), address, name, justification, description); 
        } else {
            return description;
        }
    }
    
    public IRemindService getRemindService() {
        return remindService;
    }

    public void setRemindService(IRemindService remindService) {
        this.remindService = remindService;
    }

    @Override
    protected String getDescriptionKey() {
        return IIndividualProcess.TASK_EXTENSION + TaskService.DESCRIPTION_SUFFIX;
    }
    
    @Override
    protected String getTitleKey() {
        return IIndividualProcess.TASK_EXTENSION;
    }

}
