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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
@SuppressWarnings("serial")
public class Entity implements ISelectOptionHandler, ITypedElement, Serializable  {
	
    private transient Logger log = Logger.getLogger(Entity.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(Entity.class);
        }
        return log;
    }
    
    public static final String TITLE = "ENTITY_";
    
	// map of "propertyTypeId : List of Properties"
    private Map<String, PropertyList> typedPropertyLists 
    	= new HashMap<String, PropertyList>();
    
    private transient List<IEntityChangedListener> changeListeners;
    	
    
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
	
	private synchronized List<IEntityChangedListener> getChangelisteners() {
		if (this.changeListeners == null){
			changeListeners = new ArrayList<IEntityChangedListener>();
		}
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
    
    /**
     * @param huiTypeFactory 
     * 
     */
    public void initDefaultValues(HUITypeFactory huiTypeFactory) {
        String[] types = huiTypeFactory.getEntityType(this.entityType).getAllPropertyTypeIDsIncludingGroups();
        for (String type : types) {
            PropertyType propertyType = huiTypeFactory.getPropertyType(this.entityType, type);
            if (propertyType.isNumericSelect() || propertyType.isBooleanSelect()) {
                setNumericValue(propertyType, propertyType.getNumericDefault());
            }
            else if ((propertyType.isText() || propertyType.isDate() || propertyType.isLine()) && propertyType.getDefaultRule() != null) {
                setSimpleValue(propertyType, propertyType.getDefaultRule().getValue());
            }
        }
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
	    PropertyList result = typedPropertyLists.get(propertyType);
	    if(result==null) {
	        result = new PropertyList();
	    }
		return result;
	}
	
	public Date getDate(String propertyType) {
	    Date date = null;
	    try {
	        date = new Date(Long.valueOf(getValue(propertyType)));
	    } catch (NumberFormatException t) {
            getLog().error("Error while returning date for property: " + propertyType, t);
        }
	    return date;
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
		if (propertyList == null || propertyList.getProperties().size() == 0){
			return "";
		}
		PropertyType type = HUITypeFactory.getInstance().getPropertyType(this.entityType, propertyType);
		StringBuffer result = new StringBuffer();
		
		List<IMLPropertyOption> referencedEntities = null;
		if (type.isReference()) {
			referencedEntities = type.getReferencedEntities(propertyList.getProperties()); 
			for (Iterator iter = propertyList.getProperties().iterator(); iter.hasNext();) {
				Property reference = (Property) iter.next();
				for (IMLPropertyOption resolvedReference : referencedEntities) {
					if (resolvedReference.getId().equals(reference.getPropertyValue())){
						result.append(resolvedReference.getName());
					}
				}
				if(propertyList.getProperties().indexOf(reference) != propertyList.getProperties().size() - 1){
				    result.append(", ");
				}
			}
			return result.toString();
		} else if (type.isCnaLinkReference()) {
		    return type.getReferenceResolver().getTitlesOfLinkedObjects(type.getReferencedCnaLinkType(), this.uuid);
		}
		
		// else just use the property value:
		
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
			
			if (iter.hasNext()){
				result.append(", ");
			}
		}
		return result.toString();
	}
	
	/**
     * Convenience method to return a Sring representation of the
     * given propertyType
     * 
     * @param propertyType
     * @return
     */
    public String getValue(String propertyType) {
        String result = null;
        PropertyList propertyList = typedPropertyLists.get(propertyType);
        if (propertyList != null && propertyList.getProperties().size() > 0) {
            StringBuffer sb = new StringBuffer();
            for (Iterator<Property> iter = propertyList.getProperties().iterator(); iter.hasNext();) {
                Property prop = iter.next();       
                if(prop.getPropertyValue()!=null ) {
                    sb.append(prop.getPropertyValue());
                }         
                if (iter.hasNext()){
                    sb.append(", ");
                }
            }
            result = sb.toString();
        }
        return result;
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
	
	public void setNumericValue(PropertyType type, int value) {
	    setSimpleValue(type, Integer.toString(value));
	}
	
	/**
	 * Sets the value for a given property.
	 * 
	 * <p>Since internally a property value is a multi-value this interface allows setting
	 * these values in one row.</p>
	 * 
	 * <p>Note: Using this method is preferred over modifying a {@link PropertyList} object itself.</p>
	 * 
	 * <p>Note: The actual values that are imported have to be <em>untranslated</em> IOW should directly
	 * represent the strings used in the SNCA.xml</p>
	 * @param huiTypeFactory
	 * @param propertyTypeId
	 * @param foreignProperties
	 */
	public void importProperties(HUITypeFactory huiTypeFactory, String propertyTypeId, List<String> foreignProperties) {
		PropertyList pl = typedPropertyLists.get(propertyTypeId);
        if(pl==null) {
            pl = new PropertyList();
            typedPropertyLists.put(propertyTypeId,pl);
        }
		
		// It would be possible to create a new list and make the PropertyList object
		// use that but that causes problems with hibernate. As such the existing list
		// is taken and cleared before use.
		List<Property> properties = pl.getProperties();
		if(properties==null) {
		    properties = new LinkedList<Property>();
		    pl.setProperties(properties);
		} else {
		    properties.clear();
		}
		
		
		
		for (String value : foreignProperties)
		{
		    PropertyType propertyType = huiTypeFactory.getPropertyType(this.entityType, propertyTypeId);
		    Property p = new Property();
		    
		    if (propertyType == null) {
		        getLog().warn("Property-type was not found in SNCA.xml: " + propertyTypeId + ", entity type: " + this.entityType);
		    }
		    
		    if(propertyType!=null && propertyType.isSingleSelect() && value!=null) {
		        List<IMLPropertyOption> optionList = propertyType.getOptions();
		        boolean found = false;
		        for (IMLPropertyOption option : optionList) {
		            if(value.equals(option.getName())) {
		                value = option.getId();
		                found = true;
		            } else if(value.equals(option.getId())) {
		                found = true;
		            }
                }
		        if(!found) {
		            getLog().warn("No value found for option property: " + propertyTypeId + " of entity: " + this.entityType + ". Importing unmapped value: " + value);
		        }
		    } 		
			p.setPropertyType(propertyTypeId);
			p.setPropertyValue(value);
			p.setParent(this);
			properties.add(p);
		}
	}
	
	/**
	 * Retrieves the raw, untranslated individual data values and stores them in a given
	 * list.
	 * 
	 * <p>The return values denotes the amount of values exported and can be used to find
	 * out whether any work was done.</p>
	 *  
	 * @param propertyType
	 * @param foreignProperties
	 * 
	 * @return The amount of individual values exported.
	 */
	public int exportProperties(String propertyType, List<String> foreignProperties) {
		int amount = 0;
		for (Property prop : getProperties(propertyType).getProperties())
		{
			foreignProperties.add(prop.getPropertyValue());
			amount++;
		}
		
		return amount;
	}
	
	 /**
     * Copy all property values from given entity to this one
     * 
     * @param source An Entity
     */
    public void copyEntity(Entity source) {
        List<String> emptyList = Collections.emptyList();
        copyEntity(source, emptyList);
    }
	
    /**
     * Copy all property values from given entity to this one. 
     * Properties with type from propertyTypeBlacklist will be ignored.
     * 
     * @param source An Entity
     */
    public void copyEntity(Entity source, List<String> propertyTypeBlacklist) {
        Map<String, PropertyList> sourceProperties = source.getTypedPropertyLists();
        for(Entry<String, PropertyList> entry : sourceProperties.entrySet()){
            PropertyList sourceList = entry.getValue();
            PropertyList newPropList = new PropertyList(sourceList.getProperties().size());
            for(Property sourceProp : sourceList.getProperties()) {
                if(checkProperty(sourceProp, propertyTypeBlacklist)) {
                    newPropList.add(sourceProp.copy(this));
                    if (getLog().isDebugEnabled()) {
                        getLog().debug("Prop " + entry.getKey() + " set to value: " + sourceProp.getPropertyValue());
                    }
                }
            }
            if(!newPropList.getProperties().isEmpty()) {
                typedPropertyLists.put(entry.getKey(), newPropList);
            }
        }
    }
    
    private boolean checkProperty(Property property, List<String> propertyTypeBlacklist) {
        return !property.isEmpty() && !propertyTypeBlacklist.contains(property.getPropertyType());
    }
	
	public boolean isSelected(String propertyType, String optionId) {
	    boolean result = false;
	    PropertyList propertyList = typedPropertyLists.get(propertyType);
	    if(propertyList!=null) {
    		List<Property> entries = typedPropertyLists.get(propertyType).getProperties();
    		if (entries != null) {
        		for (Property prop : entries) {
        			if (prop.getPropertyValue() != null && prop.getPropertyValue().equals(optionId)) {
        			    result = true;
        			    break;
        			}
        		}
    		}
	    }
		return result;
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
			if (isSelected(propTypeId, optionId)){
				return true;
			}
		}
		return false;
	}
	
    /**
     * @param entry
     * @throws DBException 
     */
    public Property createNewProperty(PropertyType type, String newValue)  {
    	if (type == null){
    		throw new RuntimeException("Missing property type, check XML definition.");
    	}
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
			if (typeList != null){
				typeList.add(prop);
			} else {
				typeList = new PropertyList(1);
				typeList.add(prop);
				typedPropertyLists.put(prop.getPropertyTypeID(), typeList);
			}
		} catch (AssertException e) {
			getLog().error(e);
		}
    }
    
	/* (non-Javadoc)
	 * @see sernet.snkdb.guiswt.multiselectionlist.MLEventHandler#select(sernet.snkdb.guiswt.multiselectionlist.MLOptionList, java.lang.String)
	 */
	@Override
    public void select(IMLPropertyType type, IMLPropertyOption opt) {
		createNewProperty((PropertyType)type, opt.getId());
		fireSelectionChanged(type, opt);
		
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


	/* (non-Javadoc)
	 * @see sernet.snkdb.guiswt.multiselectionlist.MLEventHandler#unselect(sernet.snkdb.guiswt.multiselectionlist.MLOptionList, java.lang.String)
	 */
	@Override
    public void unselect(IMLPropertyType type, IMLPropertyOption opt) {
		remove((PropertyType)type, opt.getId());
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
	
	public String getId() {
	    return TITLE + getDbId();
	}

    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    /**
     * @param propWeight2
     * @return
     */
    public int getInt(String propertyType) {
        PropertyList propertyList = typedPropertyLists.get(propertyType);
        if (propertyList == null || propertyList.getProperties().size() == 0){
            return Property.UNDEF;
        }
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

    /**
     * Returns the value (not the translated title)
     * of an single select option property.
     * 
     * If property with id is not single select option property
     * a warning is logged and null is returned.
     * 
     * @param 
     *      id the id of the property
     * @return 
     *      value (not the translated title) of an single select option property
     */
    public String getOptionValue(String id) {   
        String value = null;
        PropertyList propertyList = typedPropertyLists.get(id);
        if (propertyList != null && propertyList.getProperties().size()==1) {
            PropertyType type = HUITypeFactory.getInstance().getPropertyType(this.entityType, id);
            if (type.isSingleSelect()) {
                    Property prop = propertyList.getProperties().get(0);
                    value = prop.getPropertyValue();
            } else {
                getLog().warn("Property " + id + " is not of type " + PropertyType.INPUT_SINGLEOPTION + ". Can not determine option value. Entity id is: " + this.getDbId());
            }
        } else if(propertyList != null && propertyList.getProperties().size()>1) {
            getLog().warn("Property list " + id + " contains more than entry. Can not determine option value. Entity id is: " + this.getDbId());
        }
        return value;
    }

   
}
