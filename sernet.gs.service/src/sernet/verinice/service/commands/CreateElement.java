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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
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

    private transient Logger log = Logger.getLogger(CreateElement.class);

    protected CnATreeElement container;
    private Class<T> clazz;
    private String typeId;
    // may be null
    private String title;
    protected T element;
    private String stationId;

    private transient IAuthService authService;
    private transient IBaseDao<T, Serializable> dao;
    private transient IBaseDao<CnATreeElement, Serializable> containerDAO;

    private boolean skipReload;   
    protected boolean createChildren; 
    protected boolean inheritAuditPermissions = false;

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

    @Override
    public void execute() {
        try {
            if(clazz==null) {
                clazz = CnATypeMapper.getClassFromTypeId(typeId);
            }
            
            if( clazz == null) {
                Logger.getLogger(this.getClass()).error("Class is null for :\t" + typeId);
            }
            
            if (!skipReload && !getContainerDAO().contains(container)) {
                getContainerDAO().reload(container, container.getDbId());
            }
            element = createInstance();
            
            if (element == null) {
                Logger.getLogger(this.getClass()).error("Element was not created for typeId:\t" + typeId);
            }
            
            if (authService.isPermissionHandlingNeeded()) {
                element = addPermissions(element);
            }
            element = saveElement();
        } catch (Exception e) {
            getLogger().error("Error while creating element", e);
            throw new RuntimeCommandException(e);
        }
    }

    protected T saveElement() {
        element = getDao().merge(element, false);
        container.addChild(element);
        element.setParentAndScope(container);

        if(isOrganization() || isItVerbund()) {
            setScopeOfScope(element);
        }
        return element;
    }

    protected T createInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        T instance = null;
        // get constructor with parent-parameter and create new object:
        if(isOrganization()) {
            instance = (T) Organization.class.getConstructor(CnATreeElement.class,boolean.class).newInstance(container,createChildren);
        } else if(isAudit()) {
            instance = (T) Audit.class.getConstructor(CnATreeElement.class,boolean.class).newInstance(container,createChildren);
        } else {
            instance = clazz.getConstructor(CnATreeElement.class).newInstance(container);         
        }
        if (title != null) {
            // override the default title
            instance.setTitel(title);
        }
        return instance;
    }

    private boolean isOrganization() {
        return Organization.class.equals(clazz) || Organization.TYPE_ID.equals(typeId);
    }
    
    private boolean isItVerbund() {
        return ITVerbund.class.equals(clazz) || ITVerbund.TYPE_ID.equals(typeId);
    }
    
    private boolean isAudit() {
        return Audit.class.equals(clazz) || Audit.TYPE_ID.equals(typeId);
    }

    private void setScopeOfScope(CnATreeElement orgOrItVerbund) {
        orgOrItVerbund.setScopeId(orgOrItVerbund.getDbId());
        for (CnATreeElement child : orgOrItVerbund.getChildren()) {
            child.setScopeId(orgOrItVerbund.getDbId());
        }
        
    }
    
    protected T addPermissions(/*not final*/ T pElement) {
        // By default, inherit permissions from parent element but ITVerbund
        // instances cannot do this, as its parents (BSIModel) is not visible
        // and has no permissions. Therefore we use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new ITVerbund.
        if (pElement instanceof ITVerbund || pElement instanceof Organization ) {
            addPermissionsForScope(pElement);           
        } else if (pElement instanceof Audit && isInheritAuditPermissions()) {
            addPermissionsForAudit((Audit) pElement);
        } else {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setPermissions(true);
            CnATreeElement elementPerm = getContainerDAO().retrieve(container.getDbId(), ri);
            pElement.setPermissions(Permission.clonePermissionSet(pElement, elementPerm.getPermissions()));
        }
        return pElement;
    }
    
    protected void addPermissionsForScope(/*not final*/ T pElement) {
        HashSet<Permission> newperms = new HashSet<Permission>();
        newperms.add(Permission.createPermission(pElement, authService.getUsername(), true, true));
        pElement.setPermissions(newperms);
        for (CnATreeElement child : pElement.getChildren()) {
            addPermissionsForScope((T) child);
        }
    }
    
    protected void addPermissionsForAudit(/*not final*/ Audit audit) {
        HashSet<Permission> newperms = new HashSet<Permission>();       
        RetrieveInfo ri = new RetrieveInfo();
        ri.setPermissions(true);
        CnATreeElement containerWithPerm = getContainerDAO().retrieve(container.getDbId(), ri);
        newperms.addAll(Permission.clonePermissionSet(audit, containerWithPerm.getPermissions()));     
        audit.setPermissions(newperms);
        for (CnATreeElement child : audit.getChildren()) {
            addPermissions((T) child);
        }
    }

    public T getNewElement() {
        return element;
    }

    /* 
     * (non-Javadoc) @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /*
     * (non-Javadoc) @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId()
     */
    @Override
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
     * (non-Javadoc) @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
        result.add(element);
        return result;
    }

    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }
    
    public IBaseDao<T, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAOforTypedElement(element);
        }
        return dao;
    }
    
    public IBaseDao<CnATreeElement, Serializable> getContainerDAO() {
        if(containerDAO==null) {
            containerDAO = getDaoFactory().getDAOforTypedElement(container);
        }
        return containerDAO;
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
