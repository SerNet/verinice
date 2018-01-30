/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 * Alexander Koderman - initial API and implementation
 * Daniel Murygin
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
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;

/**
 * Create and save new elements in a container (group). The type of the element
 * to be created is determined by a class or a type ID.
 * 
 * @author Alexander Koderman
 * @author Daniel Murygin
 * 
 * @param <T>
 *            The class from which instances are created with the command
 */
public class CreateElement<T extends CnATreeElement> extends ChangeLoggingCommand
        implements IChangeLoggingCommand, IAuthAwareCommand {

    private static final long serialVersionUID = 7612712230183045070L;

    private static final Logger LOG = Logger.getLogger(CreateElement.class);

    protected CnATreeElement container;
    private Class<T> clazz;
    private String typeId;
    // may be null
    private String title;
    protected T element;
    private String stationId;

    private boolean skipReload;
    protected boolean createChildren;
    protected boolean inheritAuditPermissions = false;

    private transient IAuthService authService;
    private transient IBaseDao<T, Serializable> dao;
    private transient IBaseDao<CnATreeElement, Serializable> containerDAO;

    public CreateElement(CnATreeElement container, Class<T> clazz, String title, boolean skipReload,
            boolean createChildren) {
        this.container = container;
        this.clazz = clazz;
        this.title = title;
        this.stationId = ChangeLogEntry.STATION_ID;
        this.skipReload = skipReload;
        this.createChildren = createChildren;
    }

    public CreateElement(CnATreeElement container, String typeId, String title, boolean skipReload,
            boolean createChildren) {
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

    public CreateElement(CnATreeElement container, Class<T> type, boolean skipReload,
            boolean createChildren) {
        this(container, type, null, skipReload, createChildren);
    }

    @Override
    public void execute() {
        try {
            if (clazz == null) {
                clazz = CnATypeMapper.getClassFromTypeId(typeId);
            }

            if (clazz == null) {
                LOG.error("Class is null for type ID" + typeId);
            }

            if (!skipReload && !getContainerDAO().contains(container)) {
                getContainerDAO().reload(container, container.getDbId());
            }
            element = createInstance();

            if (element == null) {
                LOG.error("Element was not created for type ID: " + typeId);
            }

            if (authService.isPermissionHandlingNeeded()) {
                element = addPermissions(element);
            }
            element = saveElement();
        } catch (Exception e) {
            LOG.error("Error while creating element", e);
            throw new RuntimeCommandException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected T createInstance() throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        T instance = null;
        if (isOrganization()) {
            instance = (T) new Organization(container, createChildren);
        } else if (isItNetwork()) {
            instance = (T) new ItNetwork(container, createChildren);
        } else if (isAudit()) {
            instance = (T) new Audit(container, createChildren);
        } else {
            instance = clazz.getConstructor(CnATreeElement.class).newInstance(container);
        }
        if (title != null) {
            instance.setTitel(title);
        }
        return instance;
    }

    protected T saveElement() {
        element = getDao().merge(element, false);
        container.addChild(element);
        element.setParentAndScope(container);
        if (isScope()) {
            setScopeOfScope(element);
        }
        return element;
    }

    private boolean isScope() {
        return isOrganization() || isItVerbund() || isItNetwork();
    }

    private boolean isOrganization() {
        return Organization.class.equals(clazz) || Organization.TYPE_ID.equals(typeId);
    }

    private boolean isItVerbund() {
        return ITVerbund.class.equals(clazz) || ITVerbund.TYPE_ID.equals(typeId);
    }

    private boolean isItNetwork() {
        return ItNetwork.class.equals(clazz) || ItNetwork.TYPE_ID.equals(typeId);
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

    protected T addPermissions(/* not final */ T pElement) {
        // By default, inherit permissions from parent element but scope
        // instances cannot do this, as its parents (a model) is not visible
        // and has no permissions. Therefore we use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new scope.
        if (isScope()) {
            addPermissionsForScope(pElement);
        } else if (pElement instanceof Audit && isInheritAuditPermissions()) {
            addPermissionsForAudit((Audit) pElement);
        } else {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setPermissions(true);
            CnATreeElement elementPerm = getContainerDAO().retrieve(container.getDbId(), ri);
            pElement.setPermissions(
                    Permission.clonePermissionSet(pElement, elementPerm.getPermissions()));
        }
        return pElement;
    }

    @SuppressWarnings("unchecked")
    protected void addPermissionsForScope(/* not final */ T pElement) {
        HashSet<Permission> newperms = new HashSet<>();
        newperms.add(Permission.createPermission(pElement, authService.getUsername(), true, true));
        pElement.setPermissions(newperms);
        for (CnATreeElement child : pElement.getChildren()) {
            addPermissionsForScope((T) child);
        }
    }

    @SuppressWarnings("unchecked")
    protected void addPermissionsForAudit(/* not final */ Audit audit) {
        HashSet<Permission> newperms = new HashSet<>();
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

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

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

    @Override
    public List<CnATreeElement> getChangedElements() {
        ArrayList<CnATreeElement> result = new ArrayList<>(1);
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

    @SuppressWarnings("unchecked")
    public IBaseDao<T, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAOforTypedElement(element);
        }
        return dao;
    }

    @SuppressWarnings("unchecked")
    public IBaseDao<CnATreeElement, Serializable> getContainerDAO() {
        if (containerDAO == null) {
            containerDAO = getDaoFactory().getDAOforTypedElement(container);
        }
        return containerDAO;
    }

    public boolean isInheritAuditPermissions() {
        return inheritAuditPermissions;
    }

    public void setInheritAuditPermissions(boolean inheritAuditPermissions) {
        this.inheritAuditPermissions = inheritAuditPermissions;
    }

}
