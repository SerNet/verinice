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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.DocumentGroup;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;

/**
 * DaoTest test verious dao methods:
 * findBy(Hql)Query, findByUuid, merge and delete
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DaoTest extends UuidLoader {
    
    private static final Logger LOG = Logger.getLogger(DaoTest.class);
    
    @Resource(name="cnaTreeElementDao")
    private IBaseDao<CnATreeElement, Integer> elementDao;
    
    @Resource(name="huiTypeFactory")
    private HUITypeFactory huiTypeFactory;
    
    @Test
    public void testFindByUuid() throws Exception {
        List<String> uuidList = getAllUuids();
        
        LOG.debug("Number of elements: " + uuidList.size());
        
        for (String uuid : uuidList) {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setParent(true);
            CnATreeElement element = elementDao.findByUuid(uuid, ri);
            assertNotNull(element);
            checkScopeId(element);
         }  
    }
    
    @Test
    public void testFindByQuery() throws Exception {             
        String searchTerm = "daten";
        search(searchTerm);
        searchTerm = "data";
        search(searchTerm);
        searchTerm = "Internet";
        search(searchTerm);
    }
    
    @Test
    public void testCreateAndDelete() throws Exception {
        createAndDelete(AssetGroup.TYPE_ID, Asset.class);
        createAndDelete(ControlGroup.TYPE_ID, Control.class);
        createAndDelete(DocumentGroup.TYPE_ID, Document.class);
        createAndDelete(AuditGroup.TYPE_ID, Audit.class);
        createAndDelete(IncidentScenarioGroup.TYPE_ID, IncidentScenario.class);
    }
    
    private void createAndDelete(String groupType, Class elementClass) throws Exception {
        List<String> groupUuid = getUuidsOfType(groupType);
       
        LOG.debug("Number of " + groupType + " groups: " + groupUuid.size());
        
        List<String> uuidList = new LinkedList<String>();
        int n = 0;
        String titlePrefix = this.getClass().getSimpleName();
        for (String uuid : groupUuid) {
            CnATreeElement parent = elementDao.findByUuid(uuid, null);
            String title = titlePrefix + "_" + n;
            CnATreeElement newElement = createElement(parent, elementClass, title);
            uuidList.add(newElement.getUuid());
            n++;
        }
        checkListForSearchTerm(uuidList, titlePrefix);
        for (String uuid : uuidList) {
            CnATreeElement element = elementDao.findByUuid(uuid, RetrieveInfo.getPropertyInstance());
            assertNotNull(element);
            elementDao.delete(element);
            element = elementDao.findByUuid(uuid, RetrieveInfo.getPropertyInstance());
            assertNull(element);
            LOG.debug("Element deleted, uuid: " + uuid);
        }
    }

    protected CnATreeElement createElement(CnATreeElement parent, Class clazz, String title) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {      
        CnATreeElement element = (CnATreeElement) clazz.getConstructor(CnATreeElement.class).newInstance(parent);
        element.setTitel(title);
        elementDao.merge(element, false);
        LOG.debug("Element created: " + clazz.getSimpleName() + " / " + title);
        return element;
    }
    
    protected void search(String searchTerm) {
        String param = new StringBuilder("%").append(searchTerm).append("%").toString();
        
        LOG.debug("Searching for elements with properties containing: " + searchTerm);
        
        String hql = "select element.uuid from CnATreeElement as element " + //$NON-NLS-1$
                "inner join element.entity as entity " + //$NON-NLS-1$
                "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                "inner join propertyList.properties as props " + //$NON-NLS-1$              
                "where props.propertyValue like ?";//$NON-NLS-1$
        Object[] params = new Object[]{param};        
        List<String> uuidList = elementDao.findByQuery(hql,params);
        
        LOG.debug("Number of element containing '" + searchTerm + "': " + uuidList.size());
        
        checkListForSearchTerm(uuidList, searchTerm);
    }

    protected void checkListForSearchTerm(List<String> uuidList, String searchTerm) {
        for (String uuid : uuidList) {
            CnATreeElement element = elementDao.findByUuid(uuid, RetrieveInfo.getPropertyInstance());
            assertNotNull(element);
            boolean found = checkSearchTerm(element,searchTerm);
            assertTrue("Could not find term: " + searchTerm + " in element type/uuid: " + element.getTypeId() + "/" + element.getUuid() , found);
         }
    }

    private boolean checkSearchTerm(CnATreeElement element, String searchTerm) {
        String type = element.getTypeId();
        LOG.debug("element type/uuid: " + type + "/" + element.getUuid() );
        String[] propertyTypes = huiTypeFactory.getEntityType(type).getAllPropertyTypeIds();
        for (String propertyType : propertyTypes) {
            List<Property> propertyList = element.getEntity().getProperties(propertyType).getProperties();  
            if(checkSearchTerm(propertyList, searchTerm)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean checkSearchTerm(List<Property> propertyList, String searchTerm) {         
        for (Property property : propertyList) {
            String value = property.getPropertyValue();
            LOG.debug("checking property: " + property.getPropertyType() + " = " + value);
            if(value!=null && value.toLowerCase().contains(searchTerm.toLowerCase())) {
                if(value.length()>30) {
                    value = value.substring(0, 30) + "...";
                }
                LOG.debug(searchTerm + " found");
                return true;
            }
        }
        return false;
    }
    
    protected List<String> getAssetGroupUuids() {
        return getUuidsOfType(AssetGroup.TYPE_ID);
    }

}
