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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.CopyCommand;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.model.LoadModel;

/**
 * Test {@link CopyCommand} by copying all elements of all types to a sub-folder.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(CopyTest.class);

    private static final int NUMBER_OF_ELEMENTS = 10; 
    private static final int NUMBER_OF_GROUPS = 1; 
    
    private List<String> uuidList;
    
    @Test
    public void testCopy() throws Exception {
        // create
        uuidList = new LinkedList<String>();
        final Organization organization = createOrganization();
        uuidList.add(organization.getUuid());
        uuidList.addAll(createElementsInGroups(organization, NUMBER_OF_ELEMENTS));       
        uuidList.addAll(createGroupsInGroups(organization, NUMBER_OF_GROUPS));      
        LOG.debug("Total number of created elements: " + uuidList.size());
        
        // move (cut and paste) elements
        copyAllElements(organization); 
        checkCopiedElements(organization);
        
        // remove
        final RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(organization);
        commandService.executeCommand(removeCommand);
        for (final String uuid: uuidList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid);
            command = commandService.executeCommand(command);
            final CnATreeElement element = command.getElement();
            assertNull("Organization was not deleted.", element);
        } 
    }
    
    @Test
    public void testCopyOrganization() throws Exception {
        // create
        uuidList = new LinkedList<String>();
        final Organization organization = createOrganization();
        uuidList.add(organization.getUuid());
        uuidList.addAll(createElementsInGroups(organization, NUMBER_OF_ELEMENTS));       
        uuidList.addAll(createGroupsInGroups(organization, NUMBER_OF_GROUPS));      
        LOG.debug("Total number of created elements: " + uuidList.size());
        
        // copy org
        copyAllElements(organization);
        final String uuid = copyOrganization(organization);    
        uuidList.add(uuid);
        final Organization organizationCopy = (Organization) elementDao.findByUuid(uuid, RetrieveInfo.getChildrenInstance());
        checkCopiedElements(organizationCopy);
        
        // remove
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(organization);
        commandService.executeCommand(removeCommand);
        removeCommand = new RemoveElement<CnATreeElement>(organizationCopy);
        commandService.executeCommand(removeCommand);
        for (final String uuidDeleted: uuidList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuidDeleted);
            command = commandService.executeCommand(command);
            final CnATreeElement element = command.getElement();
            assertNull("Organization was not deleted.", element);
        } 
    }

    private String copyOrganization(final Organization organization) throws CommandException {
        final ISO27KModel model = loadIsoModel();
        final List<String> uuidList = new ArrayList<String>();
        uuidList.add(organization.getUuid());
        CopyCommand copyCommand = new CopyCommand(model.getUuid(), uuidList);
        copyCommand = commandService.executeCommand(copyCommand);
        final List<String> newUuidList = copyCommand.getNewElements();
        assertTrue("Not only one element in result list", newUuidList!=null && newUuidList.size()==1);
        return newUuidList.get(0);
    }

    /**
     * @param organization
     * @throws CommandException 
     */
    private void copyAllElements(final Organization organization) throws CommandException {
        final Set<CnATreeElement> children = organization.getChildren();
        for (final CnATreeElement child : children) {
            assertTrue("Child of organization is not a group", child instanceof Group);
            final Group<CnATreeElement> group = (Group) child;
            final Set<CnATreeElement> childrenOfGroup = group.getChildren();
            Group<CnATreeElement> subGroup = null;
            final List<String> copyUuidList = new LinkedList<String>();
            for (final CnATreeElement subChild : childrenOfGroup) {
                if(subChild instanceof Group) {
                    subGroup = (Group<CnATreeElement>) subChild;
                } else {
                    copyUuidList.add(subChild.getUuid());
                }
            }
            assertFalse("Number of elements in group is not " + (NUMBER_OF_ELEMENTS+1) + ", type: " + child.getTypeId(), copyUuidList.size()==(NUMBER_OF_ELEMENTS+1));
            assertNotNull("No sub-group found in group: " + child.getTypeId(), subGroup);
            
            CopyCommand copyCommand = new CopyCommand(subGroup.getUuid(), copyUuidList);
            copyCommand = commandService.executeCommand(copyCommand);
        }
    }
    
    /**
     * @param organization
     */
    private void checkCopiedElements(final Organization organization) {
        final Set<CnATreeElement> children = organization.getChildren();
        for (CnATreeElement child : children) {
            final RetrieveInfo ri = RetrieveInfo.getChildrenInstance();
            ri.setGrandchildren(true);
            child = elementDao.findByUuid(child.getUuid(), ri);
            assertTrue("Child of organization is not a group", child instanceof Group);
            final Group<CnATreeElement> group = (Group) child;
            
            final Set<CnATreeElement> childrenOfGroup =  group.getChildren();
            assertTrue("Number of elements in group is not:" + (NUMBER_OF_ELEMENTS+1) + "): " + child.getTypeId(), childrenOfGroup.size()==(NUMBER_OF_ELEMENTS+1));
            
            for (final CnATreeElement subChild : childrenOfGroup) {
                if(subChild instanceof Group) {
                    final Group<CnATreeElement> subGroup = (Group<CnATreeElement>) subChild;
                    assertTrue("Number of elements in group is not: " + NUMBER_OF_ELEMENTS, subGroup.getChildren().size()==NUMBER_OF_ELEMENTS);
                } 
            }
       
        }
    }
    
    private ISO27KModel loadIsoModel() throws CommandException {
        LoadModel<ISO27KModel> loadModel = new LoadModel<>(ISO27KModel.class);
        loadModel = commandService.executeCommand(loadModel);
        return loadModel.getModel();
    }
}
