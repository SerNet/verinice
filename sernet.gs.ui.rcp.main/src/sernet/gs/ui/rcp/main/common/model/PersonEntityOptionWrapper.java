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
package sernet.gs.ui.rcp.main.common.model;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.multiselectionlist.ICheckBoxHandler;
import sernet.hui.common.multiselectionlist.IContextMenuListener;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.verinice.model.bsi.Person;

public class PersonEntityOptionWrapper implements IMLPropertyOption {

	private Entity person;

	public PersonEntityOptionWrapper(Entity entity) {
		this.person = entity;
	}

	public IContextMenuListener getContextMenuListener() {
		return null;
	}

	public String getId() {
		return person.getDbId().toString();
	}

	public String getName() {
		return Person.getTitel(person);
	}

	/* (non-Javadoc)
	 * @see sernet.hui.common.multiselectionlist.IMLPropertyOption#getCheckboxHandler()
	 */
	public ICheckBoxHandler getCheckboxHandler() {
		return null;
	}

	/* (non-Javadoc)
	 * @see sernet.hui.common.multiselectionlist.IMLPropertyOption#setCheckboxHandler(sernet.hui.common.multiselectionlist.ICheckBoxHandler)
	 */
	public void setCheckboxHandler(ICheckBoxHandler checkBoxHandler) {
		// do nothing
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return getName();
	}

}
