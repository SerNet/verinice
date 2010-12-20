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
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.iso27k.ItemControlTransformer;

public class GenericItemTransformer {

	
	public static CnATreeElement  transform(IItem item) {
	    CnATreeElement elmt = null;
	    switch(item.getTypeId()) {
		    case IItem.CONTROL: 
		    	elmt = ItemControlTransformer.transformGeneric(item, new Control());
		    	break;
		    case IItem.THREAT: 
		    	elmt = ItemThreatTransformer.transform(item);
		    	break;
		    case IItem.VULNERABILITY: 
		        elmt = ItemVulnerabilityTransformer.transform(item);
		    	break;
		    case IItem.ISA_TOPIC: 
		        elmt = ItemControlTransformer.transformGeneric(item, new SamtTopic());
	    }
	    return elmt;
	}

	public static CnATreeElement transformToGroup(IItem item) {
        CnATreeElement elmt = null;
        switch(item.getTypeId()) {
		    case IItem.CONTROL: 
		    	elmt = ItemControlTransformer.transformToGroup(item, new ControlGroup());
		    	break;
		    case IItem.THREAT: 
		    	elmt = ItemThreatTransformer.transformToGroup(item);
		    	break;
		    case IItem.VULNERABILITY: 
		    	elmt = ItemVulnerabilityTransformer.transformToGroup(item);
		    	break;
		    case IItem.ISA_TOPIC: 
		    	elmt = ItemControlTransformer.transformToGroup(item, new ControlGroup());
	    } 
        return elmt;
    
	}
	
	


}
