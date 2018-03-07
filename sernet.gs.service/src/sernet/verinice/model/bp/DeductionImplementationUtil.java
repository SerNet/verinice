/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bp;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A collection of helper methods and constants for the deduction of the
 * implementation status of the modernized base protection.
 *
 * @author uz[at]sernet.de
 *
 */
public final class DeductionImplementationUtil {

    public static final String IMPLEMENTATION_STATUS = "_implementation_status";
    public static final String IMPLEMENTATION_DEDUCE = "_implementation_deduce";

    public static final String IMPLEMENTATION_STATUS_CODE_NO = "_implementation_status_no";
    public static final String IMPLEMENTATION_STATUS_CODE_YES = "_implementation_status_yes";
    public static final String IMPLEMENTATION_STATUS_CODE_PARTIALLY = "_implementation_status_partially";
    public static final String IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE = "_implementation_status_na";

    private DeductionImplementationUtil() {
        super();
    }

    /**
     * Set the implementation status of the {@link Safeguard} to the
     * {@link BpRequirement} when the deduction of the implementation status is
     * enabled for this {@link BpRequirement}.
     *
     * @return true if the status has changed
     */
    public static boolean setImplementationStausToRequirement(CnATreeElement safeguard,
            CnATreeElement requirement) {
        if (!isDeductiveImplementationEnabled(requirement)) {
            return false;
        }
        String optionValue = getImplementationStatus(safeguard);
        if (optionValue == null) {
            return false;
        }
        optionValue = optionValue.replaceFirst(Safeguard.TYPE_ID, BpRequirement.TYPE_ID);
        String propertyType = getImplementationStatusId(requirement);
        String propertyValue = getImplementationStatus(requirement);
        if (optionValue.equals(propertyValue)) {
            return false;
        }
        requirement.setSimpleProperty(propertyType, optionValue);
        return true;
    }

    /**
     * Return the implementation status of the given {@link CnATreeElement}.
     *
     * @param element
     *            - must not be null
     */
    public static String getImplementationStatus(CnATreeElement element) {
        Entity entity = element.getEntity();
        return entity.getOptionValue(getImplementationStatusId(element));
    }

    /**
     * Return the property name of the implementation status for a given
     * {@link CnATreeElement}.
     *
     * @param element
     *            - must not be null
     */
    public static String getImplementationStatusId(CnATreeElement element) {
        return element.getTypeId() + IMPLEMENTATION_STATUS;
    }

    /**
     * Return true when the implementation status is deducted from another
     * {@link CnATreeElement}.
     *
     * @param element
     *            - must not be null
     */
    public static boolean isDeductiveImplementationEnabled(CnATreeElement element) {
        String value = element.getPropertyValue(element.getTypeId() + IMPLEMENTATION_DEDUCE);
        return isSelected(value);
    }

    /**
     * Is the property selected.
     */
    private static boolean isSelected(String value) {
        return "1".equals(value);
    }

}
