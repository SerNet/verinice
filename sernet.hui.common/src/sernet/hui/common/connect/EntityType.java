package sernet.hui.common.connect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityType {
	private String id;
	private String name;
	
	private List<IEntityElement> elements = new ArrayList<IEntityElement>();
	private Map<String, PropertyType> propertyTypes = new HashMap<String, PropertyType>();
	private List<PropertyGroup> propertyGroups = new ArrayList<PropertyGroup>();
	
	public void addPropertyType(PropertyType prop) {
		propertyTypes.put(prop.getId(), prop);
		elements.add(prop);
	}
	
	public void addPropertyGroup(PropertyGroup group) {
		propertyGroups.add(group);
		elements.add(group);
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
		ArrayList<PropertyType> types = new ArrayList<PropertyType>(propertyTypes.values().size());
		types.addAll(propertyTypes.values());
		return types;
	}

	public List<IEntityElement> getElements() {
		return elements;
	}

	public List<PropertyGroup> getPropertyGroups() {
		return propertyGroups;
	}

	public PropertyType getPropertyType(String id) {
		PropertyType type = this.propertyTypes.get(id);
		if (type != null)
			return type;
		
		// search in groups:
		for (PropertyGroup group : this.propertyGroups) {
			if ((type = group.getPropertyType(id)) != null)
				return type;
		}
		// none found:
		return null;
	}
	
	
}
