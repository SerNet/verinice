package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.account.AccountSearchParameter;
import sernet.verinice.service.account.AccountSearchParameterFactory;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.SaveConfiguration;
import sernet.verinice.service.commands.UpdateElementEntity;
import sernet.verinice.service.test.helper.util.BFSTravers;
import sernet.verinice.service.test.helper.util.CnATreeTraverser;

@TransactionConfiguration(transactionManager = "txManager")
@Transactional
public class AccountServiceTest extends CommandServiceProvider {

    private static final String LOGIN_A = "login_a";
    private static final String LOGIN_B = "login_b";
    private static final String LOGIN_C = "login_c";
    private static final String LOGIN_D = "login_d";
    private static final String LOGIN_LOCAL_ADMIN_A = "login_local_a";
    private static final String LOGIN_LOCAL_ADMIN_B = "login_local_b";

    private static final String FIRST_NAME_A = "first_a";
    private static final String FIRST_NAME_B = "first_b";
    private static final String FIRST_NAME_LOCAL_ADMIN_A = "first_local_a";
    private static final String FIRST_NAME_LOCAL_ADMIN_B = "first_local_b";

    private static final String FAMILY_NAME_A = "family_a";
    private static final String FAMILY_NAME_B = "family_b";
    private static final String FAMILY_NAME_LOCAL_ADMIN_A = "family_local_a";
    private static final String FAMILY_NAME_LOCAL_ADMIN_B = "family_local_b";

    private static final String ACCOUNT_GROUP_A = "test_1";
    private static final String ACCOUNT_GROUP_B = "test_2";

    private AccountGroup accountGroupA = null;
    private AccountGroup accountGroupB = null;
    private AccountGroup accountGroupRandom = null;

    @Resource(name = "accountService")
    private IAccountService accountService;

    @Resource(name = "permissionDAO")
    private IBaseDao<Permission, Serializable> permissionDao;

    private Organization organization;
    private List<String> uuidList;
    private Set<String> configurationNames;

    @Before
    public void setUp() throws Exception {
        authService.setRoles(new String[] { ApplicationRoles.ROLE_ADMIN });
        uuidList = new LinkedList<>();
        configurationNames = new HashSet<>();
        organization = createTestOrganization();

        createAccountGroups();
    }

    @Transactional
    @Test
    public void testCreateGroup() {

        List<AccountGroup> accountGroups = accountService.listGroups();

        Assert.assertTrue(accountGroups.contains(accountGroupA));
        Assert.assertTrue(accountGroups.contains(accountGroupB));
    }

    @Transactional
    @Test
    public void testDeleteAccountGroup() {

        accountService.deleteAccountGroup(accountGroupA);
        accountService.deleteAccountGroup(accountGroupB);

        validateRemovedAccountGroups();
    }

    @Transactional
    @Test
    public void testDeleteAccountGroupByName() {

        accountService.deleteAccountGroup(accountGroupA.getName());
        accountService.deleteAccountGroup(accountGroupB.getName());

        validateRemovedAccountGroups();

    }

    private void validateRemovedAccountGroups() {
        List<AccountGroup> accountGroups = accountService.listGroups();

        Assert.assertFalse(accountGroups.contains(accountGroupA));
        Assert.assertFalse(accountGroups.contains(accountGroupB));
    }

    @Transactional
    @Test
    public void testAddRole() {
        addRoles();
        validateRoles();
    }

    private void addRoles() {
        accountService.addRole(configurationNames, IRightsService.USERDEFAULTGROUPNAME);
        accountService.addRole(configurationNames, IRightsService.USERSCOPEDEFAULTGROUPNAME);
        accountService.addRole(configurationNames, accountGroupRandom.getName());
    }

    private void validateRoles() {

        Set<String> accounts = accountService.listAccounts();

        for (String name : accounts) {
            if (configurationNames.contains(name)) {
                Configuration account = accountService.getAccountByName(name);
                Assert.assertTrue(
                        account.getRoles(false).contains(IRightsService.USERDEFAULTGROUPNAME));
                Assert.assertTrue(
                        account.getRoles(false).contains(IRightsService.USERSCOPEDEFAULTGROUPNAME));
                Assert.assertTrue(account.getRoles(false).contains(accountGroupRandom.getName()));
            }
        }

        List<String> accountGroupNames = accountService.listGroupNames();
        Assert.assertTrue(accountGroupNames.contains(accountGroupRandom.getName()));
    }

    @Transactional
    @Test
    public void testDeleteRole() {
        addRoles();
        deleteRoles();
        validateDeletedRoles();
    }

    private void deleteRoles() {
        accountService.deleteRole(configurationNames, accountGroupRandom.getName());
        accountService.deleteRole(configurationNames, IRightsService.USERSCOPEDEFAULTGROUPNAME);
    }

    private void validateDeletedRoles() {
        for (String name : accountService.listAccounts()) {
            if (configurationNames.contains(name)) {
                Configuration account = accountService.getAccountByName(name);
                Assert.assertFalse(account.getRoles(false).contains(accountGroupRandom.getName()));
                Assert.assertFalse(
                        account.getRoles(false).contains(IRightsService.USERSCOPEDEFAULTGROUPNAME));
            }
        }
    }

    @Transactional
    @Test
    public void countConnectObjectsForGroup() {
        addRoles();
        int setPermissions = setPermissions();
        validatePermissions(setPermissions);
    }

    private void validatePermissions(int setPermissions) {
        Assert.assertEquals(setPermissions,
                accountService.countConnectObjectsForGroup(accountGroupRandom.getName()));
    }

    private int setPermissions() {
        CnATreeTraverser cnATreeTraverser = new BFSTravers();
        SetPermissionsCallback setPermissionsCallback = new SetPermissionsCallback();
        cnATreeTraverser.traverse(organization, setPermissionsCallback);
        return setPermissionsCallback.getSetPermissions();
    }

    @Transactional
    @Test
    public void testListGroupNames() {

        List<String> accountGroupNames = accountService.listGroupNames();

        Assert.assertTrue(accountGroupNames.contains(accountGroupA.getName()));
        Assert.assertTrue(accountGroupNames.contains(accountGroupB.getName()));
        Assert.assertTrue(accountGroupNames.contains(accountGroupRandom.getName()));
    }

    @Transactional
    @Test
    public void testLocalAdmin() throws Exception {
        authService.setUsername(LOGIN_LOCAL_ADMIN_A);
        authService.setRoles(new String[] { ApplicationRoles.ROLE_LOCAL_ADMIN });
        authService.setPermissionHandlingNeeded(true);

        List<Configuration> configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createLoginParameter(getLoginName()));
        assertTrue("Account was found: " + getLoginName(), configurations.isEmpty());

        configurations = accountService.findAccounts(
                AccountSearchParameterFactory.createLoginParameter(LOGIN_LOCAL_ADMIN_A));
        assertTrue("Account was not found: " + LOGIN_LOCAL_ADMIN_A, configurations.size() == 1);
        assertTrue("Login was not: " + LOGIN_LOCAL_ADMIN_A,
                configurations.get(0).getUser().equals(LOGIN_LOCAL_ADMIN_A));

        configurations = accountService.findAccounts(
                AccountSearchParameterFactory.createLoginParameter(LOGIN_LOCAL_ADMIN_B));
        assertTrue("Account was found: " + LOGIN_LOCAL_ADMIN_B, configurations.isEmpty());

        authService.setRoles(new String[] { ApplicationRoles.ROLE_ADMIN });
        authService.setPermissionHandlingNeeded(false);
    }

    @Transactional
    @Test
    public void testFindByLogin() throws Exception {
        List<Configuration> configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createLoginParameter(getLoginName()));
        testIfNotEmpty(configurations);
        for (Configuration account : configurations) {
            assertNotNull("Account login is null", account.getUser());
            assertTrue("Account login does not contain: " + getLoginName(),
                    account.getUser().contains(getLoginName()));
        }
    }

    @Transactional
    @Test
    public void testFindByFirstName() throws Exception {
        List<Configuration> configurations = accountService.findAccounts(
                AccountSearchParameterFactory.createFirstNameParameter(getFirstName()));
        testIfNotEmpty(configurations);
        for (Configuration account : configurations) {
            PersonIso person = (PersonIso) account.getPerson();
            assertEquals("First name of person is not: " + getFirstName(), getFirstName(),
                    person.getName());
        }
    }

    @Transactional
    @Test
    public void testFindByFamilyName() throws Exception {
        List<Configuration> configurations = accountService.findAccounts(
                AccountSearchParameterFactory.createFamilyNameParameter(getFamilyName()));
        testIfNotEmpty(configurations);
        for (Configuration account : configurations) {
            PersonIso person = (PersonIso) account.getPerson();
            assertEquals("Family name of person is not: " + getFamilyName(), getFamilyName(),
                    person.getSurname());
        }
    }

    @Transactional
    @Test
    public void testFindByIsAdmin() throws Exception {
        List<Configuration> configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createIsAdminParameter(true));
        boolean testuserFound = false;
        for (Configuration configuration : configurations) {
            assertEquals("Account is not admin account", true, configuration.isAdminUser());
            if (configuration.getUser().startsWith(getLoginName())) {
                testuserFound = true;
            }
        }
        assertTrue("Testuser not found", testuserFound);
    }

    @Transactional
    @Test
    public void testFindByIsScopeOnly() throws Exception {
        List<Configuration> configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createIsScopeOnlyParameter(true));
        boolean testuserFound = false;
        for (Configuration configuration : configurations) {
            assertEquals("Account is not scope only account", true, configuration.isScopeOnly());
            if (configuration.getUser().equals(LOGIN_B)) {
                testuserFound = true;
            }
        }
        assertTrue("Testuser not found", testuserFound);
    }

    @Transactional
    @Test
    public void testFindByScopeId() throws Exception {
        List<Configuration> configurations = accountService.findAccounts(
                AccountSearchParameterFactory.createScopeParameter(organization.getDbId()));
        assertNumber(configurations, 9);
    }

    @Transactional
    @Test
    public void testFindByAll() throws Exception {
        IAccountSearchParameter parameter = AccountSearchParameterFactory
                .createFamilyNameParameter(FAMILY_NAME_B);
        parameter.setIsAdmin(true).setFirstName(FIRST_NAME_A).setLogin(LOGIN_A)
                .setScopeId(organization.getDbId());
        List<Configuration> configurations = accountService.findAccounts(parameter);
        assertNumber(configurations, 1);
        for (Configuration configuration : configurations) {
            assertEquals("Account is not admin account", true, configuration.isAdminUser());
            assertEquals("Account login is not: " + LOGIN_A, LOGIN_A, configuration.getUser());
            PersonIso person = (PersonIso) configuration.getPerson();
            assertEquals("Family name of person is not: " + FAMILY_NAME_B, FAMILY_NAME_B,
                    person.getSurname());
            assertEquals("First name of person is not: " + FIRST_NAME_A, FIRST_NAME_A,
                    person.getName());
            assertEquals("Scope id is not: " + organization.getDbId(), organization.getDbId(),
                    person.getScopeId());
        }
    }

    @Transactional
    @Test
    public void testFindByIsNotAdmin() throws Exception {
        List<Configuration> configurations2 = accountService
                .findAccounts(AccountSearchParameterFactory.createIsAdminParameter(false));
        for (Configuration configuration : configurations2) {
            assertEquals("Account is admin account", false, configuration.isAdminUser());

        }
    }

    @Transactional
    @Test
    public void testGetById() throws Exception {
        List<Configuration> configurations = accountService
                .findAccounts(new AccountSearchParameter());
        boolean userFound = false;
        for (Configuration configuration : configurations) {
            userFound = true;
            Integer dbId = configuration.getDbId();
            Configuration account = accountService.getAccountById(dbId);
            assertNotNull("Account is null, db-id: " + dbId, account);
            assertEquals("Db-id of account is not: " + dbId, dbId, account.getDbId());
        }
        assertTrue("No user found", userFound);
    }

    @Transactional
    @Test
    public void testRemove() throws Exception {
        List<Configuration> configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createLoginParameter(LOGIN_A));
        testIfNotEmpty(configurations);
        removeAccountsStartingWith(LOGIN_A);
    }

    @Transactional
    @Test
    public void testDisable() throws Exception {
        List<Configuration> configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createLoginParameter(LOGIN_A));
        testIfNotEmpty(configurations);
        for (Configuration account : configurations) {
            assertFalse("Account is disabled", account.isDeactivatedUser());
            accountService.deactivate(account);
        }
        configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createLoginParameter(LOGIN_A));
        testIfNotEmpty(configurations);
        for (Configuration account : configurations) {
            assertTrue("Account is not disabled", account.isDeactivatedUser());
        }
    }

    private void removeAccountsStartingWith(String login) {
        List<Configuration> configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createLoginParameter(login));
        for (Configuration account : configurations) {
            assertTrue("Account login is not: " + login, account.getUser().startsWith(login));
            accountService.delete(account);
        }
        configurations = accountService
                .findAccounts(AccountSearchParameterFactory.createLoginParameter(login));
        assertNotNull("Search result is null", configurations);
        assertTrue("Number of accounts is not 0", configurations.isEmpty());
    }

    private void testIfNotEmpty(List<Configuration> configurations) {
        assertNotNull("Search result is null", configurations);
        assertTrue("Number of accounts is 0", !configurations.isEmpty());
    }

    private void assertNumber(List<Configuration> configurations, int expectedNumber) {
        assertNotNull("Search result is null", configurations);
        assertEquals("Number of account is not " + expectedNumber, expectedNumber,
                configurations.size());
    }

    private Organization createTestOrganization() throws CommandException {
        Organization org = createOrganization();
        uuidList.add(org.getUuid());

        Group<CnATreeElement> personGroup = getGroupForClass(org, PersonIso.class);

        IAccountSearchParameter paramter = new AccountSearchParameter();
        paramter.setLogin(getLoginName()).setIsAdmin(false).setFirstName(getFirstName());
        createAccount(personGroup, paramter);

        paramter = new AccountSearchParameter();
        paramter.setLogin(getRandomLoginName()).setIsAdmin(false).setFamilyName(getFamilyName());
        createAccount(personGroup, paramter);

        paramter = new AccountSearchParameter();
        paramter.setLogin(getRandomLoginName()).setIsAdmin(true).setFamilyName(getFamilyName());
        createAccount(personGroup, paramter);

        paramter = new AccountSearchParameter();
        paramter.setLogin(LOGIN_A).setIsAdmin(true).setIsScopeOnly(false)
                .setFamilyName(FAMILY_NAME_B).setFirstName(FIRST_NAME_A);
        createAccount(personGroup, paramter);

        paramter = new AccountSearchParameter();
        paramter.setLogin(LOGIN_B).setIsAdmin(false).setIsScopeOnly(true)
                .setFamilyName(FAMILY_NAME_A).setFirstName(FIRST_NAME_A);
        createAccount(personGroup, paramter);

        paramter = new AccountSearchParameter();
        paramter.setLogin(LOGIN_C).setIsAdmin(true).setIsScopeOnly(false)
                .setFamilyName(FAMILY_NAME_B).setFirstName(FIRST_NAME_B);
        createAccount(personGroup, paramter);

        paramter = new AccountSearchParameter();
        paramter.setLogin(LOGIN_D).setIsAdmin(false).setIsScopeOnly(false)
                .setFamilyName(FAMILY_NAME_A).setFirstName(FIRST_NAME_B);
        createAccount(personGroup, paramter);

        paramter = new AccountSearchParameter();
        paramter.setLogin(LOGIN_LOCAL_ADMIN_A).setIsLocalAdmin(true).setIsAdmin(false)
                .setIsScopeOnly(false).setFamilyName(FAMILY_NAME_LOCAL_ADMIN_A)
                .setFirstName(FIRST_NAME_LOCAL_ADMIN_A);
        Set<Permission> permissions = new HashSet<>();
        permissions.add(Permission.createPermission(null, LOGIN_LOCAL_ADMIN_A, true, true));
        permissions.add(Permission.createPermission(null, LOGIN_LOCAL_ADMIN_B, true, false));
        createAccount(personGroup, paramter, permissions);

        paramter = new AccountSearchParameter();
        paramter.setLogin(LOGIN_LOCAL_ADMIN_B).setIsLocalAdmin(true).setIsAdmin(false)
                .setIsScopeOnly(false).setFamilyName(FAMILY_NAME_LOCAL_ADMIN_B)
                .setFirstName(FIRST_NAME_LOCAL_ADMIN_B);
        permissions = new HashSet<>();
        permissions.add(Permission.createPermission(null, LOGIN_LOCAL_ADMIN_A, true, false));
        permissions.add(Permission.createPermission(null, LOGIN_LOCAL_ADMIN_B, true, true));
        createAccount(personGroup, paramter, permissions);

        return org;
    }

    private void createAccount(Group<CnATreeElement> personGroup, IAccountSearchParameter paramter)
            throws CommandException {
        createAccount(personGroup, paramter, Collections.emptySet());
    }

    private void createAccount(Group<CnATreeElement> personGroup, IAccountSearchParameter paramter,
            Set<Permission> permissions) throws CommandException {
        PersonIso person = (PersonIso) createNewElement(personGroup, PersonIso.class);
        uuidList.add(person.getUuid());
        person.setName(paramter.getFirstName());
        if (paramter.getFamilyName() != null) {
            person.setSurname(paramter.getFamilyName());
        }
        for (Permission permission : permissions) {
            permission.setCnaTreeElement(person);
            person.addPermission(permission);
        }
        saveElement(person);
        Configuration configuration = createAccount(person);
        configuration.setUser(paramter.getLogin());
        if (paramter.isAdmin() != null) {
            configuration.setAdminUser(paramter.isAdmin());
        }
        if (paramter.isScopeOnly() != null) {
            configuration.setScopeOnly(paramter.isScopeOnly());
        }
        saveAccount(configuration);
    }

    private void saveElement(CnATreeElement element) throws CommandException {
        UpdateElementEntity<CnATreeElement> updateCommand;
        updateCommand = new UpdateElementEntity<>(element, ChangeLogEntry.STATION_ID);
        commandService.executeCommand(updateCommand);
    }

    private Configuration createAccount(PersonIso person) throws CommandException {
        CreateConfiguration createConfiguration = new CreateConfiguration(person);
        createConfiguration = commandService.executeCommand(createConfiguration);
        return createConfiguration.getConfiguration();
    }

    private void saveAccount(Configuration configuration) throws CommandException {
        SaveConfiguration<Configuration> command = new SaveConfiguration<>(configuration, false);
        command = commandService.executeCommand(command);
        configurationNames.add(command.getElement().getUser());
    }

    private String getLoginName() {
        return this.getClass().getSimpleName();
    }

    private String getRandomLoginName() {
        return this.getClass().getSimpleName() + UUID.randomUUID().toString();
    }

    private String getFirstName() {
        return "Dagobert";
    }

    private String getFamilyName() {
        return "Duck";
    }

    private void createAccountGroups() {
        accountGroupA = accountService.createAccountGroup(ACCOUNT_GROUP_A);
        accountGroupB = accountService.createAccountGroup(ACCOUNT_GROUP_B);
        accountGroupRandom = accountService.createAccountGroup(UUID.randomUUID().toString());
    }

    public IBaseDao<Permission, Serializable> getPermissionDao() {
        return permissionDao;
    }

    public void setPermissionDao(IBaseDao<Permission, Serializable> permissionDao) {
        this.permissionDao = permissionDao;
    }

    private final class SetPermissionsCallback implements CnATreeTraverser.CallBack {

        private Integer setPermissions;

        private SetPermissionsCallback() {
            this.setPermissions = 0;
        }

        @Override
        public void execute(CnATreeElement v) {
            if (new Random().nextFloat() > 0.5f) {
                Permission p = Permission.createPermission(v, accountGroupRandom.getName(), true,
                        true);
                getPermissionDao().merge(p);
                setPermissions++;
            }
        }

        public int getSetPermissions() {
            return setPermissions;
        }
    }
}
