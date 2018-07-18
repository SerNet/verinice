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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.common.CnALink;
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
     * Set the implementation status to a single requirement by calculating the
     * the value from the safeguards.
     *
     * @return true when the state was changed
     */
    public static boolean setImplementationStausToRequirement(CnATreeElement requirement) {
        List<CnATreeElement> safeGuards = getSafeguardsFromRequirement(requirement);
        return setImplementationStausToRequirement(safeGuards, requirement);
    }

    /**
     * Get the connected safeguards from a requirement.
     */
    public static List<CnATreeElement> getSafeguardsFromRequirement(CnATreeElement requirement) {
        List<CnATreeElement> safeGuards = new ArrayList<>(requirement.getLinksDown().size());
        for (CnALink cnALink : requirement.getLinksDown()) {
            CnATreeElement dependant = cnALink.getDependency();
            if (Safeguard.TYPE_ID.equals(dependant.getTypeId())) {
                safeGuards.add(dependant);
            }
        }
        return safeGuards;
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
        return setImplementationStausToRequirement(requirement, optionValue);
    }

    /**
     * Set a safeguard implementation status to a requirement.
     *
     * @param optionValue-
     *            the implementation status from a safeguard, must not be null.
     *
     * @return true if the state was changed
     */
    public static boolean setImplementationStausToRequirement(CnATreeElement requirement,
            String optionValue) {
        String propertyType = getImplementationStatusId(requirement);
        String propertyValue = getImplementationStatus(requirement);
        if (optionValue != null) {
            optionValue = optionValue.replaceFirst(Safeguard.TYPE_ID, BpRequirement.TYPE_ID);
            if (optionValue.equals(propertyValue)) {
                return false;
            }
        } else if (propertyValue == null) {
            return false;
        }

        requirement.setSimpleProperty(propertyType, optionValue);
        return true;
    }

    /**
     * Set the implementation status to a single requirement by calculating the
     * the value from the safeguards.
     *
     * @return true when the state was changed
     */
    public static boolean setImplementationStausToRequirement(List<CnATreeElement> safeGuards,
            CnATreeElement requirement) {
        if (safeGuards == null || safeGuards.isEmpty() || requirement == null
                || !isDeductiveImplementationEnabled(requirement)) {
            return false;
        }

        if (safeGuards.size() == 1) {
            CnATreeElement safeguard = safeGuards.get(0);
            return setImplementationStausToRequirement(safeguard, requirement);
        }
        Map<String, Integer> statusMap = new HashMap<>(safeGuards.size());
        for (CnATreeElement cnATreeElement : safeGuards) {
            String implementationStatus = getImplementationStatus(cnATreeElement);
            Integer counter = statusMap.get(implementationStatus);
            if (counter == null) {
                counter = 0;
            }
            counter = counter + 1;
            statusMap.put(implementationStatus, counter);
        }
        // all the same
        if (statusMap.size() == 1) {
            CnATreeElement safeguard = safeGuards.get(0);
            return setImplementationStausToRequirement(safeguard, requirement);
        }
        Integer stateNA = statusMap
                .get(Safeguard.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        Integer stateYES = statusMap.get(Safeguard.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES);
        Integer stateNO = statusMap.get(Safeguard.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO);
        // only na and yes=>yes
        if (stateNA != null && stateYES != null && statusMap.size() == 2) {
            return setImplementationStatus(requirement, IMPLEMENTATION_STATUS_CODE_YES);
        }
        // half of not_na must be no=>no
        if (stateNO != null && stateNA != null) {
            int notNA = safeGuards.size() - stateNA.intValue();
            if (stateNO > (notNA / 2.0f)) {
                return setImplementationStatus(requirement, IMPLEMENTATION_STATUS_CODE_NO);
            }
        }
        // every other combination=>partially
        return setImplementationStatus(requirement, IMPLEMENTATION_STATUS_CODE_PARTIALLY);
    }

    /**
     * Set the status code to the requirement.
     *
     * @return true if the status was changed
     */
    private static boolean setImplementationStatus(CnATreeElement requirement, String statusCode) {
        String oldValue = getImplementationStatus(requirement);
        String newValue = BpRequirement.TYPE_ID + statusCode;
        requirement.setSimpleProperty(getImplementationStatusId(requirement), newValue);
        return !newValue.equals(oldValue);
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
     * Checks whether the given link is relevant for the deduction of a
     * requirement's implementation state, i.e. whether it is a link between a
     * requirement and a safeguard with the respective link type.
     *
     */
    public static boolean isRelevantLinkForImplementationStateDeduction(CnALink cnALink) {
        return BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD.equals(cnALink.getRelationId())
                && BpRequirement.TYPE_ID.equals(cnALink.getDependant().getTypeId())
                && Safeguard.TYPE_ID.equals(cnALink.getDependency().getTypeId());
    }

    /**
     * Is the property selected.
     */
    private static boolean isSelected(String value) {
        return "1".equals(value);
    }

}
