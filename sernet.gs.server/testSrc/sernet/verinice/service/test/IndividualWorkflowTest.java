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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
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
public class IndividualWorkflowTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(IndividualWorkflowTest.class);
    
    @Resource(name="individualService")
    IIndividualService individualService;
    
    @Resource(name="taskService")
    ITaskService taskService;
    
    @Resource(name="huiTypeFactory")
    private HUITypeFactory huiTypeFactory;
    
    private List<String> uuidList;
    
    @Test
    public void testIndividualService() throws Exception {
        // create organization
        uuidList = new LinkedList<String>();
        Organization organization = createTestOrganization();
        linkElements(organization);
        
        List<String> assetUuidList = createProcesses(organization);     
        List<String> taskIdList = checkTasks(assetUuidList, false, IIndividualProcess.TASK_EXECUTE);      
        completeTasks(taskIdList);   
        taskIdList = checkTasks(assetUuidList, false, IIndividualProcess.TASK_CHECK);    
        completeTasks(taskIdList);       
        checkTasks(assetUuidList, true, null);
        
        // remove
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

    private void completeTasks(List<String> taskIdList) {
        for (String taskId : taskIdList) {
            taskService.completeTask(taskId);
        }
        
    }

    protected List<String> checkTasks(List<String> assetUuidList, boolean invert, String taskType) {
        ITaskParameter searchParameter = new TaskParameter();
        searchParameter.setProcessKey(IIndividualProcess.KEY);
        searchParameter.setAllUser(true);
        List<ITask> taskList = taskService.getTaskList(searchParameter); 
        List<String> taskIdList = new LinkedList<String>();
        for (String assetUuid : assetUuidList) {
            boolean found = false;
            for (ITask task : taskList) {
                if(assetUuid.equals(task.getUuid())) {
                    if(taskType!=null) {
                        assertTrue("Wrong task for element, uuid: " + assetUuid + ", type: " + task.getType(), taskType.equals(task.getType()));
                    }
                    found = true;
                    taskIdList.add(task.getId());
                    break;
                }
            }
            if(invert) {
                assertTrue("Task for asset found, uuid: " + assetUuid, !found);  
            } else {
                assertTrue("Task for asset not found, uuid: " + assetUuid, found);
            }
        }
        return taskIdList;
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
            if(huiRelation.getId().equals(Asset.REL_ASSET_PERSON_RESPO)) {
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
       CnATreeElement person = personGroup.getChildren().iterator().next();
       Group<CnATreeElement> assetGroup = getGroupForClass(organization, Asset.class);
       Set<CnATreeElement> assetSet = assetGroup.getChildren();
       for (CnATreeElement asset : assetSet) {
           createLink(asset, person, Asset.REL_ASSET_PERSON_RESPO);
       }
    }
    
    private Organization createTestOrganization() throws CommandException {
        Organization organization = createOrganization();
        uuidList.add(organization.getUuid());  
        uuidList.addAll(createInOrganisation(organization,PersonIso.class,1));
        uuidList.addAll(createInOrganisation(organization,Asset.class,10));
        
        Group<CnATreeElement> personGroup = getGroupForClass(organization, PersonIso.class);
        CnATreeElement person = personGroup.getChildren().iterator().next();
            
        CreateConfiguration createConfiguration = new CreateConfiguration(person);
        createConfiguration = commandService.executeCommand(createConfiguration);
        Configuration configuration = createConfiguration.getConfiguration();
        configuration.setUser(this.getClass().getSimpleName());
        SaveConfiguration<Configuration> command = new SaveConfiguration<Configuration>(configuration, false);         
        command = commandService.executeCommand(command);
        
        return organization;      
    }

    
}
