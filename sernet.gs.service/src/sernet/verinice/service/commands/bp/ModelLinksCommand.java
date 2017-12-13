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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
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

    private static final long serialVersionUID = 4422466491907527613L;

    private static final Logger LOG = Logger.getLogger(ModelLinksCommand.class);

    private static final Map<String, String> ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS = new HashMap<>();
    private static final Map<String, String> ELEMENT_TO_THREAT_LINK_TYPE_IDS = new HashMap<>();

    static {
        ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.put(Application.TYPE_ID,
                BpRequirement.REL_BP_REQUIREMENT_BP_APPLICATION);
        ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.put(BusinessProcess.TYPE_ID,
                BpRequirement.REL_BP_REQUIREMENT_BP_BUSINESSPROCESS);
        ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.put(Device.TYPE_ID,
                BpRequirement.REL_BP_REQUIREMENT_BP_DEVICE);
        ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.put(IcsSystem.TYPE_ID,
                BpRequirement.REL_BP_REQUIREMENT_BP_ICSSYSTEM);
        ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.put(ItNetwork.TYPE_ID,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.put(ItSystem.TYPE_ID,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITSYSTEM);
        ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.put(Network.TYPE_ID,
                BpRequirement.REL_BP_REQUIREMENT_BP_NETWORK);
        ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.put(Room.TYPE_ID,
                BpRequirement.REL_BP_REQUIREMENT_BP_ROOM);

        ELEMENT_TO_THREAT_LINK_TYPE_IDS.put(Application.TYPE_ID,
                BpThreat.REL_BP_REQUIREMENT_BP_APPLICATION);
        ELEMENT_TO_THREAT_LINK_TYPE_IDS.put(BusinessProcess.TYPE_ID,
                BpThreat.REL_BP_REQUIREMENT_BP_BUSINESSPROCESS);
        ELEMENT_TO_THREAT_LINK_TYPE_IDS.put(Device.TYPE_ID, BpThreat.REL_BP_REQUIREMENT_BP_DEVICE);
        ELEMENT_TO_THREAT_LINK_TYPE_IDS.put(IcsSystem.TYPE_ID,
                BpThreat.REL_BP_REQUIREMENT_BP_ICSSYSTEM);
        ELEMENT_TO_THREAT_LINK_TYPE_IDS.put(ItNetwork.TYPE_ID,
                BpThreat.REL_BP_REQUIREMENT_BP_ITNETWORK);
        ELEMENT_TO_THREAT_LINK_TYPE_IDS.put(ItSystem.TYPE_ID,
                BpThreat.REL_BP_REQUIREMENT_BP_ITSYSTEM);
        ELEMENT_TO_THREAT_LINK_TYPE_IDS.put(Network.TYPE_ID,
                BpThreat.REL_BP_REQUIREMENT_BP_NETWORK);
        ELEMENT_TO_THREAT_LINK_TYPE_IDS.put(Room.TYPE_ID, BpThreat.REL_BP_REQUIREMENT_BP_ROOM);
    }

    private transient ModelingMetaDao metaDao;

    private transient Set<String> moduleUuidsFromCompendium;
    private transient Set<String> newModuleUuidsFromScope;
    private transient ItNetwork itNetwork;

    private transient Set<CnATreeElement> elementsFromScope;
    private transient Set<CnATreeElement> requirementsFromCompendium;
    private transient Map<String, CnATreeElement> newRequirementsFromScope;
    private transient Map<String, CnATreeElement> allRequirementsFromScope;
    private transient Map<String, CnATreeElement> allSafeguardsFromScope;
    private transient Map<String, CnATreeElement> allThreatsFromScope;

    public ModelLinksCommand(Set<String> moduleUuidsFromCompendium,
            Set<String> newModuleUuidsFromScope, ItNetwork itNetwork,
            Set<CnATreeElement> targetElements) {
        super();
        this.moduleUuidsFromCompendium = moduleUuidsFromCompendium;
        this.newModuleUuidsFromScope = newModuleUuidsFromScope;
        this.itNetwork = itNetwork;
        this.elementsFromScope = targetElements;
    }

    @Override
    public void execute() {
        try {
            requirementsFromCompendium = loadRequirementsFromCompendium(moduleUuidsFromCompendium);
            loadAllRequirementsFromScope();
            loadAllThreatsFromScope();
            if (isNewModuleInScope()) {
                loadNewRequirementsFromScope();
                loadAllSafeguardsFromScope();
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
        CreateMultipleLinks createMultipleLinks = new CreateMultipleLinks(linkList);
        getCommandService().executeCommand(createMultipleLinks);
    }

    protected List<Link> createLinks(CnATreeElement requirementFromCompendium) {
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
        CnATreeElement requirementScope = allRequirementsFromScope
                .get(BpRequirement.getIdentifierOfRequirement(requirementFromCompendium));
        if (validate(requirementScope, elementFromScope)) {
            return new Link(requirementScope, elementFromScope,
                    getElementToRequirementLinkTypeId(elementFromScope.getObjectType()));
        }
        return null;
    }

    private String getElementToRequirementLinkTypeId(String objectType) {
        return ELEMENT_TO_REQUIREMENT_LINK_TYPE_IDS.get(objectType);
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
        CnATreeElement threatFromScope = allThreatsFromScope
                .get(BpThreat.getIdentifierOfThreat(threatFromCompendium));
        if (validate(threatFromScope, elementFromScope)) {
            return new Link(threatFromScope, elementFromScope,
                    getElementToThreatLinkTypeId(elementFromScope.getObjectType()));
        }
        return null;
    }

    private String getElementToThreatLinkTypeId(String objectType) {
        return ELEMENT_TO_THREAT_LINK_TYPE_IDS.get(objectType);
    }

    private List<Link> createLinksToSafeguardAndThreat(CnATreeElement requirementFromCompendium,
            Set<CnATreeElement> linkedElements) {
        List<Link> linkList = new LinkedList<>();
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
        CnATreeElement safeguardScope = allSafeguardsFromScope
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
        CnATreeElement threatScope = allThreatsFromScope.get(threatFromCompendium.getIdentifier());
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

    private Set<CnATreeElement> loadRequirementsFromCompendium(final Set<String> moduleUuids) {
        Set<CnATreeElement> allRequirements = findRequirementsByModuleUuid(moduleUuids);
        Set<CnATreeElement> validRequirements = new HashSet<>(allRequirements.size());
        for (CnATreeElement requirement : allRequirements) {
            if (ModelingValidator.isRequirementValidInItNetwork(requirement, itNetwork)) {
                validRequirements.add(requirement);
            }
        }
        return validRequirements;
    }

    private Set<CnATreeElement> findRequirementsByModuleUuid(final Set<String> moduleUuids) {
        return getMetaDao().loadChildrenWithProperties(moduleUuids, BpRequirement.TYPE_ID);
    }

    private Set<CnATreeElement> loadLinkedElements(final String requirementUuid) {
        return new HashSet<>(loadLinkedElementList(requirementUuid));
    }

    private List<CnATreeElement> loadLinkedElementList(final String requirementUuid) {
        return getMetaDao().loadLinkedElementsWithProperties(requirementUuid,
                new String[] { Safeguard.TYPE_ID, BpThreat.TYPE_ID });
    }

    protected void loadNewRequirementsFromScope() {
        newRequirementsFromScope = new HashMap<>();
        Set<CnATreeElement> requirementList = findRequirementsByModuleUuid(newModuleUuidsFromScope);
        for (CnATreeElement requirement : requirementList) {
            newRequirementsFromScope.put(BpRequirement.getIdentifierOfRequirement(requirement),
                    requirement);
        }
    }

    private void loadAllRequirementsFromScope() {
        List<CnATreeElement> requirements = getMetaDao()
                .loadElementsFromScope(BpRequirement.TYPE_ID, itNetwork.getDbId());
        allRequirementsFromScope = new HashMap<>();
        for (CnATreeElement requirement : requirements) {
            allRequirementsFromScope.put(BpRequirement.getIdentifierOfRequirement(requirement),
                    requirement);
        }
    }

    private void loadAllSafeguardsFromScope() {
        List<CnATreeElement> safeguards = getMetaDao().loadElementsFromScope(Safeguard.TYPE_ID,
                itNetwork.getDbId());
        allSafeguardsFromScope = new HashMap<>();
        for (CnATreeElement safeguard : safeguards) {
            allSafeguardsFromScope.put(Safeguard.getIdentifierOfSafeguard(safeguard), safeguard);
        }
    }

    private void loadAllThreatsFromScope() {
        List<CnATreeElement> threats = getMetaDao().loadElementsFromScope(BpThreat.TYPE_ID,
                itNetwork.getDbId());
        allThreatsFromScope = new HashMap<>();
        for (CnATreeElement threat : threats) {
            allThreatsFromScope.put(BpThreat.getIdentifierOfThreat(threat), threat);
        }
    }

    private boolean isNewModuleInScope() {
        return newModuleUuidsFromScope != null && !newModuleUuidsFromScope.isEmpty();
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
