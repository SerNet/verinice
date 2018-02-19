/*******************************************************************************
 * Copyright (c) 2013 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.validation.IValidationService;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.model.validation.CnAValidation;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.RetrieveCnATreeElement;
import sernet.verinice.service.commands.UpdateElementEntity;
import sernet.verinice.service.commands.crud.LoadElementForEditor;

/**
 *
 */
@TransactionConfiguration(transactionManager="txManager", defaultRollback=false)
@Transactional
public class ValidationServiceTest extends CommandServiceProvider {
    
    private static final Logger LOG = Logger.getLogger(ValidationServiceTest.class);

    @Resource(name="validationService")
    private IValidationService validationService;
    
    @Resource(name="cnaTreeElementDao")
    private IBaseDao<CnATreeElement, Integer> elementDao;
    
    @Resource(name="huiTypeFactory")
    private HUITypeFactory huiTypeFactory;
    
    @Test
    public void createAndDeleteSubTreeValidations() throws CommandException{
        Organization root = createOrganization();
        assertNotNull(root);
        checkElement(root);
        createElementsInGroups(root, 10);
        validationService.createValidationsForSubTree(root);
        List<CnAValidation> validations = validationService.getValidations(root.getDbId());
        assertNotSame(0, validations.size());
        deleteElement(root);
        assertEquals(Integer.valueOf(0), Integer.valueOf(validationService.getValidations(root.getDbId()).size()));
    }
    
    @Test
    public void createSamtTopicValidation() throws Exception{
        Organization org = createOrganization();
        assertNotNull(org);
        checkElement(org);
        List<String> uuIdList =  new LinkedList<String>();
        uuIdList.addAll(createInOrganisation(org, SamtTopic.class, 1));
        assertEquals(uuIdList.size(), 1);
        LoadElementByUuid<CnATreeElement> command;
        CnATreeElement topic = loadElementByUuid(uuIdList.get(0));
        checkElement(topic);
        assertNotNull(topic);
        validationService.createValidationForSingleElement(topic);
        List<CnAValidation> validations = getSingleElementValidations(topic);
        assertEquals(3, validations.size());
        deleteElement(org);
    }
    
    
    
    /**
     * assumes that huientity "samt_topic" has defined 3 validationrules:
     * samt_topic_maturity :        RegExRule for value between 0 and 5
     * samt_topic_audit_findings:   NotEmptyRule
     * samt_topic_audit_ra:         NotEmptyRule
     * @throws Exception
     */
    @Test
    public void resolveSamtTopicValidations() throws Exception{
        Organization org = createOrganization();
        assertNotNull(org);
        checkElement(org);
        List<String> uuIdList =  new LinkedList<String>();
        uuIdList.addAll(createInOrganisation(org, SamtTopic.class, 1));
        assertEquals(uuIdList.size(), 1);
        LoadElementByUuid<CnATreeElement> command;
        CnATreeElement topic = loadElementByUuid(uuIdList.get(0));
        assertNotNull(topic);
        checkElement(topic);
        validationService.createValidationForSingleElement(topic);
        List<CnAValidation> validations = getSingleElementValidations(topic);
        assertEquals(Integer.valueOf(3), Integer.valueOf(validations.size()));
        for(CnAValidation validation : validations){
            if(validation.getPropertyId().equals(SamtTopic.PROP_MATURITY)){
                changeProperty(topic, SamtTopic.PROP_MATURITY, "4");
            } else if(validation.getPropertyId().equals("samt_topic_audit_findings")){
                changeProperty(topic, "samt_topic_audit_findings", "TestValue");
            } else if(validation.getPropertyId().equals("samt_user_classification")){
                changeProperty(topic, "samt_user_classification", "TestValue");
            }
        }
        
        UpdateElementEntity<CnATreeElement> updater = new UpdateElementEntity<CnATreeElement>(topic, ChangeLogEntry.STATION_ID);
        try {
            commandService.executeCommand(updater);
        } catch (CommandException e) {
            LOG.error("Error on updating element", e);
        }
        topic = updater.getElement();
        checkElement(topic);
        //reload Element
        topic = loadElementByUuid(topic.getUuid(), RetrieveInfo.getPropertyInstance());
        checkElement(topic);
        assertEquals(topic.getEntity().getProperties("samt_user_classification").getProperties().get(0).getPropertyValue(), "samt_classification_good");
        
        topic = (SamtTopic)loadElement(topic.getUuid());
        checkElement(topic);
        validationService.createValidationForSingleElement(topic);
        validations = getSingleElementValidations(topic);
        assertEquals(Integer.valueOf(0), Integer.valueOf(validations.size()));
        deleteElement(topic);
        deleteElement(org);
        
    }

    private CnATreeElement loadElementByUuid(String uuId) throws CommandException {
        return loadElementByUuid(uuId, null);
    }
    
    private CnATreeElement loadElementByUuid(String uuId, RetrieveInfo ri) throws CommandException {
        LoadElementByUuid<CnATreeElement> command;
        if(ri != null){
            command = new LoadElementByUuid<CnATreeElement>(uuId, ri);
        } else {
            command = new LoadElementByUuid<CnATreeElement>(uuId);
        }
        command = commandService.executeCommand(command);
        CnATreeElement topic = command.getElement();
        return topic;
    }
    
    private List<CnAValidation> getSingleElementValidations(CnATreeElement elmt){
        return validationService.getValidations(elmt.getScopeId(), elmt.getDbId());
    }
    
    
    /**
     * deletes given element from db and referencing validation elements
     * @param element
     */
    private void deleteElement(CnATreeElement element){
        assertNotNull(element);
        String uuid = element.getUuid();
        element = elementDao.findByUuid(uuid, RetrieveInfo.getPropertyInstance());
        deleteValidations(element);
        removeElement(element);
        LOG.debug("Element " + uuid + " deleted");
    }

    private void removeElement(CnATreeElement element) {
        RemoveElement<CnATreeElement> deleteElement = new RemoveElement<>(element);
        try {
            commandService.executeCommand(deleteElement);
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(element.getUuid());
            command = commandService.executeCommand(command);
            CnATreeElement e2 = command.getElement();
            assertNull("Organization was not deleted.", e2);
        } catch (CommandException e) {
            LOG.error("Error while deleting element", e);
        }
    }

    private void deleteValidations(CnATreeElement element) {
        if(element.getTypeId().equals(ITVerbund.TYPE_ID) || element.getTypeId().equals(Organization.TYPE_ID)){
            validationService.deleteValidationsOfSubtree(element);
            LOG.debug("Validations for Subtree of " + element.getTitle() + " deleted");
        } else {
            try{
                validationService.deleteValidations(element.getScopeId(), element.getDbId());
            } catch (Exception e){
                LOG.error("Error while deleting validation", e);
            }
            LOG.debug("Validations for " + element.getTitle() + " deleted");
        }
    }
    
    private void changeProperty(CnATreeElement element, String propertyName, String propertyValue) {
        Entity entity = element.getEntity();
        EntityType type = huiTypeFactory.getEntityType(element.getTypeId());
        assertNotNull("Entity type not found, id: " + element.getTypeId(), type);
        List<PropertyType> propertyList = type.getAllPropertyTypes();
        for (PropertyType propertyType : propertyList) {
            if(propertyType.getId().equals(propertyName)){
                if(propertyType.isLine() || propertyType.isText()) {
                    entity.setSimpleValue(propertyType,propertyValue);
                    break;
                } else if(propertyType.isNumericSelect()){
                    entity.setNumericValue(propertyType, Integer.valueOf(propertyValue));
                } else if(propertyType.isSingleSelect()){
                    entity.setSimpleValue(propertyType, propertyType.getOption("samt_classification_good").getId());
                } else {
                    LOG.debug("UnseenProperty:\t" + propertyType.getName());
                }
            }
        }
    }
    
    /**
     * Loads an element similar to the BsiElementEditor
     */
    protected CnATreeElement loadElement(String uuid) throws CommandException {  
        LoadElementByUuid<CnATreeElement> loadByUuid = new LoadElementByUuid<CnATreeElement>(uuid, RetrieveInfo.getPropertyInstance());
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
    
}
