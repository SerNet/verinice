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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementForEditor;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElementEntity;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.ChangeLogEntry;
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
import sernet.verinice.model.iso27k.IISO27kRoot;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
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
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.LoadElementByTypeId;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.LoadTreeItem;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.iso27k.LoadModel;

/**
 * Tests executing several commands.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@TransactionConfiguration(transactionManager="txManager", defaultRollback=false)
@Transactional
public class CommandServiceTest extends UuidLoader {  
    
    private static final Logger LOG = Logger.getLogger(CommandServiceTest.class);
    
    public static final Map<String, Class> GROUP_TYPE_MAP;

    private static final int NUMBER_PER_GROUP = 20;  
    private static final int NUMBER_OF_IMPORTED_ELEMENTS = 29;   
    private static final String SOURCE_ID = "CommandServiceTest";
    private static final String VALUE_PREFIX = "****";
    
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
     
    @Resource(name="commandService")
    private ICommandService commandService; 
    
    @Resource(name="huiTypeFactory")
    private HUITypeFactory huiTypeFactory;
    
    private Set<CnATreeElement> importedElements;    
    private List<String> uuidList;
    private String currentDate;
    
    
    /**
     * For all elements:
     *  -Open the element similar to BSIElementEditor,
     *  -Changes all simple (line and text type) properties.
     *  -Saves the element.
     *  -Loads the element again.
     *  -Checks id proerties are changed.
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testOpenEditor() throws Exception {
        Calendar now = Calendar.getInstance();
        currentDate = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(now.getTime());
        List<String> uuidList = getAllUuids();        
        for (String uuid : uuidList) {
            loadChangeAndCheckElement(uuid);           
        }
    }

    /**
     * Loads all elements with the LoadTreeItem command.
     * ISO- and BIS-view using LoadTreeItem to load the tree.
     */
    @Test
    public void testLoadTreeItems() throws Exception {
        LoadElementByTypeId loadOrgs = new LoadElementByTypeId(Organization.TYPE_ID, RetrieveInfo.getPropertyInstance());
        loadOrgs = commandService.executeCommand(loadOrgs);
        List<Organization> allOrgs = (List<Organization>) loadOrgs.getElementList();
        
        for (Organization organization : allOrgs) {
            String title = organization.getTitle();
            assertNotNull("Title of organization is null", title);
            LOG.debug("Testing organization: " + title);
            loadChildren(organization);
        }
    }    
    
    /**
     * Calls LoadElementByUuid for all elements in DB
     */
    @Test
    public void testLoadElementByUuid() throws Exception {
        List<String> uuidList = getAllUuids();
        
        for (String uuid : uuidList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid);
            command = commandService.executeCommand(command);
            CnATreeElement element = command.getElement();
            assertNotNull(element);
            checkScopeId(element);
        }    
    }
    
    /**
     * Creates a new organization and for every group in it
     * NUMBER_PER_GROUP elements.
     * 
     * Deletes the newly created org. by command RemoveElement 
     * and checks if every element is removed.
     */
    @Test
    public void testCreateAndRemoveElement() throws Exception {
        uuidList = new LinkedList<String>();
        
        Organization organization = createOrganization();
        checkOrganization(organization);       
        createElementsInGroups(organization);
        
        LOG.debug("Total number of created elements: " + uuidList.size());
        
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(organization);
        commandService.executeCommand(removeCommand);
        
        for (String uuid: uuidList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid);
            command = commandService.executeCommand(command);
            CnATreeElement element = command.getElement();
            assertNull("Organization was not deleted.", element);
        }       
    }
    
    protected void loadChangeAndCheckElement(String uuid) throws CommandException {
        LoadElementByUuid<CnATreeElement> loadByUuid;
        CnATreeElement element = loadElement(uuid,null);
        
        LOG.debug("Element opened: " + element.getTitle());
        
        if(isEditable(element)) {
            changeElement(element);          
            CnATreeElement changedElement = loadElement(uuid,RetrieveInfo.getPropertyInstance());         
            checkElement(changedElement);
            checkChangedProperties(changedElement);
        }
    }

    /**
     * Loads an element similar to the BsiElementEditor
     */
    protected CnATreeElement loadElement(String uuid, RetrieveInfo ri) throws CommandException {  
        LoadElementByUuid<CnATreeElement> loadByUuid = new LoadElementByUuid<CnATreeElement>(uuid, ri);
        loadByUuid = commandService.executeCommand(loadByUuid);
        CnATreeElement element = loadByUuid.getElement();
        assertNotNull("Element is null, uuid: " + uuid, element);
        
        
        RetrieveCnATreeElement retrieveCommand = new RetrieveCnATreeElement(element.getTypeId(), element.getDbId(),RetrieveInfo.getChildrenInstance());
        retrieveCommand = commandService.executeCommand(retrieveCommand);           
        CnATreeElement elementWithChildren = retrieveCommand.getElement(); 
        assertNotNull("Element with children is null, uuid: " + uuid, elementWithChildren);
        assertNotNull("Children of element are null, uuid: " + uuid, elementWithChildren.getChildren());
        
        LoadElementForEditor loadForEditor = new LoadElementForEditor(element,false);
        loadForEditor = ServiceFactory.lookupCommandService().executeCommand(loadForEditor);
        element = loadForEditor.getElement();
        assertNotNull("Element for editor is null, uuid: " + uuid, element);
        element.setChildren(elementWithChildren.getChildren());
        return element;
    }
    
    protected void changeElement(CnATreeElement element) throws CommandException {
        changeProperties(element);            
        UpdateElementEntity<CnATreeElement> updateCommand = new UpdateElementEntity<CnATreeElement>(element, ChangeLogEntry.STATION_ID);
        updateCommand = commandService.executeCommand(updateCommand);
    }
    
    protected void loadChildren(CnATreeElement element) throws CommandException {
        RetrieveInfo ri = new RetrieveInfo();
        ri.setChildren(true).setChildrenProperties(true);           
        LoadTreeItem command = new LoadTreeItem(element.getUuid(), ri);
        command = commandService.executeCommand(command);
        CnATreeElement elementWithChildren = command.getElement();       
        Set<CnATreeElement> children = elementWithChildren.getChildren();
        assertNotNull("Children set of element is children", children);
        for (CnATreeElement child : children) {
            assertNotNull("Title of child is null", child.getTitle());
        }
        for (CnATreeElement child : children) {
            if(child instanceof Group) {
                LOG.debug("Loading children of: " + child.getTitle());
                loadChildren(child);
            }
        }
    }
    
    protected boolean isEditable(CnATreeElement element) {
        return ! (element instanceof IBSIStrukturKategorie) &&
           ! (element instanceof IISO27kRoot) && 
           ! (element instanceof BSIModel) &&
           ! (element instanceof ImportIsoGroup) &&
           ! (element instanceof ImportBsiGroup);
    }
    
    private void changeProperties(CnATreeElement element) {
        Calendar now = Calendar.getInstance();
        Entity entity = element.getEntity();
        EntityType type = huiTypeFactory.getEntityType(element.getTypeId());
        assertNotNull("Entity type not found, id: " + element.getTypeId(), type);
        List<PropertyType> propertyList = type.getAllPropertyTypes();
        for (PropertyType propertyType : propertyList) {
            if(propertyType.isLine() || propertyType.isText()) {
                String id = propertyType.getId();
                String value = entity.getSimpleValue(id);
                if(value.contains(VALUE_PREFIX)) {
                    value = value.substring(value.indexOf(VALUE_PREFIX));
                }
                value = value + VALUE_PREFIX + currentDate;
                entity.setSimpleValue(propertyType,value);
            }
        }
    }
    
    private void checkChangedProperties(CnATreeElement element) {
        String typeId = element.getTypeId();
        Entity entity = element.getEntity();
        EntityType type = huiTypeFactory.getEntityType(typeId);
        assertNotNull("Entity type not found, id: " + typeId, type);
        List<PropertyType> propertyList = type.getAllPropertyTypes();
        for (PropertyType propertyType : propertyList) {
            if(propertyType.isLine() || propertyType.isText()) {
                String id = propertyType.getId();
                String value = entity.getSimpleValue(id);
                assertTrue("Property not changed, type: " + typeId + ", uuid: " + element.getUuid() + ", prop: " + id ,value.contains(VALUE_PREFIX));
            }
        }
        
    }

    protected Organization createOrganization() throws CommandException {
        LoadModel loadModel = new LoadModel();
        loadModel = commandService.executeCommand(loadModel);
        ISO27KModel model = loadModel.getModel();
        
        assertNotNull("ISO model is null.", model);
        
        CreateElement<Organization> saveCommand = new CreateElement<Organization>(model, Organization.class, getClass().getSimpleName());
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = commandService.executeCommand(saveCommand);
        Organization organization = saveCommand.getNewElement();     
        uuidList.add(organization.getUuid());
        
        LOG.debug("Organisation created.");
        
        return organization;
    }
    
    protected void createElementsInGroups(Organization organization) throws CommandException {
        Set<CnATreeElement> children = organization.getChildren();
        for (CnATreeElement child : children) {
            uuidList.add(child.getUuid());
            assertTrue("Child of organization is not a group", child instanceof Group);
            Group<CnATreeElement> group = (Group) child;
            for (int i = 0; i < NUMBER_PER_GROUP; i++) {
                CnATreeElement newElement = createNewElement(group,i);
                uuidList.add(newElement.getUuid());
                LOG.debug(newElement.getTypeId() + ": " + newElement.getTitle() + " created.");
            }         
        }
    }
    
    protected CnATreeElement createNewElement(Group<CnATreeElement> group, int n) throws CommandException {
        CreateElement<CnATreeElement> command = new CreateElement<CnATreeElement>(
                group, 
                GROUP_TYPE_MAP.get(group.getTypeId()), 
                getClass().getSimpleName() + "_" + n);
        command.setInheritAuditPermissions(true);
        command = commandService.executeCommand(command);
        CnATreeElement newElement = command.getNewElement();
        checkElement(newElement);
        
        return newElement;
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
        assertNotNull("Scope-id of element is null.", element.getScopeId());
    }


    /**
     * Vna import does not work in Junit-Test. After importing only the organization is 
     * saved in DB. Probably this is a spring-junit-transaction issue.
     * 
     * This method is not annotated with @Test anymore.
     * To activate ist set the annotation again.
     */
    public void testVnaImport() throws Exception {      
        importVna();
        
        String hql = "select element.uuid from CnATreeElement element where element.sourceId = ?"; 
        Object[] params = new Object[]{SOURCE_ID}; 
        List<String> importedUuids = elementDao.findByQuery(hql, params);
        LOG.debug("Number of imported elements: " + importedUuids.size());
        
        assertEquals("number of imported elements is not: " + NUMBER_OF_IMPORTED_ELEMENTS, NUMBER_OF_IMPORTED_ELEMENTS, importedUuids.size());
        
        // Elemente 29
        // Links 40 / 20
        // Dateien 2 
        
        removeImport();
    }
    
    public void importVna() throws Exception {
        SyncParameter parameter = new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        URL vnaUrl = this.getClass().getResource("testVnaImport.vna");

        SyncCommand command = new SyncCommand(parameter, vnaUrl.getPath());
        command = commandService.executeCommand(command);
        importedElements = command.getElementSet();
        
        //sessionFactory.getCurrentSession().flush(); 
        
        LOG.debug("VNA imported: " + vnaUrl.getPath());
    }
    
    public void removeImport() throws Exception {
        for (CnATreeElement element : importedElements) {
            RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(element);
            commandService.executeCommand(removeCommand);
        }
    }
    

}
