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
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.bp.ModelCommand;
import sernet.verinice.service.commands.crud.CreateCatalogModel;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class ModelingTest extends AbstractModernizedBaseProtection {

    @Transactional
    @Rollback(true)
    @Test
    public void modelModuleOnItNetwork() throws CommandException {
        CreateCatalogModel command = new CreateCatalogModel();
        command = commandService.executeCommand(command);
        CatalogModel catalogModel = command.getElement();

        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "Requirements");
        BpRequirement requirement = createBpRequirement(requirementGroup, "Requirement");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "Safeguard");
        createLink(requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createBpThreat(threatGroup, "Threat");
        createLink(requirement, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.clear();

        itNetwork = reloadElement(itNetwork);

        CnATreeElement modeledRequirementGroup = itNetwork.getChildren().stream()
                .filter(child -> child.getTypeId().equals(BpRequirementGroup.TYPE_ID)).findFirst()
                .get();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement.getTitle(), modeledRequirement.getTitle());

        CnATreeElement modeledSafeguardGroup = itNetwork.getChildren().stream()
                .filter(child -> child.getTypeId().equals(SafeguardGroup.TYPE_ID)).findFirst()
                .get();
        assertEquals(safeguardGroup.getTitle(), modeledSafeguardGroup.getTitle());
        assertEquals(1, modeledSafeguardGroup.getChildren().size());
        CnATreeElement modeledSafeguard = modeledSafeguardGroup.getChildren().iterator().next();
        assertEquals(safeguard.getTitle(), modeledSafeguard.getTitle());

        CnATreeElement modeledThreatGroup = itNetwork.getChildren().stream()
                .filter(child -> child.getTypeId().equals(BpThreatGroup.TYPE_ID)).findFirst().get();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat.getTitle(), modeledThreat.getTitle());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void modelWithoutSafeguards() throws CommandException {
        CreateCatalogModel command = new CreateCatalogModel();
        command = commandService.executeCommand(command);
        CatalogModel catalogModel = command.getElement();

        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "Requirements");
        BpRequirement requirement = createBpRequirement(requirementGroup, "Requirement");
        SafeguardGroup safeguardGroup = createSafeguardGroup(catalogModel, "Safeguards");
        Safeguard safeguard = createSafeguard(safeguardGroup, "Safeguard");
        createLink(requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        BpThreatGroup threatGroup = createBpThreatGroup(catalogModel, "Threats");
        BpThreat threat = createBpThreat(threatGroup, "Threat");
        createLink(requirement, threat, BpRequirement.REL_BP_REQUIREMENT_BP_THREAT);

        ItNetwork itNetwork = createNewBPOrganization();

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(false);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.clear();

        itNetwork = reloadElement(itNetwork);

        CnATreeElement modeledRequirementGroup = itNetwork.getChildren().stream()
                .filter(child -> child.getTypeId().equals(BpRequirementGroup.TYPE_ID)).findFirst()
                .get();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement.getTitle(), modeledRequirement.getTitle());

        Assert.assertEquals(0l, itNetwork.getChildren().stream()
                .filter(child -> child.getTypeId().equals(SafeguardGroup.TYPE_ID)).count());

        CnATreeElement modeledThreatGroup = itNetwork.getChildren().stream()
                .filter(child -> child.getTypeId().equals(BpThreatGroup.TYPE_ID)).findFirst().get();
        assertEquals(threatGroup.getTitle(), modeledThreatGroup.getTitle());
        assertEquals(1, modeledThreatGroup.getChildren().size());
        CnATreeElement modeledThreat = modeledThreatGroup.getChildren().iterator().next();
        assertEquals(threat.getTitle(), modeledThreat.getTitle());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void modelModuleOnItNetworkWithDummySafeguards() throws CommandException {
        CreateCatalogModel command = new CreateCatalogModel();
        command = commandService.executeCommand(command);
        CatalogModel catalogModel = command.getElement();

        BpRequirementGroup requirementGroup = createRequirementGroup(catalogModel, "Requirements");
        BpRequirement requirement = createBpRequirement(requirementGroup, "Requirement");
        requirement.setSecurityLevel(SecurityLevel.BASIC);
        requirement = update(requirement);

        ItNetwork itNetwork = createNewBPOrganization();
        elementDao.flush();
        elementDao.clear();
        itNetwork = reloadElement(itNetwork);

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itNetwork.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(true);
        commandService.executeCommand(modelCommand);
        elementDao.flush();
        elementDao.clear();

        itNetwork = reloadElement(itNetwork);

        CnATreeElement modeledRequirementGroup = itNetwork.getChildren().stream()
                .filter(child -> child.getTypeId().equals(BpRequirementGroup.TYPE_ID)).findFirst()
                .get();
        assertEquals(requirementGroup.getTitle(), modeledRequirementGroup.getTitle());
        assertEquals(1, modeledRequirementGroup.getChildren().size());
        CnATreeElement modeledRequirement = modeledRequirementGroup.getChildren().iterator().next();
        assertEquals(requirement.getTitle(), modeledRequirement.getTitle());

        CnATreeElement modeledSafeguardGroup = itNetwork.getChildren().stream()
                .filter(child -> child.getTypeId().equals(SafeguardGroup.TYPE_ID)).findFirst()
                .get();
        assertEquals(requirementGroup.getTitle(), modeledSafeguardGroup.getTitle());
        assertEquals(1, modeledSafeguardGroup.getChildren().size());
        CnATreeElement modeledSafeguard = modeledSafeguardGroup.getChildren().iterator().next();
        assertNotNull(modeledSafeguard);

    }

}
