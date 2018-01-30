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
 * the IT network. Safeguards and groups are only created once in the IT
 * network.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelSafeguardsCommand extends ChangeLoggingCommand {

    private static final long serialVersionUID = 1058543062083240202L;

    private transient Logger log = Logger.getLogger(ModelSafeguardsCommand.class);

    private transient ModelingMetaDao metaDao;

    private Set<String> moduleUuids;
    private Integer targetScopeId;
    private transient Set<CnATreeElement> safeguardsFromCompendium;
    private transient Set<CnATreeElement> safeguardsFromScope;
    private transient Map<String, CnATreeElement> missingSafeguardsFromCompendium;
    private transient Map<String, CnATreeElement> safeguardsWithParents;
    private transient Map<String, CnATreeElement> safeguardParentsWithProperties;

    private String stationId;

    public ModelSafeguardsCommand(Set<String> moduleUuids, Integer targetScopeId) {
        super();
        this.stationId = ChangeLogEntry.STATION_ID;
        this.moduleUuids = moduleUuids;
        this.targetScopeId = targetScopeId;
        missingSafeguardsFromCompendium = new HashMap<>();
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
            loadSafeguardsFromScope();
            createListOfMissingSafeguards();
            if (!missingSafeguardsFromCompendium.isEmpty()) {
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
        for (CnATreeElement safeguard : safeguardsWithParents.values()) {
            insertSafeguard(safeguardGroup, safeguard);
        }
    }

    protected void insertSafeguard(CnATreeElement safeguardGroup, CnATreeElement safeguard)
            throws CommandException {
        CnATreeElement group = safeguard.getParent().getParent().getParent();
        CnATreeElement parent = getOrCreateGroup(safeguardGroup,
                safeguardParentsWithProperties.get(group.getUuid()));

        group = safeguard.getParent().getParent();
        parent = getOrCreateGroup(parent, safeguardParentsWithProperties.get(group.getUuid()));

        group = safeguard.getParent();
        parent = getOrCreateGroup(parent, safeguardParentsWithProperties.get(group.getUuid()));

        if (!isSafeguardInChildrenSet(parent.getChildren(),
                missingSafeguardsFromCompendium.get(safeguard.getUuid()))) {
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

    private boolean isSafeguardInChildrenSet(Set<CnATreeElement> targetChildren,
            CnATreeElement safeguard) {
        for (CnATreeElement targetSafeguardElement : targetChildren) {
            Safeguard targetSafeguard = (Safeguard) targetSafeguardElement;
            if (ModelCommand.nullSafeEquals(targetSafeguard.getIdentifier(),
                    Safeguard.getIdentifierOfSafeguard(safeguard))) {
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
        group = getMetaDao().loadElementWithPropertiesAndChildren(groupUuid);
        parent.addChild(group);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguard group: " + compendiumGroup.getTitle() + " created in group: "
                    + parent.getTitle());
        }
        return group;
    }

    private CnATreeElement loadSafeguardRootGroup() {
        CnATreeElement safeguardGroup = null;
        CnATreeElement scope = getMetaDao().loadElementWithChildren(targetScopeId);
        Set<CnATreeElement> children = scope.getChildren();
        for (CnATreeElement group : children) {
            if (group.getTypeId().equals(SafeguardGroup.TYPE_ID)) {
                safeguardGroup = group;
                break;
            }
        }
        if (safeguardGroup == null) {
            throw createException();
        }
        return getMetaDao().loadElementWithPropertiesAndChildren(safeguardGroup.getDbId());
    }

    private GroupNotFoundInScopeException createException() {
        CnATreeElement scopeWithProperties = getMetaDao().loadElementWithProperties(targetScopeId);
        String titleOfScope = scopeWithProperties.getTitle();
        String message = Messages.getString("ModelSafeguardsCommand.NoGroupFound", //$NON-NLS-1$
                titleOfScope);
        return new GroupNotFoundInScopeException(message);
    }

    private void loadCompendiumSafeguards() {
        safeguardsFromCompendium = new HashSet<>(loadSafeguardsByModuleUuids());
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards linked to modules: ");
            logElements(safeguardsFromCompendium);
        }
    }

    private List<CnATreeElement> loadSafeguardsByModuleUuids() {
        return getMetaDao().loadLinkedElementsOfParents(moduleUuids, Safeguard.TYPE_ID);
    }

    /**
     * Loads the safeguards and transforms the result list to a set
     * to avoid duplicate entries.
     */
    private void loadSafeguardsFromScope() {
        safeguardsFromScope = new HashSet<>(
                getMetaDao().loadElementsFromScope(Safeguard.TYPE_ID, targetScopeId));
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards in target scope: ");
            logElements(safeguardsFromScope);
        }
    }

    private void loadParents() {
        // Load the parents (predecessors) of all missing safeguards
        List<CnATreeElement> safeguards = metaDao
                .loadElementsWith3Parents(missingSafeguardsFromCompendium.keySet());

        final List<String> parentUuids = new LinkedList<>();
        for (CnATreeElement safeguard : safeguards) {
            safeguardsWithParents.put(safeguard.getUuid(), safeguard);
            parentUuids.add(safeguard.getParent().getUuid());
            parentUuids.add(safeguard.getParent().getParent().getUuid());
            parentUuids.add(safeguard.getParent().getParent().getParent().getUuid());
        }
        // Load the properties of the parents (predecessors)
        List<CnATreeElement> groupsWithProperties = getMetaDao()
                .loadElementsWithProperties(parentUuids);

        for (CnATreeElement group : groupsWithProperties) {
            safeguardParentsWithProperties.put(group.getUuid(), group);
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Safeguards parents: ");
            logElements(safeguardParentsWithProperties.values());
        }
    }

    private void createListOfMissingSafeguards() {
        missingSafeguardsFromCompendium.clear();
        for (CnATreeElement safeguard : safeguardsFromCompendium) {
            if (!isSafeguardInScope(safeguard)) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Safeguard is not in scope yet: " + safeguard);
                }
                missingSafeguardsFromCompendium.put(safeguard.getUuid(), safeguard);
            }
        }
    }

    private boolean isSafeguardInScope(CnATreeElement compendiumSafeguard) {
        for (CnATreeElement scopeSafeguard : safeguardsFromScope) {
            if (ModelCommand.nullSafeEquals(Safeguard.getIdentifierOfSafeguard(scopeSafeguard),
                    Safeguard.getIdentifierOfSafeguard(compendiumSafeguard))) {
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
            log = Logger.getLogger(ModelSafeguardsCommand.class);
        }
        return log;
    }

}
