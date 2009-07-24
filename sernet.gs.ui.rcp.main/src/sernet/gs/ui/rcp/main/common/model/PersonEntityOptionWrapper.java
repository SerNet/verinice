/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.hui.common.multiselectionlist.IContextMenuListener;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;

public class PersonEntityOptionWrapper implements IMLPropertyOption {

	private Person person;

	public PersonEntityOptionWrapper(Person entity) {
		this.person = entity;
	}

	public IContextMenuListener getContextMenuListener() {
		return null;
	}

	public String getId() {
		return person.getEntity().getDbId().toString();
	}

	public String getName() {
		return person.getTitel();
	}

}
