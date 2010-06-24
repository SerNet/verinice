/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;

/**
 * Updates the access permissions for the given element.
 * 
 * <p>Optionally all child elements inherit the permissions as well.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
@SuppressWarnings("serial")
public class UpdatePermissions extends GenericCommand implements IChangeLoggingCommand {

	private static final Logger log = Logger.getLogger(UpdatePermissions.class);
	
	private String typeId;

	private Serializable id;
	
	private Set<Permission> perms;
	
	private boolean updateChildren;
	
	private String stationId;
	
	private List<CnATreeElement> changedElements = new ArrayList<CnATreeElement>();

	public UpdatePermissions(CnATreeElement cte, Set<Permission> perms, boolean updateChildren) {
		this.typeId = cte.getTypeId();
		this.id = cte.getDbId();
		this.perms = perms;
		this.updateChildren = updateChildren;
		this.stationId = ChangeLogEntry.STATION_ID;
	}

	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
		
		CnATreeElement cte = dao.findById(id);
		IBaseDao<Permission, Serializable> pdao = getDaoFactory().getDAO(Permission.class);
		
		IBaseDao<CnATreeElement, Serializable> odao = getDaoFactory().getDAOforTypedElement(cte);
		
		updateElement(odao, pdao, cte);
		
		if (updateChildren)
		{
			updateChildren(odao, pdao, cte.getChildren());
		}
		
		// Since the result of a change to permissions is that the model is reloaded completely
		// we only mark that one object has changed (otherwise there would be a reload for each
		// changed object.)
		changedElements.add(cte);
	}
	
	private void updateChildren(IBaseDao<CnATreeElement, Serializable> dao,
			IBaseDao<Permission, Serializable> pdao,
			Set<CnATreeElement> children)
	{
		for (CnATreeElement c : children)
		{
			updateElement(dao, pdao, c);
			
			updateChildren(dao, pdao, c.getChildren());
		}
	}
	
	private void updateElement(IBaseDao<CnATreeElement, Serializable> dao,
			IBaseDao<Permission, Serializable> pdao, CnATreeElement e)
	{
		for (Permission p : e.getPermissions())
		{
			pdao.delete(p);
		}
		e.setPermissions(Permission.clonePermissions(e, perms));
		
		//dao.saveOrUpdate(e);
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_PERMISSION;
	}


	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		return changedElements;
	}

}
