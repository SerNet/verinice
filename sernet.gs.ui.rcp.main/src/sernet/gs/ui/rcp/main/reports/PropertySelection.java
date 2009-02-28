package sernet.gs.ui.rcp.main.reports;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Which properties of an item should be output as columns?
 * The selection made by the user or programattically is
 * saved here.
 * 
 * @author koderman@sernet.de
 *
 */
public class PropertySelection implements Serializable {
	HashMap<String, List<String>> shownPropertyTypes;
	
	public PropertySelection() {
		this.shownPropertyTypes = new HashMap<String, List<String>>();
	}

	public List<String> get(String entityType) {
		List<String> fieldList = shownPropertyTypes.get(entityType);
		return fieldList != null ? fieldList : new ArrayList<String>();
	}


	public void add(String entityTypeId, String propertyTypeId) {
		List<String> properties = shownPropertyTypes.get(entityTypeId);
		if (properties == null) {
			properties = new ArrayList<String>();
			shownPropertyTypes.put(entityTypeId, properties);
		}
		if (! properties.contains(propertyTypeId))
			properties.add(propertyTypeId);
	}
	
//	//  remove debug method
//	public void printall() {
//		for (String key : shownPropertyTypes.keySet()) {
//			for (String value : shownPropertyTypes.get(key)) {
//				System.out.println(value);
//			}
//		}
//	}
	
	
}
