/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.interfaces.validation.IValidationService;
import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.validation.CnAValidation;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.crud.LoadScopeElementsById;

/**
 *
 */
public class ValidationService implements IValidationService {
    
    private Logger log = Logger.getLogger(ValidationService.class);
    
    private ICommandService commandService;
    private IDao<CnAValidation, Long> cnaValidationDAO;
    private IBaseDao<CnATreeElement, Long> cnaTreeElementDAO;
    
    // values from CnAValidation.hbm.xml
    private static final int MAXLENGTH_DBSTRING = 250;
    
    private HUITypeFactory huiTypeFactory;
    
    private static final String VALIDATION_SQL_SELECT_BASE = "from sernet.verinice.model.validation.CnAValidation validation where";
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#createValidation()
     */
    @Override
    public void createValidationForSingleElement(CnATreeElement elmt) {
        if(elmt == null){
            throw new RuntimeCommandException("validated element not existent");
        }
        ServerInitializer.inheritVeriniceContextState();
        HashMap<PropertyType, List<String>> hintsOfFailedValidationsMap = new HashMap<PropertyType, List<String>>();
        EntityType eType = getHuiTypeFactory().getEntityType(elmt.getTypeId());
        if(eType != null){
            for(Object pElement : eType.getAllPropertyTypes()){
                if(pElement instanceof PropertyType){
                    hintsOfFailedValidationsMap = (HashMap<PropertyType, List<String>>)updateValueMap(hintsOfFailedValidationsMap, (PropertyType)pElement, elmt);
                } else if(pElement instanceof PropertyGroup){
                    PropertyGroup pGroup = (PropertyGroup)pElement;
                    for(PropertyType pType : pGroup.getPropertyTypes()){
                        hintsOfFailedValidationsMap = (HashMap<PropertyType, List<String>>)updateValueMap(hintsOfFailedValidationsMap, pType, elmt);
                    }
                }
            }
            for(Entry<PropertyType, List<String>> entry : hintsOfFailedValidationsMap.entrySet()){
                for(String hint : entry.getValue()){
                    createCnAValidationObject(elmt, entry, hint);
                }
            }
        }
    }
    private void createCnAValidationObject(CnATreeElement elmt, Entry<PropertyType, List<String>> entry, String hint) {
        CnAValidation validation = new CnAValidation();
        validation.setElmtDbId(elmt.getDbId());
        validation.setPropertyId(truncateString(entry.getKey().getId(), MAXLENGTH_DBSTRING));
        validation.setHintId(truncateString(hint, MAXLENGTH_DBSTRING));
        validation.setElmtTitle(truncateString(elmt.getTitle(), MAXLENGTH_DBSTRING));
        validation.setScopeId(elmt.getScopeId());
        validation.setElementType(truncateString(elmt.getTypeId(), MAXLENGTH_DBSTRING));
        if(!isValidationExistant(elmt.getDbId(), entry.getKey().getId(), hint, elmt.getScopeId())){
            getCnaValidationDAO().saveOrUpdate(validation);
        }
        if(log.isDebugEnabled()){
            log.debug("Created Validation for : " + elmt.getTitle() + "(" + entry.getKey().getId() + ")\tHint:\t" + hint);
        }
    }
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#getValidations(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public List<CnAValidation> getValidations(Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();
        String hqlQuery = VALIDATION_SQL_SELECT_BASE + " validation.scopeId = ?";
        return getCnaValidationDAO().findByQuery(hqlQuery, new Object[]{scopeId});
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#getValidations(java.lang.Integer, java.lang.Integer)
     */
    /**
     * returns validations for given scope or
     * validations for given element in given scope
     * or validation for (scope-)unspecified element
     */
    @Override
    public List<CnAValidation> getValidations(Integer scopeId, Integer cnaId) {
        ServerInitializer.inheritVeriniceContextState();
        String hqlQuery = VALIDATION_SQL_SELECT_BASE;
        ArrayList<Object> paramList = new ArrayList<Object>(0);
        if(cnaId != null && scopeId == null){
            hqlQuery = hqlQuery + " validation.elmtDbId = ?";
            paramList.add(cnaId);
        } else if(cnaId != null && scopeId != null){
            hqlQuery = hqlQuery + " validation.elmtDbId = ? AND validation.scopeId = ?";
            paramList.add(cnaId);
            paramList.add(scopeId);
        } else if(cnaId == null && scopeId != null){
                hqlQuery = hqlQuery +  " validation.scopeId = ?";
                paramList.add(scopeId);
        }
        return getCnaValidationDAO().findByQuery(hqlQuery, paramList.toArray(new Object[paramList.size()]));
    }

    @Override
    public List<CnAValidation> getValidations(Integer elmtDbId, String propertyType){
        ServerInitializer.inheritVeriniceContextState();
        String hqlQuery = VALIDATION_SQL_SELECT_BASE +
        		" validation.elmtDbId = ? AND " +
        		"validation.propertyId = ?";
        if(elmtDbId != null){
            return getCnaValidationDAO().findByQuery(hqlQuery, new Object[]{elmtDbId, propertyType});
        } else {
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean isValidationExistant(Integer elmtDbId, String propertyType, String hintID, Integer scopeId){
        ServerInitializer.inheritVeriniceContextState();
        if(scopeId != null && elmtDbId != null){
            String hqlQuery = VALIDATION_SQL_SELECT_BASE + " validation.elmtDbId = ?" +
                    " AND validation.propertyId = ?" +
                    " AND validation.hintId = ?"+ 
                    " AND validation.scopeId = ?";

            return getCnaValidationDAO().findByQuery(hqlQuery, new Object[]{elmtDbId, propertyType, hintID, scopeId }).size() > 0;
        } else {
            return false;
        }
    }
    
    public boolean isValidationExistant(CnAValidation validation){
        ServerInitializer.inheritVeriniceContextState();
        return isValidationExistant(validation.getDbId(), validation.getPropertyId(), validation.getHintId(), validation.getScopeId());
    }
    
    @Override
    public boolean isValidationExistant(Integer elmtDbId, String propertyType) {
        ServerInitializer.inheritVeriniceContextState();
        String hqlQuery = VALIDATION_SQL_SELECT_BASE + " validation.elmtDbId = ?" +
                " AND validation.propertyId = ?";
        return getCnaValidationDAO().findByQuery(hqlQuery, new Object[]{elmtDbId, propertyType}).size() > 0;
    }
    
    public ICommandService getCommandService(){
        return commandService;
    }
    
    public void setCommandService(ICommandService commandService){
        this.commandService = commandService;
    }

    public IDao<CnAValidation, Long> getCnaValidationDAO() {
        return cnaValidationDAO;
    }

    public void setCnaValidationDAO(IDao<CnAValidation, Long> cnaValidationDAO) {
        this.cnaValidationDAO = cnaValidationDAO;
    }

    public IBaseDao<CnATreeElement, Long> getCnaTreeElementDAO() {
        return cnaTreeElementDAO;
    }

    public void setCnaTreeElementDAO(IBaseDao<CnATreeElement, Long> cnaTreeElementDAO) {
        this.cnaTreeElementDAO = cnaTreeElementDAO;
    }

    public HUITypeFactory getHuiTypeFactory() {
        return huiTypeFactory;
    }

    public void setHuiTypeFactory(HUITypeFactory huiTypeFactory) {
        this.huiTypeFactory = huiTypeFactory;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#deleteValidation(java.lang.Integer, java.lang.String, java.lang.String)
     */
    @Override
    public CnAValidation deleteValidation(Integer elmtDbId, String propertyType, String hintID, Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();
        String hqlQuery = VALIDATION_SQL_SELECT_BASE + " validation.elmtDbId = ?" +
                " AND validation.propertyId = ? " +
                "AND validation.hintId = ?"+ 
                " AND validation.scopeId = ?";
        CnAValidation validation = (CnAValidation) getCnaValidationDAO().findByQuery(hqlQuery, new Object[]{elmtDbId, propertyType, hintID, scopeId}).get(0);
        return deleteValidation(validation);
    }
    
    private List<String> getInvalidPropertyHints(PropertyType type, CnATreeElement elmt){
        ArrayList<String> hintsOfFailedValidations = new ArrayList<String>(0);
        List<Property> savedProperties = elmt.getEntity().getProperties(type.getId()).getProperties();
        // iterate(validate) all existant properties 
        for(Property savedProp : savedProperties){
            hintsOfFailedValidations.addAll(processValidationMap(type.validate(savedProp.getPropertyValue(), null), elmt, type));
        }
        // no property existant yet
        if(savedProperties.isEmpty()){
            hintsOfFailedValidations.addAll(processValidationMap(type.validate(null, null), elmt, type));
        }
        return hintsOfFailedValidations;
    }

    private List<String> processValidationMap(Map<String, Boolean> validationMap,
            CnATreeElement elmt, PropertyType type) {
        ArrayList<String> hintsOfFailedValidations = new ArrayList<>();
        for (Entry<String, Boolean> entry : validationMap.entrySet()) {
            boolean validationExists = isValidationExistant(elmt.getDbId(), type.getId(),
                    entry.getKey(), elmt.getScopeId());
            boolean elmtIsValid = entry.getValue().booleanValue();
            if (!elmtIsValid && !validationExists) {
                hintsOfFailedValidations.add(entry.getKey());
                if (log.isDebugEnabled()) {
                    log.debug("Validation:\t(" + type.getId() + ", " + entry.getValue() + ", "
                            + entry.getKey() + ") created");
                }
            } else if (elmtIsValid && validationExists) { // validation condition is fullfilled
                deleteValidation(elmt.getDbId(), type.getId(), entry.getKey(), elmt.getScopeId());
                if (log.isDebugEnabled()) {
                    log.debug("Validation:\t(" + type.getId() + ", " + entry.getValue() + ", "
                            + entry.getKey() + ") deleted");
                }
            } else if (!elmtIsValid && validationExists) {
                updateValidations(elmt.getScopeId(), elmt.getDbId(), elmt.getTitle());
            }
        }
        // if validation for type are existant, but map is empty (no validators
        // defined)
        // ===> delete existant validations for type
        if (validationMap.entrySet().isEmpty()) { // no negative validations existant
            for (CnAValidation validation : getValidations(elmt.getDbId(), type.getId())) {
                deleteValidation(validation);
            }
        }
        return hintsOfFailedValidations;
    }

    private Map<PropertyType, List<String>> updateValueMap(Map<PropertyType, List<String>> map,
            PropertyType type, CnATreeElement elmt) {
        List<String> invalidHints = getInvalidPropertyHints(type, elmt);
        if(map.containsKey(type)){
            List<String> listWithNewValues = map.get(type);
            listWithNewValues.addAll(invalidHints);
            map.put(type, listWithNewValues);
        } else {
            map.put(type, invalidHints);
        }
        return map;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#deleteValidation(java.lang.Integer, java.lang.String)
     */
    @Override
    public CnAValidation deleteValidation(Integer elmtDbId, String propertyType, Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();
        String hqlQuery = VALIDATION_SQL_SELECT_BASE + " validation.elmtDbId = ?" +
                " AND validation.propertyId = ? " + 
                " AND validation.scopeId = ?";
        CnAValidation validation = (CnAValidation) getCnaValidationDAO().findByQuery(hqlQuery, new Object[]{elmtDbId, propertyType, scopeId}).get(0);
        getCnaValidationDAO().delete(validation);
        return validation;
    }
    
    @Override
    public CnAValidation deleteValidation(CnAValidation validation){
        ServerInitializer.inheritVeriniceContextState();
        getCnaValidationDAO().delete(validation);
        return validation;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#getValidation(java.lang.Integer, java.lang.String, java.lang.String)
     */
    @Override
    public CnAValidation getValidation(Integer cnaDbId, String propertyType, String hint, Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();
        String hqlQuery = VALIDATION_SQL_SELECT_BASE + " validation.elmtDbId = ?" +
                " AND validation.propertyId = ? " +
                "AND validation.hintId = ?"+ 
                " AND validation.scopeId = ?";
        return (CnAValidation) getCnaValidationDAO().findByQuery(hqlQuery, new Object[]{cnaDbId, propertyType, hint, scopeId}).get(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#createValidationsForScope(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void createValidationsForScope(Integer scope) throws CommandException {
        ServerInitializer.inheritVeriniceContextState();
        LoadScopeElementsById command = new LoadScopeElementsById(scope);
        command = getCommandService().executeCommand(command);
        List<CnATreeElement> elementsToValidate = new ArrayList<>();

        for (CnATreeElement elmt : command.getResults()) {
            // IBSIStrukturKategorie does not have any fields to validate
            if (!(elmt instanceof IBSIStrukturKategorie)) {
                elementsToValidate.add(elmt);
            }
        }
        for (CnATreeElement elmt : elementsToValidate) {
            createValidationForSingleElement(elmt);
        }
    }

    @Override
    public void createValidationsForSubTree(CnATreeElement elmt) throws CommandException{
        ServerInitializer.inheritVeriniceContextState();
        if(Hibernate.isInitialized(elmt) || !elmt.isChildrenLoaded()){
            elmt = Retriever.retrieveElement(elmt, new RetrieveInfo().setChildren(true).setChildrenProperties(true).setProperties(true));
            elmt.setChildrenLoaded(true);
        }
        createValidationForSingleElement(elmt);
        for(CnATreeElement child : elmt.getChildren()){
            if(child.getScopeId() == null){
                LoadElementByUuid<CnATreeElement> childReloader = new LoadElementByUuid<CnATreeElement>(child.getUuid());
                childReloader = getCommandService().executeCommand(childReloader);
                child = childReloader.getElement();
            }
            createValidationsForSubTree(child);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#updateValidation(sernet.verinice.model.validation.CnAValidation, java.lang.String)
     */
    @Override
    public void updateValidations(Integer scopeId, Integer elmtDbId,  String title) {
        ServerInitializer.inheritVeriniceContextState();
        for(CnAValidation validation : getValidations(scopeId, elmtDbId)){
            validation.setElmtTitle(truncateString(title, MAXLENGTH_DBSTRING));
            getCnaValidationDAO().saveOrUpdate(validation); 
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#deleteValidations(java.lang.Integer, java.lang.Integer)
     */
    @Override
    public void deleteValidations(Integer scopeId, Integer elmtDbId) {
        ServerInitializer.inheritVeriniceContextState();
        String hqlQuery = VALIDATION_SQL_SELECT_BASE + " validation.elmtDbId = ?" +
                " AND validation.scopeId = ?";
        for(CnAValidation validation : (List<CnAValidation>)getCnaValidationDAO().findByQuery(hqlQuery, new Object[]{elmtDbId, scopeId})){
            getCnaValidationDAO().delete(validation);
        }
    }
    
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#getPropertyTypesToValidate(java.lang.Integer)
     */
    @Override
    public List<String> getPropertyTypesToValidate(Entity entity, Integer dbid) {
        
        ServerInitializer.inheritVeriniceContextState();
        
        List<String> failedValidationPropertyTypes = new ArrayList<String>(0);
        for(Object pElement : getHuiTypeFactory().getEntityType(entity.getEntityType()).getAllPropertyTypes()){
            if(pElement instanceof PropertyType){
                if(isValidationExistant(dbid, ((PropertyType)pElement).getId())){
                    failedValidationPropertyTypes.add(((PropertyType)pElement).getId());
                }
            } else if(pElement instanceof PropertyGroup){
                PropertyGroup pGroup = (PropertyGroup)pElement;
                for(PropertyType pType : pGroup.getPropertyTypes()){
                    if(isValidationExistant(dbid, pType.getId())){
                        failedValidationPropertyTypes.add(pType.getId());
                    }  
                }
            }
        }
        return failedValidationPropertyTypes;    
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#deleteValidationsOfSubtree(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void deleteValidationsOfSubtree(CnATreeElement elmt) {
        ServerInitializer.inheritVeriniceContextState();
        if(!Hibernate.isInitialized(elmt) || !elmt.isChildrenLoaded()){
            elmt = Retriever.retrieveElement(elmt, new RetrieveInfo().setChildren(true).setChildrenProperties(true).setProperties(true));
        }
        if(elmt != null){
            elmt.setChildrenLoaded(true);
            deleteValidations(elmt.getScopeId(), elmt.getDbId());
            for(CnATreeElement child : elmt.getChildren()){
                deleteValidationsOfSubtree(child);
            }
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#createValidationByUuid(java.lang.String)
     */
    @Override
    public void createValidationByUuid(String uuid) throws CommandException{
        ServerInitializer.inheritVeriniceContextState();
        LoadElementByUuid<CnATreeElement> elementLoader = new LoadElementByUuid<CnATreeElement>(uuid, new RetrieveInfo().setProperties(true));
        elementLoader = getCommandService().executeCommand(elementLoader);
        createValidationForSingleElement(elementLoader.getElement());
    }
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.validation.IValidationService#createValidationsForSubTreeByUuid(java.lang.String)
     */
    @Override
    public void createValidationsForSubTreeByUuid(String uuid) throws CommandException {
        ServerInitializer.inheritVeriniceContextState();
        LoadElementByUuid<CnATreeElement> elementLoader = new LoadElementByUuid<CnATreeElement>(uuid, new RetrieveInfo().setProperties(true).setChildren(true));
        elementLoader = getCommandService().executeCommand(elementLoader);
        createValidationsForSubTree(elementLoader.getElement());
    }
    
    private String truncateString(String input, int maxLength){
        String output;
        int dotAmount = 3;
        if(input.length() >= maxLength){
            StringBuilder sb = new StringBuilder();
            sb.append(input.substring(0, maxLength - dotAmount));
            for(int i = 0; i < dotAmount; i++){
                sb.append(".");
            }
            output = sb.toString();
        } else {
            output = input;
        }
        return output;
    }
}

