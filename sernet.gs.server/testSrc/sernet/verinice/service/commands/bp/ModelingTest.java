/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.criterion.DetachedCriteria;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.ImplementationStatus;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

@TransactionConfiguration(transactionManager = "txManager")
@Transactional
public class ModelingTest extends AbstractModernizedBaseProtection {

    @After
    public void cleanUp() {
        elementDao.clear();
    }

    @Transactional
    @Test
    public void modelModuleOnItNetwork() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "Requirements");
        BpRequirement requirement = createBpRequirement(requirementGroup, "Requirement");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "Safeguard");
        createLink(requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createBpThreat(threatGroup, "Threat");
        createLink(requirement, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(2, itNetwork.getLinksUp().size());

        CnATreeElement modeledRequirementGroup = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement.getTitle(), modeledRequirement.getTitle());
        assertTrue(
                modeledRequirement.getEntity().isFlagged(BpRequirement.PROP_IMPLEMENTATION_DEDUCE));

        assertEquals(3, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementNetwork = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirementNetwork.size());
        assertEquals(linksRequirementNetwork.iterator().next().getDependency(), itNetwork);

        CnATreeElement modeledSafeguardGroup = getChildrenWithTypeId(itNetwork,
                SafeguardGroup.TYPE_ID).iterator().next();
        assertEquals(safeguardGroup.getTitle(), modeledSafeguardGroup.getTitle());
        assertEquals(1, modeledSafeguardGroup.getChildren().size());
        CnATreeElement modeledSafeguard = modeledSafeguardGroup.getChildren().iterator().next();
        assertEquals(safeguard.getTitle(), modeledSafeguard.getTitle());

        Set<CnALink> linksSafeguardRequirement = getLinksWithType(modeledSafeguard,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals(1, linksSafeguardRequirement.size());
        assertEquals(linksSafeguardRequirement.iterator().next().getDependency(), modeledSafeguard);

        CnATreeElement modeledThreatGroup = getChildrenWithTypeId(itNetwork, BpThreatGroup.TYPE_ID)
                .iterator().next();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat.getTitle(), modeledThreat.getTitle());

        Set<CnALink> linksThreatRequirement = getLinksWithType(modeledThreat,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksThreatRequirement.size());
        assertEquals(linksThreatRequirement.iterator().next().getDependency(), modeledThreat);

        Set<CnALink> linksThreatTargetObject = getLinksWithType(modeledThreat,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(linksThreatTargetObject.iterator().next().getDependency(), itNetwork);

    }

    @Transactional
    @Test
    public void modelTwoModules() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup1 = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirementGroup requirementGroup2 = createRequirementGroup(catalogModel, "R2",
                "Requirements 2");
        BpRequirement requirement1 = createBpRequirement(requirementGroup1, "R1.1",
                "Requirement 1");
        BpRequirement requirement2 = createBpRequirement(requirementGroup2, "R2.1",
                "Requirement 2");
        SafeguardGroup safeguardGroup1 = createSafeguardGroup(catalogModel, "S1", "Safeguards");
        Safeguard safeguard1 = createSafeguard(safeguardGroup1, "S1.1", "Safeguard");
        createLink(requirement1, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        SafeguardGroup safeguardGroup2 = createSafeguardGroup(catalogModel, "S2", "Safeguards");
        Safeguard safeguard2 = createSafeguard(safeguardGroup2, "S2.1", "Safeguard");
        createLink(requirement1, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement2, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createBpThreat(threatGroup, "Threat");
        createLink(requirement1, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        createLink(requirement2, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Stream.of(requirementGroup1.getUuid(), requirementGroup2.getUuid()).collect(
                        Collectors.toSet()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(false);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(3, itNetwork.getLinksUp().size());

        Set<CnATreeElement> modeledRequirementGroups = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(2, modeledRequirementGroups.size());

        CnATreeElement modeledRequirementGroup1 = findChildWithTitle(itNetwork,
                requirementGroup1.getTitle());
        assertNotNull(modeledRequirementGroup1);
        assertEquals(1, modeledRequirementGroup1.getChildren().size());
        CnATreeElement modeledRequirement1 = modeledRequirementGroup1.getChildren().iterator()
                .next();
        assertEquals(requirement1.getTitle(), modeledRequirement1.getTitle());

        assertEquals(2, modeledRequirement1.getLinksDown().size());
        assertEquals(0, modeledRequirement1.getLinksUp().size());
        Set<CnALink> linksRequirement1Network = getLinksWithType(modeledRequirement1,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirement1Network.size());
        assertEquals(linksRequirement1Network.iterator().next().getDependency(), itNetwork);

        CnATreeElement modeledRequirementGroup2 = findChildWithTitle(itNetwork,
                requirementGroup2.getTitle());
        assertNotNull(modeledRequirementGroup2);
        assertEquals(1, modeledRequirementGroup2.getChildren().size());
        CnATreeElement modeledRequirement2 = modeledRequirementGroup2.getChildren().iterator()
                .next();
        assertEquals(requirement2.getTitle(), modeledRequirement2.getTitle());

        assertEquals(2, modeledRequirement2.getLinksDown().size());
        assertEquals(0, modeledRequirement2.getLinksUp().size());
        Set<CnALink> linksRequirement2Network = getLinksWithType(modeledRequirement2,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirement2Network.size());
        assertEquals(linksRequirement2Network.iterator().next().getDependency(), itNetwork);

        CnATreeElement modeledThreatGroup = getChildrenWithTypeId(itNetwork, BpThreatGroup.TYPE_ID)
                .iterator().next();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat.getTitle(), modeledThreat.getTitle());

        Set<CnALink> linksThreatRequirement = getLinksWithType(modeledThreat,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(2, linksThreatRequirement.size());
        Set<CnATreeElement> requirements = linksThreatRequirement.stream()
                .map(CnALink::getDependant).collect(Collectors.toSet());
        assertThat(requirements, JUnitMatchers.hasItems(modeledRequirement1, modeledRequirement2));
        Set<CnATreeElement> threats = linksThreatRequirement.stream().map(CnALink::getDependency)
                .collect(Collectors.toSet());
        assertEquals(1, threats.size());
        assertEquals(linksThreatRequirement.iterator().next().getDependency(), modeledThreat);

        Set<CnALink> linksThreatTargetObject = getLinksWithType(modeledThreat,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(linksThreatTargetObject.iterator().next().getDependency(), itNetwork);

    }

    @Transactional
    @Test
    public void modelOneOfTwoModulesSharingAThreat() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup1 = createRequirementGroup(catalogModel, "R1",
                "Requirements");
        BpRequirementGroup requirementGroup2 = createRequirementGroup(catalogModel, "R2",
                "Requirements");
        BpRequirement requirement1 = createBpRequirement(requirementGroup1, "R1.1",
                "Requirement 1");
        BpRequirement requirement2 = createBpRequirement(requirementGroup2, "R2.1",
                "Requirement 2");
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createBpThreat(threatGroup, "Threat");
        createLink(requirement1, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        createLink(requirement2, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup1.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(false);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(2, itNetwork.getLinksUp().size());

        CnATreeElement modeledRequirementGroup = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup1.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement1.getTitle(), modeledRequirement.getTitle());

        assertEquals(2, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementNetwork = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirementNetwork.size());
        assertEquals(linksRequirementNetwork.iterator().next().getDependency(), itNetwork);

        CnATreeElement modeledThreatGroup = getChildrenWithTypeId(itNetwork, BpThreatGroup.TYPE_ID)
                .iterator().next();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat.getTitle(), modeledThreat.getTitle());

        Set<CnALink> linksThreatRequirement = getLinksWithType(modeledThreat,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksThreatRequirement.size());
        assertEquals(linksThreatRequirement.iterator().next().getDependency(), modeledThreat);

        Set<CnALink> linksThreatTargetObject = getLinksWithType(modeledThreat,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(linksThreatTargetObject.iterator().next().getDependency(), itNetwork);

    }

    @Transactional
    @Test
    public void modelWithoutSafeguards() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "Requirements");
        BpRequirement requirement = createBpRequirement(requirementGroup, "Requirement");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "Safeguard");
        createLink(requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createBpThreat(threatGroup, "Threat");
        createLink(requirement, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(false);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(2, itNetwork.getLinksUp().size());

        CnATreeElement modeledRequirementGroup = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement.getTitle(), modeledRequirement.getTitle());
        assertFalse(
                modeledRequirement.getEntity().isFlagged(BpRequirement.PROP_IMPLEMENTATION_DEDUCE));

        assertEquals(2, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementNetwork = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirementNetwork.size());
        assertEquals(linksRequirementNetwork.iterator().next().getDependency(), itNetwork);

        Assert.assertEquals(0l, getChildrenWithTypeId(itNetwork, SafeguardGroup.TYPE_ID).size());

        CnATreeElement modeledThreatGroup = getChildrenWithTypeId(itNetwork, BpThreatGroup.TYPE_ID)
                .iterator().next();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat.getTitle(), modeledThreat.getTitle());

        Set<CnALink> linksThreatRequirement = getLinksWithType(modeledThreat,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksThreatRequirement.size());
        assertEquals(linksThreatRequirement.iterator().next().getDependency(), modeledThreat);

        Set<CnALink> linksThreatTargetObject = getLinksWithType(modeledThreat,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(linksThreatTargetObject.iterator().next().getDependency(), itNetwork);
    }

    @Transactional
    @Test
    public void modelWithSafeguardsWithoutSafeguardsPresent() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "Requirements");
        BpRequirement requirement = createBpRequirement(requirementGroup, "Requirement");
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createBpThreat(threatGroup, "Threat");
        createLink(requirement, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(2, itNetwork.getLinksUp().size());

        CnATreeElement modeledRequirementGroup = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement.getTitle(), modeledRequirement.getTitle());
        assertFalse(
                modeledRequirement.getEntity().isFlagged(BpRequirement.PROP_IMPLEMENTATION_DEDUCE));

        assertEquals(2, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementNetwork = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirementNetwork.size());
        assertEquals(linksRequirementNetwork.iterator().next().getDependency(), itNetwork);

        Assert.assertEquals(0l, getChildrenWithTypeId(itNetwork, SafeguardGroup.TYPE_ID).size());

        CnATreeElement modeledThreatGroup = getChildrenWithTypeId(itNetwork, BpThreatGroup.TYPE_ID)
                .iterator().next();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat.getTitle(), modeledThreat.getTitle());

        Set<CnALink> linksThreatRequirement = getLinksWithType(modeledThreat,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksThreatRequirement.size());
        assertEquals(linksThreatRequirement.iterator().next().getDependency(), modeledThreat);

        Set<CnALink> linksThreatTargetObject = getLinksWithType(modeledThreat,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(linksThreatTargetObject.iterator().next().getDependency(), itNetwork);
    }

    @Transactional
    @Test
    public void modelModuleOnItNetworkWithDummySafeguards() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "Requirements");
        BpRequirement requirement = createBpRequirement(requirementGroup, "Requirement");
        requirement.setSecurityLevel(SecurityLevel.BASIC);
        requirement = update(requirement);

        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(true);
        commandService.executeCommand(modelCommand);
        elementDao.flush();
        elementDao.clear();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(1, itNetwork.getLinksUp().size());

        CnATreeElement modeledRequirementGroup = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement.getTitle(), modeledRequirement.getTitle());
        assertTrue(
                modeledRequirement.getEntity().isFlagged(BpRequirement.PROP_IMPLEMENTATION_DEDUCE));

        assertEquals(2, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementNetwork = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirementNetwork.size());
        assertEquals(linksRequirementNetwork.iterator().next().getDependency(), itNetwork);

        CnATreeElement modeledSafeguardGroup = getChildrenWithTypeId(itNetwork,
                SafeguardGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledSafeguardGroup.getTitle());
        assertEquals(1, modeledSafeguardGroup.getChildren().size());
        CnATreeElement modeledSafeguard = modeledSafeguardGroup.getChildren().iterator().next();
        assertNotNull(modeledSafeguard);

        Set<CnALink> linksSafeguardRequirement = getLinksWithType(modeledSafeguard,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals(1, linksSafeguardRequirement.size());
        assertEquals(linksSafeguardRequirement.iterator().next().getDependency(), modeledSafeguard);

    }

    @Transactional
    @Test
    public void modelModuleWithMultipleRequirementsOnItNetworkWithDummySafeguards()
            throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "Requirements");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "Requirement1");
        BpRequirement requirement2 = createBpRequirement(requirementGroup, "Requirement2");
        BpRequirement requirement3 = createBpRequirement(requirementGroup, "Requirement3");
        BpRequirement requirement4 = createBpRequirement(requirementGroup, "Requirement4");
        requirement1.setSecurityLevel(SecurityLevel.BASIC);
        requirement1 = update(requirement1);
        requirement2.setSecurityLevel(SecurityLevel.BASIC);
        requirement2 = update(requirement1);
        requirement3.setSecurityLevel(SecurityLevel.BASIC);
        requirement3 = update(requirement1);
        requirement4.setSecurityLevel(SecurityLevel.BASIC);
        requirement4 = update(requirement1);

        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(true);
        commandService.executeCommand(modelCommand);
        elementDao.flush();
        elementDao.clear();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(4, itNetwork.getLinksUp().size());

        CnATreeElement modeledRequirementGroup = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(4, modeledRequirementGroup.getChildren().size());
        for (CnATreeElement modeledRequirement : modeledRequirementGroup.getChildren()) {
            assertTrue(modeledRequirement.getEntity()
                    .isFlagged(BpRequirement.PROP_IMPLEMENTATION_DEDUCE));

            assertEquals(2, modeledRequirement.getLinksDown().size());
            assertEquals(0, modeledRequirement.getLinksUp().size());
            Set<CnALink> linksRequirementNetwork = getLinksWithType(modeledRequirement,
                    BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
            assertEquals(1, linksRequirementNetwork.size());
            Set<CnALink> linksSafeguardRequirement = getLinksWithType(modeledRequirement,
                    BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
            assertEquals(1, linksSafeguardRequirement.size());
        }

        CnATreeElement modeledSafeguardGroup = getChildrenWithTypeId(itNetwork,
                SafeguardGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledSafeguardGroup.getTitle());
        assertEquals(4, modeledSafeguardGroup.getChildren().size());

    }

    @Transactional
    @Test
    public void modelWithDummySafeguardsWhereSafeguardGroupExistsInOtherElement()
            throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1");
        BpRequirement requirement = createBpRequirement(requirementGroup, "R1.1");
        requirement.setSecurityLevel(SecurityLevel.BASIC);
        requirement = update(requirement);

        ItNetwork itNetwork = createNewBPOrganization();
        ApplicationGroup appGroup = createBpApplicationGroup(itNetwork);
        Application app = createBpApplication(appGroup);
        createSafeguardGroup(app, "R1", "Safeguards for R1");

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(true);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(1, itNetwork.getLinksUp().size());

        CnATreeElement modeledSafeguardGroup = getChildrenWithTypeId(itNetwork,
                SafeguardGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledSafeguardGroup.getTitle());
        assertEquals(1, modeledSafeguardGroup.getChildren().size());
        CnATreeElement modeledSafeguard = modeledSafeguardGroup.getChildren().iterator().next();
        assertNotNull(modeledSafeguard);

    }

    @Transactional
    @Test
    public void modelModuleWithOneExistingRequirement() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        BpRequirement requirement2 = createBpRequirement(requirementGroup, "R1.2", "Requirement 2");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "S1", "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "S1.1", "Safeguard");
        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement2, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createThreat(threatGroup, "T1.1", "Threat");
        createLink(requirement1, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        createLink(requirement2, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(3, itNetwork.getLinksUp().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(requirementGroupItNetwork, firstRequirementsGroupInNetwork);
        assertEquals(2, firstRequirementsGroupInNetwork.getChildren().size());
        assertEquals(requirement1ItNetwork,
                findChildWithTitle(firstRequirementsGroupInNetwork, "Requirement 1"));
        CnATreeElement modeledRequirement = findChildWithTitle(firstRequirementsGroupInNetwork,
                "Requirement 2");

        assertEquals(3, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementNetwork = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirementNetwork.size());
        assertEquals(itNetwork, linksRequirementNetwork.iterator().next().getDependency());

        CnATreeElement modeledSafeguardGroup = getChildrenWithTypeId(itNetwork,
                SafeguardGroup.TYPE_ID).iterator().next();
        assertEquals(safeguardGroup.getTitle(), modeledSafeguardGroup.getTitle());
        assertEquals(1, modeledSafeguardGroup.getChildren().size());
        CnATreeElement modeledSafeguard = modeledSafeguardGroup.getChildren().iterator().next();
        assertEquals(safeguard.getTitle(), modeledSafeguard.getTitle());

        Set<CnALink> linksSafeguardRequirement = getLinksWithType(modeledSafeguard,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals(2, linksSafeguardRequirement.size());

        CnATreeElement modeledThreatGroup = getChildrenWithTypeId(itNetwork, BpThreatGroup.TYPE_ID)
                .iterator().next();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat.getTitle(), modeledThreat.getTitle());

        Set<CnALink> linksThreatRequirement = getLinksWithType(modeledThreat,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(2, linksThreatRequirement.size());

        Set<CnALink> linksThreatTargetObject = getLinksWithType(modeledThreat,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(linksThreatTargetObject.iterator().next().getDependency(), itNetwork);

    }

    @Transactional
    @Test
    public void modelModuleWithExistingSafeguard() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "S1", "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "S1.1", "Safeguard");
        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        ItNetwork itNetwork = createNewBPOrganization();
        SafeguardGroup safeguardGroupItNetwork = createSafeguardGroup(itNetwork, "S1",
                "Safeguards");
        Safeguard safeguard1ItNetwork = createSafeguard(safeguardGroupItNetwork, "S1.1",
                "Safeguard");

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(1, itNetwork.getLinksUp().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        CnATreeElement modeledRequirement = firstRequirementsGroupInNetwork.getChildren().iterator()
                .next();

        assertEquals(2, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementSafeguard = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals(1, linksRequirementSafeguard.size());

        assertEquals(safeguard1ItNetwork,
                linksRequirementSafeguard.iterator().next().getDependency());
    }

    @Transactional
    @Test
    public void modelModuleWithExistingRequirementAndSafeguardButNoLinks() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "S1", "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "S1.1", "Safeguard");
        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        ItNetwork itNetwork = createNewBPOrganization();
        SafeguardGroup safeguardGroupItNetwork = createSafeguardGroup(itNetwork, "S1",
                "Safeguards");
        Safeguard safeguard1ItNetwork = createSafeguard(safeguardGroupItNetwork, "S1.1",
                "Safeguard");
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        createBpRequirement(requirementGroupItNetwork, "R1.1", "Requirement 1");

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(1, itNetwork.getLinksUp().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        CnATreeElement modeledRequirement = firstRequirementsGroupInNetwork.getChildren().iterator()
                .next();

        assertEquals(2, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementSafeguard = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals(1, linksRequirementSafeguard.size());
        assertEquals(safeguard1ItNetwork,
                linksRequirementSafeguard.iterator().next().getDependency());
        Set<CnALink> linksRequirementTargetObject = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirementTargetObject.size());
        assertEquals(itNetwork, linksRequirementTargetObject.iterator().next().getDependency());
    }

    @Transactional
    @Test
    public void modelModuleWithExistingRequirementAndSafeguardAndLinkBetweenRequirementAndTargetObject()
            throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "S1", "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "S1.1", "Safeguard");
        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        ItNetwork itNetwork = createNewBPOrganization();
        SafeguardGroup safeguardGroupItNetwork = createSafeguardGroup(itNetwork, "S1",
                "Safeguards");
        Safeguard safeguard1ItNetwork = createSafeguard(safeguardGroupItNetwork, "S1.1",
                "Safeguard");
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(1, itNetwork.getLinksUp().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        CnATreeElement modeledRequirement = firstRequirementsGroupInNetwork.getChildren().iterator()
                .next();

        assertEquals(2, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementSafeguard = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals(1, linksRequirementSafeguard.size());

        assertEquals(safeguard1ItNetwork,
                linksRequirementSafeguard.iterator().next().getDependency());
    }

    @Transactional
    @Test
    public void modelModuleWithExistingThreat() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createThreat(threatGroup, "T1.1", "Threat");
        createLink(requirement1, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();
        BpThreatGroup threatGroupItNetwork = createBpThreatGroup(itNetwork, "Threats");
        BpThreat threat1ItNetwork = createThreat(threatGroupItNetwork, "T1.1", "Threat 1");
        createLink(threat1ItNetwork, itNetwork, BpThreat.REL_BP_THREAT_BP_ITNETWORK);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();
        elementDao.clear();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(2, itNetwork.getLinksUp().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        CnATreeElement modeledRequirement = firstRequirementsGroupInNetwork.getChildren().iterator()
                .next();

        assertEquals(2, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());
        Set<CnALink> linksRequirementThreat = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksRequirementThreat.size());

        assertEquals(threat1ItNetwork, linksRequirementThreat.iterator().next().getDependency());
    }

    @Transactional
    @Test
    public void modelModuleWithExistingElementsButNoLinks() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "S1", "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "S1.1", "Safeguard");
        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createThreat(threatGroup, "T1.1", "Threat");
        createLink(requirement1, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();
        SafeguardGroup safeguardGroupItNetwork = createSafeguardGroup(itNetwork, "S1",
                "Safeguards");
        Safeguard safeguard1ItNetwork = createSafeguard(safeguardGroupItNetwork, "S1.1",
                "Safeguard");
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        createBpRequirement(requirementGroupItNetwork, "R1.1", "Requirement 1");
        BpThreatGroup threatGroupItNetwork = createBpThreatGroup(itNetwork, "Threats");
        BpThreat threat1ItNetwork = createThreat(threatGroupItNetwork, "T1.1", "Threat 1");

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();
        elementDao.clear();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(2, itNetwork.getLinksUp().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        CnATreeElement modeledRequirement = firstRequirementsGroupInNetwork.getChildren().iterator()
                .next();

        assertEquals(3, modeledRequirement.getLinksDown().size());
        assertEquals(0, modeledRequirement.getLinksUp().size());

        Set<CnALink> linksRequirementTargetObject = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        assertEquals(1, linksRequirementTargetObject.size());
        assertEquals(itNetwork, linksRequirementTargetObject.iterator().next().getDependency());

        Set<CnALink> linksRequirementSafeguard = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals(1, linksRequirementSafeguard.size());
        assertEquals(safeguard1ItNetwork,
                linksRequirementSafeguard.iterator().next().getDependency());

        Set<CnALink> linksRequirementThreat = getLinksWithType(modeledRequirement,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksRequirementThreat.size());
        CnATreeElement threatFromScope = linksRequirementThreat.iterator().next().getDependency();
        assertEquals(threat1ItNetwork, threatFromScope);

        Set<CnALink> linksThreatTargetObject = getLinksWithType(threatFromScope,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(linksThreatTargetObject.iterator().next().getDependency(), itNetwork);
    }

    @Transactional
    @Test
    public void doNotCopyThreatThatIsNotLinkedToModeledRequirements() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup1 = createRequirementGroup(catalogModel, "R1",
                "Requirements");
        BpRequirementGroup requirementGroup2 = createRequirementGroup(catalogModel, "R2",
                "Requirements");
        BpRequirement requirement1 = createBpRequirement(requirementGroup1, "R1.1",
                "Requirement 1");
        BpRequirement requirement2 = createBpRequirement(requirementGroup2, "R2.1",
                "Requirement 2");
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat1 = createBpThreat(threatGroup, "Threat 1");
        BpThreat threat2 = createBpThreat(threatGroup, "Threat 2");
        createLink(requirement1, threat1, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        createLink(requirement2, threat2, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup1.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(false);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();
        elementDao.clear();

        itNetwork = reloadElement(itNetwork);

        CnATreeElement modeledThreatGroup = getChildrenWithTypeId(itNetwork, BpThreatGroup.TYPE_ID)
                .iterator().next();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat1.getTitle(), modeledThreat.getTitle());

        Set<CnALink> linksThreatRequirement = getLinksWithType(modeledThreat,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksThreatRequirement.size());
        assertEquals(linksThreatRequirement.iterator().next().getDependency(), modeledThreat);

        Set<CnALink> linksThreatTargetObject = getLinksWithType(modeledThreat,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(linksThreatTargetObject.iterator().next().getDependency(), itNetwork);

    }

    @Test
    public void checkUpdatableReleases() throws CommandException {
        assertFalse(ModelCopyTask.canUpdateFrom(null, "2018-0"));
        assertFalse(ModelCopyTask.canUpdateFrom("", "2018-0"));
        assertFalse(ModelCopyTask.canUpdateFrom("", ""));
        assertFalse(ModelCopyTask.canUpdateFrom(null, null));
        assertFalse(ModelCopyTask.canUpdateFrom("2018-0", null));
        assertFalse(ModelCopyTask.canUpdateFrom("2018-0", "2018-0"));
        assertFalse(ModelCopyTask.canUpdateFrom("2020-0", "2018-0"));
        assertFalse(ModelCopyTask.canUpdateFrom("2018-0", "2020-0"));

        assertTrue(ModelCopyTask.canUpdateFrom("2019-0", "2019-5"));
        assertTrue(ModelCopyTask.canUpdateFrom("2019-0", "2020-0"));
        assertTrue(ModelCopyTask.canUpdateFrom("2019-1", "2020-0"));
        assertTrue(ModelCopyTask.canUpdateFrom("2019-0", "2020-3"));

    }

    @Transactional
    @Test
    public void updateExistingRequirement() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1",
                "Requirement 1 (updated)");
        requirement1.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-1");
        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        requirement1ItNetwork.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-0");
        requirement1ItNetwork.setImplementationStatus(ImplementationStatus.YES);
        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(requirementGroupItNetwork, firstRequirementsGroupInNetwork);
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        assertEquals("Requirement 1 (updated)",
                firstRequirementsGroupInNetwork.getChildren().iterator().next().getTitle());
        assertEquals(ImplementationStatus.YES,
                ((BpRequirement) firstRequirementsGroupInNetwork.getChildren().iterator().next())
                        .getImplementationStatus());
    }

    @Transactional
    @Test
    public void updateExistingRequirementWithRemovedVersion() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "ENTFALLEN");
        requirement1.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-1");

        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        requirement1ItNetwork.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-0");
        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(requirementGroupItNetwork, firstRequirementsGroupInNetwork);
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        CnATreeElement firstRequirement = firstRequirementsGroupInNetwork.getChildren().iterator()
                .next();
        assertEquals("Requirement 1", firstRequirement.getTitle());
        assertEquals("bp_requirement_change_type_removed",
                firstRequirement.getEntity().getRawPropertyValue(BpRequirement.PROP_CHANGE_TYPE));

    }

    @Transactional
    @Test
    // VN-2908
    public void updateExistingRequirementWithNoChangesSinceLast() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        requirement1.setSimpleProperty(BpRequirement.PROP_RELEASE, "2021-0");
        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        requirement1ItNetwork.setSimpleProperty(BpRequirement.PROP_RELEASE, "2020-0");
        requirement1ItNetwork.setSimpleProperty(BpRequirement.PROP_CHANGE_TYPE,
                "bp_requirement_change_type_changed");
        requirement1ItNetwork.setSimpleProperty(BpRequirement.PROP_CHANGE_DETAILS,
                "those were the previous change details");

        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(requirementGroupItNetwork, firstRequirementsGroupInNetwork);
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        BpRequirement updateRequirement = (BpRequirement) firstRequirementsGroupInNetwork
                .getChildren().iterator().next();
        assertEquals("Requirement 1", updateRequirement.getTitle());
        assertNull(
                updateRequirement.getEntity().getRawPropertyValue(BpRequirement.PROP_CHANGE_TYPE));
        assertNull(updateRequirement.getEntity()
                .getRawPropertyValue(BpRequirement.PROP_CHANGE_DETAILS));
    }

    @Transactional
    @Test
    public void doNotUpdateExistingRequirementWithNewerRelease() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1",
                "Requirement 1 (updated)");
        requirement1.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-1");
        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        requirement1ItNetwork.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-5");
        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(requirementGroupItNetwork, firstRequirementsGroupInNetwork);
        assertEquals(1, firstRequirementsGroupInNetwork.getChildren().size());
        assertEquals("Requirement 1",
                firstRequirementsGroupInNetwork.getChildren().iterator().next().getTitle());

    }

    @Transactional
    @Test
    public void updateExistingSafeguardGroup() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1",
                "Requirement 1 (updated)");
        SafeguardGroup safeguardGroupCompendium = createSafeguardGroup(catalogModel, "S1",
                "Renamed Safeguards");
        safeguardGroupCompendium.setSimpleProperty(SafeguardGroup.PROP_RELEASE, "2019-1");
        safeguardGroupCompendium.setSimpleProperty(SafeguardGroup.PROP_CHANGE_TYPE,
                "bp_safeguard_group_change_type_changed");
        Safeguard safeguard = createSafeguard(safeguardGroupCompendium, "S1.1", "Safeguard");
        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        ItNetwork itNetwork = createNewBPOrganization();
        SafeguardGroup safeguardGroupItNetwork = createSafeguardGroup(itNetwork, "S1",
                "Safeguards");
        safeguardGroupItNetwork.setSimpleProperty(SafeguardGroup.PROP_RELEASE, "2019-0");

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        Set<CnATreeElement> safeguardGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                SafeguardGroup.TYPE_ID);
        assertEquals(1, safeguardGroupsInNetwork.size());
        CnATreeElement firstSafeguardGroupInNetwork = safeguardGroupsInNetwork.iterator().next();
        assertEquals("2019-1",
                firstSafeguardGroupInNetwork.getPropertyValue(SafeguardGroup.PROP_RELEASE));
        assertEquals("bp_safeguard_group_change_type_changed", firstSafeguardGroupInNetwork
                .getEntity().getRawPropertyValue(SafeguardGroup.PROP_CHANGE_TYPE));
        assertEquals("Renamed Safeguards", firstSafeguardGroupInNetwork.getEntity()
                .getRawPropertyValue(SafeguardGroup.PROP_NAME));

    }

    @Transactional
    @Test
    public void updateExistingThreatWithMarkedAsRemovedVersion() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createThreat(threatGroup, "T1.1", "ENTFALLEN");
        threat.setSimpleProperty(BpThreat.PROP_RELEASE, "2019-1");
        threat.setSimpleProperty(BpThreat.PROP_CHANGE_TYPE, BpThreat.PROP_CHANGE_TYPE_REMOVED);
        threat.setSimpleProperty("bp_threat_change_details", "This threat was removed");
        createLink(requirement1, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        BpThreatGroup threatGroupItNetwork = createBpThreatGroup(itNetwork, "Threats");
        BpThreat threat1ItNetwork = createThreat(threatGroupItNetwork, "T1.1", "Threat 1 (RIP)");
        threat1ItNetwork.setSimpleProperty(BpThreat.PROP_RELEASE, "2019-0");
        threat1ItNetwork.setSimpleProperty(BpThreat.PROP_CHANGE_TYPE,
                "bp_threat_change_type_changed");
        threat1ItNetwork.setSimpleProperty("bp_threat_change_details",
                "Something important was changed");
        threat1ItNetwork.setRiskWithoutAdditionalSafeguards("risk1");
        createLink(requirement1, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        createLink(requirement1ItNetwork, threat1ItNetwork,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        createLink(threat1ItNetwork, itNetwork, BpThreat.REL_BP_THREAT_BP_ITNETWORK);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        Set<CnALink> linksThreatTargetObject = getLinksWithType(itNetwork,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        assertEquals(1, linksThreatTargetObject.size());
        Set<CnALink> linksRequirementThreat = getLinksWithType(requirement1ItNetwork,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksRequirementThreat.size());
        BpThreat linkedThreat = (BpThreat) linksThreatTargetObject.iterator().next().getDependant();
        assertEquals("2019-1", linkedThreat.getPropertyValue(BpThreat.PROP_RELEASE));
        assertEquals("Threat 1 (RIP)", linkedThreat.getTitle());
        assertEquals("bp_threat_change_type_removed",
                linkedThreat.getEntity().getRawPropertyValue(BpThreat.PROP_CHANGE_TYPE));
        assertEquals("This threat was removed",
                linkedThreat.getEntity().getRawPropertyValue("bp_threat_change_details"));
        assertEquals("risk1", linkedThreat.getRiskWithoutAdditionalSafeguards());

    }

    @Transactional
    @Test
    public void updateExistingThreatWithNoLongerLinkedVersion() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        requirement.setSimpleProperty(BpRequirement.PROP_RELEASE, "2022-0");
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createThreat(threatGroup, "T1.1", "Threat 1");
        threat.setSimpleProperty(BpThreat.PROP_RELEASE, "2022-0");

        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        requirement1ItNetwork.setSimpleProperty(BpRequirement.PROP_RELEASE, "2021-1");

        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        BpThreatGroup threatGroupItNetwork = createBpThreatGroup(itNetwork, "Threats");
        BpThreat threat1ItNetwork = createThreat(threatGroupItNetwork, "T1.1", "Threat 1");
        threat1ItNetwork.setSimpleProperty(BpThreat.PROP_RELEASE, "2021-1");
        threat1ItNetwork.setSimpleProperty(BpThreat.PROP_CHANGE_TYPE,
                "bp_threat_change_type_changed");
        threat1ItNetwork.setSimpleProperty("bp_threat_change_details",
                "Something important was changed");
        threat1ItNetwork.setRiskWithoutAdditionalSafeguards("risk1");
        createLink(threat1ItNetwork, itNetwork, BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        createLink(requirement1ItNetwork, threat1ItNetwork,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        requirement1ItNetwork = reloadElement(requirement1ItNetwork);
        assertEquals("2022-0", requirement1ItNetwork.getPropertyValue(BpRequirement.PROP_RELEASE));

        Set<CnALink> linksThreatTargetObject = getLinksWithType(itNetwork,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        Set<CnALink> linksRequirementThreat = getLinksWithType(requirement1ItNetwork,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(1, linksThreatTargetObject.size());
        assertEquals(0, linksRequirementThreat.size());
        BpThreat linkedThreat = (BpThreat) linksThreatTargetObject.iterator().next().getDependant();
        assertEquals("2021-1", linkedThreat.getPropertyValue(BpThreat.PROP_RELEASE));
        assertEquals("Threat 1", linkedThreat.getTitle());
        assertEquals("bp_threat_change_type_changed",
                linkedThreat.getEntity().getRawPropertyValue(BpThreat.PROP_CHANGE_TYPE));
        assertEquals("Something important was changed",
                linkedThreat.getEntity().getRawPropertyValue("bp_threat_change_details"));
        assertEquals("risk1", linkedThreat.getRiskWithoutAdditionalSafeguards());

    }

    @Transactional
    @Test
    public void updateExistingThreatWithNoLongerLinkedVersionWithAdditionalRequirement()
            throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "Requirement 1");
        requirement1.setSimpleProperty(BpRequirement.PROP_RELEASE, "2022-0");

        BpRequirement requirement2 = createBpRequirement(requirementGroup, "R1.2", "Requirement 2");
        requirement2.setSimpleProperty(BpRequirement.PROP_RELEASE, "2022-0");

        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat1 = createThreat(threatGroup, "T1.1", "Threat 1");
        threat1.setSimpleProperty(BpThreat.PROP_RELEASE, "2022-0");
        BpThreat threat2 = createThreat(threatGroup, "T1.2", "Threat 2");
        threat2.setSimpleProperty(BpThreat.PROP_RELEASE, "2022-0");

        createLink(requirement1, threat1, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        // no link from requirement 2 to threat 2!

        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");
        BpRequirement requirement1ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.1",
                "Requirement 1");
        requirement1ItNetwork.setSimpleProperty(BpRequirement.PROP_RELEASE, "2021-1");
        createLink(requirement1ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);
        BpRequirement requirement2ItNetwork = createBpRequirement(requirementGroupItNetwork, "R1.2",
                "Requirement 2");
        requirement2ItNetwork.setSimpleProperty(BpRequirement.PROP_RELEASE, "2021-1");
        createLink(requirement2ItNetwork, itNetwork, BpRequirement.REL_BP_REQUIREMENT_BP_ITNETWORK);

        BpThreatGroup threatGroupItNetwork = createBpThreatGroup(itNetwork, "Threats");
        BpThreat threat1ItNetwork = createThreat(threatGroupItNetwork, "T1.1", "Threat 1");
        threat1ItNetwork.setSimpleProperty(BpThreat.PROP_RELEASE, "2021-1");
        threat1ItNetwork.setSimpleProperty(BpThreat.PROP_CHANGE_TYPE,
                "bp_threat_change_type_changed");
        threat1ItNetwork.setSimpleProperty("bp_threat_change_details",
                "Something important was changed");
        threat1ItNetwork.setRiskWithoutAdditionalSafeguards("risk1");
        createLink(threat1ItNetwork, itNetwork, BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        createLink(requirement1ItNetwork, threat1ItNetwork,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        BpThreat threat2ItNetwork = createThreat(threatGroupItNetwork, "T1.2", "Threat 2");
        threat2ItNetwork.setSimpleProperty(BpThreat.PROP_RELEASE, "2021-1");
        threat2ItNetwork.setSimpleProperty(BpThreat.PROP_CHANGE_TYPE,
                "bp_threat_change_type_changed");
        threat2ItNetwork.setSimpleProperty("bp_threat_change_details",
                "Something important was changed");
        createLink(threat2ItNetwork, itNetwork, BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        createLink(requirement2ItNetwork, threat2ItNetwork,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        requirement2ItNetwork = reloadElement(requirement2ItNetwork);
        assertEquals("2022-0", requirement2ItNetwork.getPropertyValue(BpRequirement.PROP_RELEASE));

        Set<CnALink> linksThreatTargetObject = getLinksWithType(itNetwork,
                BpThreat.REL_BP_THREAT_BP_ITNETWORK);
        Set<CnALink> linksRequirementThreat = getLinksWithType(requirement2ItNetwork,
                BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);
        assertEquals(2, linksThreatTargetObject.size());
        assertEquals(0, linksRequirementThreat.size());

        BpThreat linkedThreat1 = (BpThreat) linksThreatTargetObject.stream()
                .filter(link -> link.getDependant().getTitle().equals("Threat 1")).findFirst()
                .orElseThrow().getDependant();
        assertEquals("2022-0", linkedThreat1.getPropertyValue(BpThreat.PROP_RELEASE));
        BpThreat linkedThreat2 = (BpThreat) linksThreatTargetObject.stream()
                .filter(link -> link.getDependant().getTitle().equals("Threat 2")).findFirst()
                .orElseThrow().getDependant();
        assertEquals("2021-1", linkedThreat2.getPropertyValue(BpThreat.PROP_RELEASE));

    }

    @Transactional
    @Test
    public void doNotCopyRemovedElementIfNotPresentInExistingModule() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "ENTFALLEN");
        requirement1.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-1");
        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork, "R1",
                "Requirements 1");

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertEquals(requirementGroupItNetwork, firstRequirementsGroupInNetwork);
        assertTrue(firstRequirementsGroupInNetwork.getChildren().isEmpty());

    }

    @Transactional
    @Test
    public void doNotCopyRemovedElementIfNotPresentInNewModule() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "Requirements 1");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "ENTFALLEN");
        requirement1.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-1");
        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertEquals(1, requirementsGroupsInNetwork.size());
        CnATreeElement firstRequirementsGroupInNetwork = requirementsGroupsInNetwork.iterator()
                .next();
        assertTrue(firstRequirementsGroupInNetwork.getChildren().isEmpty());

    }

    @Transactional
    @Test
    public void doNotCopyRemovedGroupIfNotPresentInNewModule() throws CommandException {
        CatalogModel catalogModel = loadCatalogModel();
        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "R1",
                "ENTFALLEN");
        BpRequirement requirement1 = createBpRequirement(requirementGroup, "R1.1", "ENTFALLEN");
        requirement1.setSimpleProperty(BpRequirement.PROP_RELEASE, "2019-1");
        ItNetwork itNetwork = createNewBPOrganization();

        elementDao.flush();
        elementDao.clear();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        Set<CnATreeElement> requirementsGroupsInNetwork = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID);
        assertTrue(requirementsGroupsInNetwork.isEmpty());

    }

    private CatalogModel loadCatalogModel() {
        return (CatalogModel) elementDao
                .findByCriteria(DetachedCriteria.forClass(CatalogModel.class)).get(0);
    }
}