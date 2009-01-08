package sernet.gs.ui.rcp.main.common.model;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyList;

public class HydratorUtil {

	public static void hydrateEntity(Entity entity) {
		if (entity == null)
			return;
		Map<String, PropertyList> lists = entity.getTypedPropertyLists();
		Set<Entry<String, PropertyList>> entrySet = lists.entrySet();
		for (Entry<String, PropertyList> entry : entrySet) {
			PropertyList list = entry.getValue();
			list.getProperties().size();
		}
	}
	
	public static void hydrateElement(CnATreeElement element) {
		element.getChildren().size();
		element.getLinksDown().size();
		element.getLinksUp().size();
	}

}
