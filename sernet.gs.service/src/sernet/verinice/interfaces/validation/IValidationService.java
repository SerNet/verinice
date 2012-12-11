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
package sernet.verinice.interfaces.validation;

import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.validation.CnAValidation;

/**
 * Service to manage {@link CnAValidation} elements
 * 
 */
public interface IValidationService {
    
    /**
     * Creates validations for every validation rule defined in snca.xml for any property of given element
     * @param elmt
     */
    public void createValidationForSingleElement(CnATreeElement elmt);
    
    public void createValidationByUuid(String uuid) throws CommandException;
    
    
    /**
     * validates a whole scope own thread, supplies feedback to user via monitor
     * @param scope
     * @param monitor (use {@link IProgressMonitor} ) to provide feedback
     * @throws CommandException
     */
    public void createValidationsForScope(Integer scope) throws CommandException;
    
    /**
     * creates validations for a cnatreeElement and all of its children (recursivly)
     * @param elmt
     * @throws CommandException
     */
    public void createValidationsForSubTree(CnATreeElement elmt) throws CommandException;
    
    public void createValidationsForSubTreeByUuid(String uuid) throws CommandException;

    /**
     * Returns all validations for given scope
     * @param cnaDbId
     * @return
     */
    public List<CnAValidation> getValidations(Integer scopeId);
    
    /**
     * Returns all validations for a given element and scope
     * @param scopeId
     * @param cnaId
     * @return
     */
    public List<CnAValidation> getValidations(Integer scopeId, Integer cnaId);
    
    /**
     * Returns a single validation given by 3 determining parameters
     * @param cnaDbId
     * @param propertyType
     * @param hint
     * @return
     */
    public CnAValidation getValidation(Integer cnaDbId, String propertyType, String hint, Integer scopeId);
    
    /**
     * Returns if a validation is a db entry already
     * @param elmtDbId
     * @param propertyType
     * @param hintID
     * @return
     */
    public boolean isValidationExistant(Integer elmtDbId, String propertyType, String hintID, Integer scopeId);
    
    public boolean isValidationExistant(Integer elmtDbId, String propertyType);
    
    /**
     * Deletes a validation
     * @param elmtDbId
     * @param propertyType
     * @param hintID
     */
    public CnAValidation deleteValidation(Integer elmtDbId, String propertyType, String hintID, Integer scopeId);
    
    public CnAValidation deleteValidation(Integer elmtDbId, String propertyType, Integer scopeId);
    
    public CnAValidation deleteValidation(CnAValidation validation);
    
    public void deleteValidations(Integer scopeId, Integer elmtDbId);
    
    public void deleteValidationsOfSubtree(CnATreeElement elmt);
    
    /**
     * Returns all validations for a specified {@link PropertType} of a given {@link CnATreeElement} 
     * @param elmtDbId
     * @param propertyType
     * @return
     */
    public List<CnAValidation> getValidations(Integer elmtDbId, String propertyType);
    
    /**
     * Updates title in existant {@link CnAValidation} 
     * @param scopeId
     * @param elmtDbId
     * @param title
     */
    public void updateValidations(Integer scopeId, Integer elmtDbId,  String title);
    
    /**
     * returns all propertytypeids that needs to be validated for specified cnatreeelement
     * @param dbid
     * @return
     */
    public List<String> getPropertyTypesToValidate(Entity entity, Integer cnaDbId);
}
