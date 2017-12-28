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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.bp.exceptions.BpModelingException;
import sernet.verinice.service.bp.exceptions.GroupNotFoundInScopeException;
import sernet.verinice.service.commands.CopyCommand;

/**
 * This command sorts modules and requirements from the compendium into an
 * information network. The group structure of the modules from the
 * compendium is retained in the information network.
 * 
 * This command does not create links between requirements and target
 * objects. Links are created by the {@link ModelLinksCommand}.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelModulesCommand extends ChangeLoggingCommand {

    private static final long serialVersionUID = -6698388849147857588L;

    private static final Logger LOG = Logger.getLogger(ModelModulesCommand.class);

    private transient ModelingMetaDao metaDao;

    private transient ItNetwork itNetwork;
    private transient Set<BpRequirementGroup> modulesFromCompendium;
    private transient Set<CnATreeElement> requirementsFromCompendium;
    private transient Set<CnATreeElement> allRequirementsFromScope;
    private transient Map<String, CnATreeElement> missingRequirementsFromCompendium;
    private transient Map<String, CnATreeElement> missingRequirementsWithParents;
    private transient Map<String, CnATreeElement> requirementParentsWithProperties;
    private Set<String> moduleUuids = new HashSet<>();

    private String stationId;

    public ModelModulesCommand(Set<BpRequirementGroup> modulesFromCompendium,
            ItNetwork itNetwork) {
        super();

        this.modulesFromCompendium = modulesFromCompendium;
        this.itNetwork = itNetwork;

        requirementsFromCompendium = new HashSet<>();
        missingRequirementsFromCompendium = new HashMap<>();
        missingRequirementsWithParents = new HashMap<>();
        requirementParentsWithProperties = new HashMap<>();

        this.stationId = ChangeLogEntry.STATION_ID;
    }

    @Override
    public void execute() {
        try {
            loadRequirementsFromCompendium();
            loadAllRequirementsFromScope();
            rememberMissingRequirements();
            if (!missingRequirementsFromCompendium.isEmpty()) {
                loadParents();
                insertMissingRequirements();
            }
        } catch (CommandException e) {
            LOG.error("Error while modeling safeguards.", e); //$NON-NLS-1$
            throw new RuntimeCommandException("Error while modeling safeguards.", e); //$NON-NLS-1$
        }
    }

    private void insertMissingRequirements() throws CommandException {
        CnATreeElement requirementGroup = loadRequirementRootGroup();
        for (CnATreeElement requirement : missingRequirementsWithParents.values()) {
            insertRequirement(requirementGroup, requirement);
        }
    }

    protected void insertRequirement(CnATreeElement requirementGroup, CnATreeElement requirement)
            throws CommandException {
        CnATreeElement parent = insertRequirementGroups(requirementGroup, requirement);

        if (!isRequirementInChildrenSet(parent.getChildren(),
                missingRequirementsFromCompendium.get(requirement.getUuid()))) {
            CopyCommand copyCommand = new CopyCommand(parent.getUuid(),
                    Arrays.asList(requirement.getUuid()));
            getCommandService().executeCommand(copyCommand);
            moduleUuids.add(parent.getUuid());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Requirement: " + requirement.getTitle() + " created in group: " //$NON-NLS-1$ //$NON-NLS-2$
                        + parent.getTitle());
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Requirement: " + requirement.getTitle() + " already exists in group: " //$NON-NLS-1$ //$NON-NLS-2$
                    + parent.getTitle());
        }
    }

    /**
     * Creates the 3 groups into which a requirement is sorted.
     * If the groups already exist, they are not created again.
     */
    private CnATreeElement insertRequirementGroups(CnATreeElement requirementGroup,
            CnATreeElement requirement) throws CommandException {
        validate(requirement);
        CnATreeElement group = requirement.getParent().getParent().getParent();
        CnATreeElement parent = getOrCreateGroup(requirementGroup,
                requirementParentsWithProperties.get(group.getUuid()));

        group = requirement.getParent().getParent();
        parent = getOrCreateGroup(parent, requirementParentsWithProperties.get(group.getUuid()));

        group = requirement.getParent();
        parent = getOrCreateGroup(parent, requirementParentsWithProperties.get(group.getUuid()));
        return parent;
    }

    /**
     * Validates the requirement and checks checks whether the requirement has
     * three predecessors.
     * 
     * @param requirement
     *            A requirement
     * @throws
     *             BpModelingException
     *             if this is the requirement has not three
     *             predecessors
     */
    private void validate(CnATreeElement requirement) {
        if (!BpRequirement.TYPE_ID.equals(requirement.getTypeId())) {
            throw createBpModelingException(requirement.getDbId());
        }
        if (requirement.getParent() == null || requirement.getParent().getParent() == null
                || requirement.getParent().getParent().getParent() == null) {
            throw createBpModelingException(requirement.getDbId());
        }
        if (!BpRequirementGroup.TYPE_ID.equals(requirement.getParent().getTypeId())) {
            throw createBpModelingException(requirement.getDbId());
        }
        if (!BpRequirementGroup.TYPE_ID.equals(requirement.getParent().getParent().getTypeId())) {
            throw createBpModelingException(requirement.getDbId());
        }
        if (!BpRequirementGroup.TYPE_ID
                .equals(requirement.getParent().getParent().getParent().getTypeId())) {
            throw createBpModelingException(requirement.getDbId());
        }
    }

    private boolean isRequirementInChildrenSet(Set<CnATreeElement> targetChildren,
            CnATreeElement requirement) {
        for (CnATreeElement targetSafeguardElement : targetChildren) {
            CnATreeElement targetSafeguard = targetSafeguardElement;
            String targetIdentifier = BpRequirement.getIdentifierOfRequirement(targetSafeguard);
            String identifier = BpRequirement.getIdentifierOfRequirement(requirement);
            if (ModelCommand.nullSafeEquals(targetIdentifier, identifier)) {
                return true;
            }
        }
        return false;
    }

    private void loadRequirementsFromCompendium() {
        requirementsFromCompendium.clear();
        for (CnATreeElement module : modulesFromCompendium) {
            Set<CnATreeElement> children = getMetaDao().loadChildrenOfElement(module.getUuid());
            for (CnATreeElement child : children) {
                if (ModelingValidator.isRequirementValidInItNetwork(child, itNetwork)) {
                    requirementsFromCompendium.add(child);
                }
            }

        }
    }

    /**
     * Loads the safeguards and transforms the result list to a set
     * to avoid duplicate entries.
     */
    private void loadAllRequirementsFromScope() {
        allRequirementsFromScope = new HashSet<>(
                getMetaDao().loadElementsFromScope(BpRequirement.TYPE_ID, itNetwork.getDbId()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("missingRequirementsFromCompendium in target scope: "); //$NON-NLS-1$
            logElements(allRequirementsFromScope);
        }
    }

    private void rememberMissingRequirements() {
        missingRequirementsFromCompendium.clear();
        for (CnATreeElement requirementCompendium : requirementsFromCompendium) {
            CnATreeElement requirementScope = getRequirementFromScope(requirementCompendium);
            if (requirementScope == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Requirement is not in scope yet: " + requirementCompendium); //$NON-NLS-1$
                }
                missingRequirementsFromCompendium.put(requirementCompendium.getUuid(),
                        requirementCompendium);
            }
        }
    }

    private void loadParents() {
        final Set<String> parentUuids = loadParentUuidsOfMissingRequirements();
        List<CnATreeElement> parentsWithProperties = getMetaDao()
                .loadElementsWithProperties(parentUuids);
        for (CnATreeElement group : parentsWithProperties) {
            requirementParentsWithProperties.put(group.getUuid(), group);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("missingRequirementsFromCompendium parents: "); //$NON-NLS-1$
            logElements(requirementParentsWithProperties.values());
        }
    }

    private Set<String> loadParentUuidsOfMissingRequirements() {
        // Load the parents (predecessors) of all missing requirements
        List<CnATreeElement> requirements = getMetaDao()
                .loadElementsWith3Parents(missingRequirementsFromCompendium.keySet());

        final Set<String> parentUuids = new HashSet<>();
        for (CnATreeElement requirement : requirements) {
            validate(requirement);
            missingRequirementsWithParents.put(requirement.getUuid(), requirement);
            parentUuids.add(requirement.getParent().getUuid());
            parentUuids.add(requirement.getParent().getParent().getUuid());
            parentUuids.add(requirement.getParent().getParent().getParent().getUuid());
        }
        return parentUuids;
    }

    private CnATreeElement getRequirementFromScope(CnATreeElement requirementFromCompendium) {
        for (CnATreeElement requirementScope : allRequirementsFromScope) {
            if (ModelCommand.nullSafeEquals(
                    BpRequirement.getIdentifierOfRequirement(requirementScope),
                    BpRequirement.getIdentifierOfRequirement(requirementFromCompendium))) {
                return requirementScope;
            }
        }
        return null;
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
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("Requirement group: " + compendiumGroup.getTitle() //$NON-NLS-1$
                    + " already exists in group: " + parent.getTitle()); //$NON-NLS-1$
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
        if (LOG.isDebugEnabled()) {
            LOG.debug(
                    "Requirement group: " + compendiumGroup.getTitle() + " created in group: " //$NON-NLS-1$ //$NON-NLS-2$
                            + parent.getTitle());
        }
        return group;
    }

    private CnATreeElement loadRequirementRootGroup() {
        CnATreeElement requirementGroup = null;
        CnATreeElement scope = getMetaDao().loadElementWithChildren(itNetwork.getDbId());
        Set<CnATreeElement> children = scope.getChildren();
        for (CnATreeElement group : children) {
            if (group.getTypeId().equals(BpRequirementGroup.TYPE_ID)) {
                requirementGroup = group;
                break;
            }
        }
        if (requirementGroup == null) {
            throw createGroupNotFoundInScopeException();
        }
        return getMetaDao().loadElementWithPropertiesAndChildren(requirementGroup.getDbId());
    }

    private GroupNotFoundInScopeException createGroupNotFoundInScopeException() {
        String titleOfScope = itNetwork.getTitle();
        String message = Messages.getString("ModelModulesCommand.NoGroupFound", //$NON-NLS-1$
                titleOfScope);
        return new GroupNotFoundInScopeException(message);
    }

    private BpModelingException createBpModelingException(Integer requirementId) {
        CnATreeElement requirement = getMetaDao().loadElementWithProperties(requirementId);
        String title = requirement.getTitle();
        String message = Messages.getString("ModelModulesCommand.ModelingException", //$NON-NLS-1$
                title);
        return new BpModelingException(message);
    }

    public Set<String> getModuleUuidsFromScope() {
        return moduleUuids;
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

    @Override
    public String getStationId() {
        return stationId;
    }

    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    private void logElements(Collection<?> collection) {
        for (Object element : collection) {
            LOG.debug(element);
        }
    }
}
