package sernet.verinice.service.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.gs.service.SecurityException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.test.AbstractModernizedBaseProtection;

@TransactionConfiguration(transactionManager = "txManager")
@Transactional
public class NaturalizeCommandTest extends AbstractModernizedBaseProtection {

    private final String userName = "NaturalizeCommandTest-user";

    @Resource(name = "configurationDao")
    private IDao<Configuration, Serializable> configurationDao;

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

    @Test
    public void cannotNaturalizeReadOnlyElement() throws CommandException {
        Configuration conf = new Configuration();
        ItNetwork itNetwork = createNewBPOrganization();
        BpPersonGroup personGroup = createGroup(itNetwork, BpPersonGroup.class);
        BpPerson person = createElement(personGroup, BpPerson.class);
        conf.setPerson(person);
        conf.setUser(userName);
        conf.setScopeOnly(true);
        configurationDao.merge(conf);
        itNetwork.addPermission(Permission.createPermission(itNetwork, userName, true, false));
        itNetwork.setSourceId("foo");
        itNetwork.setExtId("bar");
        itNetwork = update(itNetwork);
        elementDao.flush();
        elementDao.clear();
        authService.setPermissionHandlingNeeded(true);
        authService.setUsername(userName);

        NaturalizeCommand command = new NaturalizeCommand(
                Collections.singleton(itNetwork.getUuid()));
        try {
            command = commandService.executeCommand(command);
            Assert.fail("An exception should have been thrown");
        } catch (CommandException e) {
            Throwable cause = e.getCause();
            Assert.assertThat(cause, CoreMatchers.instanceOf(SecurityException.class));
            Assert.assertThat(cause.getMessage(),
                    JUnitMatchers.containsString("Security check failed"));
        }

        itNetwork = reloadElement(itNetwork);
        assertEquals("foo", itNetwork.getSourceId());
        assertEquals("bar", itNetwork.getExtId());
    }

}
