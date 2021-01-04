package sernet.verinice.service.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

@TransactionConfiguration(transactionManager = "txManager")
@Transactional
public class NaturalizeCommandTest extends AbstractModernizedBaseProtection {

    @Test
    public void naturalizeResetsSourceAndExtId() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();
        itNetwork.setSourceId("foo");
        itNetwork.setExtId("bar");
        itNetwork = update(itNetwork);

        NaturalizeCommand command = new NaturalizeCommand(
                Collections.singleton(itNetwork.getUuid()));
        command = commandService.executeCommand(command);
        List<CnATreeElement> elements = command.getChangedElements();
        assertEquals(1, elements.size());
        CnATreeElement firstElement = elements.get(0);
        assertNull(firstElement.getSourceId());
        assertNull(firstElement.getExtId());
        itNetwork = reloadElement(itNetwork);
        assertNull(itNetwork.getSourceId());
        assertNull(itNetwork.getExtId());
    }

}
