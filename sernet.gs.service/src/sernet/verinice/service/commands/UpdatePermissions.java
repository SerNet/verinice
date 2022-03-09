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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.CollectionUtil;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;

/**
 * Updates the access permissions for the given element.
 * 
 * <p>
 * Optionally all child elements inherit the permissions as well.
 * </p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings({ "serial" })
public class UpdatePermissions extends GenericCommand implements IChangeLoggingCommand {

    private Integer dbId;

    private Set<Permission> permissionSetAdd;
    private Set<Permission> permissionSetRemove;

    private boolean updateChildren;
    private boolean overridePermission;

    private List<CnATreeElement> changedElements = new ArrayList<>();
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    private transient IBaseDao<Permission, Serializable> permissionDao;
    private String stationId;
    private transient Set<CnATreeElement> elementsToSave;

    private transient Map<Integer, List<CnATreeElement>> elementsByParentId;

    public UpdatePermissions(Integer dbId, Set<Permission> permissionAdd, boolean updateChildren,
            boolean overridePermission) {
        this(dbId, permissionAdd, null, updateChildren, overridePermission);
    }

    public UpdatePermissions(CnATreeElement cte, Set<Permission> permissionAdd,
            Set<Permission> permissionRemove, boolean updateChildren, boolean overridePermission) {
        this(cte.getDbId(), permissionAdd, permissionRemove, updateChildren, overridePermission);
    }

    private UpdatePermissions(Integer dbId, Set<Permission> permissionAdd,
            Set<Permission> permissionRemove, boolean updateChildren, boolean overridePermission) {
        this.dbId = dbId;
        this.permissionSetAdd = permissionAdd;
        if (permissionRemove == null) {
            this.permissionSetRemove = Collections.emptySet();
        } else {
            this.permissionSetRemove = permissionRemove;
        }
        this.updateChildren = updateChildren;
        this.overridePermission = overridePermission;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    @Override
    public void execute() {
        try {
            CnATreeElement cte = getDao().findById(dbId);
            if (getConfigurationService().isWriteAllowed(cte)) {
                elementsToSave = new HashSet<>();
                updateElement(cte);
                if (updateChildren) {
                    elementsByParentId = new HashMap<>();

                    LoadSubtreeIds loadSubtreeIds = new LoadSubtreeIds(cte);
                    Set<Integer> subTreeIds = getCommandService().executeCommand(loadSubtreeIds)
                            .getDbIdsOfSubtree();
                    CollectionUtil
                            .partition(new ArrayList<>(subTreeIds), IDao.QUERY_MAX_ITEMS_IN_LIST)
                            .stream().forEach(partition -> {
                                DetachedCriteria crit = DetachedCriteria
                                        .forClass(CnATreeElement.class)
                                        .add(Restrictions.in("dbId", partition));
                                new RetrieveInfo().setPermissions(true).setLinksDown(true)
                                        .setProperties(true).configureCriteria(crit);
                                List<CnATreeElement> allElementsInPartition = getDao()
                                        .findByCriteria(crit);
                                Map<Integer, List<CnATreeElement>> allElementsInPartitionByParentId = allElementsInPartition
                                        .stream().collect(
                                                Collectors.groupingBy(CnATreeElement::getParentId));
                                allElementsInPartitionByParentId.forEach(
                                        (parentId, childrenInCurrentPartition) -> elementsByParentId
                                                .merge(parentId, childrenInCurrentPartition,
                                                        (l1, l2) -> Stream
                                                                .concat(l1.stream(), l2.stream())
                                                                .collect(Collectors.toList())));
                            });

                    List<CnATreeElement> children = elementsByParentId.get(cte.getDbId());
                    if (children != null) {
                        updateElements(children);
                    }
                    elementsByParentId = null;
                }
                getDao().saveOrUpdateAll(elementsToSave);
                elementsToSave = null;

            }

            // Since the result of a change to permissions is that the model is
            // reloaded completely we only mark that one object has changed (
            // otherwise there would be a reload for each changed object.)
            changedElements.add(cte);
        } catch (CommandException e) {
            throw new RuntimeCommandException("Error updating permissions", e); //$NON-NLS-1$
        }
    }

    private void updateElement(CnATreeElement element) {
        initializePermissions(element);
        if (overridePermission) {
            removeAllPermissions(element);
        }
        // flush, to delete old entries before inserting new ones
        getPermissionDao().flush();
        for (Permission permission : permissionSetAdd) {
            addPermission(element, permission);
        }
        for (Permission permission : permissionSetRemove) {
            removePermission(element, permission);
        }
        elementsToSave.add(element);
    }

    private void initializePermissions(CnATreeElement element) {
        if (element.getPermissions() == null) {
            // initialize
            final int size = (permissionSetAdd != null) ? permissionSetAdd.size() : 0;
            element.setPermissions(new HashSet<>(size));
        }
    }

    private void removeAllPermissions(CnATreeElement element) {
        // remove all old permission

        getPermissionDao().delete(List.copyOf(element.getPermissions()));
        element.getPermissions().clear();
    }

    private Permission addPermission(CnATreeElement element, Permission permission) {
        // remove old version
        boolean isNew = true;
        for (Permission oldPermission : element.getPermissions()) {
            if (oldPermission.getRole().equals(permission.getRole())) {
                oldPermission.setReadAllowed(permission.isReadAllowed());
                oldPermission.setWriteAllowed(permission.isWriteAllowed());
                isNew = false;
                break;
            }
        }
        if (isNew) {
            permission = Permission.clonePermission(element, permission);
            element.getPermissions().add(permission);
        }
        return permission;
    }

    private void removePermission(CnATreeElement element, Permission permission) {
        Permission permissionForCurrentElement = Permission.clonePermission(element, permission);
        getPermissionDao().delete(permissionForCurrentElement);
        element.getPermissions().remove(permissionForCurrentElement);
    }

    private void updateElements(Collection<CnATreeElement> elements) {
        for (CnATreeElement element : elements) {
            if (getConfigurationService().isWriteAllowed(element)) {
                updateElement(element);
                List<CnATreeElement> children = elementsByParentId.get(element.getDbId());
                if (children != null) {
                    updateElements(children);
                }
            }
        }
    }

    public IBaseDao<CnATreeElement, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    public IBaseDao<Permission, Serializable> getPermissionDao() {
        if (permissionDao == null) {
            permissionDao = getDaoFactory().getDAO(Permission.class);
        }
        return permissionDao;
    }

    @Override
    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#
     * getChangeType ()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_PERMISSION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#
     * getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

    protected IConfigurationService getConfigurationService() {
        return (IConfigurationService) VeriniceContext.get(VeriniceContext.CONFIGURATION_SERVICE);
    }
}
