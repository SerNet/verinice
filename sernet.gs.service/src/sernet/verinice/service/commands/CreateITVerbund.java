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
package sernet.verinice.service.commands;

import java.util.Set;

import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings({ "serial", "unchecked", "rawtypes" })
public class CreateITVerbund extends CreateElement {
    
    public CreateITVerbund(CnATreeElement container, Class type) {
        super(container,type,true,true);
    }
    
    public CreateITVerbund(CnATreeElement container, Class type, boolean createChildren) {
        super(container, type, true, createChildren);
    }
    
    @Override
    public void execute() {
        super.execute();
        if (super.element instanceof ITVerbund) {
            ITVerbund verbund = (ITVerbund) element;
            if(createChildren) {
                verbund.createNewCategories();
            }
            Set<CnATreeElement> children = verbund.getChildren();
            for (CnATreeElement child : children) {
                addPermissionsForScope(child);
            }
            element.setScopeId(element.getDbId());
            for (CnATreeElement group : element.getChildren()) {
                group.setScopeId(element.getDbId());
            }
        }
        
    }
    
    @Override
    public ITVerbund getNewElement() {
        return (ITVerbund) super.getNewElement();
    }

}
