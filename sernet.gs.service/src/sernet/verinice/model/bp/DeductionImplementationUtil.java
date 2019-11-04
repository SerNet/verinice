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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static final String IMPLEMENTATION_DEDUCE = "_implementation_deduce";

    private DeductionImplementationUtil() {
        super();
    }

    /**
     * Set the implementation status to a single requirement by calculating the
     * the value from the safeguards.
     *
     * @return true when the state was changed
     */
    public static boolean setImplementationStatusToRequirement(CnATreeElement requirement) {
        List<CnATreeElement> safeGuards = getSafeguardsFromRequirement(requirement);
        return setImplementationStatusToRequirement(safeGuards, requirement);
    }

    /**
     * Get the connected safeguards from a requirement.
     */
    public static List<CnATreeElement> getSafeguardsFromRequirement(CnATreeElement requirement) {
        return requirement.getLinksDown().stream()
                .filter(DeductionImplementationUtil::isRelevantLinkForImplementationStateDeduction)
                .map(CnALink::getDependency).collect(Collectors.toList());
    }

    /**
     * Set the implementation status of the {@link Safeguard} to the
     * {@link BpRequirement} when the deduction of the implementation status is
     * enabled for this {@link BpRequirement}.
     *
     * @return true if the status has changed
     */
    public static boolean setImplementationStatusToRequirement(CnATreeElement safeguard,
            CnATreeElement requirement) {
        if (!isDeductiveImplementationEnabled(requirement)) {
            return false;
        }
        ImplementationStatus optionValue = getImplementationStatus(safeguard);
        return setImplementationStatusToRequirement(requirement, optionValue);
    }

    /**
     * Set a safeguard implementation status to a requirement.
     *
     * @param optionValue-
     *            the implementation status from a safeguard, must not be null.
     *
     * @return true if the state was changed
     */
    public static boolean setImplementationStatusToRequirement(CnATreeElement requirement,
            ImplementationStatus optionValue) {
        ImplementationStatus propertyValue = getImplementationStatus(requirement);
        if (optionValue == propertyValue) {
            return false;
        }

        setImplementationStatus(requirement, optionValue);
        return true;
    }

    /**
     * Set the implementation status to a single requirement by calculating the
     * the value from the safeguards.
     *
     * @return true when the state was changed
     */
    public static boolean setImplementationStatusToRequirement(List<CnATreeElement> safeGuards,
            CnATreeElement requirement) {
        if (safeGuards == null || requirement == null
                || !isDeductiveImplementationEnabled(requirement)) {
            return false;
        }
        ImplementationStatus implementationStatus;
        if (safeGuards.isEmpty()) {
            implementationStatus = null;
        } else {
            implementationStatus = getComputedImplementationStatus(safeGuards);
        }
        return setImplementationStatus(requirement, implementationStatus);
    }

    /**
     * Return the calculated implementation status from the given safeguards.
     */
    public static ImplementationStatus getComputedImplementationStatus(
            List<CnATreeElement> safeGuards) {
        if (safeGuards == null || safeGuards.isEmpty()) {
            throw new IllegalArgumentException("Safeguard list is null or empty.");
        }

        if (safeGuards.size() == 1) {
            CnATreeElement safeguard = safeGuards.get(0);
            return getImplementationStatus(safeguard);
        }
        Map<ImplementationStatus, Integer> statusCounterMap = new HashMap<>(
                ImplementationStatus.values().length + 1);
        for (CnATreeElement cnATreeElement : safeGuards) {
            ImplementationStatus implementationStatus = getImplementationStatus(cnATreeElement);
            statusCounterMap.compute(implementationStatus,
                    (key, value) -> Optional.ofNullable(value).orElse(0) + 1);
        }
        // all the same
        if (statusCounterMap.size() == 1) {
            CnATreeElement safeguard = safeGuards.get(0);
            return getImplementationStatus(safeguard);
        }
        Integer countNA = statusCounterMap.getOrDefault(ImplementationStatus.NOT_APPLICABLE, 0);
        Integer countYES = statusCounterMap.getOrDefault(ImplementationStatus.YES, 0);
        Integer countNO = statusCounterMap.getOrDefault(ImplementationStatus.NO, 0);
        // only na and yes => yes
        if (countNA != 0 && countYES != 0 && statusCounterMap.size() == 2) {
            return ImplementationStatus.YES;
        }
        // half of not_na is no => no
        if (countNO != 0) {
            int notNA = safeGuards.size() - countNA;
            if (countNO > (notNA / 2.0f)) {
                return ImplementationStatus.NO;
            }
        }
        // every other combination => partially
        return ImplementationStatus.PARTIALLY;
    }

    /**
     * Return the implementation status of the given {@link CnATreeElement}.
     */
    public static ImplementationStatus getImplementationStatus(CnATreeElement element) {
        if (BpRequirement.isBpRequirement(element)) {
            return BpRequirement.getImplementationStatus(element.getEntity()
                    .getRawPropertyValue(BpRequirement.PROP_IMPLEMENTATION_STATUS));
        }
        if (Safeguard.TYPE_ID.equals(element.getTypeId())) {
            return Safeguard.getImplementationStatus(
                    element.getEntity().getRawPropertyValue(Safeguard.PROP_IMPLEMENTATION_STATUS));
        }
        throw new IllegalArgumentException("Unhandled element type " + element.getTypeId());
    }

    /**
     * Set the implementation status in the requirement
     *
     * @return true if the status was changed
     */
    private static boolean setImplementationStatus(CnATreeElement requirement,
            ImplementationStatus status) {
        ImplementationStatus oldValue = getImplementationStatus(requirement);
        if (status == oldValue) {
            return false;
        }
        String rawValue = BpRequirement.toRawValue(status);
        requirement.getEntity().trackChange("system");
        requirement.setSimpleProperty(BpRequirement.PROP_IMPLEMENTATION_STATUS, rawValue);
        return true;
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
