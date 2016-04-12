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

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.model.bsi.*;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.CnATypeMapper;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class HUIObjectModelService implements IObjectModelService {

    private static final Logger LOG = Logger.getLogger(HUIObjectModelService.class);
    private static HUIObjectModelService instance = null;
    private static HUITypeFactory huiTypeFactory = null;

    private Set<String> allTypeIds;
    private Map<String, Set<String>> possibleChildren;
    private Map<String, Set<String>> possibleParents;

    private HUIObjectModelService() {

        ServerInitializer.inheritVeriniceContextState();
        huiTypeFactory = HUITypeFactory.getInstance();
        allTypeIds = new HashSet<>(huiTypeFactory.getAllTypeIds());
        addAllBSIGroups();
    }

    private void addAllBSIGroups() {
        
        allTypeIds.add(AnwendungenKategorie.TYPE_ID);
        allTypeIds.add(GebaeudeKategorie.TYPE_ID);
        allTypeIds.add(ClientsKategorie.TYPE_ID);
        allTypeIds.add(ServerKategorie.TYPE_ID);
        allTypeIds.add(SonstigeITKategorie.TYPE_ID);
        allTypeIds.add(TKKategorie.TYPE_ID);
        allTypeIds.add(PersonenKategorie.TYPE_ID);
        allTypeIds.add(NKKategorie.TYPE_ID);
        allTypeIds.add(RaeumeKategorie.TYPE_ID);
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getRelationsFrom(java.lang.String)
     */
    @Override
    public Set<String> getRelations(String fromEntityTypeID, String toEntityTypeID) {
        if (huiTypeFactory.getEntityType(fromEntityTypeID) == null
                || huiTypeFactory.getEntityType(toEntityTypeID) == null) {
            return new HashSet<>();
        }
        Set<HuiRelation> relations = huiTypeFactory.getPossibleRelations(fromEntityTypeID,
                toEntityTypeID);
        relations.addAll(huiTypeFactory.getPossibleRelations(toEntityTypeID,
                fromEntityTypeID));
        HashSet<String> relationIds = new HashSet<>();
        for (HuiRelation huiRelation : relations) {

            relationIds.add(huiRelation.getId());
        }

        return relationIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getPossibleRelationPartners(java.lang.String)
     */
    @Override
    public Set<String> getPossibleRelationPartners(String typeID) {
        if (huiTypeFactory.getEntityType(typeID) == null) {
            return new HashSet<>();
        }
        HashSet<String> possiblePartners = new HashSet<>();

        Set<HuiRelation> relations = huiTypeFactory.getPossibleRelationsFrom(typeID);
        for (HuiRelation relation : relations) {
            possiblePartners.add(relation.getTo());
        }
        relations = huiTypeFactory.getPossibleRelationsTo(typeID);
        for (HuiRelation relation : relations) {
            possiblePartners.add(relation.getFrom());
        }
        return possiblePartners;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.linktable.ILinkTableContentService#getTypeIDs()
     */
    @Override
    public Set<String> getAllTypeIDs() {
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
        if (huiTypeFactory.getEntityType(typeID) == null) {
            return new HashSet<>();
        }
        return new HashSet<>(
                Arrays.asList(huiTypeFactory.getEntityType(typeID).getAllPropertyTypeIds()));
    }

    public static HUIObjectModelService getInstance() {
        if (instance == null) {
            instance = new HUIObjectModelService();
        }
        return instance;
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
        return huiTypeFactory.getMessage(id);
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
        return huiTypeFactory.getMessage(id + "_name");
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
        if (possibleChildren == null) {
            try {
                fillPossibleChildrenMap();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                LOG.error("getting possible Children went wrong", e);
                possibleChildren = null;
                return new HashSet<>();
            }
        }
        Set<String> set = possibleChildren.get(typeID);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;

    }

    private void fillPossibleChildrenMap() throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        possibleChildren = new HashMap<>();
        CnATreeElement parentInstance;
        CnATreeElement childInstance;
        Class<? extends CnATreeElement> parentClass;
        Class<? extends CnATreeElement> childClass;
        HashSet<String> possibleChildrenSet;

        for (String typeIdParent : allTypeIds) {

            try {
            parentClass = CnATypeMapper.getClassFromTypeId(typeIdParent);
            } catch (IllegalStateException e) {
                LOG.error("ParentClass for possible Children is not mapped", e);
                continue;
            }
            parentInstance = createInstance(parentClass, typeIdParent);
            possibleChildrenSet = new HashSet<>();
            for (String typeIdChild : allTypeIds) {

                try {
                    childClass = CnATypeMapper.getClassFromTypeId(typeIdChild);
                } catch (IllegalStateException e) {
                    LOG.error("ChildClass for possible Children is not mapped", e);
                    continue;
                }
                childInstance = createInstance(childClass, typeIdChild);
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


        if (possibleParents == null) {
            try {
                fillPossibleParentsMap();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                LOG.error("getting possible parents went wrong", e);
                possibleParents = null;
                return new HashSet<>();
            }
        }
        Set<String> set = possibleParents.get(typeID);
        if(set == null){
            set = new HashSet<>();
        }
        return set;

    }

    private void fillPossibleParentsMap() throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        possibleParents = new HashMap<>();
        CnATreeElement childInstance;
        Class<? extends CnATreeElement> childClass;
        Class<? extends CnATreeElement> parentClass;
        HashSet<String> possibleParentsSet;
        for (String typeIChild : allTypeIds) {

            try {
                childClass = CnATypeMapper.getClassFromTypeId(typeIChild);
            } catch (IllegalStateException e) {
                LOG.error("ChildClass for possible parents is not mapped", e);
                continue;
            }
            childInstance = createInstance(childClass, typeIChild);
            possibleParentsSet = new HashSet<>();
            for (String typeIdParent : allTypeIds) {
                try {
                    parentClass = CnATypeMapper.getClassFromTypeId(typeIdParent);
                } catch (IllegalStateException e) {
                    LOG.error("ParentClass for possible Parents is not mapped", e);
                    continue;
                }
                CnATreeElement parentInsance = createInstance(parentClass, typeIdParent);
                if (parentInsance.canContain(childInstance)) {
                    possibleParentsSet.add(typeIdParent);
                }
            }
            possibleParents.put(typeIChild, possibleParentsSet);

        }
    }
    

}
