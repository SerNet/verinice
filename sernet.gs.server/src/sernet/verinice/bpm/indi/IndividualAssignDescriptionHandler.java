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

import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.bpm.TaskService;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.ITaskDescriptionHandler;
import sernet.verinice.model.bpm.Messages;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndividualAssignDescriptionHandler extends IndividualTaskDescriptionHandler implements ITaskDescriptionHandler {

    private static final String NAME_SUFFIX = "_name";
    
    @Override
    public String loadTitle(String taskId, Map<String, Object> varMap) {
        return Messages.getString(getTitleKey());
    }
    

    @Override
    public String loadDescription(String taskId, Map<String, Object> varMap) {
        String relationId = (String) varMap.get(getRelationVar());
        String relationKey = relationId + NAME_SUFFIX;
        String relation = HUITypeFactory.getInstance().getMessage(relationKey);
        
        Object value = varMap.get(getDescriptionVar());
        String description = null;
        if(value instanceof char[]) {
            description = new String((char[])value);
        } else if(value!=null) {
            description = (String) value;
        }
        
        if(relationId!=null && description!=null) {
            return Messages.getString(getDescriptionRelationKey(), relation, description);
        } else {
            return Messages.getString(getDescriptionKey());
        }
    }
    
    protected String getRelationVar() {
        return IIndividualProcess.VAR_RELATION_ID;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.indi.IndividualTaskDescriptionHandler#getDescriptionVar()
     */
    protected String getDescriptionVar() {
        return IIndividualProcess.VAR_DESCRIPTION;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.indi.IndividualTaskDescriptionHandler#getDescriptionKey()
     */
    protected String getDescriptionKey() {
        return IIndividualProcess.TASK_ASSIGN + TaskService.DESCRIPTION_SUFFIX;
    }
    
    private String getDescriptionRelationKey() {
        return IIndividualProcess.TASK_ASSIGN + ".relation" + TaskService.DESCRIPTION_SUFFIX;
    }
    
    protected String getTitleKey() {
        return IIndividualProcess.TASK_ASSIGN;
    }

}
