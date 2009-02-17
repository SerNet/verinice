package sernet.gs.ui.rcp.main.common.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
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
		hydrateEntity(dao, element.getEntity());
		dao.initialize(element.getLinks());
		dao.initialize(element.getLinksDown());
		dao.initialize(element.getLinksUp());

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



}
