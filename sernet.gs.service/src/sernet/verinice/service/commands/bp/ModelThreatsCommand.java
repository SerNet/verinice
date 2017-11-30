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
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.groups.BpThreatGroup;
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
 * the ITBP Compendium all threats and all applicable groups are created in
 * the I network. Threats and groups are only created once in the IT network.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelThreatsCommand extends ChangeLoggingCommand {

    private static final long serialVersionUID = -4075156601968217297L;

    private transient Logger log = Logger.getLogger(ModelThreatsCommand.class);

    private transient ModelingMetaDao metaDao;

    private Set<String> moduleUuids;
    private Integer targetScopeId;
    private transient Set<CnATreeElement> compendiumThreats;
    private transient Set<CnATreeElement> scopeThreats;
    private transient Map<String, CnATreeElement> missingThreats;
    private transient Map<String, CnATreeElement> threatsWithParents;
    private transient Map<String, CnATreeElement> threatParentsWithProperties;

    private String stationId;

    public ModelThreatsCommand(Set<String> moduleUuids, Integer targetScopeId) {
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
            if (!missingThreats.isEmpty()) {
                loadParents();
                insertMissingThreats();
            }
        } catch (CommandException e) {
            getLog().error("Error while modeling threats.", e);
            throw new RuntimeCommandException("Error while modeling threats.", e);
        }
    }

    private void insertMissingThreats() throws CommandException {
        CnATreeElement threatGroup = loadThreatRootGroup();
        for (CnATreeElement threat : threatsWithParents.values()) {
            insertThreat(threatGroup, threat);
        }
    }

    protected void insertThreat(CnATreeElement threatGroup, CnATreeElement threat)
            throws CommandException {
        CnATreeElement group = threat.getParent();
        CnATreeElement parent = getOrCreateGroup(threatGroup,
                threatParentsWithProperties.get(group.getUuid()));

        if (!isThreatInChildrenSet(parent.getChildren(), missingThreats.get(threat.getUuid()))) {
            CopyCommand copyCommand = new CopyCommand(parent.getUuid(),
                    Arrays.asList(threat.getUuid()));
            getCommandService().executeCommand(copyCommand);
            if (getLog().isDebugEnabled()) {
                getLog().debug(
                        "Threat: " + threat.getTitle() + " created in group: " + parent.getTitle());
            }
        } else if (getLog().isDebugEnabled()) {
            getLog().debug("Threat: " + threat.getTitle() + " already exists in group: "
                    + parent.getTitle());
        }
    }

    private boolean isThreatInChildrenSet(Set<CnATreeElement> targetChildren,
            CnATreeElement threat) {
        for (CnATreeElement targetThreatElement : targetChildren) {
            BpThreat targetThreat = (BpThreat) targetThreatElement;
            if (ModelCommand.nullSafeEquals(targetThreat.getIdentifier(),
                    BpThreat.getIdentifierOfThreat(threat))) {
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
        group = getMetaDao().loadElementWithPropertiesAndChildren(groupUuid);
        parent.addChild(group);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Threat group: " + compendiumGroup.getTitle() + " created in group: "
                    + parent.getTitle());
        }
        return group;
    }

    protected CnATreeElement loadThreatRootGroup() {
        CnATreeElement threatGroup = null;
        CnATreeElement scope = getMetaDao().loadElementWithChildren(targetScopeId);
        Set<CnATreeElement> children = scope.getChildren();
        for (CnATreeElement group : children) {
            if (group.getTypeId().equals(BpThreatGroup.TYPE_ID)) {
                threatGroup = group;
                break;
            }
        }
        if (threatGroup == null) {
            throw createException();
        }
        return getMetaDao().loadElementWithPropertiesAndChildren(threatGroup.getDbId());
    }

    private GroupNotFoundInScopeException createException() {
        CnATreeElement scopeWithProperties = getMetaDao().loadElementWithProperties(targetScopeId);
        String titleOfScope = scopeWithProperties.getTitle();
        String message = Messages.getString("ModelThreatsCommand.NoGroupFound", //$NON-NLS-1$
                titleOfScope);
        return new GroupNotFoundInScopeException(message);
    }

    private void loadCompendiumThreats() {
        compendiumThreats = new HashSet<>(findThreatsByModuleUuids());
        if (getLog().isDebugEnabled()) {
            getLog().debug("Threats linked to modules: ");
            logElements(compendiumThreats);
        }
    }

    private List<CnATreeElement> findThreatsByModuleUuids() {
        return getMetaDao().loadLinkedElementsOfParents(moduleUuids, BpThreat.TYPE_ID);
    }

    /**
     * Loads the threats and transforms the result list to a set
     * to avoid duplicate entries.
     */
    private void loadScopeThreats() {
        scopeThreats = new HashSet<>(
                getMetaDao().loadElementsFromScope(BpThreat.TYPE_ID, targetScopeId));
        if (getLog().isDebugEnabled()) {
            getLog().debug("Threats in target scope: ");
            logElements(scopeThreats);
        }
    }

    private void loadParents() {
        List<CnATreeElement> threats = getMetaDao().loadElementsWithParent(missingThreats.keySet());

        final List<String> parentUuids = new LinkedList<>();
        for (CnATreeElement threat : threats) {
            threatsWithParents.put(threat.getUuid(), threat);
            parentUuids.add(threat.getParent().getUuid());
        }

        List<CnATreeElement> groupsWithProperties = getMetaDao()
                .loadElementsWithProperties(parentUuids);

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
        for (CnATreeElement threat : compendiumThreats) {
            if (!isThreatInScope(threat)) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Threat is not in scope yet: " + threat);
                }
                missingThreats.put(threat.getUuid(), threat);
            }
        }
    }

    private boolean isThreatInScope(CnATreeElement compendiumThreat) {
        for (CnATreeElement scopeThreat : scopeThreats) {
            if (ModelCommand.nullSafeEquals(BpThreat.getIdentifierOfThreat(scopeThreat),
                    BpThreat.getIdentifierOfThreat(compendiumThreat))) {
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

    public ModelingMetaDao getMetaDao() {
        if (metaDao == null) {
            metaDao = new ModelingMetaDao(getDao());
        }
        return metaDao;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /*
     * (non-Javadoc)
     * 
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
