/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * Create and save new element of type type to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class CreateLink<T extends CnALink, U extends CnATreeElement, V extends CnATreeElement> 
extends GenericCommand {

	private U dragged;
	private V target;
	private CnALink link;
	private String typeId;
	private String comment;

	public CreateLink(V target, U dragged) {
		this(target, dragged, "", "");
	}
	
	public CreateLink(V target, U dragged, String typeId) {
		this(target, dragged, typeId, "");
	}
	
	public CreateLink(V target, U dragged, String typeId, String comment) {
		this.target = target;
		this.dragged = dragged;
		this.typeId = typeId;
		this.comment = comment;
	}
	
	public void execute() {
		IBaseDao<CnALink, Serializable> linkDao 
			= (IBaseDao<CnALink, Serializable>) getDaoFactory().getDAO(CnALink.class);
		
		IBaseDao<U, Serializable> draggedDao 
		= (IBaseDao<U, Serializable>) getDaoFactory().getDAO(dragged.getClass());

		IBaseDao<V, Serializable> targetDao 
		= (IBaseDao<V, Serializable>) getDaoFactory().getDAO(target.getClass());

		// if dragged or target are cglib enhanced, we won't get a DAO, but in this case we don't need to reload
		// because we're already inside the session:
		if (draggedDao != null && targetDao != null) {
			draggedDao.reload(dragged, dragged.getDbId());
			targetDao.reload(target, target.getDbId());
		}
		
		link = new CnALink(target, dragged, typeId, comment);
		linkDao.merge(link, true);
		
	}

	public CnALink getLink() {
		return link;
	}


}
