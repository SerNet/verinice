/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.exceptions.GroupNotFoundInScopeException;
import sernet.verinice.service.commands.CopyCommand;

/**
 * This command models modules (requirements groups) from the ITBP compendium
 * with certain target object types of an IT network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * If an implementation hint (safeguard group) is available for the module in
 * the ITBP Compendium all safeguards and all applicable groups are created in
 * the IT network. Safeguards and groups are only created once in the IT network.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelSafeguardsCommand extends ChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(ModelSafeguardsCommand.class);

    /**
     * HQL query to load the linked safeguards of a module
     */
    private static final String HQL_LINKED_SAFEGUARDS = "select safeguard from CnATreeElement safeguard " +
            "join safeguard.linksUp as linksUp " +
            "join linksUp.dependant as requirement " +
            "join requirement.parent as module " +
            "join fetch safeguard.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where safeguard.objectType = '" + Safeguard.TYPE_ID + "' " +
            "and module.uuid in (:uuids)"; //$NON-NLS-1$
    
    private Set<String> moduleUuids;
    private Integer targetScopeId;
    private transient Set<Safeguard> compendiumSafeguards;
    private transient Set<Safeguard> scopeSafeguards;
    private transient Map<String, Safeguard> missingSafeguards;
    private transient Map<String, Safeguard> safeguardsWithParents;
    private transient Map<String, CnATreeElement> safeguardParentsWithProperties;

    private String stationId;

    public ModelSafeguardsCommand(Set<String> moduleUuids, Integer targetScopeId) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        this.moduleUuids = moduleUuids;
        this.targetScopeId = targetScopeId;
        missingSafeguards = new HashMap<>();
        safeguardsWithParents = new HashMap<>();
        safeguardParentsWithProperties = new HashMap<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            loadCompendiumSafeguards();
            loadScopeSafeguards();
            createListOfMissingSafeguards();
            if(!missingSafeguards.isEmpty()) {
                loadParents();
                insertMissingSafeguards();
            }
        } catch (CommandException e) {
            getLog().error("Error while modeling safeguards.", e);
            throw new RuntimeCommandException("Error while modeling safeguards.", e);
        }
    }

    private void insertMissingSafeguards() throws CommandException {
        CnATreeElement safeguardGroup = loadSafeguardRootGroup();
        for (Safeguard safeguard : safeguardsWithParents.values()) {
            insertSafeguard(safeguardGroup, safeguard);
        }
    }

    protected void insertSafeguard(CnATreeElement safeguardGroup, Safeguard safeguard)
            throws CommandException {
        CnATreeElement group = safeguard.getParent().getParent().getParent();
        CnATreeElement parent = getOrCreateGroup(safeguardGroup,
                safeguardParentsWithProperties.get(group.getUuid()));

        group = safeguard.getParent().getParent();
        parent = getOrCreateGroup(parent, safeguardParentsWithProperties.get(group.getUuid()));

        group = safeguard.getParent();
        parent = getOrCreateGroup(parent, safeguardParentsWithProperties.get(group.getUuid()));

        if (!isSafeguardInChildrenSet(parent.getChildren(),
                missingSafeguards.get(safeguard.getUuid()))) {
            CopyCommand copyCommand = new CopyCommand(parent.getUuid(),
                    Arrays.asList(safeguard.getUuid()));
            getCommandService().executeCommand(copyCommand);
            if (getLog().isDebugEnabled()) {
                getLog().debug("Safeguard: " + safeguard.getTitle() + " created in group: "
                        + parent.getTitle());
            }
        } else if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguard: " + safeguard.getTitle() + " already exists in group: "
                    + parent.getTitle());
        }
    }

    private boolean isSafeguardInChildrenSet(Set<CnATreeElement> targetChildren, Safeguard safeguard) {
        for (CnATreeElement targetSafeguardElement : targetChildren) {
            Safeguard targetSafeguard = (Safeguard) targetSafeguardElement;
            if (ModelCommand.nullSafeEquals(targetSafeguard.getIdentifier(), safeguard.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    private CnATreeElement getOrCreateGroup(CnATreeElement parent, CnATreeElement compendiumGroup)
            throws CommandException {
        CnATreeElement group = null;
        boolean groupFound = false;
        for (CnATreeElement child : parent.getChildren()) {
            if (child.getTitle().equals(compendiumGroup.getTitle())) {
                group = child;
                groupFound = true;
                break;
            }
        }
        if (!groupFound) {
            group = createGroup(parent, compendiumGroup);
        } else if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguard group: " + compendiumGroup.getTitle()
                    + " already exists in group: " + parent.getTitle());
        }
        return group;
    }

    private CnATreeElement createGroup(CnATreeElement parent, CnATreeElement compendiumGroup)
            throws CommandException {
        CnATreeElement group;
        CopyCommand copyCommand = new CopyCommand(parent.getUuid(),
                Arrays.asList(compendiumGroup.getUuid()));
        copyCommand.setCopyChildren(false);
        copyCommand = getCommandService().executeCommand(copyCommand);
        String groupUuid = copyCommand.getNewElements().get(0);
        group = getDao().findByUuid(groupUuid,
                RetrieveInfo.getChildrenInstance().setChildrenProperties(true));
        parent.addChild(group);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguard group: " + compendiumGroup.getTitle() + " created in group: "
                    + parent.getTitle());
        }
        return group;
    }

    private CnATreeElement loadSafeguardRootGroup() {
        CnATreeElement safeguardGroup = null;
        CnATreeElement scope = getDao().retrieve(targetScopeId, RetrieveInfo.getChildrenInstance());
        Set<CnATreeElement> children = scope.getChildren();
        for (CnATreeElement group : children) {
            if (group.getTypeId().equals(SafeguardGroup.TYPE_ID)) {
                safeguardGroup = group;
            }
        }
        if(safeguardGroup==null) {
            throw new GroupNotFoundInScopeException(targetScopeId, SafeguardGroup.TYPE_ID);
        }
        return getDao().retrieve(safeguardGroup.getDbId(),
                RetrieveInfo.getChildrenInstance().setChildrenProperties(true));
    }

    
    private void loadCompendiumSafeguards() {
        compendiumSafeguards = new HashSet<>(loadSafeguardsByModuleUuids());
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards linked to modules: ");
            logElements(compendiumSafeguards);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Safeguard> loadSafeguardsByModuleUuids() {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_LINKED_SAFEGUARDS).setParameterList("uuids",
                        moduleUuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
    }
    
    /**
     * Loads the safeguards and transforms the result list to a set
     * to avoid duplicate entries.
     */
    private void loadScopeSafeguards() {
        scopeSafeguards = new HashSet<>(loadSafeguardsByDao());
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards in target scope: ");
            logElements(scopeSafeguards);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Safeguard> loadSafeguardsByDao() {
        return getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_SCOPE_ELEMENTS).setParameter("scopeId",
                        targetScopeId).setParameter("typeId", Safeguard.TYPE_ID);
                query.setReadOnly(true);
                return query.list();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadParents() {
        // Load the parents (predecessors) of all missing safeguards 
        List<Safeguard> safeguards = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_LOAD_PARENT_IDS).setParameterList("uuids",
                        missingSafeguards.keySet());
                query.setReadOnly(true);
                return query.list();
            }
        });
        final List<String> parentUuids = new LinkedList<>();
        for (Safeguard safeguard : safeguards) {
            safeguardsWithParents.put(safeguard.getUuid(), safeguard);
            parentUuids.add(safeguard.getParent().getUuid());
            parentUuids.add(safeguard.getParent().getParent().getUuid());
            parentUuids.add(safeguard.getParent().getParent().getParent().getUuid());
        }
        // Load the properties of the parents (predecessors)
        List<CnATreeElement> groupsWithProperties = getDao()
                .findByCallback(new HibernateCallback() {
                    @Override
                    public Object doInHibernate(Session session) throws SQLException {
                        Query query = session.createQuery(ModelCommand.HQL_ELEMENT_WITH_PROPERTIES)
                                .setParameterList("uuids", parentUuids);
                        query.setReadOnly(true);
                        return query.list();
                    }
                });
        for (CnATreeElement group : groupsWithProperties) {
            safeguardParentsWithProperties.put(group.getUuid(), group);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards parents: ");
            logElements(safeguardParentsWithProperties.values());
        }
    }

    private void createListOfMissingSafeguards() {
        missingSafeguards.clear();
        for (Safeguard safeguard : compendiumSafeguards) {
            if (!isSafeguardInScope(safeguard)) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Safeguard is not in scope yet: " + safeguard);
                }
                missingSafeguards.put(safeguard.getUuid(), safeguard);
            }
        }
    }

    private boolean isSafeguardInScope(Safeguard compendiumSafeguard) {
        for (Safeguard scopeSafeguard : scopeSafeguards) {
            if (ModelCommand.nullSafeEquals(scopeSafeguard.getIdentifier(),
                    compendiumSafeguard.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    private void logElements(Collection<?> collection) {
        for (Object element : collection) {
            getLog().debug(element);
        }

    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ModelSafeguardsCommand.class);
        }
        return log;
    }

}
