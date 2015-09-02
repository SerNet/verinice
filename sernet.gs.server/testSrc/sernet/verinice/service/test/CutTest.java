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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.CutCommand;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;

/**
 * Test {@link CutCommand} by moving elements of all types to a sub-folder.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CutTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(CutTest.class);

    private static final int NUMBER_PER_GROUP = 1; 
    
    private List<String> uuidList;
    
    @Test
    public void testCut() throws Exception {
        // create
        uuidList = new LinkedList<String>();
        Organization organization = createOrganization();
        uuidList.add(organization.getUuid());
        uuidList.addAll(createElementsInGroups(organization, NUMBER_PER_GROUP));       
        uuidList.addAll(createGroupsInGroups(organization, NUMBER_PER_GROUP));      
        LOG.debug("Total number of created elements: " + uuidList.size());
        
        // move (cut and paste) elements
        moveAllElements(organization); 
        checkMovedElements(organization);
        
        // remove
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(organization);
        commandService.executeCommand(removeCommand);
        for (String uuid: uuidList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid);
            command = commandService.executeCommand(command);
            CnATreeElement element = command.getElement();
            assertNull("Organization was not deleted.", element);
        } 
    }

    /**
     * @param organization
     * @throws CommandException 
     */
    private void moveAllElements(Organization organization) throws CommandException {
        Set<CnATreeElement> children = organization.getChildren();
        for (CnATreeElement child : children) {
            assertTrue("Child of organization is not a group", child instanceof Group);
            Group<CnATreeElement> group = (Group) child;
            Set<CnATreeElement> childrenOfGroup = group.getChildren();
            Group<CnATreeElement> subGroup = null;
            CnATreeElement element = null;
            for (CnATreeElement subChild : childrenOfGroup) {
                if(subChild instanceof Group) {
                    subGroup = (Group<CnATreeElement>) subChild;
                } else {
                    element = subChild;
                }
            }
            assertNotNull("No element found in group: " + child.getTypeId(), element);
            assertNotNull("No sub-group found in group: " + child.getTypeId(), subGroup);
            List<String> copyUuidList = new LinkedList<String>();
            copyUuidList.add(element.getUuid());
            CutCommand cutCommand = new CutCommand(subGroup.getUuid(), copyUuidList);
            cutCommand = commandService.executeCommand(cutCommand);
            LOG.debug("Element " + element.getTypeId() + " moved to group.");
        }
    }
    
    /**
     * @param organization
     */
    private void checkMovedElements(Organization organization) {
        Set<CnATreeElement> children = organization.getChildren();
        for (CnATreeElement child : children) {
            child = elementDao.findByUuid(child.getUuid(), RetrieveInfo.getChildrenInstance());
            assertTrue("Child of organization is not a group", child instanceof Group);
            Group<CnATreeElement> group = (Group) child;
            
            Set<CnATreeElement> childrenOfGroup =  group.getChildren();
            assertTrue("Group has more or less than one child (" + childrenOfGroup.size() + "): " + child.getTypeId(), childrenOfGroup.size()==1);
            
            CnATreeElement subChild = childrenOfGroup.iterator().next();
            assertTrue("Sub-child of organization is not a group", subChild instanceof Group);
            Group<CnATreeElement> subGroup = (Group<CnATreeElement>) subChild;
            
            Set<CnATreeElement> childrenOfSubGroup =  subGroup.getChildren();
            assertTrue("Sub-group has more or less than one child: " + child.getTypeId(), childrenOfGroup.size()==1);        
        }
    }
}
