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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.Set;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.Permission;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

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
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(cte.getClass());
		
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
