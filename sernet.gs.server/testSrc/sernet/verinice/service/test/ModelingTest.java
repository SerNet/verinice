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
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.criterion.DetachedCriteria;
import org.junit.After;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import junit.framework.Assert;
import sernet.verinice.interfaces.CommandException;
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
import sernet.verinice.service.commands.bp.ModelCommand;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class ModelingTest extends AbstractModernizedBaseProtection {

    @After
    public void cleanUp() {
        elementDao.clear();
    }

    @Transactional
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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
    @Rollback(true)
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

    private CatalogModel loadCatalogModel() {
        return (CatalogModel) elementDao
                .findByCriteria(DetachedCriteria.forClass(CatalogModel.class)).get(0);
    }
}