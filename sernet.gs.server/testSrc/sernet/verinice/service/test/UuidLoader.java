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

import java.util.List;

import javax.annotation.Resource;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class UuidLoader extends ContextConfiguration {
    
    @Resource(name="cnaTreeElementDao")
    protected IBaseDao<CnATreeElement, Integer> elementDao;
    
    protected List<String> getAllUuids() {
        String hql = "select element.uuid from CnATreeElement element"; 
        return elementDao.findByQuery(hql, new Object[0]);
    }
    
    protected List<String> getUuidsOfType(String typeId) {
        String hql = "select element.uuid from CnATreeElement element where element.objectType = ?"; 
        Object[] params = new Object[]{typeId}; 
        return elementDao.findByQuery(hql, params);
    }
    
    protected List<Long> getDbidsOfType(String typeId) {
        String hql = "select element.dbid from CnATreeElement element where element.objectType = ?"; 
        Object[] params = new Object[]{typeId}; 
        return elementDao.findByQuery(hql, params);
    }
    
    protected void checkScopeId(CnATreeElement element) {
        String typeId = element.getTypeId();
        String parentTypeId = null;
        if(element.getParent()!=null) {
            parentTypeId = element.getParent().getTypeId();
        }
        if(!ISO27KModel.TYPE_ID.equals(typeId) 
           && !BSIModel.TYPE_ID.equals(typeId)
           && !ImportIsoGroup.TYPE_ID.equals(typeId)
           && !ImportBsiGroup.TYPE_ID.equals(typeId)
           && !(GefaehrdungsUmsetzung.TYPE_ID.equals(typeId) && element.getParentId()==null)
           && !ImportBsiGroup.TYPE_ID.equals(parentTypeId)
           && !ImportIsoGroup.TYPE_ID.equals(parentTypeId)) {
            assertNotNull("Scope-Id is null, uuid: " + element.getUuid() + ", type: " + element.getTypeId(), element.getScopeId());
        } 
    }
}
