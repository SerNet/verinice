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
package sernet.verinice.model.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Utility class for commonly used initialization of lazy loaded collections on objects.
 * When loading objects that are passed between server and client tier they must be initialized
 * while the database session is still open. Server code can use these utility methods or use
 * own hydration or none at all depending on the use case.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class HydratorUtil {
	
	private HydratorUtil(){}

	public static void hydrateEntity(IBaseDao dao, Entity entity) {
		if (dao == null) {
			Logger.getLogger(HydratorUtil.class).error("Missing DAO, cannot hydrate.");
			return;
		}
		if (entity == null){
			return;
		}
		Map<String, PropertyList> lists = entity.getTypedPropertyLists();
		Set<Entry<String, PropertyList>> entrySet = lists.entrySet();
		for (Entry<String, PropertyList> entry : entrySet) {
			PropertyList list = entry.getValue();
			List<Property> propertyList = list.getProperties();
			dao.initialize(propertyList);
			//set the parent in the property since it is not mapped by hibernate anymore
			for (Property property : propertyList) {
				property.setParent(entity);
			}
		}
	}
	
	public static void hydrateElement(IBaseDao dao, CnATreeElement element, boolean retrieveChildren) {
		RetrieveInfo ri = null;
		if(retrieveChildren) {
			ri = new RetrieveInfo();
			ri.setChildren(true);
		}
		hydrateElement(dao, element, ri);
	}
	
	
	public static void hydrateElement(IBaseDao dao, CnATreeElement element, RetrieveInfo ri) {
		if (element == null){
			return;
		}
		hydrateEntity(dao, element.getEntity());

		// Initialize permissions, so it should be possible to access an elements'
		// permissions anywhere. 
		for (Permission p : element.getPermissions())
		{
			p.getRole();
			p.isReadAllowed();
			p.isWriteAllowed();
		}
		
		// Initialize the complete child->parent chain, since that is needed for checks
		// whether an element belongs to a specific IT-Verbund.
		for (CnATreeElement e = element.getParent(); e != null; e = e.getParent()){}

		if (ri!=null ) {
			CnATreeElement elementWithChildren = (CnATreeElement) dao.retrieve(element.getDbId(),ri);
			if(elementWithChildren!=null) {
				element.setChildren(elementWithChildren.getChildren());
			}
		}
		
	}
	
	public static void hydrateEntity(IBaseDao dao, CnATreeElement element) {
		hydrateEntity(dao, element.getEntity());
	}
	
	public static <T extends CnATreeElement> void hydrateElements(IBaseDao dao, List<T> elements, boolean retrieveChildren) {
		RetrieveInfo ri = null;
		if(retrieveChildren) {
			ri = new RetrieveInfo();
			ri.setChildren(true);
		}
		hydrateElements(dao, elements, ri);
	}
	
	public static <T extends CnATreeElement> void hydrateElements(IBaseDao dao, List<T> elements, RetrieveInfo ri) {
		for (CnATreeElement element : elements) {
			hydrateElement(dao, element, ri);
		}
	}


	public static void hydrateElement(
			IBaseDao<Configuration, Serializable> dao,
			Configuration configuration, boolean includingCollections) {
		hydrateEntity(dao, configuration.getEntity());
	}

	/**
	 * @param dao
	 * @param finishedRiskLists
	 */
	public static void hydrateElement(
			IBaseDao<FinishedRiskAnalysisLists, Serializable> dao,
			FinishedRiskAnalysisLists finishedRiskLists) {
	    if(finishedRiskLists!=null) {
	        List<GefaehrdungsUmsetzung> list = finishedRiskLists.getAllGefaehrdungsUmsetzungen();
    		for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : list) {
    			hydrateEntity(dao, gefaehrdungsUmsetzung.getEntity());
    		}
    		
    		List<GefaehrdungsUmsetzung> list2 = finishedRiskLists.getAssociatedGefaehrdungen();
    		dao.initialize(list2);
    		for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : list2) {
    			hydrateEntity(dao, gefaehrdungsUmsetzung.getEntity());
    		}
    		
    		List<GefaehrdungsUmsetzung> list3 = finishedRiskLists.getNotOKGefaehrdungsUmsetzungen();
    		dao.initialize(list3);
    		for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : list3) {
    			hydrateEntity(dao, gefaehrdungsUmsetzung.getEntity());
    			RetrieveInfo ri = new RetrieveInfo();
    			ri.setChildren(true);
    			hydrateElement(dao, gefaehrdungsUmsetzung, ri);
    			
    			Set<CnATreeElement> children = gefaehrdungsUmsetzung.getChildren();
    			for (CnATreeElement child : children) {
    				if (child instanceof MassnahmenUmsetzung) {
    					MassnahmenUmsetzung mn = (MassnahmenUmsetzung) child;
    					hydrateElement(dao, mn, null);
    				}
    			}
    		}
	    }
	}
}
