package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.TestExecutionListeners;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.crudcommands.PrepareObjectWithAccountDataForDeletion;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElementEntity;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.AccountSearchParameter;
import sernet.verinice.service.AccountSearchParameterFactory;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SaveConfiguration;

public class AccountServiceTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(AccountServiceTest.class);
    
    @Resource(name="accountService")
    private IAccountService accountService;
    
    private Organization organization;
    private List<String> uuidList;
    
    @Test
    public void testFindByLogin() throws Exception {
        List<Configuration> configurations = accountService.findAccounts(AccountSearchParameterFactory.createLoginParameter(getLoginName()));
        assertNumber(configurations, 1);
        Configuration account = configurations.get(0);
        assertEquals("Account login is not: " + getLoginName(), getLoginName(), account.getUser());      
    }
 
    @Test
    public void testFindByFirstName() throws Exception {
        List<Configuration> configurations = accountService.findAccounts(AccountSearchParameterFactory.createFirstNameParameter(getFirstName()));
        assertNumber(configurations, 1);
        Configuration account = configurations.get(0);
        PersonIso person = (PersonIso) account.getPerson();
        assertEquals("First name of person is not: " + getFirstName(), getFirstName(), person.getName());        
    }
    
    @Test
    public void testFindByFamilyName() throws Exception {
        List<Configuration> configurations = accountService.findAccounts(AccountSearchParameterFactory.createFamilyNameParameter(getFamilyName()));
        assertNumber(configurations, 1);
        Configuration account = configurations.get(0);
        PersonIso person = (PersonIso) account.getPerson();
        assertEquals("Family name of person is not: " + getFamilyName(), getFamilyName(), person.getSurname());
    }
    
    @Test
    public void testFindByIsAdmin() throws Exception {
        List<Configuration> configurations = accountService.findAccounts(AccountSearchParameterFactory.createIsAdminParameter(true));
        assertNumber(configurations, 1);
        Configuration account = configurations.get(0);
        assertEquals("Account is not admin account", true, account.isAdminUser()); 
        
        configurations = accountService.findAccounts(AccountSearchParameterFactory.createIsAdminParameter(false));
        assertNumber(configurations, 2);
        for (Configuration configuration : configurations) {
            assertEquals("Account is admin account", false, configuration.isAdminUser());
        }
    }
    
    private void assertNumber(List<Configuration> configurations, int expectedNumber) {
        assertNotNull("Search result is null",  configurations);
        assertEquals("Number of account is not " + expectedNumber, expectedNumber, configurations.size());
    }
    
    @Before
    public void setUp() throws Exception {
        uuidList = new LinkedList<String>();
        organization = createTestOrganization();
    }

    
    @After
    public void tearDown() throws CommandException {
        removeTestOrganization(organization); 
    }
   
    private Organization createTestOrganization() throws CommandException {
        Organization organization = createOrganization();
        uuidList.add(organization.getUuid());  
        uuidList.addAll(createInOrganisation(organization,PersonIso.class,3));
        uuidList.addAll(createInOrganisation(organization,Asset.class,10));
        
        Group<CnATreeElement> personGroup = getGroupForClass(organization, PersonIso.class);
        personGroup = (Group<CnATreeElement>) retrieveChildren(personGroup);
        Iterator<CnATreeElement> personIterator = personGroup.getChildren().iterator();
        PersonIso person = (PersonIso) personIterator.next();
        person.setName(getFirstName());
        saveElement(person);
        Configuration configuration = createAccount(person);
        configuration.setUser(getLoginName());
        configuration.setAdminUser(false);
        saveAccount(configuration);
        
        person = (PersonIso) personIterator.next();
        person.setSurname(getFamilyName());
        saveElement(person);
        configuration = createAccount(person);
        configuration.setUser(getRandomLoginName());
        configuration.setAdminUser(false);
        saveAccount(configuration);
        
        person = (PersonIso) personIterator.next();
        configuration = createAccount(person);
        configuration.setUser(getRandomLoginName());
        configuration.setAdminUser(true);
        saveAccount(configuration);

        return organization;      
    }

    private CnATreeElement retrieveChildren(Group<CnATreeElement> personGroup) throws CommandException {
        RetrieveInfo ri = RetrieveInfo.getChildrenInstance().setChildrenProperties(true);
        LoadElementByUuid<CnATreeElement> retrieveCommand = new LoadElementByUuid<CnATreeElement>(personGroup.getUuid(), ri);
        retrieveCommand = commandService.executeCommand(retrieveCommand);
        return retrieveCommand.getElement();
    }

    private void saveElement(CnATreeElement element) throws CommandException {
        UpdateElementEntity<CnATreeElement> updateCommand;
        updateCommand = new UpdateElementEntity<CnATreeElement>(element, ChangeLogEntry.STATION_ID);
        updateCommand = commandService.executeCommand(updateCommand);
    }

    private Configuration createAccount(PersonIso person) throws CommandException {
        CreateConfiguration createConfiguration = new CreateConfiguration(person);
        createConfiguration = commandService.executeCommand(createConfiguration);
        Configuration configuration = createConfiguration.getConfiguration();
        return configuration;
    }
    
    private void saveAccount(Configuration configuration) throws CommandException {
        SaveConfiguration<Configuration> command = new SaveConfiguration<Configuration>(configuration, false);         
        command = commandService.executeCommand(command);
    }

    private String getLoginName() {
        return this.getClass().getSimpleName();
    }
    
    private String getRandomLoginName() {
        return this.getClass().getSimpleName() + UUID.randomUUID().toString();
    }
    
    private String getFirstName() {
        return "Foo";
    }
    
    private String getFamilyName() {
        return "Bar";
    }
    
    private void removeTestOrganization(Organization organization) throws CommandException {
        PrepareObjectWithAccountDataForDeletion removeAccount = new PrepareObjectWithAccountDataForDeletion(organization);
        commandService.executeCommand(removeAccount);
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(organization);
        commandService.executeCommand(removeCommand);
        for (String uuid: uuidList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid);
            command = commandService.executeCommand(command);
            CnATreeElement element = command.getElement();
            assertNull("Organization was not deleted.", element);
        }
    }
    
}
