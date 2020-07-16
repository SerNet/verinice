package sernet.verinice.service.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
@Transactional
public class LoadCnAElementByEntityTypeIdTest extends AbstractModernizedBaseProtection {

    private BpRequirement requirement;
    private BpRequirement requirementCatalog;

    @Before
    public void setup() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        requirement = createBpRequirement(requirementGroup);

        CatalogModel catalogModel = loadCatalogModel();
        ItNetwork catalog1 = new ItNetwork(catalogModel);
        elementDao.saveOrUpdate(catalog1);
        BpRequirementGroup requirementGroupCatalog = createRequirementGroup(catalog1);
        requirementCatalog = createBpRequirement(requirementGroupCatalog);
    }

    @Test
    public void loadAllRequirements() throws CommandException {

        LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(
                BpRequirement.TYPE_ID);
        command = commandService.executeCommand(command);
        List<CnATreeElement> elements = command.getElements();
        assertEquals(2, elements.size());
        assertTrue(elements.contains(requirement));
        assertTrue(elements.contains(requirementCatalog));

    }

    @Test
    public void loadAllRequirementsThatAreNotCatalogElements() throws CommandException {

        LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(
                BpRequirement.TYPE_ID, false);
        command = commandService.executeCommand(command);
        List<CnATreeElement> elements = command.getElements();
        assertEquals(1, elements.size());
        assertEquals(requirement, elements.get(0));

    }

    private CatalogModel loadCatalogModel() {
        return (CatalogModel) elementDao
                .findByCriteria(DetachedCriteria.forClass(CatalogModel.class)).get(0);
    }
}
