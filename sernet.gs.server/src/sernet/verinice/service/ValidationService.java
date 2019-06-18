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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

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
import sernet.verinice.service.commands.LoadSubtreeIds;
import sernet.verinice.service.commands.crud.LoadScopeElementsById;

public class ValidationService implements IValidationService {

    private static final Logger log = Logger.getLogger(ValidationService.class);

    private ICommandService commandService;
    private IDao<CnAValidation, Long> cnaValidationDAO;
    private IBaseDao<CnATreeElement, Long> cnaTreeElementDAO;

    // values from CnAValidation.hbm.xml
    private static final int MAXLENGTH_DBSTRING = 250;

    private HUITypeFactory huiTypeFactory;

    /*
     * @see
     * sernet.verinice.interfaces.validation.IValidationService#createValidation
     * ()
     */
    @Override
    public void createValidationForSingleElement(CnATreeElement elmt) {
        if (elmt == null) {
            throw new RuntimeCommandException("validated element not existent");
        }
        ServerInitializer.inheritVeriniceContextState();
        EntityType eType = getHuiTypeFactory().getEntityType(elmt.getTypeId());
        if (eType != null) {
            List<CnAValidation> existingValidationsForElement = getValidations(elmt.getScopeId(),
                    elmt.getDbId());
            createValidationForSingleElement(elmt, eType, existingValidationsForElement);
        }
    }

    protected void createValidationForSingleElement(CnATreeElement element, EntityType entityType,
            List<CnAValidation> existingValidationsForElement) {
        HashMap<PropertyType, List<String>> hintsOfFailedValidationsMap = new HashMap<>();
        for (Object pElement : entityType.getAllPropertyTypes()) {
            if (pElement instanceof PropertyType) {
                updateValueMap(hintsOfFailedValidationsMap, (PropertyType) pElement, element,
                        existingValidationsForElement);
            } else if (pElement instanceof PropertyGroup) {
                PropertyGroup pGroup = (PropertyGroup) pElement;
                for (PropertyType pType : pGroup.getPropertyTypes()) {
                    updateValueMap(hintsOfFailedValidationsMap, pType, element,
                            existingValidationsForElement);
                }
            }
        }
        for (Entry<PropertyType, List<String>> entry : hintsOfFailedValidationsMap.entrySet()) {
            for (String hint : entry.getValue()) {
                createCnAValidationObject(element, entry, hint);
            }
        }
    }

    private void createCnAValidationObject(CnATreeElement elmt,
            Entry<PropertyType, List<String>> entry, String hint) {
        CnAValidation validation = new CnAValidation();
        validation.setElmtDbId(elmt.getDbId());
        validation
                .setPropertyId(StringUtils.abbreviate(entry.getKey().getId(), MAXLENGTH_DBSTRING));
        validation.setHintId(StringUtils.abbreviate(hint, MAXLENGTH_DBSTRING));
        validation.setElmtTitle(StringUtils.abbreviate(elmt.getTitle(), MAXLENGTH_DBSTRING));
        validation.setScopeId(elmt.getScopeId());
        validation.setElementType(StringUtils.abbreviate(elmt.getTypeId(), MAXLENGTH_DBSTRING));
        if (!isValidationExistant(elmt.getDbId(), entry.getKey().getId(), hint,
                elmt.getScopeId())) {
            getCnaValidationDAO().saveOrUpdate(validation);
        }
        if (log.isDebugEnabled()) {
            log.debug("Created Validation for : " + elmt.getTitle() + "(" + entry.getKey().getId()
                    + ")\tHint:\t" + hint);
        }
    }

    /*
     * @see
     * sernet.verinice.interfaces.validation.IValidationService#getValidations(
     * sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public List<CnAValidation> getValidations(Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();
        DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class)
                .add(createScopeIdRestriction(scopeId));
        return getCnaValidationDAO().findByCriteria(criteria);
    }

    /*
     * @see
     * sernet.verinice.interfaces.validation.IValidationService#getValidations(
     * java.lang.Integer, java.lang.Integer)
     */
    /**
     * returns validations for given scope or validations for given element in
     * given scope or validation for (scope-)unspecified element
     */
    @Override
    public List<CnAValidation> getValidations(Integer scopeId, Integer cnaId) {
        ServerInitializer.inheritVeriniceContextState();
        DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class);
        if (scopeId != null) {
            criteria.add(createScopeIdRestriction(scopeId));
        }
        if (cnaId != null) {
            criteria.add(createDbIdRestriction(cnaId));
        }
        return getCnaValidationDAO().findByCriteria(criteria);
    }

    @Override
    public List<CnAValidation> getValidations(Integer elmtDbId, String propertyType) {
        ServerInitializer.inheritVeriniceContextState();
        if (elmtDbId != null) {
            DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class)
                    .add(createDbIdRestriction(elmtDbId))
                    .add(createPropertyIdRestriction(propertyType));
            return getCnaValidationDAO().findByCriteria(criteria);

        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isValidationExistant(Integer elmtDbId, String propertyType, String hintID,
            Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();
        if (scopeId != null && elmtDbId != null) {
            DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class)
                    .add(createDbIdRestriction(elmtDbId))
                    .add(createPropertyIdRestriction(propertyType))
                    .add(createHintIdRestriction(hintID)).add(createScopeIdRestriction(scopeId));

            return !getCnaValidationDAO().findByCriteria(criteria).isEmpty();

        } else {
            return false;
        }
    }

    public boolean isValidationExistant(CnAValidation validation) {
        ServerInitializer.inheritVeriniceContextState();
        return isValidationExistant(validation.getDbId(), validation.getPropertyId(),
                validation.getHintId(), validation.getScopeId());
    }

    @Override
    public boolean isValidationExistant(Integer elmtDbId, String propertyType) {
        return !(getValidations(elmtDbId, propertyType)).isEmpty();
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
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

    /*
     * @see
     * sernet.verinice.interfaces.validation.IValidationService#deleteValidation
     * (java.lang.Integer, java.lang.String, java.lang.String)
     */
    @Override
    public CnAValidation deleteValidation(Integer elmtDbId, String propertyType, String hintID,
            Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();

        DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class)
                .add(createDbIdRestriction(elmtDbId)).add(createPropertyIdRestriction(propertyType))
                .add(createHintIdRestriction(hintID)).add(createScopeIdRestriction(scopeId));

        CnAValidation validation = (CnAValidation) getCnaValidationDAO().findByCriteria(criteria)
                .get(0);
        return deleteValidation(validation);
    }

    private List<String> getInvalidPropertyHints(PropertyType type, CnATreeElement elmt,
            List<CnAValidation> existingValidationsForType) {
        ArrayList<String> hintsOfFailedValidations = new ArrayList<>();
        List<Property> savedProperties = elmt.getEntity().getProperties(type.getId())
                .getProperties();
        // iterate(validate) all existing properties
        for (Property savedProp : savedProperties) {
            hintsOfFailedValidations
                    .addAll(processValidationMap(type.validate(savedProp.getPropertyValue(), null),
                            elmt, type, existingValidationsForType));
        }
        // no property exists yet
        if (savedProperties.isEmpty()) {
            hintsOfFailedValidations.addAll(processValidationMap(type.validate(null, null), elmt,
                    type, existingValidationsForType));
        }
        return hintsOfFailedValidations;
    }

    private List<String> processValidationMap(Map<String, Boolean> validationMap,
            CnATreeElement elmt, PropertyType type,
            List<CnAValidation> existingValidationsForType) {
        ArrayList<String> hintsOfFailedValidations = new ArrayList<>();
        for (Entry<String, Boolean> entry : validationMap.entrySet()) {
            boolean validationExists = existingValidationsForType.stream()
                    .anyMatch(validation -> validation.getHintId().equals(entry.getKey()));

            boolean elmtIsValid = entry.getValue().booleanValue();
            if (!elmtIsValid) {
                if (!validationExists) {
                    hintsOfFailedValidations.add(entry.getKey());
                    if (log.isDebugEnabled()) {
                        log.debug("Validation:\t(" + type.getId() + ", " + entry.getValue() + ", "
                                + entry.getKey() + ") created");
                    }
                } else {
                    updateValidations(elmt.getScopeId(), elmt.getDbId(), elmt.getTitle());
                }
            } else if (validationExists) { // validation
                                           // condition is
                                           // fulfilled
                deleteValidation(elmt.getDbId(), type.getId(), entry.getKey(), elmt.getScopeId());
                if (log.isDebugEnabled()) {
                    log.debug("Validation:\t(" + type.getId() + ", " + entry.getValue() + ", "
                            + entry.getKey() + ") deleted");
                }
            }
        }
        // if validation for type exists, but map is empty (no validators
        // defined)
        // ===> delete existing validations for type
        if (validationMap.isEmpty()) { // no negative validations exist
            for (CnAValidation validation : existingValidationsForType) {
                deleteValidation(validation);
            }
        }
        return hintsOfFailedValidations;
    }

    private void updateValueMap(Map<PropertyType, List<String>> map, PropertyType type,
            CnATreeElement elmt, List<CnAValidation> existingValidationsForElement) {
        List<CnAValidation> existingValidationsForType = existingValidationsForElement.stream()
                .filter(validation -> validation.getPropertyId().equals(type.getId()))
                .collect(Collectors.toList());

        List<String> invalidHints = getInvalidPropertyHints(type, elmt, existingValidationsForType);
        if (map.containsKey(type)) {
            List<String> listWithNewValues = map.get(type);
            listWithNewValues.addAll(invalidHints);
            map.put(type, listWithNewValues);
        } else {
            map.put(type, invalidHints);
        }
    }

    /*
     * @see
     * sernet.verinice.interfaces.validation.IValidationService#deleteValidation
     * (java.lang.Integer, java.lang.String)
     */
    @Override
    public CnAValidation deleteValidation(Integer elmtDbId, String propertyType, Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();
        DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class)
                .add(createDbIdRestriction(elmtDbId)).add(createPropertyIdRestriction(propertyType))
                .add(createScopeIdRestriction(scopeId));
        CnAValidation validation = (CnAValidation) getCnaValidationDAO().findByCriteria(criteria)
                .get(0);
        getCnaValidationDAO().delete(validation);
        return validation;
    }

    @Override
    public CnAValidation deleteValidation(CnAValidation validation) {
        ServerInitializer.inheritVeriniceContextState();
        getCnaValidationDAO().delete(validation);
        return validation;
    }

    /*
     * @see
     * sernet.verinice.interfaces.validation.IValidationService#getValidation(
     * java.lang.Integer, java.lang.String, java.lang.String)
     */
    @Override
    public CnAValidation getValidation(Integer cnaDbId, String propertyType, String hint,
            Integer scopeId) {
        ServerInitializer.inheritVeriniceContextState();
        DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class)
                .add(createDbIdRestriction(cnaDbId)).add(createPropertyIdRestriction(propertyType))
                .add(createHintIdRestriction(hint)).add(createScopeIdRestriction(scopeId));
        return (CnAValidation) getCnaValidationDAO().findByCriteria(criteria).get(0);
    }

    /*
     * @see sernet.verinice.interfaces.validation.IValidationService#
     * createValidationsForScope(sernet.verinice.model.common.CnATreeElement)
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
    public void createValidationsForSubTree(CnATreeElement elmt) throws CommandException {
        ServerInitializer.inheritVeriniceContextState();
        if (Hibernate.isInitialized(elmt) || !elmt.isChildrenLoaded()) {
            elmt = Retriever.retrieveElement(elmt, new RetrieveInfo().setChildren(true)
                    .setChildrenProperties(true).setProperties(true));
            elmt.setChildrenLoaded(true);
        }
        createValidationForSingleElement(elmt);
        for (CnATreeElement child : elmt.getChildren()) {
            if (child.getScopeId() == null) {
                LoadElementByUuid<CnATreeElement> childReloader = new LoadElementByUuid<>(
                        child.getUuid());
                childReloader = getCommandService().executeCommand(childReloader);
                child = childReloader.getElement();
            }
            createValidationsForSubTree(child);
        }
    }

    /*
     * @see
     * sernet.verinice.interfaces.validation.IValidationService#updateValidation
     * (sernet.verinice.model.validation.CnAValidation, java.lang.String)
     */
    @Override
    public void updateValidations(Integer scopeId, Integer elmtDbId, String title) {
        ServerInitializer.inheritVeriniceContextState();
        for (CnAValidation validation : getValidations(scopeId, elmtDbId)) {
            validation.setElmtTitle(StringUtils.abbreviate(title, MAXLENGTH_DBSTRING));
            getCnaValidationDAO().saveOrUpdate(validation);
        }
    }

    /*
     * @see sernet.verinice.interfaces.validation.IValidationService#
     * deleteValidations(java.lang.Integer, java.lang.Integer)
     */
    @Override
    public void deleteValidations(Integer scopeId, Integer elmtDbId) {
        ServerInitializer.inheritVeriniceContextState();
        DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class)
                .add(createDbIdRestriction(elmtDbId)).add(createScopeIdRestriction(scopeId));
        for (CnAValidation validation : (List<CnAValidation>) getCnaValidationDAO()
                .findByCriteria(criteria)) {
            getCnaValidationDAO().delete(validation);
        }
    }

    /*
     * @see sernet.verinice.interfaces.validation.IValidationService#
     * getPropertyTypesToValidate(java.lang.Integer)
     */
    @Override
    public List<String> getPropertyTypesToValidate(Entity entity, Integer dbid) {

        ServerInitializer.inheritVeriniceContextState();

        List<String> failedValidationPropertyTypes = new ArrayList<>(0);
        for (Object pElement : getHuiTypeFactory().getEntityType(entity.getEntityType())
                .getAllPropertyTypes()) {
            if (pElement instanceof PropertyType) {
                if (isValidationExistant(dbid, ((PropertyType) pElement).getId())) {
                    failedValidationPropertyTypes.add(((PropertyType) pElement).getId());
                }
            } else if (pElement instanceof PropertyGroup) {
                PropertyGroup pGroup = (PropertyGroup) pElement;
                for (PropertyType pType : pGroup.getPropertyTypes()) {
                    if (isValidationExistant(dbid, pType.getId())) {
                        failedValidationPropertyTypes.add(pType.getId());
                    }
                }
            }
        }
        return failedValidationPropertyTypes;
    }

    /*
     * @see sernet.verinice.interfaces.validation.IValidationService#
     * deleteValidationsOfSubtree(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public void deleteValidationsOfSubtree(CnATreeElement elmt) throws CommandException {
        ServerInitializer.inheritVeriniceContextState();
        if (elmt == null) {
            return;
        }

        LoadSubtreeIds loadSubtreeIdsCommand = new LoadSubtreeIds(elmt);
        loadSubtreeIdsCommand = getCommandService().executeCommand(loadSubtreeIdsCommand);
        Set<Integer> dbIdsOfSubtree = loadSubtreeIdsCommand.getDbIdsOfSubtree();
        DetachedCriteria criteria = DetachedCriteria.forClass(CnAValidation.class)
                .add(Restrictions.in("elmtDbId", dbIdsOfSubtree))
                .add(createScopeIdRestriction(elmt.getScopeId()));
        for (CnAValidation validation : (List<CnAValidation>) getCnaValidationDAO()
                .findByCriteria(criteria)) {
            getCnaValidationDAO().delete(validation);
        }
    }

    /*
     * @see sernet.verinice.interfaces.validation.IValidationService#
     * createValidationByUuid(java.lang.String)
     */
    @Override
    public void createValidationByUuid(String uuid) throws CommandException {
        ServerInitializer.inheritVeriniceContextState();
        LoadElementByUuid<CnATreeElement> elementLoader = new LoadElementByUuid<>(uuid,
                new RetrieveInfo().setProperties(true));
        elementLoader = getCommandService().executeCommand(elementLoader);
        createValidationForSingleElement(elementLoader.getElement());
    }

    @Override
    public void createValidationsByUuids(Collection<String> uuids) throws CommandException {
        DetachedCriteria criteriaElements = DetachedCriteria.forClass(CnATreeElement.class)
                .add(Restrictions.in("uuid", uuids));
        new RetrieveInfo().setProperties(true).configureCriteria(criteriaElements);
        @SuppressWarnings("unchecked")
        List<CnATreeElement> elements = getCnaTreeElementDAO().findByCriteria(criteriaElements);

        DetachedCriteria criteriaValidations = DetachedCriteria.forClass(CnAValidation.class);
        criteriaValidations.add(Restrictions.in("elmtDbId",
                elements.stream().map(CnATreeElement::getDbId).collect(Collectors.toSet())));
        @SuppressWarnings("unchecked")
        List<CnAValidation> existingValidations = getCnaValidationDAO()
                .findByCriteria(criteriaValidations);
        Map<Integer, List<CnAValidation>> existingValidationsByElementId = existingValidations
                .stream().collect(Collectors.groupingBy(CnAValidation::getElmtDbId));
        ServerInitializer.inheritVeriniceContextState();

        for (CnATreeElement element : elements) {
            createValidationForSingleElement(element, element.getEntityType(),
                    existingValidationsByElementId.getOrDefault(element.getDbId(),
                            Collections.emptyList()));
        }
    }

    /*
     * @see sernet.verinice.interfaces.validation.IValidationService#
     * createValidationsForSubTreeByUuid(java.lang.String)
     */
    @Override
    public void createValidationsForSubTreeByUuid(String uuid) throws CommandException {
        ServerInitializer.inheritVeriniceContextState();
        LoadElementByUuid<CnATreeElement> elementLoader = new LoadElementByUuid<>(uuid,
                new RetrieveInfo().setProperties(true).setChildren(true));
        elementLoader = getCommandService().executeCommand(elementLoader);
        createValidationsForSubTree(elementLoader.getElement());
    }

    private static Criterion createScopeIdRestriction(Integer scopeId) {
        return Restrictions.eq("scopeId", scopeId);
    }

    private static Criterion createDbIdRestriction(Integer cnaId) {
        return Restrictions.eq("elmtDbId", cnaId);
    }

    private static Criterion createPropertyIdRestriction(String propertyType) {
        return Restrictions.eq("propertyId", propertyType);
    }

    private static Criterion createHintIdRestriction(String hintID) {
        return Restrictions.eq("hintId", hintID);
    }
}
