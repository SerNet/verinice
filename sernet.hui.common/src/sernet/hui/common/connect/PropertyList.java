package sernet.hui.common.connect;

import java.util.ArrayList;
import java.util.List;

/**
 * All properties of a given type for one entity.
 * 
 * @author koderman@sernet.de
 *
 */
public class PropertyList {
	private List<Property> properties;
	
	private Integer dbId;
	
	
	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public PropertyList(int size) {
		properties = new ArrayList<Property>(size);
	}

	public PropertyList() {
	}

	public List<Property> getProperties() {
		return properties;
	}

	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}

	public void add(Property prop) {
		properties.add(prop);
	}
	
	
	public Property getProperty(int idx)  {
			if (properties != null && properties.size() > 0)
				return properties.get(idx);
			else
				return null;
	}

}
