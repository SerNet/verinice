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
package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

public class RefreshElement<T extends CnATreeElement> extends GenericCommand {

	private T element;
	private boolean includeCollections;
	private Integer dbId;
	private String typeId;

	public RefreshElement(T element, boolean includeCollections) {
		// slim down for transfer:
		dbId = element.getDbId();
		typeId = element.getTypeId();
		this.includeCollections = includeCollections;
	}
	
	public RefreshElement(T element) {
		this(element, false);
	}
	
	public void execute() {
		IBaseDao dao =  getDaoFactory().getDAO(this.typeId);
		element = (T) dao.findById(this.dbId);
		HydratorUtil.hydrateElement(dao, element, includeCollections);
	}

	private Integer getId(T element2) {
		if (element2 instanceof CnATreeElement) {
			CnATreeElement elmt = (CnATreeElement) element2;
			return elmt.getDbId();
		}
		
		return null;
		
	}

	public T getElement() {
		return element;
	}

}
