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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class LoadChildrenForExpansion extends GenericCommand {
	
	private CnATreeElement parent;
	private Integer dbId;

    private String typeId;

	public LoadChildrenForExpansion(CnATreeElement parent) {
		this(parent, new HashSet<Class<?>>());
	}

	public LoadChildrenForExpansion(CnATreeElement parent, Set<Class<?>> filteredClasses) {
		// slim down for transfer:
		dbId = parent.getDbId();
		typeId = parent.getTypeId();
		this.parent = null;
	}
	
	@Override
    public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
		
		RetrieveInfo ri = new RetrieveInfo();
		ri.setParent(true).setProperties(true).setChildren(true).setChildrenProperties(true).setGrandchildren(true);
		parent = dao.retrieve(dbId,ri);	
	}
	
	public CnATreeElement getElementWithChildren() {
		return parent;
	}
}
