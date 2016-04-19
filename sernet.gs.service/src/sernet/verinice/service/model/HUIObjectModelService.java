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

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.CnATypeMapper;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class HUIObjectModelService implements IObjectModelService {

    private static final Logger LOG = Logger.getLogger(HUIObjectModelService.class);
    private HUITypeFactory huiTypeFactory;

    private Map<String, Set<String>> allRelationPartners = null;
    private Map<String, Set<String>> allPossibleProperties = null;
    private Map<String, String> allLabels = null;
    private Map<String, String> allRelationLabels = null;

    private Set<String> allTypeIds = null;

    private Map<String, Set<String>> possibleChildren = null;
    private Map<String, Set<String>> possibleParents = null;

    public static IObjectModelService getInstance() {

        return (HUIObjectModelService) VeriniceContext
                .get(VeriniceContext.OBJECT_MODEL_SERVICE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.model.IObjectModelService#init()
     */
    @Override
    public void init() {
        if (allTypeIds == null || allTypeIds.isEmpty()) {
            fillAllTypeIds();
        }
        if (possibleChildren == null || possibleChildren.isEmpty()) {
            fillPossibleChildrenMap();
        }
        if (possibleParents == null || possibleParents.isEmpty()) {
            fillPossibleParentsMap();
        }

        LOG.debug("init objectModelService finished");
    }

    private void fillAllTypeIds() {

        if (allTypeIds != null) {
            return;
        }
        allTypeIds = new HashSet<>(getHuiTypeFactory().getAllTypeIds());

        // TODO rmotza better way!
        allTypeIds.remove("note");
        allTypeIds.remove("role");
        allTypeIds.remove("configuration");
        allTypeIds.remove("attachment");
            // addAllBSIGroups();
    }

    // TODO rmotza to be commented in as soon as the LTR can work with BSI
    // categories
    // private void addAllBSIGroups() {
    //
    // allTypeIds.add(AnwendungenKategorie.TYPE_ID);
    // allTypeIds.add(GebaeudeKategorie.TYPE_ID);
    // allTypeIds.add(ClientsKategorie.TYPE_ID);
    // allTypeIds.add(ServerKategorie.TYPE_ID);
    // allTypeIds.add(SonstigeITKategorie.TYPE_ID);
    // allTypeIds.add(TKKategorie.TYPE_ID);
    // allTypeIds.add(PersonenKategorie.TYPE_ID);
    // allTypeIds.add(NKKategorie.TYPE_ID);
    // allTypeIds.add(RaeumeKategorie.TYPE_ID);
    //
    // }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getRelationsFrom(java.lang.String)
     */
    @Override
    public Set<String> getRelations(String fromEntityTypeID, String toEntityTypeID) {

        if (getHuiTypeFactory().getEntityType(fromEntityTypeID) == null
                || getHuiTypeFactory().getEntityType(toEntityTypeID) == null) {
            return new HashSet<>();
        }
        Set<HuiRelation> relations = getHuiTypeFactory().getPossibleRelations(fromEntityTypeID,
                toEntityTypeID);
        relations.addAll(getHuiTypeFactory().getPossibleRelations(toEntityTypeID,
                fromEntityTypeID));
        HashSet<String> relationIds = new HashSet<>();
        for (HuiRelation huiRelation : relations) {

            relationIds.add(huiRelation.getId());
        }

        return relationIds;
    }

    public HUITypeFactory getHuiTypeFactory() {
        return huiTypeFactory;
    }

    public void setHuiTypeFactory(HUITypeFactory huiTypeFactory) {
        this.huiTypeFactory = huiTypeFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getPossibleRelationPartners(java.lang.String)
     */
    @Override
    public Set<String> getPossibleRelationPartners(String typeID) {
        if (getHuiTypeFactory().getEntityType(typeID) == null) {
            return new HashSet<>();
        }
        HashSet<String> possiblePartners = new HashSet<>();

        Set<HuiRelation> relations = getHuiTypeFactory().getPossibleRelationsFrom(typeID);
        for (HuiRelation relation : relations) {
            possiblePartners.add(relation.getTo());
        }
        relations = getHuiTypeFactory().getPossibleRelationsTo(typeID);
        for (HuiRelation relation : relations) {
            possiblePartners.add(relation.getFrom());
        }
        return possiblePartners;
    }

    public Set<String> getAllTypeIDs() {
        if (allTypeIds == null) {
            fillAllTypeIds();
        }
        return allTypeIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getPossibleProperties(java.lang.String)
     */
    @Override
    public Set<String> getPossibleProperties(String typeID) {
        if (getHuiTypeFactory().getEntityType(typeID) == null) {
            return new HashSet<>();
        }
        return new HashSet<>(
                Arrays.asList(getHuiTypeFactory().getEntityType(typeID).getAllPropertyTypeIds()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.ILinkTableContentService#getLabel(java.
     * lang.String)
     */
    @Override
    public String getLabel(String id) {
        return getHuiTypeFactory().getMessage(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.ILinkTableContentService#getLabel(java.
     * lang.String)
     */
    @Override
    public String getRelationLabel(String id) {
        return getHuiTypeFactory().getMessage(id + "_name");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.IObjectModelService#getPossibleChildren
     * (java.lang.String)
     */
    @Override
    public Set<String> getPossibleChildren(String typeID) {
        Set<String> set = possibleChildren.get(typeID);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;

    }

    private void fillPossibleChildrenMap() {

        possibleChildren = new HashMap<>();
        CnATreeElement parentInstance;
        CnATreeElement childInstance;
        Class<? extends CnATreeElement> parentClass;
        Class<? extends CnATreeElement> childClass;
        HashSet<String> possibleChildrenSet;

        for (String typeIdParent : getAllTypeIDs()) {

            try {
                parentClass = CnATypeMapper.getClassFromTypeId(typeIdParent);
            } catch (IllegalStateException e) {
                LOG.info(
                        "ParentClass for possible Children is not mapped" + ": " + e.getMessage());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e);
                }
                continue;
            }
            try {
                parentInstance = createInstance(parentClass, typeIdParent);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                LOG.info("something went wrong while creating " + parentClass.getSimpleName() + ": "
                        + e.getMessage());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e);
                }
                continue;
            }
            possibleChildrenSet = new HashSet<>();
            for (String typeIdChild : getAllTypeIDs()) {

                try {
                    childClass = CnATypeMapper.getClassFromTypeId(typeIdChild);
                } catch (IllegalStateException e) {
                    LOG.info("ChildClass for possible Children is not mapped" + ": "
                            + e.getMessage());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                    continue;
                }

                try {
                    childInstance = createInstance(childClass, typeIdChild);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    LOG.warn("something went wrong while creating " + childClass.getSimpleName()
                            + ": " + e.getMessage());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                    continue;
                }
                if (parentInstance.canContain(childInstance)) {
                    possibleChildrenSet.add(typeIdChild);
                }
            }
            possibleChildren.put(typeIdParent, possibleChildrenSet);

        }

    }

    private CnATreeElement createInstance(Class<? extends CnATreeElement> clazz, String typeId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        CnATreeElement container = null;
        CnATreeElement element;
        boolean createChildren = false;
        if (isOrganization(clazz, typeId)) {
            element = (CnATreeElement) Organization.class
                    .getConstructor(CnATreeElement.class, boolean.class)
                    .newInstance(container, createChildren);
        } else if (isAudit(clazz, typeId)) {
            element = (CnATreeElement) Audit.class
                    .getConstructor(CnATreeElement.class, boolean.class)
                    .newInstance(container, createChildren);
        } else {
            element = clazz.getConstructor(CnATreeElement.class).newInstance(container);
        }
        return element;
    }

    private boolean isOrganization(Class<? extends CnATreeElement> clazz, String typeId) {
        return Organization.class.equals(clazz) || Organization.TYPE_ID.equals(typeId);
    }

    private boolean isAudit(Class<? extends CnATreeElement> clazz, String typeId) {
        return Audit.class.equals(clazz) || Audit.TYPE_ID.equals(typeId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.IObjectModelService#getPossibleParents(
     * java.lang.String)
     */
    @Override
    public Set<String> getPossibleParents(String typeID) {

        Set<String> set = possibleParents.get(typeID);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;

    }

    private void fillPossibleParentsMap() {

        possibleParents = new HashMap<>();
        CnATreeElement childInstance;
        Class<? extends CnATreeElement> childClass;
        Class<? extends CnATreeElement> parentClass;
        HashSet<String> possibleParentsSet;
        for (String typeIChild : getAllTypeIDs()) {

            try {
                childClass = CnATypeMapper.getClassFromTypeId(typeIChild);
            } catch (IllegalStateException e) {
                LOG.info("ChildClass for possible parents is not mapped" + ": " + e.getMessage());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e);
                }
                continue;
            }
            try {
                childInstance = createInstance(childClass, typeIChild);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                LOG.warn("something went wrong while creating " + childClass.getSimpleName() + ": "
                        + e.getMessage());
                if (LOG.isDebugEnabled()) {
                    LOG.debug(e);
                }
                continue;
            }
            possibleParentsSet = new HashSet<>();
            for (String typeIdParent : getAllTypeIDs()) {
                try {
                    parentClass = CnATypeMapper.getClassFromTypeId(typeIdParent);
                } catch (IllegalStateException e) {
                    LOG.info("ParentClass for possible Parents is not mapped" + ": "
                            + e.getMessage());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                    continue;
                }
                try {
                    CnATreeElement parentInsance = createInstance(parentClass, typeIdParent);
                    if (parentInsance.canContain(childInstance)) {
                        possibleParentsSet.add(typeIdParent);
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                        | NoSuchMethodException e) {
                    LOG.warn("something went wrong while creating " + parentClass.getSimpleName()
                            + ": " + e.getMessage());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(e);
                    }
                    continue;
                }
            }
            possibleParents.put(typeIChild, possibleParentsSet);

        }
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

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.model.IObjectModelService#loadAll()
     */
    @Override
    public ObjectModelContainer loadAll() {
        init();
        ObjectModelContainer container = new ObjectModelContainer();

        container.setAllLabels(getAllLabels());
        container.setAllPossibleProperties(getAllPossibleProperties());
        container.setAllRelationLabels(getAllRelationLabels());
        container.setAllRelationPartners(getAllRelationPartners());
        container.setAllTypeIds(getAllTypeIDs());
        container.setPossibleChildren(getAllPossibleChildren());
        container.setPossibleParents(getAllPossibleParents());

        return container;
    }

    private Map<String, Set<String>> getAllRelationPartners() {
        if (allRelationPartners == null) {
            allRelationPartners = new HashMap<>();
            for (String id : getAllTypeIDs()) {
                allRelationPartners.put(id, getPossibleRelationPartners(id));
            }
        }
        return allRelationPartners;
    }

    private Map<String, String> getAllRelationLabels() {

        if(allRelationLabels == null){
            allRelationLabels = new HashMap<>();
            Set<HuiRelation> allRelationIDs = new HashSet<>();
            for(String typeId : getAllTypeIDs()){
                
                allRelationIDs.addAll(huiTypeFactory.getPossibleRelationsFrom(typeId));
                
            }
            for (HuiRelation relation : allRelationIDs) {
                allRelationLabels.put(relation.getId(), getRelationLabel(relation.getId()));

            }
            
        }
        return allRelationLabels;
    }

    private Map<String, Set<String>> getAllPossibleProperties() {

        if (allPossibleProperties == null) {
            allPossibleProperties = new HashMap<>();
            for (String typeId : getAllTypeIDs()) {
                allPossibleProperties.put(typeId, getPossibleProperties(typeId));
            }
        }
        return allPossibleProperties;
    }

    private Map<String, String> getAllLabels() {
        if (allLabels == null) {
            allLabels = new HashMap<>();
            for (String typeId : getAllTypeIDs()) {
                
                allLabels.put(typeId, getLabel(typeId));
                for (String propertyId : getPossibleProperties(typeId)) {
                    allLabels.put(propertyId, getLabel(propertyId));
                }

            }
        }
        return allLabels;
    }

}
