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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.EvidenceGroup;
import sernet.verinice.model.iso27k.ExceptionGroup;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.FindingGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.InterviewGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.RecordGroup;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.RequirementGroup;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.ResponseGroup;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.ThreatGroup;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.iso27k.VulnerabilityGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.LoadCnAElementByExternalID;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.UpdateElementEntity;
import sernet.verinice.service.model.LoadModel;

/**
 * Tests executing several commands.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class CommandServiceProvider extends UuidLoader {

    private static final Logger LOG = Logger.getLogger(CommandServiceProvider.class);

    public static final Map<String, Class> GROUP_TYPE_MAP;

    static {
        GROUP_TYPE_MAP = new HashMap<String, Class>();
        GROUP_TYPE_MAP.put(AssetGroup.TYPE_ID, Asset.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(AuditGroup.TYPE_ID, Audit.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(ControlGroup.TYPE_ID, Control.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(DocumentGroup.TYPE_ID, Document.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(EvidenceGroup.TYPE_ID, Evidence.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(ExceptionGroup.TYPE_ID, sernet.verinice.model.iso27k.Exception.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(FindingGroup.TYPE_ID, Finding.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(IncidentGroup.TYPE_ID, Incident.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(IncidentScenarioGroup.TYPE_ID, IncidentScenario.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(InterviewGroup.TYPE_ID, Interview.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(PersonGroup.TYPE_ID, PersonIso.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(ProcessGroup.TYPE_ID, sernet.verinice.model.iso27k.Process.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(RecordGroup.TYPE_ID, Record.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(RequirementGroup.TYPE_ID, Requirement.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(ResponseGroup.TYPE_ID, Response.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(ThreatGroup.TYPE_ID, Threat.class); //$NON-NLS-1$
        GROUP_TYPE_MAP.put(VulnerabilityGroup.TYPE_ID, Vulnerability.class); //$NON-NLS-1$
    }

    @Resource(name = "commandService")
    protected ICommandService commandService;

    protected Organization createOrganization() throws CommandException {
        return createOrganization(null);
    }

    protected Organization createOrganization(String name) throws CommandException {
        LoadModel<ISO27KModel> loadModel = new LoadModel<>(ISO27KModel.class);
        loadModel = commandService.executeCommand(loadModel);
        ISO27KModel model = loadModel.getModel();

        assertNotNull("ISO model is null.", model);

        if (name == null || name.isEmpty()) {
            name = getClass().getSimpleName();
        }

        CreateElement<Organization> saveCommand = new CreateElement<Organization>(model, Organization.class, name);
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = commandService.executeCommand(saveCommand);
        Organization organization = saveCommand.getNewElement();
        checkOrganization(organization);

        return organization;
    }

    protected List<String> createElementsInGroups(Organization organization, int numberPerGroup) throws CommandException {
        List<String> uuidList = new LinkedList<String>();
        Set<CnATreeElement> children = organization.getChildren();
        for (CnATreeElement child : children) {
            uuidList.add(child.getUuid());
            assertTrue("Child of organization is not a group", child instanceof Group);
            Group<CnATreeElement> group = (Group) child;
            for (int i = 0; i < numberPerGroup; i++) {
                CnATreeElement newElement = createNewElement(group, i);
                uuidList.add(newElement.getUuid());
                LOG.debug(newElement.getTypeId() + ": " + newElement.getTitle() + " created.");
            }
        }
        return uuidList;
    }

    protected Collection<? extends String> createInOrganisation(Organization organization, Class clazz, int i) throws CommandException {
        List<String> uuidList = new LinkedList<String>();
        Group<CnATreeElement> group;
        if (clazz == SamtTopic.class) {
            group = getGroupForClass(organization, Control.class);
        } else {
            group = getGroupForClass(organization, clazz);
        }
        for (int n = 0; n < i; n++) {
            uuidList.add(createNewElement(group, clazz, n).getUuid());
        }
        return uuidList;
    }

    protected Group<CnATreeElement> getGroupForClass(Organization organization, Class clazz) {
        Group<CnATreeElement> group = null;
        Set<CnATreeElement> children = organization.getChildren();
        for (CnATreeElement child : children) {
            assertTrue("Child of organization is not a group", child instanceof Group);
            if (clazz.equals(GROUP_TYPE_MAP.get(child.getTypeId()))) {
                group = (Group) child;
                break;
            }
        }
        return group;
    }

    protected List<String> createGroupsInGroups(Organization organization, int numberPerGroup) throws CommandException {
        List<String> uuidList = new LinkedList<String>();
        Set<CnATreeElement> children = organization.getChildren();
        for (CnATreeElement child : children) {
            uuidList.add(child.getUuid());
            assertTrue("Child of organization is not a group", child instanceof Group);
            Group<CnATreeElement> group = (Group) child;
            for (int i = 0; i < numberPerGroup; i++) {
                CnATreeElement newGroup = createNewNamedGroup(group, i);
                uuidList.add(newGroup.getUuid());
                LOG.debug(newGroup.getTypeId() + ": " + newGroup.getTitle() + " created.");
            }
        }
        return uuidList;
    }

    protected CnATreeElement createNewNamedGroup(Group<CnATreeElement> group, String name) throws CommandException {
        return createNewGroup(group, name);
    }    
    
    protected CnATreeElement createNewNamedGroup(Group<CnATreeElement> group, int n) throws CommandException {
        return createNewGroup(group, getClass().getSimpleName() + "_" + n);
    }
    
    private  CnATreeElement createNewGroup(Group<CnATreeElement> group, String name) throws CommandException
    {
        CreateElement<CnATreeElement> command = new CreateElement<CnATreeElement>(group, group.getTypeId(), name);
        command.setInheritAuditPermissions(true);
        command = commandService.executeCommand(command);
        CnATreeElement newElement = command.getNewElement();
        checkElement(newElement);
        return newElement;
    }

    protected CnATreeElement createNewElement(Group<CnATreeElement> group, int n) throws CommandException {
        Class clazz = GROUP_TYPE_MAP.get(group.getTypeId());
        return createNewElement(group, clazz, n);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected CnATreeElement createNewElement(Group<CnATreeElement> group, Class clazz) throws CommandException {
      return createNewElement(group, clazz, (int)Math.round(Math.random()*1000.0));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected CnATreeElement createNewElement(Group<CnATreeElement> group, Class clazz, int n) throws CommandException {
        CreateElement<CnATreeElement> command = new CreateElement<CnATreeElement>(group, clazz, getClass().getSimpleName() + "_" + n);
        command.setInheritAuditPermissions(true);
        command = commandService.executeCommand(command);
        CnATreeElement newElement = command.getNewElement();
        checkElement(newElement);

        return newElement;
    }

    protected CnALink createLink(CnATreeElement source, CnATreeElement destination, String linkType) throws CommandException {
        CreateLink command = new CreateLink(source, destination, linkType, this.getClass().getSimpleName());
        command = commandService.executeCommand(command);
        return command.getLink();
    }

    protected void checkOrganization(Organization organization) {
        checkElement(organization);
        Set<CnATreeElement> children = organization.getChildren();
        assertNotNull("Children of organization are null.", children);
        assertEquals("Organization does not contain 14 groups.", 14, children.size());
    }

    protected void checkElement(CnATreeElement element) {
        assertNotNull("Element is null.", element);
        assertNotNull("Db-id of element is null.", element.getDbId());
        checkScopeId(element);
    }
    
    protected void checkProperty(CnATreeElement element, String propertyId, String expectedValue) {
        checkProperty(element,propertyId,expectedValue,true);
    }
    
    protected void checkProperty(CnATreeElement element, String propertyId, String value, boolean equals) {
        assertTrue("Unexpected value of property: " + propertyId, equals && value.equals(element.getEntity().getSimpleValue(propertyId)));        
    }
    
    protected void checkProperty(CnATreeElement element, String propertyId, int expectedValue) {
        checkProperty(element,propertyId,expectedValue,true);     
    }
    
    protected void checkProperty(CnATreeElement element, String propertyId, int value, boolean equals) {
        assertTrue("Unexpected value of property: " + propertyId, (equals) == (value==element.getEntity().getInt(propertyId)));        
    }

    /**
     * Removes all with {@link #registerElementsWithUuid(String)} registered
     * Elements from the Database.
     * 
     * @throws CommandException
     */
    protected void removeOrganization(Organization org) throws CommandException {
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<>(org);
        commandService.executeCommand(removeCommand);

        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<>(org.getUuid());
        command = commandService.executeCommand(command);
        CnATreeElement element = command.getElement();
        assertNull("organization " + org.getUuid() + " was not deleted.", element);
    }

    protected CnATreeElement updateElement(CnATreeElement element) throws CommandException {
        UpdateElementEntity<CnATreeElement> updateElementCommand = new UpdateElementEntity<>(element, ChangeLogEntry.STATION_ID);
        updateElementCommand = commandService.executeCommand(updateElementCommand);
        return updateElementCommand.getElement();
    }

    protected CnATreeElement loadElement(String sourceId, String extId) throws CommandException {
        LoadCnAElementByExternalID command = new LoadCnAElementByExternalID(sourceId, extId, false, true);
        command.setProperties(true);
        command = commandService.executeCommand(command);
        List<CnATreeElement> elementList = command.getElements();
        assertEquals("Element with source-id " + sourceId + " and ext-id" + extId + " was not found.", elementList.size(), 1);
        return elementList.get(0);
    }

}
