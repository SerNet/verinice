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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;

/**
 * Create and save new element of clazz clazz to the database using its class to
 * lookup the DAO from the factory.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class CreateElement<T extends CnATreeElement> extends ChangeLoggingCommand implements IChangeLoggingCommand, IAuthAwareCommand {

    transient private Logger log = Logger.getLogger(CreateElement.class);

    private CnATreeElement container;
    private Class<T> clazz;
    private String typeId;
    // may be null
    private String title;
    protected T child;
    private String stationId;

    private transient IAuthService authService;

    private boolean skipReload;
    
    protected boolean createChildren;
    
    protected boolean inheritAuditPermissions = false;

    /**
     * @param container2
     * @param clazz
     * @param typeId
     */
    public CreateElement(CnATreeElement container, Class<T> clazz, String title, boolean skipReload, boolean createChildren) {
        this.container = container;
        this.clazz = clazz;
        this.title = title;
        this.stationId = ChangeLogEntry.STATION_ID;
        this.skipReload = skipReload;
        this.createChildren = createChildren;
    }
    
    public CreateElement(CnATreeElement container, String typeId, String title, boolean skipReload, boolean createChildren) {
        this.container = container;
        this.typeId = typeId;
        this.title = title;
        this.stationId = ChangeLogEntry.STATION_ID;
        this.skipReload = skipReload;
        this.createChildren = createChildren;
    }

    public CreateElement(CnATreeElement container, String typeId, String title) {
        this(container, typeId, title, false, true);
    }
    
    public CreateElement(CnATreeElement container, Class<T> type, String title) {
        this(container, type, title, false, true);
    }

    public CreateElement(CnATreeElement container, Class<T> type) {
        this(container, type, null, false, true);
    }
    
    public CreateElement(CnATreeElement container, String type) {
        this(container, type, null, false, true);
    }

    public CreateElement(CnATreeElement container, Class<T> type, boolean skipReload) {
        this(container, type, null, skipReload, true);
    }
    
    public CreateElement(CnATreeElement container, Class<T> type, boolean skipReload, boolean createChildren) {
        this(container, type, null, skipReload, createChildren);
    }

    public void execute() {
        IBaseDao<T, Serializable> dao;
        if(clazz==null) {
            clazz = CnATypeMapper.getClassFromTypeId(typeId);
        }
        
        dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(clazz);
        
        IBaseDao<CnATreeElement, Serializable> containerDAO = getDaoFactory().getDAOforTypedElement(container);

        try {
            if (!skipReload && !containerDAO.contains(container)) {
                containerDAO.reload(container, container.getDbId());
            }

            // get constructor with parent-parameter and create new object:
            if(isOrganization()) {
                child = (T) Organization.class.getConstructor(CnATreeElement.class,boolean.class).newInstance(container,createChildren);
            } else if(isAudit()) {
                child = (T) Audit.class.getConstructor(CnATreeElement.class,boolean.class).newInstance(container,createChildren);
            } else {
                child = (T) clazz.getConstructor(CnATreeElement.class).newInstance(container);
            
            }
            if (title != null) {
                // override the default title
                child.setTitel(title);
            }

            if (authService.isPermissionHandlingNeeded()) {
                addPermissions(containerDAO);
            }

            child = dao.merge(child, false);
            container.addChild(child);
            child.setParentAndScope(container);

            if(isOrganization()) {
                setScope((Organization)child);
            }
            
            // initialize UUID, used to find container in display in views:
            container.getUuid();
        } catch (Exception e) {
            getLogger().error("Error while creating element", e);
            throw new RuntimeCommandException(e);
        }
    }

    private boolean isOrganization() {
        return Organization.class.equals(clazz) || Organization.TYPE_ID.equals(typeId);
    }
    
    private boolean isAudit() {
        return Audit.class.equals(clazz) || Audit.TYPE_ID.equals(typeId);
    }

    /**
     * @param child2
     */
    private void setScope(Organization org) {
        org.setScopeId(org.getDbId());
        for (CnATreeElement child : org.getChildren()) {
            child.setScopeId(org.getDbId());
        }
        
    }

    private void addPermissions(IBaseDao<CnATreeElement, Serializable> containerDAO) {
        // By default, inherit permissions from parent element but ITVerbund
        // instances cannot do this, as its parents (BSIModel) is not visible
        // and has no permissions. Therefore we use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new ITVerbund.
        if (child instanceof ITVerbund || child instanceof Organization || (child instanceof Audit && !isInheritAuditPermissions())) {
            addPermissions(child);           
        } else {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setPermissions(true);
            CnATreeElement elementPerm = containerDAO.retrieve(container.getDbId(), ri);
            child.setPermissions(Permission.clonePermissionSet(child, elementPerm.getPermissions()));
        }
    }
    
    protected void addPermissions(/*not final*/ CnATreeElement element) {
        HashSet<Permission> newperms = new HashSet<Permission>();
        newperms.add(Permission.createPermission(element, authService.getUsername(), true, true));
        element.setPermissions(newperms);
        for (CnATreeElement child : element.getChildren()) {
            addPermissions(child);
        }
    }

    public T getNewElement() {
        return child;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType
     * ()
     */
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId
     * ()
     */
    public String getStationId() {
        return stationId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangedElements()
     */
    public List<CnATreeElement> getChangedElements() {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
        result.add(child);
        return result;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService service) {
        this.authService = service;
    }

    /**
     * @return the inheritAuditPermissions
     */
    public boolean isInheritAuditPermissions() {
        return inheritAuditPermissions;
    }

    /**
     * @param inheritAuditPermissions the inheritAuditPermissions to set
     */
    public void setInheritAuditPermissions(boolean inheritAuditPermissions) {
        this.inheritAuditPermissions = inheritAuditPermissions;
    }

    private Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(CreateElement.class);

        }
        return log;
    }

}
