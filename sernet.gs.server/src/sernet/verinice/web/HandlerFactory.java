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
package sernet.verinice.web;

import java.util.LinkedList;
import java.util.List;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * Creates web {@link IActionHandler}s for an {@link IISO27kGroup}.
 * In general two handlers are created one for creating new elements 
 * and one for creating new groups.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class HandlerFactory {
    
    public static List<IActionHandler> getHandlerForElement(IISO27kGroup group) {       
        List<IActionHandler> handlers = new LinkedList<IActionHandler>();
        int n = 0;
        for (String childType : group.getChildTypes()) {
            handlers.add(new CreateElementHandler((CnATreeElement) group, childType, "h" + n));
            n++;
            handlers.add(new CreateElementHandler((CnATreeElement) group, group.getTypeId(), "h" + n));
            n++;
        }
        return handlers;
    }
}
