/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.iso27k.rcp.CatalogView;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.service.iso27k.ItemControlTransformer;

public class GenericItemTransformer {

	
	public static CnATreeElement  transform(IItem item) {
	    CnATreeElement elmt = null;
	    if (item.getTypeId() == IItem.CONTROL) {
	        elmt = ItemControlTransformer.transform(item);
	    } 
	    else if (item.getTypeId() == IItem.THREAT) {
	        elmt = ItemThreatTransformer.transform(item);
	    }
	    else if (item.getTypeId() == IItem.VULNERABILITY) {
	        elmt = ItemVulnerabilityTransformer.transform(item);
	    }
	    return elmt;
	}

	public static CnATreeElement transformToGroup(IItem item) {
        CnATreeElement elmt = null;
        if (item.getTypeId() == IItem.CONTROL) {
            elmt = ItemControlTransformer.transformToGroup(item);
        } 
        else if (item.getTypeId() == IItem.THREAT) {
            elmt = ItemThreatTransformer.transformToGroup(item);
        }
        else if (item.getTypeId() == IItem.VULNERABILITY) {
            elmt = ItemVulnerabilityTransformer.transformToGroup(item);
        }
        return elmt;
    
	}
	
	


}
