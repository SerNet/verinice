/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyList;

/**
 * Utility class for commonly used initialization of lazy loaded collections on objects.
 * When loading objects that are passed between server and client tier they must be initialized
 * while the database session is still open. Server code can use these utility methods or use
 * own hydration or none at all depending on the use case.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class HydratorUtil {

	public static void hydrateEntity(IBaseDao dao, Entity entity) {
		if (entity == null)
			return;
		Map<String, PropertyList> lists = entity.getTypedPropertyLists();
		Set<Entry<String, PropertyList>> entrySet = lists.entrySet();
		for (Entry<String, PropertyList> entry : entrySet) {
			PropertyList list = entry.getValue();
			dao.initialize(list.getProperties());
		}
	}
	
	
	public static void hydrateElement(IBaseDao dao, CnATreeElement element, boolean includingCollections) {
		if (element == null)
			return;
		
		hydrateEntity(dao, element.getEntity());
		dao.initialize(element.getLinks());
		dao.initialize(element.getLinksDown());
		dao.initialize(element.getLinksUp());
		
		// Initialize the complete child->parent chain, since that is needed for checks
		// whether an element belongs to a specific IT-Verbund.
		for (CnATreeElement e = element.getParent(); e != null; e = e.getParent());

		if (includingCollections ) {
			dao.initialize(element.getChildren());
		}
	}
	
	public static void hydrateEntity(IBaseDao dao, CnATreeElement element) {
		hydrateEntity(dao, element.getEntity());
	}
	
	public static <T extends CnATreeElement> void hydrateElements(IBaseDao dao, List<T> elements, boolean includingCollections) {
		for (CnATreeElement element : elements) {
			hydrateElement(dao, element, includingCollections);
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
			hydrateElement(dao, gefaehrdungsUmsetzung, true);
			
			Set<CnATreeElement> children = gefaehrdungsUmsetzung.getChildren();
			for (CnATreeElement child : children) {
				if (child instanceof MassnahmenUmsetzung) {
					MassnahmenUmsetzung mn = (MassnahmenUmsetzung) child;
					hydrateElement(dao, mn, false);
				}
			}
		}
	}
}
