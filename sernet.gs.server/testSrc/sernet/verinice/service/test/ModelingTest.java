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
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.junit.After;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import junit.framework.Assert;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
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

        itNetwork = reloadElement(itNetwork);
        assertEquals(0, itNetwork.getLinksDown().size());
        assertEquals(1, itNetwork.getLinksUp().size());

        CnATreeElement modeledRequirementGroup = getChildrenWithTypeId(itNetwork,
                BpRequirementGroup.TYPE_ID).iterator().next();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement.getTitle(), modeledRequirement.getTitle());

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

    private CatalogModel loadCatalogModel() {
        return (CatalogModel) elementDao
                .findByCriteria(DetachedCriteria.forClass(CatalogModel.class)).get(0);
    }
}