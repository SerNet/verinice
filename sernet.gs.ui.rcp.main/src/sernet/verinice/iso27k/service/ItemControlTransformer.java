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
package sernet.verinice.iso27k.service;

import java.util.ArrayList;
import java.util.List;

import sernet.verinice.iso27k.model.Control;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class ItemControlTransformer implements ITransformer<IItem, Control> {

	List<IItem> itemList;
	List<Control> controlList;
	
	/**
	 * @param itemList
	 */
	public ItemControlTransformer(List<IItem> itemList) {
		this.itemList = itemList;
		transform();
	}

	/**
	 * 
	 */
	private void transform() {
		if(itemList!=null) {
			if(controlList==null) {
				controlList = new ArrayList<Control>(itemList.size());
			} else {
				controlList.clear();
			}
			for (IItem item : itemList) {
				Control control = new Control();
				control.setAbbreviation(item.getName());
				control.setTitel(item.getName());
				control.setDescription(item.getDescription());
				controlList.add(control);		
			}	
		}	
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.ITransformer#getResultList()
	 */
	public List<Control> getResultList() {
		if( (itemList!=null && controlList==null) 
			|| (itemList!=null && itemList.size()!=controlList.size()) ) {
			transform();
		}
		return controlList;
	}

}
