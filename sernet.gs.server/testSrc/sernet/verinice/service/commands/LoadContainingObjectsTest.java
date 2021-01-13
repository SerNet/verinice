package sernet.verinice.service.commands;

import java.util.Arrays;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

@TransactionConfiguration(transactionManager = "txManager")
@Transactional
public class LoadContainingObjectsTest extends AbstractModernizedBaseProtection {

    @Test
    public void targetObjectIsReturnedForRequirement() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroupItNetwork = createRequirementGroup(itNetwork);
        BpRequirement requirementItNetwork = createBpRequirement(requirementGroupItNetwork);

        ItSystemGroup itSystems = createGroup(itNetwork, ItSystemGroup.class);
        ItSystem itSystem = createElement(itSystems, ItSystem.class);
        BpRequirementGroup requirementGroupItSystem = createRequirementGroup(itSystem);
        BpRequirement requirementItSystem = createBpRequirement(requirementGroupItSystem);

        LoadContainingObjects loadContainingObjects = new LoadContainingObjects(
                Arrays.asList(requirementItNetwork, requirementItSystem));
        loadContainingObjects = commandService.executeCommand(loadContainingObjects);
        Map<Integer, CnATreeElement> result = loadContainingObjects.getResult();
        Assert.assertEquals(itNetwork, result.get(requirementItNetwork.getDbId()));
        Assert.assertEquals(itSystem, result.get(requirementItSystem.getDbId()));
    }

    @Test
    public void bpModelIsReturnedForItNetwork() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        LoadContainingObjects loadContainingObjects = new LoadContainingObjects(
                Arrays.asList(itNetwork));
        loadContainingObjects = commandService.executeCommand(loadContainingObjects);
        Map<Integer, CnATreeElement> result = loadContainingObjects.getResult();
        Assert.assertThat(result.get(itNetwork.getDbId()), CoreMatchers.is(BpModel.class));
    }

}
