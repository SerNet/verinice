package sernet.hui.common.connect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PropertyGroup implements IEntityElement {
	private String id;
	private String name;
	private List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
	private HashSet<String> dependencies = new HashSet<String>();
	
	public void addPropertyType(PropertyType prop) {
		propertyTypes.add(prop);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<PropertyType> getPropertyTypes() {
		return propertyTypes;
	}
	public void setPropertyTypes(List propertyTypes) {
		this.propertyTypes = propertyTypes;
	}
	
	public PropertyType getPropertyType(String id) {
		for (PropertyType type : propertyTypes) {
			if (type.getId().equals(id))
				return type;
		}
		return null;
	}

	public boolean dependenciesFulfilled(Entity hui) {
		// no deps defined:
		if (dependencies.size() < 1)
			return true;

		// if deps defined, at least one of them must be there:
		for (Iterator iter = dependencies.iterator(); iter.hasNext();) {
			String dep = (String) iter.next();
			if (hui.isSelected(dep))
				return true;
		}
		return false;
	}

	public void setDependencies(HashSet<String> set) {
		this.dependencies = set;
	}
}
