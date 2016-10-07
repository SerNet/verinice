/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadEntitiesByIds;
import sernet.verinice.service.linktable.ILinkTableConfiguration;

/**
 * This graph command loads the values of element properties of input type "reference".
 * Loaded properties values are saved in a reference value cache in the entity that 
 * contains the property.
 * 
 * The command is used to load data for link tables (link tables reports, LTR).
 * 
 * See SNCA.xml for all properties of input type "reference".
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class LinkTableGraphCommand extends GraphCommand {

    private static final long serialVersionUID = 9088625615234598118L;

    private transient Logger log = Logger.getLogger(LinkTableGraphCommand.class);
    
    private static final NumericStringComparator NSC = new NumericStringComparator();

    /**
     * A set with all property type ids in the link table configuration
     */
    private Set<String> propertyTypeIdsInConfiguration;

    /*
     * Set with element type ids in link table configuration which contains
     * reference properties.
     */
    private Set<String> referenceElementTypeIds;

    /*
     * Set with property type ids of reference properties in link table
     * configuration
     */
    private Set<String> referencePropertyTypeIds;

    /*
     * Set with db ids of referenced entities
     */
    private Set<Integer> referenceIds;
    
    /**
     * Map to save the values of the reference properties.
     * Key: Entity database id
     * Values: The values (or label) of the referenced entity
     */
    private Map<Integer, String> referenceValueMap;

    public LinkTableGraphCommand(ILinkTableConfiguration configuration) {
        GraphElementLoader loader = new GraphElementLoader();
        loader.setScopeIds(configuration.getScopeIdArray());
        Set<String> objectTypeIds = configuration.getObjectTypeIds();
        loader.setTypeIds(objectTypeIds.toArray(new String[objectTypeIds.size()]));
        addLoader(loader);
        for (String relation : configuration.getLinkTypeIds()) {
            addRelationId(relation);
        }
        propertyTypeIdsInConfiguration = configuration.getPropertyTypeIds();
    }

    /**
     * The following steps are executed:
     * 
     * 1. Collect property type ids of reference properties
     * 2. Collect the ids of there referenced entities
     * 3. Load the referenced entities
     * 4. Cache the values of the reference properties
     * 
     * @see sernet.verinice.interfaces.IGraphCommand#executeWithGraph()
     */
    @Override
    public void executeWithGraph() {
        try {
            doExecute();
        } catch (CommandException e) {
            getLog().error("Error while loading reference properties", e);
            throw new RuntimeCommandException(e);
        }  
    }

    private void doExecute() throws CommandException {
        getLog().debug("Collecting reference properties in link table configuration...");
        collectReferencePropertyTypeIds();
        if (referencePropertyTypeIds.isEmpty()) {
            // no reference properties in configuration, nothing to do
            getLog().debug("No reference properties found.");
            return;
        }
        
        getLog().debug("Collecting reference ids in verinice graph...");
        collectReferencedEntityIds();
        if (referenceIds.isEmpty()) {
            // no reference properties in configuration, nothing to do
            getLog().debug("No reference ids found.");
            return;
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("Loading " + referenceIds.size() + " references...");
        } 
        
        loadReferencedEntities();
        
        cacheReferencePropertyValues();
    }
    

    
    /**
     * Collects all property and element type ids of reference properties from the link
     * table configuration 
     */
    private void collectReferencePropertyTypeIds() {
        referenceElementTypeIds = new HashSet<>();
        referencePropertyTypeIds = new HashSet<>();
        Collection<EntityType> entityTypes = getHuiTypeFactory().getAllEntityTypes();
        for (EntityType entityType : entityTypes) {
            collectReferencePropertyTypeIds(entityType);
        }
    }

    private void collectReferencePropertyTypeIds(EntityType entityType) {
        List<PropertyType> propertyTypes = entityType.getAllPropertyTypes();
        for (PropertyType propertyType : propertyTypes) {
            collectReferencePropertyTypeIds(entityType, propertyType);
        }
    }

    private void collectReferencePropertyTypeIds(EntityType entityType, PropertyType propertyType) {
        if (propertyType.isReference() && propertyTypeIdsInConfiguration.contains(propertyType.getId())) {
            referenceElementTypeIds.add(entityType.getId());
            referencePropertyTypeIds.add(propertyType.getId());
            if (getLog().isDebugEnabled()) {
                getLog().debug("Reference property found, element type: " + entityType.getId() + ", property type: " + propertyType.getId());
            }
        }
    }
    
    
   
    private void collectReferencedEntityIds() {
        referenceIds = new HashSet<>();
        Set<CnATreeElement> elements = getGraph().getElements();
        for (CnATreeElement element : elements) {
            if (referenceElementTypeIds.contains(element.getTypeId())) {
                collectReferenceIds(element);
            }
        }
    }

    private void collectReferenceIds(CnATreeElement element) {
        Entity entity = element.getEntity();
        // key: propertyTypeId, value: PropertyList with propertyTypeId
        Map<String, PropertyList> propertyMap = entity.getTypedPropertyLists();
        for (Map.Entry<String, PropertyList> propertyEntry : propertyMap.entrySet()) {
            if (referencePropertyTypeIds.contains(propertyEntry.getKey())) {
                collectReferenceIds(propertyEntry.getValue());
            }
        }
    }

    private void collectReferenceIds(PropertyList propertyList) {
        if(propertyList==null) {
            return;
        }
        List<Property> listOfProperties = propertyList.getProperties();
        for (Property property : listOfProperties) {
            if(isPropertyValue(property)) {
                Integer entityDbId = Integer.valueOf(property.getPropertyValue());
                referenceIds.add(entityDbId);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Reference id found, property type: " + property.getPropertyType() + ", entity db id: " + entityDbId);
                }
            }
        }

    }

    private boolean isPropertyValue(Property property) {
        return property!=null && property.getPropertyValue()!=null && !property.getPropertyValue().isEmpty();
    }
    
    

    private void loadReferencedEntities() throws CommandException {
        referenceValueMap = new HashMap<>();
        LoadEntitiesByIds command = new LoadEntitiesByIds(referenceIds);
        command = getCommandService().executeCommand(command);
        List<Entity> entities = command.getEntities();
        for (Entity entity : entities) {
            if(!Person.TYPE_ID.equals(entity.getEntityType())) {
                getLog().error("Referenced entity type is not supported. The only supported type is: " + Person.TYPE_ID );
            }
            String value = Person.getTitel(entity);
            referenceValueMap.put(entity.getDbId(), value);
            if (getLog().isDebugEnabled()) {
                getLog().debug("Reference values loaded, entity db id: " + entity.getDbId() + ", value: " + value);
            }
        }
    }
    

   
    private void cacheReferencePropertyValues() {
        Set<CnATreeElement> elements = getGraph().getElements();
        for (CnATreeElement element : elements) {
            if (referenceElementTypeIds.contains(element.getTypeId())) {
                cacheReferencePropertyValues(element);
            }
        }
    }
    
    private void cacheReferencePropertyValues(CnATreeElement element) {
        Entity entity = element.getEntity();
        // key: propertyTypeId, value: PropertyList with propertyTypeId
        Map<String, PropertyList> propertyMap = entity.getTypedPropertyLists();
        for (Map.Entry<String, PropertyList> propertyEntry : propertyMap.entrySet()) {
            String propertyTypeId = propertyEntry.getKey();
            if (referencePropertyTypeIds.contains(propertyTypeId)) {
                cacheReferencePropertyValues(entity,propertyEntry);
            }
        }
    }
    
    private void cacheReferencePropertyValues(Entity entity, Entry<String, PropertyList> propertyEntry) {
        String propertyTypeId = propertyEntry.getKey();
        PropertyList propertyList = propertyEntry.getValue();
        List<Property> listOfProperties = propertyList.getProperties();
        
        List<String> values = new LinkedList<>();
        for (Property property : listOfProperties) {
            String value = getReferenceValue(property);
            if(value!=null) {
                values.add(value);
            }             
        } 
        
        String referenceValue = sortAndConvertListToString(values);                 
        entity.addToReferenceValueCache(propertyTypeId, referenceValue);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Reference value added to cache: " + referenceValue + ", entity db id: " + entity.getDbId() + ", property type id: " + propertyTypeId);
        }
    }

    private String sortAndConvertListToString(List<String> values) {
        Collections.sort(values, NSC);
        return StringUtils.join(values, ",");
    }

    
    
    private String getReferenceValue(Property property) {
        String value = null;
        if(isPropertyValue(property)) {           
            Integer entityDbId = Integer.valueOf(property.getPropertyValue());
            value = referenceValueMap.get(entityDbId);
        }
        return value;
    }
     


    private HUITypeFactory getHuiTypeFactory() {
        return HUITypeFactory.getInstance();
    }

    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LinkTableGraphCommand.class);
        }
        return log;
    }  
}
