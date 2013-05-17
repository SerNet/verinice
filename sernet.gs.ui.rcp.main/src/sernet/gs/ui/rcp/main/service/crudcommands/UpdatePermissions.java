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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
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
@SuppressWarnings({ "serial", "restriction" })
public class UpdatePermissions extends ChangeLoggingCommand implements IChangeLoggingCommand {
	
	private String cteTypeId;

	private Serializable cteDbId;
	
	private Set<Permission> permissionSetNew;
	
	private boolean updateChildren;
	
	private boolean overridePermission;
	
	private String stationId;
	
	private List<CnATreeElement> changedElements = new ArrayList<CnATreeElement>();
	
	private transient IBaseDao<CnATreeElement, Serializable> dao;
	
	private transient IBaseDao<Permission, Serializable> permissionDao;

	public UpdatePermissions(CnATreeElement cte, Set<Permission> permissionSet, boolean updateChildren) {
		this(cte,permissionSet,updateChildren,true);
	}

	public UpdatePermissions(CnATreeElement cte, Set<Permission> permissionSet, boolean updateChildren, boolean overridePermission) {
		this.cteTypeId = cte.getTypeId();
		this.cteDbId = cte.getDbId();
		this.permissionSetNew = permissionSet;
		this.updateChildren = updateChildren;
		this.overridePermission = overridePermission;
		this.stationId = ChangeLogEntry.STATION_ID;
	}
	
	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao0 = getDaoFactory().getDAO(cteTypeId);
		CnATreeElement cte = dao0.findById(cteDbId);
		
		updateElement(cte);
		
		if (updateChildren) {
			updateChildren(cte.getChildren());
		}
		
		// Since the result of a change to permissions is that the model is reloaded completely
		// we only mark that one object has changed (otherwise there would be a reload for each
		// changed object.)
		changedElements.add(cte);
	}
	
	private void updateChildren(Set<CnATreeElement> children) {
		for (CnATreeElement child : children) {
			updateElement(child);		
			updateChildren(child.getChildren());
		}
	}
	
	private void updateElement(CnATreeElement element) {
		if(element.getPermissions()==null) {
			// initialize
			final int size = (permissionSetNew!=null) ? permissionSetNew.size() : 0; 
			element.setPermissions(new HashSet<Permission>(size));
		}
		if(overridePermission) {
			// remove all old permission
			for (Permission permission : element.getPermissions()) {
				getPermissionDao().delete(permission);
			}
			element.getPermissions().clear();
		}
		// flush, to delete old entries before inserting new ones
		getPermissionDao().flush();
		for (Permission permission : permissionSetNew) {
			// remove old version
			boolean isNew = true;
		    for (Permission oldPermission : element.getPermissions()) {
                if(oldPermission.getRole().equals(permission.getRole())) {
                    oldPermission.setReadAllowed(permission.isReadAllowed());
                    oldPermission.setWriteAllowed(permission.isWriteAllowed());
                    isNew = false;
                    break;
                }
            }
			if(isNew){
			    permission = Permission.clonePermission(element, permission);
	            element.getPermissions().add(permission);
			}			
			getDao().saveOrUpdate(element);	
		}
	}
	
	public IBaseDao<CnATreeElement, Serializable> getDao() {
		if(dao==null) {
			dao = getDaoFactory().getDAO(CnATreeElement.class);
		}
		return dao;
	}

	public IBaseDao<Permission, Serializable> getPermissionDao() {
		if(permissionDao==null) {
			permissionDao = getDaoFactory().getDAO(Permission.class);
		}
		return permissionDao;
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
