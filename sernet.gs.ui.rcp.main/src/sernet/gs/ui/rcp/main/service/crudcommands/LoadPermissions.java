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
import java.util.Set;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;

/**
 * Load permission items for cnatreeelement.
 * 
 */
@SuppressWarnings("serial")
public class LoadPermissions extends GenericCommand {

	private CnATreeElement cte;
	
	private Set<Permission> permissions;
	
	public LoadPermissions(CnATreeElement cte) {
		this.cte = cte;
	}

	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(cte.getTypeId());
		
		cte = dao.findById(cte.getDbId());
		
		permissions = cte.getPermissions();
		
		// Hydrate the elements.
		for (Permission p : permissions)
		{
			p.getRole();
			p.isReadAllowed();
			p.isWriteAllowed();
		}
	}
	
	public Set<Permission> getPermissions()
	{
		return permissions;
	}

}
