/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads all elements for a given entity type id and hydrates their titles so
 * they can be displayed in a list.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
@SuppressWarnings("serial")
public class LoadCnAElementByEntityTypeId extends GenericCommand {

    private String typeId;   
    private Integer scopeId;   
    private Integer groupId;

    private List<CnATreeElement> list = new ArrayList<CnATreeElement>();

    public LoadCnAElementByEntityTypeId(String typeId) {
        this(typeId, null, null);
    }
    
    public LoadCnAElementByEntityTypeId(String typeId, Integer scopeId) {
        this(typeId, scopeId, null);
    }
    
    public LoadCnAElementByEntityTypeId(String typeId, Integer scopeId, Integer groupId) {
        if(MassnahmenUmsetzung.TYPE_ID.equals(typeId)) {
            typeId = MassnahmenUmsetzung.HIBERNATE_TYPE_ID;
        }
        if(BausteinUmsetzung.TYPE_ID.equals(typeId)) {
            typeId = BausteinUmsetzung.HIBERNATE_TYPE_ID;
        }

        this.typeId = typeId;
        this.scopeId = scopeId;
        this.groupId= groupId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {        
        IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
        
        DetachedCriteria crit = DetachedCriteria.forClass(CnATreeElement.class);      
        crit.setFetchMode("entity", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        crit.add(Restrictions.eq("objectType", typeId));
        if(scopeId!=null) {
            crit.add(Restrictions.eq("scopeId", scopeId));
        }
        if(groupId!=null) {
            crit.add(Restrictions.eq("parentId", groupId));
        }
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        list = dao.findByCriteria(crit);
    }    

    public List<CnATreeElement> getElements() {
        return list;
    }

}
