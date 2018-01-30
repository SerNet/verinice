/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.IIndividualService;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SaveConfiguration;
import sernet.verinice.service.commands.crud.PrepareObjectWithAccountDataForDeletion;

/**
 * Test the creation and execution of individual processes.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskServiceTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(TaskServiceTest.class);

    private static final int NUMBER_OF_ASSETS = 10;

    @Resource(name = "taskService")
    ITaskService taskService;

    @Resource(name = "individualService")
    IIndividualService individualService;

    @Resource(name = "huiTypeFactory")
    private HUITypeFactory huiTypeFactory;

    private Organization organization;
    private List<String> assetUuidList;
    private List<String> uuidList;
    
    private Calendar startTime;

    @Before
    public void setUp() throws Exception {
        startTime = Calendar.getInstance();
        // create organization
        uuidList = new LinkedList<String>();
        organization = createTestOrganization();
        linkElements(organization);
        assetUuidList = createProcesses(organization);
    }

    @After
    public void tearDown() throws CommandException {
        // remove tasks
        removeTasks();
        // remove
        PrepareObjectWithAccountDataForDeletion removeAccount = new PrepareObjectWithAccountDataForDeletion(organization);
        commandService.executeCommand(removeAccount);
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(organization);
        commandService.executeCommand(removeCommand);
        for (String uuid : uuidList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid);
            command = commandService.executeCommand(command);
            CnATreeElement element = command.getElement();
            assertNull("Organization was not deleted.", element);
        }
    }

    @Test
    public void testSearchWithoutPerson() {
        String taskType = IIndividualProcess.TASK_EXECUTE;
        List<ITask> taskList = getTaskList();
        assertEquals("Size of task list is not " + NUMBER_OF_ASSETS, NUMBER_OF_ASSETS, taskList.size());
        List<String> taskIdList = new LinkedList<String>();
        for (String assetUuid : assetUuidList) {
            boolean found = false;
            for (ITask task : taskList) {
                if (assetUuid.equals(task.getUuid())) {
                    if (taskType != null) {
                        assertTrue("Wrong task for element, uuid: " + assetUuid + ", type: " + task.getType(), taskType.equals(task.getType()));
                    }
                    found = true;
                    taskIdList.add(task.getId());
                    break;
                }
            }
            assertTrue("Task for asset not found, uuid: " + assetUuid, found);
        }
    }

    @Test
    public void testSearchPerson() {
        List<ITask> taskList = getTaskListForPerson("person1");
        assertEquals("Size of task list is not " + NUMBER_OF_ASSETS / 2, NUMBER_OF_ASSETS / 2, taskList.size());
        taskList = getTaskListForPerson("person2");
        assertEquals("Size of task list is not " + NUMBER_OF_ASSETS / 2, NUMBER_OF_ASSETS / 2, taskList.size());
    }
    
    @Test
    public void testMarkAsRead() {
        ITaskParameter searchParameter = new TaskParameter();
        searchParameter.setProcessKey(IIndividualProcess.KEY);
        searchParameter.setAllUser(true);
        searchParameter.setRead(false);
        List<ITask> taskList = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not " + NUMBER_OF_ASSETS, NUMBER_OF_ASSETS, taskList.size());

        searchParameter.setRead(true);
        searchParameter.setUnread(false);
        List<ITask> taskListRead = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not 0", 0, taskListRead.size());
        
        taskService.markAsRead(taskList.get(0).getId());
        taskService.markAsRead(taskList.get(1).getId());
        taskService.markAsRead(taskList.get(2).getId());
        
        searchParameter.setRead(false);
        searchParameter.setUnread(true);
        taskList = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not " + (NUMBER_OF_ASSETS-3), NUMBER_OF_ASSETS-3, taskList.size());
        
        searchParameter.setRead(true);
        searchParameter.setUnread(false);
        taskListRead = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not 3", 3, taskListRead.size());
    }
    
    @Test
    public void testSearchGetSince() {
        Date afterSetUp = Calendar.getInstance().getTime();
        ITaskParameter searchParameter = new TaskParameter();
        searchParameter.setAllUser(true);
        searchParameter.setProcessKey(IIndividualProcess.KEY);
        startTime.add(Calendar.DATE, -1);
        searchParameter.setSince(startTime.getTime());
        List<ITask> taskList = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not " + NUMBER_OF_ASSETS, NUMBER_OF_ASSETS, taskList.size());
        // create another process
        IndividualServiceParameter parameter = createParameter();
        parameter.setUuid(organization.getUuid());
        individualService.startProcess(parameter);
        searchParameter.setSince(afterSetUp);
        taskList = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not " + 1, 1, taskList.size());       
    }
    
    @Test
    public void testSearchTaskType() {
        ITaskParameter searchParameter = new TaskParameter();
        searchParameter.setProcessKey(IIndividualProcess.KEY);
        searchParameter.setTaskId(IIndividualProcess.TASK_EXECUTE);
        searchParameter.setAllUser(true);
        List<ITask> taskList = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not " + NUMBER_OF_ASSETS, NUMBER_OF_ASSETS, taskList.size());

        searchParameter.setTaskId(IIndividualProcess.TASK_CHECK);
        List<ITask> taskListCheck = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not 0", 0, taskListCheck.size());
        
        taskService.completeTask(taskList.get(0).getId());
        taskService.completeTask(taskList.get(1).getId());  
        
        taskListCheck = taskService.getTaskList(searchParameter);
        assertEquals("Size of task list is not 2", 2, taskListCheck.size());
    }

    private List<ITask> getTaskListForPerson(String login) {
        ITaskParameter searchParameter = new TaskParameter();
        searchParameter.setUsername(login);
        return taskService.getTaskList(searchParameter);        
    }

    private void removeTasks() {
        List<ITask> taskList = getTaskList();
        for (ITask task : taskList) {
            taskService.cancelTask(task.getId());
        }
        taskList = getTaskList();
        assertEquals("Size of task list is not 0", 0, taskList.size());
    }

    private List<ITask> getTaskList() {
        ITaskParameter searchParameter = new TaskParameter();
        searchParameter.setProcessKey(IIndividualProcess.KEY);
        searchParameter.setAllUser(true);
        return taskService.getTaskList(searchParameter);
    }

    private void completeTasks(List<String> taskIdList) {
        for (String taskId : taskIdList) {
            taskService.completeTask(taskId);
        }

    }

    protected List<String> createProcesses(Organization organization) {
        IndividualServiceParameter parameter = createParameter();
        List<String> assetUuidList = new LinkedList<String>();
        Group<CnATreeElement> assetGroup = getGroupForClass(organization, Asset.class);
        Set<CnATreeElement> assetSet = assetGroup.getChildren();
        for (CnATreeElement asset : assetSet) {
            parameter.setUuid(asset.getUuid());
            individualService.startProcess(parameter);
            assetUuidList.add(asset.getUuid());
        }
        return assetUuidList;
    }

    private IndividualServiceParameter createParameter() {
        IndividualServiceParameter parameter = new IndividualServiceParameter();
        parameter.setAssigneeRelationId(Asset.REL_ASSET_PERSON_RESPO);
        String relationTitle = null;
        EntityType entityType = huiTypeFactory.getEntityType(Asset.TYPE_ID);
        Set<HuiRelation> personRelations = entityType.getPossibleRelations(PersonIso.TYPE_ID);
        for (HuiRelation huiRelation : personRelations) {
            if (huiRelation.getId().equals(Asset.REL_ASSET_PERSON_RESPO)) {
                relationTitle = huiRelation.getName();
            }
        }
        parameter.setAssigneeRelationName(relationTitle);
        parameter.setDescription(this.getClass().getName());
        parameter.setTitle(this.getClass().getSimpleName());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 14);
        parameter.setDueDate(cal.getTime());
        parameter.setReminderPeriodDays(7);
        Set<String> typeIds = new HashSet<String>();
        Set<String> typeNames = new HashSet<String>();
        for (PropertyType type : entityType.getAllPropertyTypes()) {
            typeIds.add(type.getId());
            typeNames.add(type.getName());
        }
        parameter.setProperties(typeIds);
        parameter.setPropertyNames(typeNames);
        parameter.setTypeId(Asset.TYPE_ID);
        return parameter;
    }

    private void linkElements(Organization organization) throws CommandException {
        Group<CnATreeElement> personGroup = getGroupForClass(organization, PersonIso.class);
        Set<CnATreeElement> personSet = personGroup.getChildren();
        assertTrue("Number of persons is not 2.", personSet.size() == 2);
        Iterator<CnATreeElement> personsIterator = personSet.iterator();
        CnATreeElement person1 = personsIterator.next();
        CnATreeElement person2 = personsIterator.next();

        Group<CnATreeElement> assetGroup = getGroupForClass(organization, Asset.class);
        Set<CnATreeElement> assetSet = assetGroup.getChildren();
        CnATreeElement currentPerson = person1;
        for (CnATreeElement asset : assetSet) {
            createLink(asset, currentPerson, Asset.REL_ASSET_PERSON_RESPO);
            currentPerson = (currentPerson.equals(person1)) ? person2 : person1;
        }
    }

    private Organization createTestOrganization() throws CommandException {
        Organization organization = createOrganization();
        uuidList.add(organization.getUuid());
        String uuidPerson1 = createInOrganisation(organization, PersonIso.class, 1).iterator().next();
        uuidList.add(uuidPerson1);
        String uuidPerson2 = createInOrganisation(organization, PersonIso.class, 1).iterator().next();
        uuidList.add(uuidPerson2);
        uuidList.addAll(createInOrganisation(organization, Asset.class, NUMBER_OF_ASSETS));

        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuidPerson1);
        command = commandService.executeCommand(command);
        CnATreeElement person1 = command.getElement();
        createAccount(person1, "person1");

        command = new LoadElementByUuid<CnATreeElement>(uuidPerson2);
        command = commandService.executeCommand(command);
        CnATreeElement person2 = command.getElement();
        createAccount(person2, "person2");

        return organization;
    }

    private void createAccount(CnATreeElement person1, String name) throws CommandException {
        CreateConfiguration createConfiguration = new CreateConfiguration(person1);
        createConfiguration = commandService.executeCommand(createConfiguration);
        Configuration configuration = createConfiguration.getConfiguration();
        configuration.setUser(name);
        SaveConfiguration<Configuration> saveConfigurationCommand = new SaveConfiguration<Configuration>(configuration, false);
        saveConfigurationCommand = commandService.executeCommand(saveConfigurationCommand);
    }

}
