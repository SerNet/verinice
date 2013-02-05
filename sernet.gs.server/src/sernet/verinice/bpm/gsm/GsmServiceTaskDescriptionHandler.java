/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.bpm.gsm;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.bpm.IGsmIsmExecuteProzess;
import sernet.verinice.interfaces.bpm.ITaskDescriptionHandler;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GsmServiceTaskDescriptionHandler implements ITaskDescriptionHandler {

    private static final Logger LOG = Logger.getLogger(GsmServiceTaskDescriptionHandler.class);
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskDescriptionHandler#loadDescription(java.lang.String, java.util.Map)
     */
    @Override
    public String loadDescription(String taskId, Map<String, Object> processVars) {        
        Object value = processVars.get(IGsmIsmExecuteProzess.VAR_ELEMENT_SET);
        String description = "unknown";
        if(!(value instanceof Set<?>)) {
            LOG.error("Process variable " + IGsmIsmExecuteProzess.VAR_ELEMENT_SET + " is not a Set. This is nasty...");
        } else {
            description = GsmService.createElementInformation((Set<CnATreeElement>) value);
        }
        return description;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.bpm.ITaskDescriptionHandler#loadTitle(java.lang.String, java.util.Map)
     */
    @Override
    public String loadTitle(String taskId, Map<String, Object> processVars) {
        return sernet.verinice.model.bpm.Messages.getString(taskId);
    }

}
