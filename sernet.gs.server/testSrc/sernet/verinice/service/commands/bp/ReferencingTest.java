/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade.
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
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.junit.After;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

@TransactionConfiguration(transactionManager = "txManager")
@Transactional
public class ReferencingTest extends AbstractModernizedBaseProtection {

    @After
    public void cleanUp() {
        elementDao.clear();
    }

    @Transactional
    @Test
    public void referenceModuleFromOtherITSystem() throws CommandException {
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
        ItSystemGroup itSystems = createGroup(itNetwork, ItSystemGroup.class, "IT Systems");
        ItSystem itSystem1 = createElement(itSystems, ItSystem.class, "IT System 1");
        ItSystem itSystem2 = createElement(itSystems, ItSystem.class, "IT System 2");

        ModelCommand modelCommand = new ModelCommand(
                Collections.singleton(requirementGroup.getUuid()),
                Collections.singletonList(itSystem1.getUuid()));
        modelCommand.setHandleSafeguards(true);
        modelCommand.setHandleDummySafeguards(false);
        commandService.executeCommand(modelCommand);
        elementDao.flush();

        itSystem1 = reloadElement(itSystem1);

        CnATreeElement modeledRequirementGroup = getChildrenWithTypeId(itSystem1,
                BpRequirementGroup.TYPE_ID).iterator().next();
        CnATreeElement modeledThreatGroup = getChildrenWithTypeId(itSystem1, BpThreatGroup.TYPE_ID)
                .iterator().next();

        ReferencingCommand referencingCommand = new ReferencingCommand(
                Set.of(modeledRequirementGroup.getDbId()), itSystem2.getDbId());
        commandService.executeCommand(referencingCommand);

        itSystem2 = reloadElement(itSystem2);

        assertEquals(0, itSystem2.getChildren().size());
        assertEquals(0, itSystem2.getLinksDown().size());
        assertEquals(2, itSystem2.getLinksUp().size());
        Set<CnATreeElement> dependants = getDependantsFromLinks(itSystem2.getLinksUp());
        assertThat(dependants,
                JUnitMatchers.hasItems(modeledRequirementGroup.getChildren().iterator().next(),
                        modeledThreatGroup.getChildren().iterator().next()));
    }

    private CatalogModel loadCatalogModel() {
        return (CatalogModel) elementDao
                .findByCriteria(DetachedCriteria.forClass(CatalogModel.class)).get(0);
    }
}