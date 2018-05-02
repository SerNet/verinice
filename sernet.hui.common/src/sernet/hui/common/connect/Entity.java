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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.hui.common.multiselectionlist.ISelectOptionHandler;
import sernet.snutils.AssertException;
import sernet.snutils.FormInputParser;
import sernet.snutils.HuiRuntimeException;
import sernet.snutils.Tester;

/**
 * This class defines an entity to be used as a DA-object in applications.
 * 
 * This is a practical implementation of a dynamic object model, but without
 * dynamic entities. The reasoning behind this is that the defined entities (i.e
 * "customer") usually do not change much for a given application, but their
 * properties will ("tel. no.", "mobile no.", "home no."...).
 * 
 * Relations between entities are also modeled as properties, which allows for
 * arbitrary relations between entities.
 * 
 * @author koderman[at]sernet[dot]de Initial implementation
 * @author Daniel Murygin <dm[at]sernet[dot]d> Refactoring
 */
@SuppressWarnings("serial")
public class Entity implements ISelectOptionHandler, ITypedElement, Serializable {

    public static final String TITLE = "ENTITY_";
    public static final String TYPE_ID = "huientity";

    private static final Logger logger = Logger.getLogger(Entity.class);

    private String uuid;
    private Integer dbId;
    private String entityType;

    // key: propertyTypeId, value: PropertyList with propertyTypeId
    private Map<String, PropertyList> typedPropertyLists = new HashMap<>();

    private transient List<IEntityChangedListener> changeListeners;

    /**
     * This map caches the values of properties which are defined with input
     * type "reference" in SNCA.xml. The map is used in method
     * {@link #getValue(String)} and
     * {@link #getValueOfReferenceProperty(PropertyType)}
     */
    private Map<String, String> referenceValueCache;

    protected Entity() {
        uuid = UUID.randomUUID().toString();
    }

    public Entity(String entType) {
        this();
        this.entityType = entType;
    }

    /**
     * Sets the default values of properties as defined in configuration file
     * SNCA.xml
     * 
     * @param huiTypeFactory
     */
    public void initDefaultValues(HUITypeFactory huiTypeFactory) {
        EntityType entityTypeFromFactory = huiTypeFactory.getEntityType(this.entityType);
        if (entityTypeFromFactory == null) {
            throw new IllegalArgumentException("Cannot initialize default values for " + this
                    + " from the given HUITypeFactory since it does not support the entity type.");
        }
        String[] types = entityTypeFromFactory.getAllPropertyTypeIds();
        for (String type : types) {
            PropertyType propertyType = huiTypeFactory.getPropertyType(this.entityType, type);
            if (propertyType.isNumericSelect() || propertyType.isBooleanSelect()) {
                setNumericValue(propertyType, propertyType.getNumericDefault());
            } else if ((propertyType.isText() || propertyType.isDate() || propertyType.isLine())
                    && propertyType.getDefaultRule() != null) {
                setSimpleValue(propertyType, propertyType.getDefaultRule().getValue());
            }
        }
    }

    /**
     * Convenience method to return a String representation of the given
     * propertyType. If there is no value with the propertyType in the database
     * an empty String is returned.
     * 
     * See SNCA.xml for valid propertyTypes.
     * 
     * @param propertyTypeId
     *            The type id of a property
     * @return The value of the property or an empty String if there is no value
     *         in the database
     * @deprecated Replaced by {@link #getPropertyValue(String)} because of the
     *             improper name
     */
    @Deprecated
    public String getSimpleValue(String propertyTypeId) {
        return getPropertyValue(propertyTypeId);
    }

    /**
     * Convenience method to return a String representation of the given
     * propertyTypeId. If there is no value with the propertyType in the
     * database an empty String is returned.
     * 
     * See SNCA.xml for valid propertyTypes.
     * 
     * This method replaces deprecated method {@link #getSimpleValue(String)}.
     * 
     * @param propertyType
     *            The type id of a property
     * @return The value of the property or an empty String if there is no value
     *         in the database
     */
    public String getPropertyValue(String propertyTypeId) {
        PropertyType propertyType = HUITypeFactory.getInstance().getPropertyType(this.entityType,
                propertyTypeId);

        if (propertyType == null) {
            return String.valueOf("");
        }

        if (propertyType.isReference()) {
            return getValueOfReferenceProperty(propertyType);
        }

        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        StringBuilder sb = new StringBuilder();
        if (propertyList != null) {
            boolean firstProperty = true;
            for (Property property : propertyList.getProperties()) {
                if (!firstProperty) {
                    sb.append(", ");
                }
                String value;
                if (propertyType.isSingleSelect() || propertyType.isMultiselect()) {
                    value = getValueOfOptionProperty(propertyType, property);
                } else if (propertyType.isDate()) {
                    value = getValueOfDateProperty(property);
                } else {
                    value = property.getPropertyValue();
                }

                if (value != null) {
                    sb.append(value);
                }
                firstProperty = false;
            }
        }
        return sb.toString();
    }

    /**
     * Convenience method to set a String representation of the given
     * propertyTypeId.
     * 
     * See SNCA.xml for valid propertyTypes.
     * 
     * @param propertyTypeId
     *            The type id of a property
     * @param value
     *            The new value of a property
     */
    public void setPropertyValue(String propertyTypeId, String value) {
        PropertyType propertyType = HUITypeFactory.getInstance().getPropertyType(this.entityType,
                propertyTypeId);
        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        if (propertyType.isReference()) {
            typedPropertyLists.put(propertyTypeId, null);
            setMultiselectProperty(propertyTypeId, value);
        } else if (propertyType.isMultiselect()) {
            typedPropertyLists.put(propertyTypeId, null);
            setMultiselectProperty(propertyTypeId, value);
        } else {
            if (propertyList != null) {
                for (Property property : propertyList.getProperties()) {
                    if (propertyType.isDate()) {
                        setDateProperty(value, property);
                    } else {
                        property.setPropertyValue(value);
                    }
                }
            }
        }
    }

    private String getValueOfReferenceProperty(PropertyType type) {
        String propertyTypeId = type.getId();
        if (!type.isReference()) {
            throw new HuiRuntimeException(
                    "Type of property with type id " + propertyTypeId + " is not 'reference'");
        }
        String value = getReferenceValueCache().get(propertyTypeId);
        if (value == null) {
            value = loadValueOfReferenceProperty(type);
            getReferenceValueCache().put(propertyTypeId, value);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Reference value found in cache: " + value + ", property type id: "
                    + propertyTypeId + ", entity db id: " + getDbId());
        }
        return value;
    }

    private String loadValueOfReferenceProperty(PropertyType type) {
        String propertyTypeId = type.getId();
        StringBuilder sb = new StringBuilder();
        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        if (propertyList != null) {
            List<IMLPropertyOption> referencedEntities = type
                    .getReferencedEntities(propertyList.getProperties());
            boolean first = true;
            for (IMLPropertyOption referenceEntity : referencedEntities) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(referenceEntity.getName());
                first = false;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Reference value loaded from db: " + sb.toString()
                        + ", property type id: " + propertyTypeId + ", entity db id: " + getDbId());
            }
        }
        return sb.toString();
    }

    private String getValueOfOptionProperty(PropertyType type, Property property) {
        PropertyOption option = type.getOption(property.getPropertyValue());
        return (option != null) ? option.getName() : "";
    }

    private void setMultiselectProperty(String propertyTypeId, String value) {
        String[] propertyOptions = value.split(",");
        for (String propertyOptionValue : propertyOptions) {
            if (StringUtils.isNotEmpty(propertyOptionValue)) {
                createNewProperty(propertyTypeId, propertyOptionValue);
            }
        }
    }

    private void setDateProperty(String value, Property property) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(FormInputParser.stringToDate(value.trim()).getTime());
            property.setPropertyValue(calendar, false, null);
        } catch (AssertException e) {
            logger.error("Exception while setting the value of a date property", e);
        }
    }

    private String getValueOfDateProperty(Property property) {
        String date = null;
        String propertyValue = property.getPropertyValue();
        if (propertyValue == null) {
            return date;
        }
        propertyValue = propertyValue.trim();
        if (propertyValue.isEmpty()) {
            return date;
        }
        try {
            date = FormInputParser.dateToString(new java.sql.Date(Long.parseLong(propertyValue)));
        } catch (NumberFormatException | AssertException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Exception while getting the value of a date property", e);
            }
            // Skip this value and continue processing
        }
        return date;
    }

    /**
     * Returns the property value as a Java date. If the property value can not
     * be converted to a date an error message is logged an null is returned.
     * 
     * @param propertyTypeId
     *            The type id of a property
     * @return The property value as a Java date or null if the value can not be
     *         converted to a date
     */
    public Date getDate(String propertyTypeId) {
        Date date = null;
        try {
            date = new Date(Long.valueOf(getRawPropertyValue(propertyTypeId)));
        } catch (NumberFormatException t) {
            logger.error("Error while returning date for property: " + propertyTypeId, t);
        }
        return date;
    }

    /**
     * Returns the property value as a date (without the time) in ISO 8601
     * format.
     * 
     * e.g.: 1975-09-25 or 2004-05-24
     * 
     * If the property value can not be converted to a date an error message is
     * logged an null is returned.
     * 
     * @see https://en.wikipedia.org/wiki/ISO_8601
     * @param propertyTypeId
     *            The type id of a property
     * @return The property value as a date in ISO 8601 format or null if the
     *         value can not be converted to a date
     */
    public String getDateInISO8601(String propertyTypeId) {
        String dateInISO8601 = null;
        Date date = getDate(propertyTypeId);
        if (date != null) {
            dateInISO8601 = new SimpleDateFormat("yyyy-MM-dd").format(date);
        }
        return dateInISO8601;
    }

    /**
     * This method returns the value of a property just like it is saved in the
     * database. In contrast to {@link #getPropertyValue(String)} this method
     * does not do any formatting or conversion.
     * 
     * @param propertyTypeId
     *            The type id of a property
     * @return The raw value of property
     * @deprecated Replaced by {@link #getRawPropertyValue(String)} because of
     *             the improper name
     */
    @Deprecated
    public String getValue(String propertyTypeId) {
        return getRawPropertyValue(propertyTypeId);
    }

    /**
     * This method returns the value of a property just like it is saved in the
     * database. In contrast to {@link #getPropertyValue(String)} this method
     * does not do any formatting or conversion.
     * 
     * This method replaces deprecated method {@link #getValue(String)}.
     * 
     * @see {@link #getPropertyValue(String)}
     * @param propertyTypeId
     *            The type id of a property
     * @return The raw value of property
     */
    public String getRawPropertyValue(String propertyTypeId) {
        String result = null;
        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        if (propertyList != null && propertyList.getProperties().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Iterator<Property> iter = propertyList.getProperties().iterator(); iter
                    .hasNext();) {
                Property prop = iter.next();
                if (prop.getPropertyValue() != null) {
                    sb.append(prop.getPropertyValue());
                }
                if (iter.hasNext()) {
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
        } else {
            list.getProperty(0).setPropertyValue(value);
        }
    }

    public Integer getNumericValue(String propertyType) {
        try {
            return Integer.valueOf(getPropertyValue(propertyType));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void setNumericValue(PropertyType type, int value) {
        setSimpleValue(type, Integer.toString(value));
    }

    /**
     * Copy all property values from given entity to this one
     * 
     * @param source
     *            The source entity for copying
     */
    public void copyEntity(Entity source) {
        List<String> emptyList = Collections.emptyList();
        copyEntity(source, emptyList);
    }

    /**
     * Sets the value for a given property.
     * 
     * <p>
     * Since internally a property value is a multi-value this interface allows
     * setting these values in one row.
     * </p>
     * 
     * <p>
     * Note: Using this method is preferred over modifying a
     * {@link PropertyList} object itself.
     * </p>
     * 
     * <p>
     * Note: The actual values that are imported have to be
     * <em>untranslated</em> IOW should directly represent the strings used in
     * the SNCA.xml
     * </p>
     * 
     * @param huiTypeFactory
     * @param propertyTypeId
     * @param foreignProperties
     */
    public void importProperties(HUITypeFactory huiTypeFactory, String propertyTypeId,
            List<String> foreignProperties, List<Boolean> foreignLimitedLicense,
            List<String> foreignContentId, boolean licenseManagement) {
        PropertyList pl = typedPropertyLists.get(propertyTypeId);
        if (pl == null) {
            pl = new PropertyList();
            typedPropertyLists.put(propertyTypeId, pl);
        }

        // It would be possible to create a new list and make the PropertyList
        // object
        // use that but that causes problems with hibernate. As such the
        // existing list
        // is taken and cleared before use.
        List<Property> properties = pl.getProperties();
        if (properties == null) {
            properties = new LinkedList<Property>();
            pl.setProperties(properties);
        } else {
            properties.clear();
        }

        for (int i = 0; i < foreignProperties.size(); i++) {
            String value = foreignProperties.get(i);
            PropertyType propertyType = huiTypeFactory.getPropertyType(this.entityType,
                    propertyTypeId);
            Property p = new Property();

            if (propertyType == null) {
                logger.warn("Property-type was not found in SNCA.xml: " + propertyTypeId
                        + ", entity type: " + this.entityType);
            }

            if (propertyType != null && propertyType.isSingleSelect() && value != null
                    && !value.isEmpty()) {
                List<IMLPropertyOption> optionList = propertyType.getOptions();
                boolean found = false;
                for (IMLPropertyOption option : optionList) {
                    if (value.equals(option.getName())) {
                        value = option.getId();
                        found = true;
                    } else if (value.equals(option.getId())) {
                        found = true;
                    }
                }
                if (!found) {
                    logger.warn(
                            "No value found for option property: " + propertyTypeId + " of entity: "
                                    + this.entityType + ". Importing unmapped value: " + value);
                }
            }
            p.setPropertyType(propertyTypeId);
            p.setPropertyValue(value);
            p.setParent(this);
            if (licenseManagement && foreignContentId.size() > 0
                    && foreignLimitedLicense.size() > 0) {
                p.setLimitedLicense(foreignLimitedLicense.get(i));
                p.setLicenseContentId(foreignContentId.get(i));
            }
            properties.add(p);
        }
    }

    /**
     * Retrieves the raw, untranslated individual data values and stores them in
     * a given list.
     * 
     * <p>
     * The return values denotes the amount of values exported and can be used
     * to find out whether any work was done.
     * </p>
     * 
     * @param propertyType
     * @param foreignProperties
     * 
     * @return The amount of individual values exported.
     */
    public int exportProperties(String propertyType, List<String> foreignProperties,
            List<Boolean> foreignIsLicenseLimited, List<String> foreignContentId) {
        int amount = 0;
        for (Property prop : getProperties(propertyType).getProperties()) {
            foreignProperties.add(prop.getPropertyValue());
            foreignIsLicenseLimited
                    .add(prop.isLimitedLicense() != null ? prop.isLimitedLicense() : false);
            foreignContentId
                    .add(prop.getLicenseContentId() != null ? prop.getLicenseContentId() : "");
            amount++;
        }

        return amount;
    }

    /**
     * Copy all property values from given entity to this one. Properties with
     * ids from list propertyTypeBlacklist will be ignored.
     * 
     * @param source
     *            The source entity for copying
     * @param propertyTypeBlacklist
     *            A list with property ids which will not be copied
     */
    public void copyEntity(Entity source, List<String> propertyTypeBlacklist) {
        Map<String, PropertyList> sourceProperties = source.getTypedPropertyLists();
        for (Entry<String, PropertyList> propertyListMapEntry : sourceProperties.entrySet()) {
            copyPropertyList(propertyListMapEntry, propertyTypeBlacklist);
        }
    }

    private void copyPropertyList(Entry<String, PropertyList> propertyListMapEntry,
            List<String> propertyTypeBlacklist) {
        PropertyList sourcePropertyList = propertyListMapEntry.getValue();
        PropertyList newPropertyList = new PropertyList(sourcePropertyList.getProperties().size());
        for (Property sourceProp : sourcePropertyList.getProperties()) {
            if (checkProperty(sourceProp, propertyTypeBlacklist)) {
                newPropertyList.add(sourceProp.copy(this));
                if (logger.isDebugEnabled()) {
                    logger.debug("Prop " + propertyListMapEntry.getKey() + " set to value: "
                            + sourceProp.getPropertyValue());
                }
            }
        }
        if (!newPropertyList.getProperties().isEmpty()) {
            typedPropertyLists.put(propertyListMapEntry.getKey(), newPropertyList);
        }
    }

    private boolean checkProperty(Property property, List<String> propertyTypeBlacklist) {
        return !property.isEmpty() && !propertyTypeBlacklist.contains(property.getPropertyType());
    }

    /**
     * Check if given option is selected for any of the properties.
     * 
     * @param optionId
     * @return
     */
    public boolean isSelected(String optionId) {
        for (String propertyTypeId : typedPropertyLists.keySet()) {
            if (isSelected(propertyTypeId, optionId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSelected(String propertyTypeId, String optionId) {
        boolean result = false;
        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        if (propertyList != null) {
            List<Property> entries = typedPropertyLists.get(propertyTypeId).getProperties();
            if (entries != null) {
                for (Property prop : entries) {
                    if (prop.getPropertyValue() != null
                            && prop.getPropertyValue().equals(optionId)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public Property createNewProperty(PropertyType propertyType, String newValue) {
        if (propertyType == null) {
            throw new HuiRuntimeException("Property type is null.");
        }
        Property entry = PropertyFactory.create(propertyType, newValue, this);
        addProperty(entry);
        return entry;
    }

    public Property createNewProperty(String propertyTypeId, String propertyValue) {
        if (propertyTypeId == null) {
            throw new HuiRuntimeException("Property type id is null");
        }
        Property entry = PropertyFactory.create(propertyTypeId, propertyValue, this);
        addProperty(entry);
        return entry;
    }

    /**
     * Add a new property to the list of already present properties for its
     * type.
     * 
     * @param property
     *            A property
     */
    private void addProperty(Property property) {
        try {
            Tester.assertTrue("Eigenschaft nicht mehr definiert für Wert: '"
                    + property.getPropertyValue() + "'.", property.getPropertyTypeID() != null);
            PropertyList typeList = this.typedPropertyLists.get(property.getPropertyTypeID());
            if (typeList != null) {
                typeList.add(property);
            } else {
                typeList = new PropertyList(1);
                typeList.add(property);
                typedPropertyLists.put(property.getPropertyTypeID(), typeList);
            }
        } catch (AssertException e) {
            logger.error(e);
        }
    }

    /**
     * Removes a property with the given {@link PropertyType} and value from
     * this entity.
     * 
     * @param propertyType
     *            A {@link PropertyType}
     * @param propertyValue
     *            The values of the property
     */
    public void remove(PropertyType propertyType, String propertyValue) {
        PropertyList list = typedPropertyLists.get(propertyType.getId());
        if (list == null) {
            return;
        }
        List<Property> setProperties = list.getProperties();
        for (Iterator<Property> iter = setProperties.iterator(); iter.hasNext();) {
            Property property = iter.next();
            if (property.getPropertyValue().equals(propertyValue)) {
                iter.remove();
            }
        }
    }

    /**
     * Returns the int value of a property with propertyTypeId.
     * 
     * If no property value exists with the given propertyTypeId
     * {@link Property}.UNDEF is returned.
     * 
     * If a property value exists but property has another input type than
     * "numericoption" {@link Property}.UNDEF is returned.
     * 
     * @param propertyTypeId
     *            The type id of a property
     * @return Int value of a property
     */
    public int getInt(String propertyTypeId) {
        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        if (propertyList == null || propertyList.getProperties().size() == 0) {
            return Property.UNDEF;
        }
        PropertyType type = HUITypeFactory.getInstance().getPropertyType(this.entityType,
                propertyTypeId);
        if (type.isNumericSelect()) {
            for (Property property : propertyList.getProperties()) {
                return property.getNumericPropertyValue();
            }
        }
        return Property.UNDEF;

    }

    /**
     * Returns the value (not the translated title) of an single select option
     * property.
     * 
     * If property with id is not single select option property a warning is
     * logged and null is returned.
     * 
     * @param propertyTypeId
     *            The type id of a property
     * @return value (not the translated title) of an single select option
     *         property
     */
    public String getOptionValue(String propertyTypeId) {
        String value = null;
        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        if (propertyList != null && propertyList.getProperties().size() == 1) {
            PropertyType type = HUITypeFactory.getInstance().getPropertyType(this.entityType,
                    propertyTypeId);
            if (type.isSingleSelect()) {
                Property prop = propertyList.getProperties().get(0);
                value = prop.getPropertyValue();
            } else {
                logger.warn("Property " + propertyTypeId + " is not of type "
                        + PropertyType.INPUT_SINGLEOPTION
                        + ". Can not determine option value. Entity id is: " + this.getDbId());
            }
        } else if (propertyList != null && propertyList.getProperties().size() > 1) {
            logger.warn("Property list " + propertyTypeId
                    + " contains more than entry. Can not determine option value. Entity id is: "
                    + this.getDbId());
        }
        return value;
    }

    /**
     * Sets the value for a given property.
     * 
     * <p>
     * Since internally a property value is a multi-value this interface allows
     * setting these values in one row.
     * </p>
     * 
     * <p>
     * Note: Using this method is preferred over modifying a
     * {@link PropertyList} object itself.
     * </p>
     * 
     * <p>
     * Note: The actual values that are imported have to be
     * <em>untranslated</em> IOW should directly represent the strings used in
     * the SNCA.xml
     * </p>
     * 
     * @param huiTypeFactory
     * @param propertyTypeId
     * @param foreignProperties
     */
    public void importProperties(HUITypeFactory huiTypeFactory, String propertyTypeId,
            List<String> foreignProperties) {
        List<Property> properties = initializePropertyListForImport(propertyTypeId);

        for (String value : foreignProperties) {
            PropertyType propertyType = huiTypeFactory.getPropertyType(this.entityType,
                    propertyTypeId);
            Property p = new Property();

            if (propertyType == null) {
                logger.warn("Property-type was not found in SNCA.xml: " + propertyTypeId
                        + ", entity type: " + this.entityType);
            }

            if (propertyType != null && propertyType.isSingleSelect() && value != null) {
                List<IMLPropertyOption> optionList = propertyType.getOptions();
                boolean found = false;
                for (IMLPropertyOption option : optionList) {
                    if (value.equals(option.getName())) {
                        value = option.getId();
                        found = true;
                    } else if (value.equals(option.getId())) {
                        found = true;
                    }
                }
                if (!found) {
                    logger.warn(
                            "No value found for option property: " + propertyTypeId + " of entity: "
                                    + this.entityType + ". Importing unmapped value: " + value);
                }
            }
            p.setPropertyType(propertyTypeId);
            p.setPropertyValue(value);
            p.setParent(this);
            properties.add(p);
        }
    }

    protected List<Property> initializePropertyListForImport(String propertyTypeId) {
        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        if (propertyList == null) {
            propertyList = new PropertyList();
            typedPropertyLists.put(propertyTypeId, propertyList);
        }

        // It would be possible to create a new list and make the PropertyList
        // object use that but that causes problems with hibernate. As such the
        // existing list is taken and cleared before use.
        List<Property> properties = propertyList.getProperties();
        if (properties == null) {
            properties = new LinkedList<>();
            propertyList.setProperties(properties);
        } else {
            properties.clear();
        }
        return properties;
    }

    /**
     * Retrieves the raw, untranslated individual data values and stores them in
     * a given list.
     * 
     * <p>
     * The return values denotes the amount of values exported and can be used
     * to find out whether any work was done.
     * </p>
     * 
     * @param propertyType
     * @param foreignProperties
     * 
     * @return The amount of individual values exported.
     */
    public int exportProperties(String propertyType, List<String> foreignProperties) {
        int amount = 0;
        for (Property prop : getProperties(propertyType).getProperties()) {
            foreignProperties.add(prop.getPropertyValue());
            amount++;
        }

        return amount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.hui.common.multiselectionlist.ISelectOptionHandler#select(sernet.
     * hui.common.multiselectionlist.IMLPropertyType,
     * sernet.hui.common.multiselectionlist.IMLPropertyOption)
     */
    @Override
    public void select(IMLPropertyType type, IMLPropertyOption opt) {
        createNewProperty((PropertyType) type, opt.getId());
        fireSelectionChanged(type, opt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.hui.common.multiselectionlist.ISelectOptionHandler#unselect(sernet
     * .hui.common.multiselectionlist.IMLPropertyType,
     * sernet.hui.common.multiselectionlist.IMLPropertyOption)
     */
    @Override
    public void unselect(IMLPropertyType type, IMLPropertyOption opt) {
        remove((PropertyType) type, opt.getId());
        fireSelectionChanged(type, opt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    private synchronized List<IEntityChangedListener> getChangelisteners() {
        if (this.changeListeners == null) {
            changeListeners = new ArrayList<>();
        }
        return changeListeners;
    }

    public void addChangeListener(IEntityChangedListener changeListener) {
        getChangelisteners().add(changeListener);
    }

    public void removeListener(IEntityChangedListener listener) {
        getChangelisteners().remove(listener);
    }

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

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public String getEntityType() {
        return entityType;
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

    public Map<String, PropertyList> getTypedPropertyLists() {
        return typedPropertyLists;
    }

    public void setTypedPropertyLists(Map<String, PropertyList> typedPropertyLists) {
        this.typedPropertyLists = typedPropertyLists;
    }

    /**
     * Returns all properties with the given property type id. Returns an empty
     * PropertyList if there are no properties with the given property type id.
     * 
     * @param propertyTypeId
     *            The type id of a property
     * @return Returns all properties with the given property type id
     */
    public PropertyList getProperties(String propertyTypeId) {
        PropertyList propertyList = typedPropertyLists.get(propertyTypeId);
        if (propertyList == null) {
            propertyList = new PropertyList();
        }
        return propertyList;
    }

    public void addToReferenceValueCache(String propertyTypeId, String value) {
        getReferenceValueCache().put(propertyTypeId, value);
    }

    private Map<String, String> getReferenceValueCache() {
        if (referenceValueCache == null) {
            referenceValueCache = new HashMap<>();
        }
        return referenceValueCache;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj
                || (obj instanceof Entity && this.uuid.equals(((Entity) obj).getUuid())));
    }

    @Override
    public String toString() {
        return "Entity [entityType=" + entityType + ", dbId=" + dbId + ", uuid=" + uuid + "]";
    }

}
