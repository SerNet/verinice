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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.hui.common.connect.PropertyGroup;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BpDocumentGroup;
import sernet.verinice.model.bp.groups.BpIncidentGroup;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bp.groups.BpRecordGroup;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.DeviceGroup;
import sernet.verinice.model.bp.groups.IcsSystemGroup;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.ClientsKategorie;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.NKKategorie;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.bsi.RaeumeKategorie;
import sernet.verinice.model.bsi.ServerKategorie;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.bsi.TKKategorie;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.CnATypeMapper;
import sernet.verinice.service.linktable.CnaLinkPropertyConstants;

/**
 * Server implementation of {@link IObjectModelService}
 * 
 * @see HUIObjectModelLoader
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class HUIObjectModelService implements IObjectModelService {

    private static final Logger LOG = Logger.getLogger(HUIObjectModelService.class);
    private HUITypeFactory huiTypeFactory;

    private Map<String, Set<String>> allRelationPartners = null;
    private Map<String, Set<String>> allPossibleProperties = null;
    private Map<String, String> allLabels = null;
    private Map<String, String> allRelationLabels = null;
    private Map<String, CnATreeElement> allTypeInstances = null;

    private Set<String> allTypeIds = null;
    private Set<String> allBSICategories = null;
    private Set<String> allStaticProperties = null;
    private Set<String> allBpCategories = null;

    private Map<String, Set<String>> possibleChildren = null;
    private Map<String, Set<String>> possibleParents = null;

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.model.IObjectModelService#init()
     */
    @Override
    public void init() {
        ServerInitializer.inheritVeriniceContextState();
        if (allTypeIds == null || allTypeIds.isEmpty()) {
            fillAllTypeIds();
        }
        if (allTypeInstances == null || allTypeInstances.isEmpty()) {
            fillAllTypeInstances();
        }
        if (possibleChildren == null || possibleChildren.isEmpty()) {
            fillPossibleChildrenMap();
        }
        if (possibleParents == null || possibleParents.isEmpty()) {
            fillPossibleParentsMap();
        }
        if (LOG.isDebugEnabled()) {

            LOG.debug("init objectModelService finished"); //$NON-NLS-1$
        }
    }

    private void fillAllTypeInstances() {

        allTypeInstances = new HashMap<>();
        CnATreeElement parentInstance;
        Class<? extends CnATreeElement> clazz;
        for (String typeId : getAllTypeIDs()) {
            clazz = CnATypeMapper.getClassFromTypeId(typeId);
            if (clazz != null) {
                try {
                    if("moditbp_room".equals(typeId)) {
                        "".hashCode();
                    }
                    parentInstance = createInstance(clazz, typeId);
                } catch (InstantiationException | IllegalAccessException
                        | InvocationTargetException | NoSuchMethodException e) {
                    LOG.error(e);
                    throw new IllegalStateException(e);
                }

                if (parentInstance != null) {
                    allTypeInstances.put(typeId, parentInstance);
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("typeIDs instantiated"); //$NON-NLS-1$
        }
    }

    private void fillAllTypeIds() {

        if (allTypeIds != null) {
            return;
        }
        allTypeIds = new HashSet<>(getHuiTypeFactory().getAllTypeIds());
        removeNonCnaTreeElementTypeIDs();
        addAllBSIElements();
        addBpCategories();
        addAllStaticProperties();
    }

    

    public void removeNonCnaTreeElementTypeIDs() {
        allTypeIds.remove("note"); //$NON-NLS-1$
        allTypeIds.remove("role"); //$NON-NLS-1$
        allTypeIds.remove("configuration"); //$NON-NLS-1$
        allTypeIds.remove("attachment"); //$NON-NLS-1$
    }

    private void addAllBSIElements() {
        allBSICategories = new HashSet<>();
        allBSICategories.add(AnwendungenKategorie.TYPE_ID);
        allBSICategories.add(GebaeudeKategorie.TYPE_ID);
        allBSICategories.add(ClientsKategorie.TYPE_ID);
        allBSICategories.add(ServerKategorie.TYPE_ID);
        allBSICategories.add(SonstigeITKategorie.TYPE_ID);
        allBSICategories.add(TKKategorie.TYPE_ID);
        allBSICategories.add(PersonenKategorie.TYPE_ID);
        allBSICategories.add(NKKategorie.TYPE_ID);
        allBSICategories.add(RaeumeKategorie.TYPE_ID);
        allBSICategories.add(FinishedRiskAnalysis.TYPE_ID);

        allTypeIds.addAll(allBSICategories);    
    }
    
    private void addBpCategories () {
        allBpCategories = new HashSet<>(8);
        allBpCategories.add(ApplicationGroup.TYPE_ID);
        allBpCategories.add(BpPersonGroup.TYPE_ID);
        allBpCategories.add(BpRequirementGroup.TYPE_ID);
        allBpCategories.add(BpThreatGroup.TYPE_ID);
        allBpCategories.add(BusinessProcessGroup.TYPE_ID);
        allBpCategories.add(DeviceGroup.TYPE_ID);
        allBpCategories.add(IcsSystemGroup.TYPE_ID);
        allBpCategories.add(ItSystemGroup.TYPE_ID);
        allBpCategories.add(NetworkGroup.TYPE_ID);
        allBpCategories.add(RoomGroup.TYPE_ID);
        allBpCategories.add(SafeguardGroup.TYPE_ID);
        allBpCategories.add(BpDocumentGroup.TYPE_ID);
        allBpCategories.add(BpIncidentGroup.TYPE_ID);
        allBpCategories.add(BpRecordGroup.TYPE_ID);
        
        allTypeIds.addAll(allBpCategories);
    }
    
    
    private void addAllStaticProperties() {
        allStaticProperties = new HashSet<>();
        allStaticProperties.add(CnATreeElement.SCOPE_ID);
        allStaticProperties.add(CnATreeElement.PARENT_ID);
        allStaticProperties.add(CnATreeElement.DBID);
        allStaticProperties.add(CnATreeElement.UUID);    
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.linktable.ILinkTableContentService#
     * getRelationsFrom(java.lang.String)
     */
    @Override
    public Set<String> getRelations(String fromEntityTypeID, String toEntityTypeID) {

        ServerInitializer.inheritVeriniceContextState();
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
        ServerInitializer.inheritVeriniceContextState();
        if (getHuiTypeFactory().getEntityType(typeID) == null || isBSICategory(typeID) || isBpCategory(typeID)) {
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

    private boolean isBSICategory(String typeID) {
        return allBSICategories.contains(typeID);

    }
    
    private boolean isBpCategory(String typeId) {
        return allBpCategories.contains(typeId);
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
        ServerInitializer.inheritVeriniceContextState();
        if (isBSICategory(typeID)) {
            HashSet<String> set = new HashSet<>();
            set.add(typeID + "_name"); //$NON-NLS-1$
            set.addAll(allStaticProperties);
            return set;
        }
        if (getHuiTypeFactory().getEntityType(typeID) == null) {
            return new HashSet<>();
        }
        HashSet<String> set = new HashSet<>(
                Arrays.asList(getHuiTypeFactory().getEntityType(typeID).getAllPropertyTypeIds()));
        set.addAll(allStaticProperties);
        return set;
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
        if (isDefaultMessage(id)) {
            return id;
        }
        return getHuiTypeFactory().getMessage(id);
    }

    private boolean isDefaultMessage(String id) {
        return id.contains(" "); //$NON-NLS-1$
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
        ServerInitializer.inheritVeriniceContextState();
        return getHuiTypeFactory().getMessage(id + "_name"); //$NON-NLS-1$
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
        init();
        Set<String> set = possibleChildren.get(typeID);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;

    }

    private void fillPossibleChildrenMap() {

        possibleChildren = new HashMap<>();
        CnATreeElement parentInstance;
        Set<String> possibleChildrenSet;

        for (String typeIdParent : getAllTypeIDs()) {
            parentInstance = allTypeInstances.get(typeIdParent);
            if (parentInstance != null) {
                possibleChildrenSet = getPossibleChildren(parentInstance);
                if (possibleChildrenSet != null) {
                    possibleChildren.put(typeIdParent, possibleChildrenSet);
                }
            }
        }
    }

    public Set<String> getPossibleChildren(CnATreeElement parentInstance) {
        ServerInitializer.inheritVeriniceContextState();
        CnATreeElement childInstance;
        HashSet<String> possibleChildrenSet = new HashSet<>();
        for (String typeIdChild : getAllTypeIDs()) {
            childInstance = allTypeInstances.get(typeIdChild);
            if (childInstance != null && parentInstance.canContain(childInstance)) {
                possibleChildrenSet.add(typeIdChild);
            }
        }
        return possibleChildrenSet;
    }

    private CnATreeElement createInstance(Class<? extends CnATreeElement> clazz, String typeId)
            throws InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {

        CnATreeElement container = null;
        CnATreeElement element;
        boolean createChildren = false;
        if (isOrganization(clazz, typeId)) {
            element = new Organization(container, createChildren);
        } else if (isAudit(clazz, typeId)) {
            element = new Audit(container, createChildren);
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
        init();
        Set<String> set = possibleParents.get(typeID);
        if (set == null) {
            set = new HashSet<>();
        }
        return set;

    }

    private void fillPossibleParentsMap() {

        possibleParents = new HashMap<>();
        CnATreeElement childInstance;
        Set<String> possibleParentsSet;
        for (String typeIdChild : getAllTypeIDs()) {

            childInstance = allTypeInstances.get(typeIdChild);
            if (childInstance != null) {
                possibleParentsSet = getPossibleParents(childInstance);
                if (possibleParentsSet != null) {
                    possibleParents.put(typeIdChild, possibleParentsSet);
                }
            }

        }
    }

    public Set<String> getPossibleParents(CnATreeElement childInstance) {
        HashSet<String> possibleParentsSet;
        possibleParentsSet = new HashSet<>();
        for (String typeIdParent : getAllTypeIDs()) {
            CnATreeElement parentInsance = allTypeInstances.get(typeIdParent);
            if (parentInsance != null && parentInsance.canContain(childInstance)) {
                    possibleParentsSet.add(typeIdParent);
                }
        }
        return possibleParentsSet;
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

        ServerInitializer.inheritVeriniceContextState();
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
    
    @Override
    public boolean isValidTypeId(String typeID) {
        return allLabels.containsKey(typeID);
    }

    @Override
    public boolean isValidRelationId(String relationID) {
        return allRelationLabels.containsKey(relationID);
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
                if (!isBSICategory(typeId) && !isBpCategory(typeId)) {
                    allRelationIDs.addAll(huiTypeFactory.getPossibleRelationsFrom(typeId));
                }
                
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
                    allLabels.put(propertyId, getPropertyLabel(typeId, propertyId));
                }

            }
        }
        return allLabels;
    }

    private String getPropertyLabel(String entityId, String propertyId) {

        ServerInitializer.inheritVeriniceContextState();
        StringBuilder label = new StringBuilder();

        PropertyGroup propertyGroup = getHuiTypeFactory().getPropertyGroup(entityId, propertyId);
        if (propertyGroup != null) {
            label.append(getLabel(propertyGroup.getId()));
            label.append(" - "); //$NON-NLS-1$
        }
        label.append(getLabel(propertyId));

        return  label.toString();
    }
    
    public static String getCnaLinkPropertyMessage(String cnaLinkProperty) {
        switch (cnaLinkProperty) {
        case CnaLinkPropertyConstants.TYPE_TITLE:
            return Messages.getString("HUIObjectModelService.10");  //$NON-NLS-1$
        case CnaLinkPropertyConstants.TYPE_DESCRIPTION:
            return Messages.getString("HUIObjectModelService.11");  //$NON-NLS-1$
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_C:
            return Messages.getString("HUIObjectModelService.12");  //$NON-NLS-1$
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_I:
            return Messages.getString("HUIObjectModelService.13");  //$NON-NLS-1$
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_A:
            return Messages.getString("HUIObjectModelService.14");  //$NON-NLS-1$
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_C_WITH_CONTROLS:
            return Messages.getString("HUIObjectModelService.1");   //$NON-NLS-1$
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_I_WITH_CONTROLS:
            return Messages.getString("HUIObjectModelService.2");   //$NON-NLS-1$
        case CnaLinkPropertyConstants.TYPE_RISK_VALUE_A_WITH_CONTROLS:
            return Messages.getString("HUIObjectModelService.3");    //$NON-NLS-1$
        case CnaLinkPropertyConstants.TYPE_RISK_TREATMENT:
            return Messages.getString("HUIObjectModelService.4");  //$NON-NLS-1$
        default:
            return Messages.getString("HUIObjectModelService.15");  //$NON-NLS-1$
        }
    }
    

}
