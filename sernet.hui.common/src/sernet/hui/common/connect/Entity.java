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
package sernet.hui.common.connect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.common.multiselectionlist.ISelectOptionHandler;
import sernet.snutils.AssertException;
import sernet.snutils.DBException;
import sernet.snutils.FormInputParser;
import sernet.snutils.Tester;

/**
 * This class defines an entity to be used as a DA-object in applications.
 * 
 * This is a practical implementation of a dynamic object model, but without
 * dynamic entities. The reasoning behind this is that the defined entities 
 * (i.e "customer") usually do not change much for a given 
 * application, but their properties will ("tel. no.", "mobile no.", "home no."...).
 * 
 * Relations between entities are also modeled as properties, which allows
 * for arbitrary relations between entities.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class Entity implements ISelectOptionHandler, ITypedElement, Serializable  {
	
	// map of "propertyTypeId : List of Properties"
    private Map<String, PropertyList> typedPropertyLists 
    	= new HashMap<String, PropertyList>();
    
    private transient ArrayList<IEntityChangedListener> changeListeners;
    	
    
	private String entityType;

	private Integer dbId;
	
	private String uuid;

    public static final String TYPE_ID = "huientity";
	
	public Integer getDbId() {
		return dbId;
	}
	
	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public String getEntityType() {
		return entityType;
	}
	
	public void addChangeListener(IEntityChangedListener changeListener) {
		getChangelisteners().add(changeListener);
	}
	
	private synchronized ArrayList<IEntityChangedListener> getChangelisteners() {
		if (this.changeListeners == null)
			changeListeners = new ArrayList<IEntityChangedListener>();
		return changeListeners;
	}

	public void removeListener(IEntityChangedListener listener) {
		getChangelisteners().remove(listener);
	}
	
	protected Entity() {
		uuid = UUID.randomUUID().toString();
	}
	
    public Entity(String entType) {
    	this();
        this.entityType = entType;
    }
    
    @Override
    public int hashCode() {
    	return uuid.hashCode();
    }
    
	@Override
	public boolean equals(Object obj) {
		return (this == obj
				|| (obj instanceof Entity
					&& this.uuid.equals(((Entity)obj).getUuid())
					)
				);
	}
    
    
    public Map<String, PropertyList> getTypedPropertyLists() {
		return typedPropertyLists;
	}

	public void setTypedPropertyLists(Map<String, PropertyList> typedPropertyLists) {
		this.typedPropertyLists = typedPropertyLists;
	}

	/**
	 * @return Returns all set properties of the given type.
	 */
	public PropertyList getProperties(String propertyType) {
		PropertyList result = typedPropertyLists.get(propertyType) != null 
								? typedPropertyLists.get(propertyType)
								: new PropertyList();
		return result;
	}
	
	/**
	 * Convenience method to return a Sring representation of the
	 * given propertyType
	 * 
	 * @param propertyType
	 * @return
	 */
	public String getSimpleValue(String propertyType) {
		PropertyList propertyList = typedPropertyLists.get(propertyType);
		if (propertyList == null || propertyList.getProperties().size() == 0)
			return "";

		PropertyType type = HUITypeFactory.getInstance().getPropertyType(this.entityType, 
				propertyType);
		StringBuffer result = new StringBuffer();
		
		List<IMLPropertyOption> referencedEntities = new ArrayList<IMLPropertyOption>();
		if (type.isReference()) {
			referencedEntities = type.getReferencedEntities(propertyList.getProperties()); 
			for (Iterator iter = propertyList.getProperties().iterator(); iter.hasNext();) {
				Property reference = (Property) iter.next();
				for (IMLPropertyOption resolvedReference : referencedEntities) {
					if (resolvedReference.getId().equals(reference.getPropertyValue()))
						result.append(resolvedReference.getName());
				}
			}
			return result.toString();
		}
		
		for (Iterator iter = propertyList.getProperties().iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			
			if (type.isSingleSelect()
					|| type.isMultiselect()) {
					PropertyOption option = type.getOption(prop.getPropertyValue());
					result.append(option != null ? option.getName() : "");
			}
			
			else if (type.isDate()) {
				try {
					result.append(FormInputParser.dateToString(new java.sql.Date(
							Long.parseLong(prop.getPropertyValue()))));
				} catch (NumberFormatException e) {
					// skip value
				} catch (AssertException e) {
					// skip value
				}
			}
			else if(prop.getPropertyValue()!=null ) {
				result.append(prop.getPropertyValue());
			}
			
			if (iter.hasNext())
				result.append(", ");
		}
		return result.toString();
	}
	
	public void setSimpleValue(PropertyType type, String value) {
		PropertyList list = typedPropertyLists.get(type.getId());
		if (list == null || list.getProperties().size() == 0) {
			createNewProperty(type, value);
		}
		else {
				list.getProperty(0).setPropertyValue(value);
		}
	}
	
	/**
	 * Copy all property values from given entity to this one.
	 * @param source
	 */
	public void copyEntity(Entity source) {
		Map<String, PropertyList> sourceProperties = source.getTypedPropertyLists();
		for (String propType : sourceProperties.keySet()) {
			PropertyList sourceList = sourceProperties.get(propType);
			
			PropertyList newPropList = new PropertyList(sourceList.getProperties().size());
			for (Property sourceProp : sourceList.getProperties()) {
				// do not copy empty values:
				if (sourceProp.getPropertyValue() != null
						&& !sourceProp.getPropertyValue().equals("")) {
					Property property = new Property(this);
					property.setPropertyType(propType);
					property.setPropertyValue(
							sourceProp.getPropertyValue(),
							false /*no property change fired*/,
							source);
					newPropList.add(property);
				}
			}
			if (newPropList.getProperties().size() > 0)
				typedPropertyLists.put(propType, newPropList);
		}
	}
	
	public boolean isSelected(String propertyType, String optionId) {
		List<Property> entries = typedPropertyLists.get(propertyType).getProperties();
		if (entries == null)
			return false;
		
		for (Property prop : entries) {
			if (prop.getPropertyValue() != null && prop.getPropertyValue().equals(optionId))
				return true;
		}
		return false;
	}
	
	/**
	 * Check if given option is selected for any of the properties.
	 * 
	 * @param optionId
	 * @return
	 */
	public boolean isSelected(String optionId) {
		for (Iterator iter = typedPropertyLists.keySet().iterator(); iter.hasNext();) {
			String propTypeId = (String) iter.next();
			if (isSelected(propTypeId, optionId))
				return true;
		}
		return false;
	}
	
    /**
     * @param entry
     * @throws DBException 
     */
    public Property createNewProperty(PropertyType type, String newValue)  {
    	if (type == null)
    		throw new RuntimeException("Missing property type, check XML definition.");
    	
    	Property entry = PropertyFactory.create(type, newValue, this);
    	addProperty(entry);
        return entry;
    }
    
    public void remove(PropertyType type, String value) {
    	PropertyList list = typedPropertyLists.get(type.getId());
    	List<Property> setProperties = list.getProperties();
    	for (Iterator iter = setProperties.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			if (prop.getPropertyValue().equals(value)) {
				iter.remove();
			}
		}
    }
    
   
    
    /**
     * Checks if all properties are of the same type.
     * 
     * @param type
     * @param properties
     * @throws PropertyTypeException
     * @throws AssertException
     */
    private void checkSameType(String type, List properties) throws PropertyTypeException {
    	for (Iterator iter = properties.iterator(); iter.hasNext();) {
			Property prop = (Property) iter.next();
			if (!prop.getPropertyTypeID().equals(type))
				throw new PropertyTypeException("Falsche Typenzuordnung in dynamischer Dokumentation.");
		}
    }
    
    /**
     * Add a new property to the list of already present properties for its type.
     * 
     * @param prop
     * @throws AssertException 
     * @throws AssertException
     */
    private void addProperty(Property prop) {
		try {
			Tester.assertTrue("Eigenschaft nicht mehr definiert für Wert: '" 
					+ prop.getPropertyValue() + "'.",
					prop.getPropertyTypeID() != null);
			PropertyList typeList = this.typedPropertyLists.get(prop.getPropertyTypeID());
			if (typeList != null)
				typeList.add(prop);
			else {
				typeList = new PropertyList(1);
				typeList.add(prop);
				typedPropertyLists.put(prop.getPropertyTypeID(), typeList);
			}
		} catch (AssertException e) {
			Logger.getLogger(Entity.class).error(e);
		}
    }
    
	/* (non-Javadoc)
	 * @see sernet.snkdb.guiswt.multiselectionlist.MLEventHandler#select(sernet.snkdb.guiswt.multiselectionlist.MLOptionList, java.lang.String)
	 */
	public void select(IMLPropertyType type, IMLPropertyOption opt) {
		createNewProperty((PropertyType)type, opt.getId());
		if (isDependency(opt))
			fireDependencyChanged(type, opt);
		else 
			fireSelectionChanged(type, opt);
		
	}
	
	/**
	 * @param type
	 * @param opt
	 */
	private void fireDependencyChanged(IMLPropertyType type, IMLPropertyOption opt) {
		for (IEntityChangedListener listener : getChangelisteners()) {
			listener.dependencyChanged(type, opt);
		}
	}
	
	/**
	 * @param type
	 * @param opt
	 */
	private void fireSelectionChanged(IMLPropertyType type, IMLPropertyOption opt) {
		for (IEntityChangedListener listener : getChangelisteners()) {
			listener.selectionChanged(type, opt);
		}
	}
	
	void firePropertyChanged(Property prop, Object source) {
		for (IEntityChangedListener listener : getChangelisteners()) {
			listener.propertyChanged(new PropertyChangedEvent(this, prop, source));
		}
	}
	
	/**
	 * Checks if the given option is listed as a dependency for any other property.
	 */
	public boolean isDependency(IMLPropertyOption opt) {
		return HUITypeFactory.getInstance().isDependency(opt);
	}


	/* (non-Javadoc)
	 * @see sernet.snkdb.guiswt.multiselectionlist.MLEventHandler#unselect(sernet.snkdb.guiswt.multiselectionlist.MLOptionList, java.lang.String)
	 */
	public void unselect(IMLPropertyType type, IMLPropertyOption opt) {
		remove((PropertyType)type, opt.getId());
		if (isDependency(opt))
			fireDependencyChanged(type, opt);
		else 
			fireSelectionChanged(type, opt);
	}

	

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }

    /**
     * @param propWeight2
     * @return
     */
    public int getInt(String propertyType) {
        PropertyList propertyList = typedPropertyLists.get(propertyType);
        if (propertyList == null || propertyList.getProperties().size() == 0)
            return Property.UNDEF;

        PropertyType type = HUITypeFactory.getInstance().getPropertyType(this.entityType, 
                propertyType);
        for (Iterator iter = propertyList.getProperties().iterator(); iter.hasNext();) {
            Property prop = (Property) iter.next();
            if (type.isNumericSelect()) {
                return prop.getNumericPropertyValue(); 
            }
        }
        return Property.UNDEF;
        
    }

   
}
