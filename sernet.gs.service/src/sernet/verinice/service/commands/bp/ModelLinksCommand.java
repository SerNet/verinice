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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Link;
import sernet.verinice.service.commands.CreateMultipleLinks;

/**
 * This command creates all necessary links between the objects when modelling
 * modules from the compendium and target objects from an information network.
 * 
 * See {@link ModelCommand} for more documentation about the modeling process.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ModelLinksCommand extends GenericCommand {

    private static final long serialVersionUID = -2954681557726115473L;

    private static final Logger LOG = Logger.getLogger(ModelLinksCommand.class);

    private transient ModelingMetaDao metaDao;

    private transient ItNetwork itNetwork;

    private transient Set<CnATreeElement> elementsFromScope;
    private transient Set<CnATreeElement> requirementsFromCompendium;
    private transient Set<String> moduleUuidsFromScope;

    // the map's keys are requirement identifiers
    private transient Map<String, CnATreeElement> newRequirementsFromScope;
    // the inner maps' keys are requirement identifiers, the outer maps' keys
    // are target element database ids
    private transient Map<Integer, Map<String, CnATreeElement>> allRequirementsFromScope;
    private transient Map<Integer, Map<String, CnATreeElement>> allSafeguardsFromScope;
    private transient Map<Integer, Map<String, CnATreeElement>> allThreatsFromScope;

    private boolean handleSafeguards;

    public ModelLinksCommand(ModelingData modelingData) {
        super();
        this.elementsFromScope = modelingData.getTargetElements();
        this.itNetwork = modelingData.getItNetwork();
        this.elementsFromScope = modelingData.getTargetElements();
        this.handleSafeguards = modelingData.isHandleSafeguards();
        this.moduleUuidsFromScope = modelingData.getModuleUuidsFromScope();
        this.requirementsFromCompendium = modelingData.getRequirementGroups().stream()
                .flatMap(group -> group.getChildren().stream()).collect(Collectors.toSet());
    }

    @Override
    public void execute() {
        try {
            loadAllRequirementsFromScope();
            loadAllThreatsFromScope();
            if (isNewModuleInScope()) {
                loadNewRequirementsFromScope();
                if (handleSafeguards) {
                    loadAllSafeguardsFromScope();
                }
            }
            createLinks();
        } catch (CommandException e) {
            LOG.error("Error while creating links", e);
            throw new RuntimeCommandException("Error while creating links", e);
        }
    }

    private void createLinks() throws CommandException {
        List<Link> linkList = new LinkedList<>();
        for (CnATreeElement requirementFromCompendium : requirementsFromCompendium) {
            linkList.addAll(createLinks(requirementFromCompendium));
        }
        if (!linkList.isEmpty()) {
            CreateMultipleLinks createMultipleLinks = new CreateMultipleLinks(linkList, true);
            getCommandService().executeCommand(createMultipleLinks);
        }
    }

    private List<Link> createLinks(CnATreeElement requirementFromCompendium) {
        List<Link> linkList = new LinkedList<>();
        linkList.addAll(linkRequirementWithTargetElements(requirementFromCompendium));
        Set<CnATreeElement> linkedElements = loadLinkedElements(
                requirementFromCompendium.getUuid());
        linkList.addAll(linkThreatsWithTargetElements(linkedElements));
        if (isNewModuleInScope()) {
            linkList.addAll(
                    createLinksToSafeguardAndThreat(requirementFromCompendium, linkedElements));
        }
        return linkList;
    }

    private List<Link> linkRequirementWithTargetElements(CnATreeElement requirementFromCompendium) {
        List<Link> linkList = new LinkedList<>();
        for (CnATreeElement elementFromScope : elementsFromScope) {
            Link link = createLinkFromRequirementToElement(requirementFromCompendium,
                    elementFromScope);
            if (link != null) {
                linkList.add(link);
            }
        }
        return linkList;
    }

    private Link createLinkFromRequirementToElement(CnATreeElement requirementFromCompendium,
            CnATreeElement elementFromScope) {
        CnATreeElement requirementScope = allRequirementsFromScope.get(elementFromScope.getDbId())
                .get(BpRequirement.getIdentifierOfRequirement(requirementFromCompendium));
        if (validate(requirementScope, elementFromScope)) {
            return new Link(requirementScope, elementFromScope,
                    BpRequirement.getLinkTypeToTargetObject(elementFromScope.getObjectType()));
        }
        return null;
    }

    private Collection<? extends Link> linkThreatsWithTargetElements(
            Set<CnATreeElement> linkedElements) {
        List<Link> linkList = new LinkedList<>();
        for (CnATreeElement element : linkedElements) {
            if (element instanceof BpThreat) {
                BpThreat threatFromCompendium = (BpThreat) element;
                for (CnATreeElement elementFromScope : elementsFromScope) {
                    Link link = createLinkFromThreatToElement(threatFromCompendium,
                            elementFromScope);
                    if (link != null) {
                        linkList.add(link);
                    }
                }
            }
        }
        return linkList;
    }

    private Link createLinkFromThreatToElement(CnATreeElement threatFromCompendium,
            CnATreeElement elementFromScope) {
        CnATreeElement threatFromScope = allThreatsFromScope.get(elementFromScope.getDbId())
                .get(BpThreat.getIdentifierOfThreat(threatFromCompendium));
        if (validate(threatFromScope, elementFromScope)) {
            return new Link(threatFromScope, elementFromScope,
                    BpThreat.getLinkTypeToTargetObject(elementFromScope.getObjectType()));
        }
        return null;
    }

    private List<Link> createLinksToSafeguardAndThreat(CnATreeElement requirementFromCompendium,
            Set<CnATreeElement> linkedElements) {
        List<Link> linkList = new ArrayList<>(linkedElements.size());
        for (CnATreeElement element : linkedElements) {
            if (element instanceof Safeguard) {
                Safeguard safeguardFromCompendium = (Safeguard) element;
                Link link = createLink(requirementFromCompendium, safeguardFromCompendium);
                if (link != null) {
                    linkList.add(link);
                }
            }
            if (element instanceof BpThreat) {
                BpThreat threatFromCompendium = (BpThreat) element;
                Link link = createLink(requirementFromCompendium, threatFromCompendium);
                if (link != null) {
                    linkList.add(link);
                }
            }
        }
        return linkList;
    }

    private Link createLink(CnATreeElement requirementFromCompendium,
            Safeguard safeguardFromCompendium) {
        CnATreeElement requirementScope = newRequirementsFromScope
                .get(BpRequirement.getIdentifierOfRequirement(requirementFromCompendium));
        CnATreeElement elementFromScope = requirementScope.getParent().getParent();
        CnATreeElement safeguardScope = allSafeguardsFromScope.get(elementFromScope.getDbId())
                .get(safeguardFromCompendium.getIdentifier());
        if (validate(requirementScope, safeguardScope)) {
            return new Link(requirementScope, safeguardScope,
                    BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        }
        return null;

    }

    private Link createLink(CnATreeElement requirementFromCompendium,
            BpThreat threatFromCompendium) {
        CnATreeElement requirementScope = newRequirementsFromScope
                .get(BpRequirement.getIdentifierOfRequirement(requirementFromCompendium));
        CnATreeElement elementFromScope = requirementScope.getParent().getParent();
        CnATreeElement threatScope = allThreatsFromScope.get(elementFromScope.getDbId())
                .get(threatFromCompendium.getIdentifier());
        if (validate(requirementScope, threatScope)) {
            return new Link(requirementScope, threatScope,
                    BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        } else {
            return null;
        }
    }

    private boolean validate(CnATreeElement elementA, CnATreeElement elementB) {
        if (elementA == null || elementB == null) {
            LOG.warn("Element is null. Can not create link.");
        }
        return elementA != null && elementB != null;
    }

    private Set<CnATreeElement> findRequirementsByModuleUuid(final Set<String> moduleUuids) {
        return getMetaDao().loadChildrenWithProperties(moduleUuids, BpRequirement.TYPE_ID);
    }

    private Set<CnATreeElement> loadLinkedElements(final String requirementUuid) {
        return new HashSet<>(loadLinkedElementList(requirementUuid));
    }

    private List<CnATreeElement> loadLinkedElementList(final String requirementUuid) {
        if (handleSafeguards) {
            return getMetaDao().loadLinkedElementsWithProperties(requirementUuid,
                    new String[] { Safeguard.TYPE_ID, BpThreat.TYPE_ID });
        } else {
            return getMetaDao().loadLinkedElementsWithProperties(requirementUuid,
                    new String[] { BpThreat.TYPE_ID });
        }
    }

    protected void loadNewRequirementsFromScope() {
        newRequirementsFromScope = new HashMap<>();
        Set<CnATreeElement> requirementList = findRequirementsByModuleUuid(moduleUuidsFromScope);
        for (CnATreeElement requirement : requirementList) {
            String identifier = BpRequirement.getIdentifierOfRequirement(requirement);
            CnATreeElement previousMapping = newRequirementsFromScope.put(identifier, requirement);
            if (previousMapping != null) {
                LOG.warn("Found multiple new requirements with identifier " + identifier);
            }
        }
    }

    private void loadAllRequirementsFromScope() {
        List<CnATreeElement> requirements = getMetaDao()
                .loadElementsFromScope(BpRequirement.TYPE_ID, itNetwork.getDbId());
        allRequirementsFromScope = new HashMap<>();
        for (CnATreeElement requirement : requirements) {
            CnATreeElement elementFromScope = requirement.getParent().getParent();
            String identifier = BpRequirement.getIdentifierOfRequirement(requirement);
            CnATreeElement previousMapping = allRequirementsFromScope
                    .computeIfAbsent(elementFromScope.getDbId(), key -> new HashMap<>())
                    .put(identifier, requirement);
            if (previousMapping != null) {
                LOG.warn("Found multiple requirements with identifier " + identifier
                        + " underneath " + elementFromScope);
            }
        }
    }

    private void loadAllSafeguardsFromScope() {
        List<CnATreeElement> safeguards = getMetaDao().loadElementsFromScope(Safeguard.TYPE_ID,
                itNetwork.getDbId());
        allSafeguardsFromScope = new HashMap<>(safeguards.size());
        for (CnATreeElement safeguard : safeguards) {
            CnATreeElement elementFromScope = safeguard.getParent().getParent();
            String identifier = Safeguard.getIdentifierOfSafeguard(safeguard);
            CnATreeElement previousMapping = allSafeguardsFromScope
                    .computeIfAbsent(elementFromScope.getDbId(), key -> new HashMap<>())
                    .put(identifier, safeguard);
            if (previousMapping != null) {
                LOG.warn("Found multiple safeguards with identifier " + identifier + " underneath "
                        + elementFromScope);
            }
        }
    }

    private void loadAllThreatsFromScope() {
        List<CnATreeElement> threats = getMetaDao().loadElementsFromScope(BpThreat.TYPE_ID,
                itNetwork.getDbId());
        allThreatsFromScope = new HashMap<>(threats.size());
        for (CnATreeElement threat : threats) {
            CnATreeElement elementFromScope = threat.getParent().getParent();
            String identifier = BpThreat.getIdentifierOfThreat(threat);
            CnATreeElement previousMapping = allThreatsFromScope
                    .computeIfAbsent(elementFromScope.getDbId(), key -> new HashMap<>())
                    .put(identifier, threat);
            if (previousMapping != null) {
                LOG.warn("Found multiple threats with identifier " + identifier + " underneath "
                        + elementFromScope);
            }
        }
    }

    private boolean isNewModuleInScope() {
        return moduleUuidsFromScope != null && !moduleUuidsFromScope.isEmpty();
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

}
