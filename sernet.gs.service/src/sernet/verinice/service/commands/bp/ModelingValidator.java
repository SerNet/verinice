/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.bp;

import org.apache.log4j.Logger;

import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.common.CnATreeElement;

/**
 * This class provides static methods for validating elements during basic
 * protection modeling.
 *
 * Don't instantiate this class, use public static methods.
 * 
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public final class ModelingValidator {

    private static final Logger LOG = Logger.getLogger(ModelingValidator.class);

    /**
     * Don't instantiate this class, use public static methods.
     */
    private ModelingValidator() {
        // empty
    }

    /**
     * Checks if a requirement is valid when modelling in an IT network. The
     * method checks whether the set proceeding of the requirement matches the
     * proceeding in the IT network.
     * 
     * @param requirement
     *            A CnATreeElement that should be a requirement
     * @param itNetwork
     *            An IT network
     * @return true if the CnATreeElement is valid, false if not
     */
    public static boolean isRequirementValidInItNetwork(CnATreeElement requirement, ItNetwork itNetwork) {
        return BpRequirement.isBpRequirement(requirement)
                && isRequirementValidInItNetworkTypeSafe((BpRequirement) requirement, itNetwork);
    }

    private static boolean isRequirementValidInItNetworkTypeSafe(BpRequirement requirement,
            ItNetwork itNetwork) {
        String proceedingOfItNetwork = itNetwork.getEntity()
                .getRawPropertyValue(ItNetwork.PROP_QUALIFIER);
        String proceedingOfRequirement = requirement.getEntity()
                .getRawPropertyValue(BpRequirement.PROP_QUALIFIER);
        if (proceedingOfItNetwork == null || proceedingOfItNetwork.isEmpty()) {
            return true;
        }
        if (isItNetworkProceedingBasis(proceedingOfItNetwork)) {
            return isRequirementValidForProceedingBasis(proceedingOfRequirement);
        }
        if (isItNetworkProceedingStandard(proceedingOfItNetwork)) {
            return isRequirementValidForProceedingStandard(proceedingOfRequirement);
        }
        if (isItNetworkProceedingHigh(proceedingOfItNetwork)) {
            return true;
        }
        // Proceeding is unknown, accept the requirement anyway
        if (LOG.isInfoEnabled()) {
            LOG.info("It network " + itNetwork.getTitle()
                    + " has an unknown proceeding of securing: " + proceedingOfItNetwork);
        }
        return true;
    }

    private static boolean isRequirementValidForProceedingBasis(String proceedingOfRequirement) {
        return !BpRequirement.PROP_QUALIFIER_STANDARD.equals(proceedingOfRequirement)
                && !BpRequirement.PROP_QUALIFIER_HIGH.equals(proceedingOfRequirement);
    }

    private static boolean isRequirementValidForProceedingStandard(String proceedingOfRequirement) {
        return !BpRequirement.PROP_QUALIFIER_HIGH.equals(proceedingOfRequirement);
    }

    private static boolean isItNetworkProceedingBasis(String proceedingOfItNetwork) {
        return ItNetwork.PROP_QUALIFIER_BASIC.equals(proceedingOfItNetwork);
    }

    private static boolean isItNetworkProceedingStandard(String proceedingOfItNetwork) {
        return ItNetwork.PROP_QUALIFIER_STANDARD.equals(proceedingOfItNetwork);
    }

    private static boolean isItNetworkProceedingHigh(String proceedingOfItNetwork) {
        return ItNetwork.PROP_QUALIFIER_HIGH.equals(proceedingOfItNetwork);
    }

}
