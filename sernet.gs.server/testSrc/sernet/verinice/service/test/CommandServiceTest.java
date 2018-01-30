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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kRoot;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.LoadElementByTypeId;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.LoadTreeItem;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.RetrieveCnATreeElement;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.UpdateElementEntity;
import sernet.verinice.service.commands.crud.LoadElementForEditor;

/**
 * Tests executing several commands.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@TransactionConfiguration(transactionManager="txManager", defaultRollback=false)
@Transactional
public class CommandServiceTest extends CommandServiceProvider {  
    
    private static final Logger LOG = Logger.getLogger(CommandServiceTest.class);
    
    private static final int NUMBER_PER_GROUP = 20;  
    private static final int NUMBER_OF_IMPORTED_ELEMENTS = 29;   
    private static final String SOURCE_ID = "CommandServiceTest";
    private static final String VALUE_PREFIX = "****";
    private static final int NUMBER_OF_ELEMENTS = 300;
     
    @Resource(name="huiTypeFactory")
    private HUITypeFactory huiTypeFactory;
    
    private Set<CnATreeElement> importedElements;    
    private List<String> uuidList;
    private String currentDate;
    
    
    /**
     * For randomly choosed NUMBER_OF_ELEMENTS elements:
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
        double factor = 1;
        if(uuidList.size()>NUMBER_OF_ELEMENTS) {
            factor = (NUMBER_OF_ELEMENTS*1.0) / uuidList.size();
        } 
        LOG.info(uuidList.size() + " elements, test factor is: " + factor);
        int n = 0;
        for (String uuid : uuidList) {
            if(Math.random()<factor) {              
                loadChangeAndCheckElement(uuid);  
                n++;
            }
        }
        LOG.info(n + " of " + uuidList.size() + " elements tested.");
    }

    /**
     * Loads all elements with the LoadTreeItem command.
     * ISO- and BSI-View using LoadTreeItem to load the tree.
     */
    @Test
    public void testLoadTreeItems() throws Exception {
        LoadElementByTypeId loadOrgs = new LoadElementByTypeId(Organization.TYPE_ID, RetrieveInfo.getPropertyInstance());
        loadOrgs = commandService.executeCommand(loadOrgs);
        List<Organization> allOrgs = (List<Organization>) loadOrgs.getElementList();
        
        loadTree(allOrgs);
        
        LoadElementByTypeId loadItVerbunds = new LoadElementByTypeId(ITVerbund.TYPE_ID, RetrieveInfo.getPropertyInstance());
        loadItVerbunds = commandService.executeCommand(loadItVerbunds);
        List<ITVerbund> allItVerbunds = (List<ITVerbund>) loadItVerbunds.getElementList();
        
        loadTree(allItVerbunds);
    }

    protected void loadTree(List<? extends CnATreeElement> elementList) throws CommandException {
        for (CnATreeElement element : elementList) {
            String title = element.getTitle();
            assertNotNull("Title of element is null", title);
            LOG.debug("Testing element: " + title);
            loadChildren(element);
        }
    }    
    
    /**
     * Calls LoadElementByUuid for all elements in DB
     */
    @Test
    public void testLoadElementByUuid() throws Exception {
        List<String> uuidList = getAllUuids();
        
        for (String uuid : uuidList) {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setParent(true);
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid, ri);
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
        uuidList.add(organization.getUuid());
        checkOrganization(organization);       
        uuidList.addAll(createElementsInGroups(organization, NUMBER_PER_GROUP));
        
        LOG.info("Total number of created elements: " + uuidList.size());
        
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
        CnATreeElement element = loadElement(uuid, new RetrieveInfo());
        
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
        loadForEditor = commandService.executeCommand(loadForEditor);
        element = loadForEditor.getElement();
        assertNotNull("Element for editor is null, uuid: " + uuid, element);
        element.setChildren(elementWithChildren.getChildren());
        return element;
    }
    
    protected void changeElement(CnATreeElement element) throws CommandException {
        changeProperties(element);            
        UpdateElementEntity<CnATreeElement> updateCommand = new UpdateElementEntity<CnATreeElement>(element, ChangeLogEntry.STATION_ID);
        commandService.executeCommand(updateCommand);
    }
    
    protected void loadChildren(CnATreeElement element) throws CommandException {
        RetrieveInfo ri = new RetrieveInfo();
        ri.setChildren(true).setChildrenProperties(true);           
        LoadTreeItem command = new LoadTreeItem(element.getUuid(), ri);
        command = commandService.executeCommand(command);
        CnATreeElement elementWithChildren = command.getElement();       
        Set<CnATreeElement> children = elementWithChildren.getChildren();
        assertNotNull("Children set of element is null", children);
        for (CnATreeElement child : children) {
            assertNotNull("Title of child is null", child.getTitle());
        }
        for (CnATreeElement child : children) {
            LOG.debug("Loading children of: " + child.getTitle());
            loadChildren(child);
        }
    }
    
    protected boolean isEditable(CnATreeElement element) {
        return ! (element instanceof IBSIStrukturKategorie) &&
           ! (element instanceof IISO27kRoot) && 
           ! (element instanceof BSIModel) &&
           ! (element instanceof ImportIsoGroup) &&
           ! (element instanceof ImportBsiGroup) &&
           ! (element instanceof FinishedRiskAnalysis);
    }
    
    private void changeProperties(CnATreeElement element) {
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
    
    /**
     * Vna import does not work in Junit-Test. After importing only the organization is 
     * saved in DB. Probably this is a spring-junit-transaction issue.
     * 
     * This method is not annotated with @Test anymore.
     * To activate it activate the annotation again.
     */
    //@Test
    //@Transactional
    //@Rollback(false)
    public void testVnaImport() throws Exception {      
        importVna();
        
        String hql = "select element.uuid from CnATreeElement element where element.sourceId = ?"; 
        Object[] params = new Object[]{SOURCE_ID}; 
        List<String> importedUuids = elementDao.findByQuery(hql, params);
        LOG.info("Number of imported elements: " + importedUuids.size());
        
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
        
        LOG.info("VNA imported: " + vnaUrl.getPath());
    }
    
    public void removeImport() throws Exception {
        for (CnATreeElement element : importedElements) {
            RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(element);
            commandService.executeCommand(removeCommand);
        }
    }
    

}
