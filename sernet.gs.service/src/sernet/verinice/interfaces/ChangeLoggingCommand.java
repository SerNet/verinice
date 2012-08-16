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
package sernet.verinice.interfaces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Base class for change-logging-commands with two methods which converts.
 * one result list to another.
 * 
 * You only have to override getChangedElements()
 * getChanges() uses getChangedElements to get it's result
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class ChangeLoggingCommand extends GenericCommand implements IChangeLoggingCommand {


    
   
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return Collections.emptyList();
    }
    
    /**
     * Creates a list ElementChanges by returnig the changed element list 
     * and using the generic change type of the command.
     * 
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChanges()
     */
    @Override
    public List<ElementChange> getChanges() {
        List<ElementChange> result = Collections.emptyList();
        List<CnATreeElement> elements = getChangedElements();
        if(elements==null || elements.isEmpty()) {
            return result;
        }
        result = new ArrayList<ElementChange>(elements.size());
        for (CnATreeElement element : elements) {
            result.add(new ElementChange(element, getChangeType())); 
        }
        return result;
    }

}
