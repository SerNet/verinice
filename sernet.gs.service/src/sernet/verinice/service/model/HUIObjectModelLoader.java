/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.model;

import java.util.*;

import sernet.hui.common.VeriniceContext;

/**
 * 
 * Client implementation of {@link IObjectModelService}
 * 
 * @see HUIObjectModelService
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class HUIObjectModelLoader implements IObjectModelService {

    private IObjectModelService objectModelService;

    private Map<String, Set<String>> allRelationPartners = null;
    private Map<String, Set<String>> allPossibleProperties = null;
    private Map<String, String> allLabels = null;
    private Map<String, String> allRelationLabels = null;

    private Set<String> allTypeIds = null;

    private Map<String, Set<String>> possibleChildren = null;
    private Map<String, Set<String>> possibleParents = null;

    public static IObjectModelService getInstance() {

        return (HUIObjectModelLoader) VeriniceContext
                .get(VeriniceContext.OBJECT_MODEL_SERVICE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.model.IObjectModelService#init()
     */
    @Override
    public void init() {

        if (oneFieldUninitialized()) {

            ObjectModelContainer object = loadAll();
            allTypeIds = object.getAllTypeIds();
            possibleChildren = object.getPossibleChildren();
            possibleParents = object.getPossibleParents();
            allLabels = object.getAllLabels();
            allRelationLabels = object.getAllRelationLabels();
            allRelationPartners = object.getAllRelationPartners();
            allPossibleProperties = object.getAllPossibleProperties();
        }
    }

    private boolean oneFieldUninitialized() {

        if (allRelationPartners == null || allPossibleProperties == null) {
            return true;
        }
        if (allLabels == null || allRelationLabels == null || allTypeIds == null) {
            return true;
        }
        if (possibleChildren == null || possibleParents == null) {
            return true;
        }
        return false;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.service.model.IObjectModelService#getRelations(java.lang.String, java.lang.String)
     */
    @Override
    public Set<String> getRelations(String fromEntityTypeID, String toEntityTypeID) {
        init();
        return objectModelService.getRelations(fromEntityTypeID, toEntityTypeID);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.IObjectModelService#getPossibleRelationPartners(java.lang.String)
     */
    @Override
    public Set<String> getPossibleRelationPartners(String typeID) {
        init();
        Set<String> possibleRelationPartners = allRelationPartners.get(typeID);
//        if (possibleRelationPartners == null) {
//            possibleRelationPartners = new HashSet<>();
//        }
        return possibleRelationPartners;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.service.model.IObjectModelService#getAllTypeIDs()
     */
    @Override
    public Set<String> getAllTypeIDs() {
        init();
        return allTypeIds;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.IObjectModelService#getPossibleProperties(java.lang.String)
     */
    @Override
    public Set<String> getPossibleProperties(String typeID) {
        init();
        Set<String> possibleProperties = allPossibleProperties.get(typeID);
//        if (possibleProperties == null) {
//            possibleProperties = new HashSet<>();
//        }
        return possibleProperties;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.IObjectModelService#getLabel(java.lang.String)
     */
    @Override
    public String getLabel(String id) {
        init();
        String label = allLabels.get(id);
        if (label == null) {
            label = objectModelService.getLabel(id);
            allLabels.put(id, label);
        }
        return label;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.IObjectModelService#getRelationLabel(java.lang.String)
     */
    @Override
    public String getRelationLabel(String id) {
        init();
        return allRelationLabels.get(id);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.IObjectModelService#getPossibleChildren(java.lang.String)
     */
    @Override
    public Set<String> getPossibleChildren(String typeID) {
        init();
        Set<String> set = getAllPossibleChildren().get(typeID);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.service.model.IObjectModelService#getPossibleParents(java.lang.String)
     */
    @Override
    public Set<String> getPossibleParents(String typeID) {
        init();
        Set<String> set = getAllPossibleParents().get(typeID);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.model.IObjectModelService#getAllPossibleChildren(
     * )
     */
    @Override
    public Map<String, Set<String>> getAllPossibleChildren() {
        init();
        return possibleChildren;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.model.IObjectModelService#getAllPossibleParents()
     */
    @Override
    public Map<String, Set<String>> getAllPossibleParents() {
        return possibleParents;
    }

    public IObjectModelService getObjectModelService() {
        return objectModelService;
    }

    public void setObjectModelService(IObjectModelService objectModelService) {
        this.objectModelService = objectModelService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.model.IObjectModelService#loadAll()
     */
    @Override
    public ObjectModelContainer loadAll() {
        return objectModelService.loadAll();
    }

    public boolean isValidTypeId(String typeID) {
        return allLabels.containsKey(typeID);
    }

    public boolean isValidRelationId(String relationID) {
        return allRelationLabels.containsKey(relationID);
    }
}

