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

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;

import sernet.gs.ui.rcp.main.bsi.dnd.CnPItems;
import sernet.verinice.iso27k.model.Group;

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
		Group selectedGroup = (Group) receiver;
		List sourceItemList = CnPItems.getItems();
		boolean enabled = (sourceItemList!=null && sourceItemList.size()>0);
		for (Object object : sourceItemList) {
			if(!selectedGroup.canContain(object)) {
				enabled = false;
				break;
			}
		}
		return enabled;
	}

}
