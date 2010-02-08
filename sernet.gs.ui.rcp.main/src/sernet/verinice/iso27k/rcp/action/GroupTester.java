/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.expressions.PropertyTester;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.verinice.iso27k.rcp.CnPItems;
import sernet.verinice.iso27k.service.CopyService;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class GroupTester extends PropertyTester {

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		CnATreeElement selectedElement = (CnATreeElement) receiver;
		List copyList = CnPItems.getCopyItems();
		List cutList = CnPItems.getCutItems();
		List activeList = Collections.EMPTY_LIST;
		boolean enabled = ((copyList!=null && copyList.size()>0) || (cutList!=null && cutList.size()>0));
		if(!copyList.isEmpty()) {
			activeList = copyList;
		} else if(!copyList.isEmpty()) {
			activeList = cutList;
		}
		for (Object object : activeList) {
			if(!selectedElement.canContain(object)) {
				enabled = false;
				break;
			}
			if(object instanceof CnATreeElement) {
				CnATreeElement element = (CnATreeElement) object;
				if(CopyService.BLACKLIST.contains(element.getTypeId())) {
					enabled = false;
					break;
				}
			}
		}
		
		return enabled;
	}

}
