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

/**
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class ItemControlTransformer {

	/**
	 * @param item
	 * @return
	 */
	public static Control transform(IItem item) {
		Control control = new Control();
		control.setAbbreviation(item.getName());
		control.setTitel(item.getName());
		control.setDescription(item.getDescription());
		return control;
	}

	/**
	 * @param item
	 * @return
	 */
	public static ControlGroup transformToGroup(IItem item) {
		ControlGroup controlGroup = new ControlGroup();
		controlGroup.setTitel(item.getName());
		return controlGroup;
	}

}
