/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Command that wants to notify other clients of changes.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public interface IChangeLoggingCommand {

    /**
     * The session id of the client making the changes.
     * 
     * @return
     */
    public String getStationId();

    /**
     * Returns the elements this command changed.
     * 
     * @return Changed elements
     */
    default public List<CnATreeElement> getChangedElements() {
        return Collections.emptyList();
    }

    default public List<ElementChange> getChanges() {
        List<ElementChange> result = Collections.emptyList();
        List<CnATreeElement> elements = getChangedElements();
        if (elements == null || elements.isEmpty()) {
            return result;
        }
        result = new ArrayList<>(elements.size());
        for (CnATreeElement element : elements) {
            result.add(new ElementChange(element, getChangeType()));
        }
        return result;
    }

    /**
     * @return
     */
    public int getChangeType();
}
