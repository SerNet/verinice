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
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyCommand;

/**
 * This command models modules (requirements groups) from the ITBP compendium
 * with certain target object types of an IT network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * If an implementation hint (safeguard group) is available for the module in
 * the ITBP Compendium all threats and all applicable groups are created in
 * the I network. Threats and groups are only created once in the IT network.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelThreatsCommand extends ChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(ModelThreatsCommand.class);

    /**
     * HQL query to load the linked threats of a module
     */
    private static final String HQL_LINKED_THREAT = "select distinct threat from CnATreeElement threat " +
            "join threat.linksUp as linksUp " +
            "join linksUp.dependant as requirement " +
            "join requirement.parent as module " +
            "join fetch threat.entity as entity " +
            "join fetch entity.typedPropertyLists as propertyList " +
            "join fetch propertyList.properties as props " +
            "where threat.objectType = '" + BpThreat.TYPE_ID + "' " +
            "and module.uuid in (:uuids)"; //$NON-NLS-1$
    
    /**
     * HQL query to load the linked threats of a module
     */
    private static final String HQL_LOAD_PARENT_IDS = "select distinct threat from CnATreeElement threat " +
            "join fetch threat.parent as p1 " +
            "where threat.uuid in (:uuids)"; //$NON-NLS-1$
    
    private List<String> moduleUuids;
    private Integer targetScopeId;
    private transient List<BpThreat> compendiumThreats;
    private transient List<BpThreat> scopeThreats;
    private transient Map<String, BpThreat> missingThreats;
    private transient Map<String, BpThreat> threatsWithParents;
    private transient Map<String, CnATreeElement> threatParentsWithProperties;

    private String stationId;

    public ModelThreatsCommand(List<String> moduleUuids, Integer targetScopeId) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        this.moduleUuids = moduleUuids;
        this.targetScopeId = targetScopeId;
        missingThreats = new HashMap<>();
        threatsWithParents = new HashMap<>();
        threatParentsWithProperties = new HashMap<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            loadCompendiumThreats();
            loadScopeThreats();
            createListOfMissingThreats();
            if(!missingThreats.isEmpty()) {
                loadParents();
                insertMissingThreats();
            }
        } catch (CommandException e) {
            getLog().error("Error while modeling threats.", e);
            throw new RuntimeCommandException("Error while modeling threats.", e);
        }
    }

    private void insertMissingThreats() throws CommandException {
        BpThreatGroup threatGroup = getThreatRootGroup();
        for (BpThreat threat : threatsWithParents.values()) {
            insertThreat(threatGroup, threat);
        }
    }

    protected void insertThreat(CnATreeElement threatGroup, CnATreeElement threat)
            throws CommandException {
        CnATreeElement group = threat.getParent();
        CnATreeElement parent = getOrCreateGroup(threatGroup,
                threatParentsWithProperties.get(group.getUuid()));

        if (!isThreatInChildrenSet(parent.getChildren(),
                missingThreats.get(threat.getUuid()))) {
            CopyCommand copyCommand = new CopyCommand(parent.getUuid(),
                    Arrays.asList(threat.getUuid()));
            getCommandService().executeCommand(copyCommand);
            if (getLog().isDebugEnabled()) {
                getLog().debug("Threat: " + threat.getTitle() + " created in group: "
                        + parent.getTitle());
            }
        } else if (getLog().isDebugEnabled()) {
            getLog().debug("Threat: " + threat.getTitle() + " already exists in group: "
                    + parent.getTitle());
        }
    }

    private boolean isThreatInChildrenSet(Set<CnATreeElement> targetChildren, BpThreat threat) {
        for (CnATreeElement targetThreatElement : targetChildren) {
            BpThreat targetThreat = (BpThreat) targetThreatElement;
            if (ModelCommand.nullSafeEquals(targetThreat.getIdentifier(), threat.getIdentifier())) {
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
            getLog().debug("Threat group: " + compendiumGroup.getTitle()
                    + " already exists in group: " + parent.getTitle());
        }
        return group;
    }

    protected CnATreeElement createGroup(CnATreeElement parent, CnATreeElement compendiumGroup)
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
            getLog().debug("Threat group: " + compendiumGroup.getTitle() + " created in group: "
                    + parent.getTitle());
        }
        return group;
    }

    protected BpThreatGroup getThreatRootGroup() {
        BpThreatGroup threatGroup = null;
        CnATreeElement scope = getDao().retrieve(targetScopeId, RetrieveInfo.getChildrenInstance());
        for (CnATreeElement group : scope.getChildren()) {
            if (group.getTypeId().equals(BpThreatGroup.TYPE_ID)) {
                threatGroup = (BpThreatGroup) group;
            }
        }
        return (BpThreatGroup) getDao().retrieve(threatGroup.getDbId(),
                RetrieveInfo.getChildrenInstance().setChildrenProperties(true));
    }

    @SuppressWarnings("unchecked")
    private void loadCompendiumThreats() {
        compendiumThreats = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_LINKED_THREAT).setParameterList("uuids",
                        moduleUuids);
                query.setReadOnly(true);
                return query.list();
            }
        });
        if (getLog().isDebugEnabled()) {
            getLog().debug("Threats linked to modules: ");
            logElements(compendiumThreats);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScopeThreats() {
        scopeThreats = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(ModelCommand.HQL_SCOPE_ELEMENTS).setParameter("scopeId",
                        targetScopeId).setParameter("typeId", BpThreat.TYPE_ID);
                query.setReadOnly(true);
                return query.list();
            }
        });
        if (getLog().isDebugEnabled()) {
            getLog().debug("Threats in target scope: ");
            logElements(scopeThreats);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadParents() {
        List<BpThreat> threats = getDao().findByCallback(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session session) throws SQLException {
                Query query = session.createQuery(HQL_LOAD_PARENT_IDS).setParameterList("uuids",
                        missingThreats.keySet());
                query.setReadOnly(true);
                return query.list();
            }
        });
        final List<String> parentUuids = new LinkedList<>();
        for (BpThreat threat : threats) {
            threatsWithParents.put(threat.getUuid(), threat);
            parentUuids.add(threat.getParent().getUuid());
        }
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
            threatParentsWithProperties.put(group.getUuid(), group);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Threats parents: ");
            logElements(threatParentsWithProperties.values());
        }
    }

    private void createListOfMissingThreats() {
        missingThreats.clear();
        for (BpThreat threat : compendiumThreats) {
            if (!isThreatInScope(threat)) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Threat is not in scope yet: " + threat);
                }
                missingThreats.put(threat.getUuid(), threat);
            }
        }
    }

    private boolean isThreatInScope(BpThreat compendiumThreat) {
        for (BpThreat scopeThreat : scopeThreats) {
            if (ModelCommand.nullSafeEquals(scopeThreat.getIdentifier(),
                    compendiumThreat.getIdentifier())) {
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
            log = Logger.getLogger(ModelThreatsCommand.class);
        }
        return log;
    }

}
