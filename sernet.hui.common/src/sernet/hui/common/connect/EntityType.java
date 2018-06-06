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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * This class represents an entity type in the dynamic object model (Hitro UI,
 * HUI). Use this class to get information about a verinice element
 * (CnATreeElement). Instances of this class are defined in configuration file
 * SNCA.xml. Content of SNCA.xml is defined in schema hitro.xsd.
 *
 * @author Alexander Koderman
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class EntityType {

    private String id;
    private String name;
    private String inheritingEntity;

    private List<IEntityElement> elements = new ArrayList<>();
    private List<PropertyGroup> propertyGroups = new ArrayList<>();

    // All properties of an entity type, Map of property ID : PropertyType
    private Map<String, PropertyType> propertyTypes = new HashMap<>();

    // map of target EntityType ID : set of HuiRelations (links to EntityTypes)
    private Map<String, Set<HuiRelation>> relations = new HashMap<>();

    /**
     * A list with all property types of this entity type.
     *
     * This method does not return the property types from the property groups.
     *
     * @return A list with the property types of this entity type
     */
    public List<PropertyType> getPropertyTypes() {
        ArrayList<PropertyType> types = new ArrayList<>(propertyTypes.values().size());
        types.addAll(propertyTypes.values());
        return types;
    }

    /**
     * Returns a list with the property types of this entity type. The
     * properties in the list are sorted the same way as in SNCA.xml.
     *
     * This method does not return the property types from the property groups.
     *
     * @return A SNCA sorted list with the property types of this entity type
     */
    public List<PropertyType> getPropertyTypesSorted() {
        List<PropertyType> propertyTypesSorted = new ArrayList<>();
        for (IEntityElement entity : getElements()) {
            if (entity instanceof PropertyType) {
                propertyTypesSorted.add((PropertyType) entity);
            }
        }
        return propertyTypesSorted;
    }

    /**
     * Returns a list with all property types of this entity type including the
     * types which are contained in property groups.
     * 
     * @return A list with all property types of this entity type and property
     *         groups.
     */
    public List<PropertyType> getAllPropertyTypes() {
        List<PropertyType> types = getPropertyTypes();
        for (PropertyGroup pg : propertyGroups) {
            types.addAll(pg.getPropertyTypes());
        }

        return types;
    }

    /**
     * Returns a list with all property types of this entity type including the
     * types which are contained in property groups.
     * 
     * The properties in the list are sorted the same way as in SNCA.xml.
     *
     * @return A SNCA sorted list with all property types of this entity type
     *         and property groups.
     */
    public List<PropertyType> getAllPropertyTypesSorted() {
        List<PropertyType> propertyTypeList = new ArrayList<>();
        for (IEntityElement entity : getElements()) {
            if (entity instanceof PropertyType) {
                propertyTypeList.add((PropertyType) entity);
            } else if (entity instanceof PropertyGroup) {
                propertyTypeList.addAll(((PropertyGroup) entity).getPropertyTypes());
            }
        }
        return propertyTypeList;
    }

    /**
     * Returns the property type of this entity type with the given ID. Id no
     * property type with the given ID exists null is returned.
     *
     * @param id
     *            The ID of a property type.
     * @return The property type with the given ID or null
     */
    public PropertyType getPropertyType(String id) {
        PropertyType type = this.propertyTypes.get(id);
        if (type != null) {
            return type;
        }
        // search in groups:
        for (PropertyGroup group : this.propertyGroups) {
            if ((type = group.getPropertyType(id)) != null) {
                return type;
            }
        }
        // none found:
        return null;
    }

    /**
     * @return An array with all property type ID of this entity type including
     *         the types which are contained in property groups.
     */
    public String[] getAllPropertyTypeIds() {
        List<PropertyType> types = getAllPropertyTypes();
        List<String> ids = new ArrayList<>();
        for (PropertyType type : types) {
            ids.add(type.getId());
        }
        return ids.toArray(new String[ids.size()]);
    }

    /**
     * @return An array with all property type ID of this entity type including
     *         the types which are contained in property groups.
     * @deprecated Replaced by {@link #getAllPropertyTypeIds()}
     */
    @Deprecated
    public String[] getAllPropertyTypeIDsIncludingGroups() {
        return getAllPropertyTypeIds();
    }

    /**
     * @return An array with all property type titles of this entity type
     *         including the types which are contained in property groups.
     */
    public String[] getAllPropertyTypeTitles() {
        ArrayList<String> result = new ArrayList<>();
        String[] typeIDs = getAllPropertyTypeIds();
        for (String typeId : typeIDs) {
            result.add(getPropertyType(typeId).getName());
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * @return An array with all property type titles of this entity type
     *         including the types which are contained in property groups.
     * @deprecated Replaced by {@link #getAllPropertyTypeTitles()}
     */
    @Deprecated
    public String[] getAllPropertyTypeTitlesIncludingGroups() {
        return getAllPropertyTypeTitles();
    }

    /**
     * Returns all links (relations) from this entity type to another entity
     * type with the given ID.
     *
     * @param entityTypeId
     *            The ID of an entity type
     * @return All links from this entity type to another entity type with the
     *         given ID.
     */
    public Set<HuiRelation> getPossibleRelations(String entityTypeId) {
        return relations.get(entityTypeId) != null ? relations.get(entityTypeId)
                : new HashSet<HuiRelation>(0);
    }

    /**
     * Adds a HuiRelation to this entity type. A HuiRelation is a link
     * (relation) from this entity type to another entity type.
     *
     * @param relation
     *            A HuiRelation, link from this entity type to another entity
     *            type
     */
    public void addRelation(HuiRelation relation) {
        if (relations.get(relation.getTo()) == null) {
            this.relations.put(relation.getTo(), new HashSet<HuiRelation>());
        }
        this.relations.get(relation.getTo()).add(relation);
    }

    /**
     * Returns a set with all HuiRelations of this entity type. A HuiRelation is
     * a link (relation) from this entity type to another entity type.
     *
     * @return A set with all HuiRelations of this entity type
     */
    public Set<HuiRelation> getPossibleRelations() {
        HashSet<HuiRelation> allRelations = new HashSet<>();
        Set<Entry<String, Set<HuiRelation>>> entrySet = relations.entrySet();
        for (Entry<String, Set<HuiRelation>> entry : entrySet) {
            Set<HuiRelation> relationsToOneOtherType = entry.getValue();
            allRelations.addAll(relationsToOneOtherType);
        }
        return allRelations;
    }

    /**
     * Returns the HuiRelation from this entity type with the given ID. If no
     * relation with the given ID exists null is returned.
     *
     * @param relationTypeId
     *            The ID of a relation / link
     * @return The HuiRelation with the given ID or null
     */
    public HuiRelation getPossibleRelation(String relationTypeId) {
        Set<Entry<String, Set<HuiRelation>>> entrySet = relations.entrySet();
        for (Entry<String, Set<HuiRelation>> entry : entrySet) {
            Set<HuiRelation> value = entry.getValue();
            for (HuiRelation huiRelation : value) {
                if (huiRelation.getId().equals(relationTypeId)) {
                    return huiRelation;
                }
            }
        }
        return null;
    }

    public List<PropertyType> getObjectBrowserPropertyTypes() {
        List<PropertyType> allPropertyTypesSorted = getAllPropertyTypesSorted();
        List<PropertyType> result = new ArrayList<>(allPropertyTypesSorted.size());
        for (PropertyType propertyType : allPropertyTypesSorted) {
            if (propertyType.isShowInObjectBrowser()) {
                result.add(propertyType);
            }
        }
        return result;
    }

    public void addPropertyType(PropertyType prop) {
        propertyTypes.put(prop.getId(), prop);
        elements.add(prop);
    }

    public void addPropertyGroup(PropertyGroup group) {
        propertyGroups.add(group);
        elements.add(group);
    }

    public List<IEntityElement> getElements() {
        return elements;
    }

    public List<PropertyGroup> getPropertyGroups() {
        return propertyGroups;
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

    /**
     * @return the inheritingEntity
     */
    public String getInheritingEntity() {
        return inheritingEntity;
    }

    /**
     * @param inheritingEntity
     *            the inheritingEntity to set
     */
    public void setInheritingEntity(String inheritingEntity) {
        this.inheritingEntity = inheritingEntity;
    }

    public boolean isInheritingEntity() {
        return this.inheritingEntity != null;
    }

}
