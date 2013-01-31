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
import sernet.verinice.model.iso27k.IISO27Scope;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * Creates web {@link IActionHandler}s for an {@link IISO27kGroup}.
 * In general two handlers are created one for creating new elements 
 * and one for creating new groups.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class HandlerFactory {
    
    private HandlerFactory(){};
    
    public static List<IActionHandler> getHandlerForGroup(CnATreeElement group) {       
        List<IActionHandler> handlers = new LinkedList<IActionHandler>();
        handlers.add(getGroupHandler(group));
        handlers.addAll(getElementHandler(group));
        return handlers;
    }
    
    public static List<IActionHandler> getElementHandler(CnATreeElement group) {       
        List<IActionHandler> handlers = new LinkedList<IActionHandler>();
        if(group instanceof IISO27kGroup && !(group instanceof ImportIsoGroup)) {           
            for (String childType : (((IISO27kGroup)group).getChildTypes())) {
                handlers.add(new CreateElementHandler((CnATreeElement) group, childType));          
            }
        }
        return handlers;
    }
    
    public static IActionHandler getGroupHandler(CnATreeElement group) {       
        IActionHandler handler = null;
        if(group instanceof IISO27kGroup && !(group instanceof ImportIsoGroup) && !(group instanceof IISO27Scope)) {
            handler = new CreateElementHandler((CnATreeElement) group, group.getTypeId());
        }
        return handler;
    }
     
    public static IActionHandler getOrgHandler(CnATreeElement model) {
        return new CreateElementHandler(model, Organization.TYPE_ID);
    }
}
