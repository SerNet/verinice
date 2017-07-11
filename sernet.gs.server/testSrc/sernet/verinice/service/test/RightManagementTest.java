/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uzeidler<at>sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.ProfileRef;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.HibernateCommandService;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.LoadElementByTypeId;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SaveConfiguration;
import sernet.verinice.service.commands.UpdateElement;
import sernet.verinice.service.model.HUIObjectModelService;

/**
 * Test for rightmanagement in combination with the commandservice.
 * 
 * @author urszeidler
 *
 */
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class RightManagementTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(CommandServiceProvider.class);

    // need to correspond to the profile name defined in verinice-auth-test.xml
    private static final String RIGHTMANAGEMENT_TEST_PROFILE = "rightmanagement-test-profile";
    private static final String TEST_USER_LOGIN = "testUser-login";
    private static final String DEFAULT_USER_LOGIN = "defaultUser-login";
    private static final String USER_DEFAULT_GROUP = "user-default-group";

    @Resource(name = "rightsService")
    protected IRightsService rightService;
    @Resource(name = "authService")
    protected IAuthService authService;
    // internal states
    private Organization organization;

    /**
     * Ensure that the interalAdmin is set.
     */
    @After
    public void tearDown() {
        if (authService instanceof TestAuthenticationService) {
            ((TestAuthenticationService) authService).setUsername(TestAuthenticationService.INTERNAL_ADMIN);
        }
    }

    @Transactional
    @Rollback(true)
    @Test(expected = CommandException.class)
    public void testUserNotAllowedToDelete() throws CommandException {
        createOrganizationAndTestUser(TEST_USER_LOGIN, true);

        if (authService instanceof TestAuthenticationService) {
            ((TestAuthenticationService) authService).setUsername(TEST_USER_LOGIN);
        }
        assertEquals("The configured user need to be the testuser, setUpWrong.", TEST_USER_LOGIN, authService.getUsername());

        List<Userprofile> userprofiles = rightService.getUserprofile(TEST_USER_LOGIN);
        assertEquals("Only one Profile need to be active, setUpWrong.", 1, userprofiles.size());

        Userprofile userprofile = userprofiles.get(0);
        List<ProfileRef> profileRefs = userprofile.getProfileRef();
        assertEquals("Only one ProfileReference need to be active, setUpWrong.", 1, profileRefs.size());
        ProfileRef profileRef = profileRefs.get(0);
        assertEquals("Not the right profile active, setUpWrong.", RIGHTMANAGEMENT_TEST_PROFILE, profileRef.getName());

        RemoveElement<Organization> removeElement = new RemoveElement<>(organization);
        commandService.executeCommand(removeElement);
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultUserAllowedToDelete() throws CommandException {
        createOrganizationAndTestUser(DEFAULT_USER_LOGIN, false);

        if (authService instanceof TestAuthenticationService) {
            ((TestAuthenticationService) authService).setUsername(DEFAULT_USER_LOGIN);
        }
        assertEquals("The configured user need to be the testuser, setUpWrong.", DEFAULT_USER_LOGIN, authService.getUsername());

        List<Userprofile> userprofiles = rightService.getUserprofile(DEFAULT_USER_LOGIN);
        assertEquals("One Profiles need to be active, setUpWrong.", 1, userprofiles.size());

        Userprofile userprofile = userprofiles.get(0);
        List<ProfileRef> profileRefs = userprofile.getProfileRef();
        assertEquals("Only one ProfileReference need to be active, setUpWrong.", 1, profileRefs.size());
        ProfileRef profileRef = profileRefs.get(0);
        assertEquals("Not the right profile active, setUpWrong.", "user-default-profile", profileRef.getName());

        RemoveElement<Organization> removeElement = new RemoveElement<>(organization);
        commandService.executeCommand(removeElement);

        satisfyRemoveOperation(removeElement);
//        
//        assertNull("Entity need to be null, as it was deleted.", organization.getEntity());
//        assertEquals("Only one element should be changed.", 1, removeElement.getChangedElements().size());
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testAdminUserAllowedToDelete() throws CommandException {
        createOrganizationAndTestUser(TEST_USER_LOGIN, false);

        assertEquals("The configured user need to be the adminuser, setUpWrong.", TestAuthenticationService.INTERNAL_ADMIN, authService.getUsername());

        RemoveElement<Organization> removeElement = new RemoveElement<>(organization);
        commandService.executeCommand(removeElement);

        satisfyRemoveOperation(removeElement);
    }

    /**
     * Assert the removeElement operation. Checks if the object is really
     * removed from the db.
     * 
     * @param removeElement
     */
    public void satisfyRemoveOperation(RemoveElement<Organization> removeElement) {
        String uuid = organization.getUuid();
        assertNull("Entity need to be null, as it was deleted.", organization.getEntity());
        assertEquals("Only one element should be changed.", 1, removeElement.getChangedElements().size());
        assertNull("Changed Element should be null, as it was deleted from the database.", removeElement.getChangedElements().get(0));

        LoadElementByUuid<CnATreeElement> loadElementByUuid = null;
        try {
            loadElementByUuid = new LoadElementByUuid<>(organization.getTypeId(), uuid);
            commandService.executeCommand(loadElementByUuid);
            fail("Loading a deleted object should throw an exception.");
        } catch (Exception e) {
            assertTrue(e.getClass().equals(CommandException.class));
        }
        assertNull("Loaded element should be null, as it was deleted.", loadElementByUuid.getElement());
    }

    /**
     * Create an organization and a user the test user in this organization.
     * 
     * @param userLogIn
     *            the login for the user
     * @param deleteUserDefaultGroup
     *            should the default user group deleted
     * 
     * @throws CommandException
     */
    private void createOrganizationAndTestUser(String userLogIn, boolean deleteUserDefaultGroup) throws CommandException {
        organization = createOrganization();
        CnATreeElement group = organization.getGroup(PersonGroup.TYPE_ID);

        CreateElement<PersonIso> createElement = new CreateElement<>(group, PersonIso.class, "testPerson1");
        commandService.executeCommand(createElement);
        PersonIso personIso = createElement.getNewElement();
        personIso.setName("TestPersonName");
        commandService.executeCommand(new UpdateElement<PersonIso>(personIso, true, null));

        CreateConfiguration createConfiguration = new CreateConfiguration(personIso);
        try {
            createConfiguration = commandService.executeCommand(createConfiguration);
        } catch (CommandException e) {
            LOG.error("Error while creating configuration for user: " + personIso.getFullName(), e);
        }
        Configuration configuration = createConfiguration.getConfiguration();
        configuration.setUser(userLogIn);
        if (deleteUserDefaultGroup)
            configuration.deleteRole(USER_DEFAULT_GROUP);

        SaveConfiguration<Configuration> saveConfiguration = new SaveConfiguration<Configuration>(configuration, false);
        try {
            saveConfiguration = commandService.executeCommand(saveConfiguration);
        } catch (CommandException e) {
            LOG.error("Error while saving username in configuration for user: " + configuration.getUser(), e);
        }
    }

}
