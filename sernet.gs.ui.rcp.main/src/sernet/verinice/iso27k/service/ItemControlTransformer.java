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

import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.rcp.CatalogView;

/**
 * Transforms {@link IItem} from {@link CatalogView} to ISO 27k {@link Control}s
 * or {@link ControlGroup}s
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class ItemControlTransformer {

	/**
	 * Transforms a catalog item to a control.
	 * 
	 * @param item an item from a control catalog
	 * @return an ISO 27k control
	 */
	public static Control transform(IItem item) {
		Control control = new Control();
		control.setAbbreviation(item.getNumberString());
		control.setTitel(item.getName());
		control.setDescription(item.getDescription());
		return control;
	}

	/**
	 * Transforms a catalog item to a control group.
	 * 
	 * @param item an item from a control catalog
	 * @return an ISO 27k control group
	 */
	public static ControlGroup transformToGroup(IItem item) {
		ControlGroup controlGroup = new ControlGroup();
		controlGroup.setTitel(item.getName());
		return controlGroup;
	}

}
