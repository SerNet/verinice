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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.OriginType;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.ProfileRef;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.model.auth.Userprofiles;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SaveConfiguration;
import sernet.verinice.service.commands.UpdateElement;

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

    private static final String RIGHTMANAGEMENT_TEST_PROFILE = "rightmanagement-test-profile";
    private static final String TEST_USER_LOGIN = "testUser-login";
    private static final String DEFAULT_USER_LOGIN = "defaultUser-login";
    private static final String USER_DEFAULT_GROUP = "user-default-group";
    
    // some example actions
    private static final String[] noDeleteProfileActionIds = { ActionRightIDs.IMPORTCSV, //
            ActionRightIDs.IMPORTLDAP, ActionRightIDs.ISMCATALOG, ActionRightIDs.XMLIMPORT };

    @Resource(name = "rightsService")
    protected IRightsService rightsService;
    @Resource(name = "authService")
    protected IAuthService authService;

    /**
     * Ensure that the interalAdmin is set. Clear the profiles.
     */
    @After
    public void tearDown() {
        if (authService instanceof TestAuthenticationService) {
            ((TestAuthenticationService) authService).setUsername(TestAuthenticationService.INTERNAL_ADMIN);
        }

        Auth conf = rightsService.getConfiguration();
        Profiles profiles = conf.getProfiles();
        Iterator<Profile> iterator = profiles.getProfile().iterator();
        for (Profile p; iterator.hasNext();) {
            p = iterator.next();
            if (p.getName().equals(RIGHTMANAGEMENT_TEST_PROFILE))
                iterator.remove();
        }
        Userprofiles userprofiles = conf.getUserprofiles();
        Iterator<Userprofile> iterator2 = userprofiles.getUserprofile().iterator();
        for (Userprofile up; iterator.hasNext();) {
            up = iterator2.next();
            if (up.getLogin().equals(TEST_USER_LOGIN))
                iterator2.remove();
        }

        rightsService.updateConfiguration(conf);
    }

    /**
     * Test if the user is not allowed to remove an element.
     * 
     * @throws CommandException
     */
    @Transactional
    @Rollback(true)
    @Test(expected = CommandException.class)
    public void testUserNotAllowedToDelete() throws CommandException {
        Organization organization = createOrganization();
        PersonIso person = createIsoPerson(organization);
        createUserConfiguration(TEST_USER_LOGIN, person, true);
        createCustomConfigurationProfile(TEST_USER_LOGIN, RIGHTMANAGEMENT_TEST_PROFILE, noDeleteProfileActionIds);

        if (authService instanceof TestAuthenticationService) {
            ((TestAuthenticationService) authService).setUsername(TEST_USER_LOGIN);
        }
        assertEquals("The configured user need to be the testuser, setUpWrong.", TEST_USER_LOGIN, authService.getUsername());

        List<Userprofile> userprofiles = rightsService.getUserprofile(TEST_USER_LOGIN);
        assertEquals("Only one Profile need to be active, setUpWrong.", 1, userprofiles.size());

        Userprofile userprofile = userprofiles.get(0);
        List<ProfileRef> profileRefs = userprofile.getProfileRef();
        assertEquals("Only one ProfileReference need to be active, setUpWrong.", 1, profileRefs.size());
        ProfileRef profileRef = profileRefs.get(0);
        assertEquals("Not the right profile active, setUpWrong.", RIGHTMANAGEMENT_TEST_PROFILE, profileRef.getName());

        RemoveElement<Organization> removeElement = new RemoveElement<>(organization);
        commandService.executeCommand(removeElement);
    }

    /**
     * Test if the user is allowed to remove an element via given ActionRight.
     * 
     * @throws CommandException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testUserAllowedToDelete() throws CommandException {
        Organization organization = createOrganization();
        PersonIso person = createIsoPerson(organization);
        createUserConfiguration(TEST_USER_LOGIN, person, true);
        createCustomConfigurationProfile(TEST_USER_LOGIN, RIGHTMANAGEMENT_TEST_PROFILE, ActionRightIDs.DELETEITEM);

        if (authService instanceof TestAuthenticationService) {
            ((TestAuthenticationService) authService).setUsername(TEST_USER_LOGIN);
        }
        assertEquals("The configured user need to be the testuser, setUpWrong.", TEST_USER_LOGIN, authService.getUsername());

        List<Userprofile> userprofiles = rightsService.getUserprofile(TEST_USER_LOGIN);
        assertEquals("Only one Profile need to be active, setUpWrong.", 1, userprofiles.size());

        Userprofile userprofile = userprofiles.get(0);
        List<ProfileRef> profileRefs = userprofile.getProfileRef();
        assertEquals("Only one ProfileReference need to be active, setUpWrong.", 1, profileRefs.size());
        ProfileRef profileRef = profileRefs.get(0);
        assertEquals("Not the right profile active, setUpWrong.", RIGHTMANAGEMENT_TEST_PROFILE, profileRef.getName());

        RemoveElement<Organization> removeElement = new RemoveElement<>(organization);
        commandService.executeCommand(removeElement);

        satisfyRemoveOperation(removeElement, organization);
    }

    /**
     * Test that a default user, part of the default user group can delete an
     * element.
     * 
     * @throws CommandException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultUserAllowedToDelete() throws CommandException {
        Organization organization = createOrganization();
        PersonIso personIso = createIsoPerson(organization);
        createUserConfiguration(DEFAULT_USER_LOGIN, personIso, false);

        if (authService instanceof TestAuthenticationService) {
            ((TestAuthenticationService) authService).setUsername(DEFAULT_USER_LOGIN);
        }
        assertEquals("The configured user need to be the testuser, setUpWrong.", DEFAULT_USER_LOGIN, authService.getUsername());

        List<Userprofile> userprofiles = rightsService.getUserprofile(DEFAULT_USER_LOGIN);
        assertEquals("One Profiles need to be active, setUpWrong.", 1, userprofiles.size());

        Userprofile userprofile = userprofiles.get(0);
        List<ProfileRef> profileRefs = userprofile.getProfileRef();
        assertEquals("Only one ProfileReference need to be active, setUpWrong.", 1, profileRefs.size());
        ProfileRef profileRef = profileRefs.get(0);
        assertEquals("Not the right profile active, setUpWrong.", "user-default-profile", profileRef.getName());

        RemoveElement<Organization> removeElement = new RemoveElement<>(organization);
        commandService.executeCommand(removeElement);

        satisfyRemoveOperation(removeElement, organization);
    }

    /**
     * Test if the internal admin is allowed to remove an element.
     * 
     * @throws CommandException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testAdminUserAllowedToDelete() throws CommandException {
        Organization organization = createOrganization();

        assertEquals("The configured user need to be the adminuser, setUpWrong.", TestAuthenticationService.INTERNAL_ADMIN, authService.getUsername());

        RemoveElement<Organization> removeElement = new RemoveElement<>(organization);
        commandService.executeCommand(removeElement);

        satisfyRemoveOperation(removeElement, organization);
    }

    /**
     * Creates a custom profile configuration.
     * 
     * @param userLogin
     *            the login the profile is linked to
     * @param profileName
     *            the name of the profile
     * @param actionList
     *            an array of action set to the profile
     * 
     */
    protected void createCustomConfigurationProfile(String userLogin, String profileName, String... actionList) {
        Auth conf = rightsService.getConfiguration();
        List<Profile> profileList = conf.getProfiles().getProfile();
        Profile profile = new Profile();
        profile.setName(profileName);
        profile.setOrigin(OriginType.MODIFICATION);
        for (String actionId : actionList) {
            Action action = new Action();
            action.setId(actionId);
            profile.getAction().add(action);
        }

        profileList.add(profile);
        Userprofile userProfile = new Userprofile();
        userProfile.setLogin(userLogin);
        userProfile.setOrigin(OriginType.MODIFICATION);
        ProfileRef newProfileRef = new ProfileRef();
        newProfileRef.setName(profileName);
        userProfile.getProfileRef().add(newProfileRef);
        conf.getUserprofiles().getUserprofile().add(userProfile);
        rightsService.updateConfiguration(conf);
    }

    /**
     * Create default a user configuration.
     * 
     * @param userLogIn
     *            the login for the user
     * @param personIso
     *            the person object to create the configuration for
     * @param deleteUserDefaultGroup should the default group removed from the configuration
     * @return the created configuration should the default-usergroup removed
     *         for the user
     */
    protected Configuration createUserConfiguration(String userLogIn, PersonIso personIso, boolean deleteUserDefaultGroup) {
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
        return saveConfiguration.getElement();
    }

    /**
     * Create an IsoPerson in the organization.
     * 
     * @throws CommandException
     */
    protected PersonIso createIsoPerson(Organization organization) throws CommandException {
        CnATreeElement group = organization.getGroup(PersonGroup.TYPE_ID);

        CreateElement<PersonIso> createElement = new CreateElement<>(group, PersonIso.class, "testPerson1");
        commandService.executeCommand(createElement);
        PersonIso personIso = createElement.getNewElement();
        personIso.setName("TestPersonName");
        commandService.executeCommand(new UpdateElement<PersonIso>(personIso, true, null));
        return personIso;
    }

    /**
     * Assert the removeElement operation. Checks if the object is really
     * removed from the db.
     * 
     * @param removeElement
     */
    private void satisfyRemoveOperation(RemoveElement<Organization> removeElement, CnATreeElement element) {
        String uuid = element.getUuid();
//        assertNull("Entity need to be null, as it was deleted.", element.getEntity());
// TODO : enable this assert when deletion of the entity works again
        assertEquals("Only one element should be changed.", 1, removeElement.getChangedElements().size());
        assertNull("Changed Element should be null, as it was deleted from the database.", removeElement.getChangedElements().get(0));

        LoadElementByUuid<CnATreeElement> loadElementByUuid = null;
        try {
            loadElementByUuid = new LoadElementByUuid<>(element.getTypeId(), uuid);
            commandService.executeCommand(loadElementByUuid);
        } catch (Exception e) {
            assertTrue(e.getClass().equals(CommandException.class));
        }
        assertNull("Loaded element should be null, as it was deleted.", loadElementByUuid.getElement());
    }

}
