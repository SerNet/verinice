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

import sernet.verinice.model.bp.Proceeding;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.Safeguard;
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
    public static boolean isRequirementValid(CnATreeElement requirement,
            Proceeding proceeding) {
        return BpRequirement.isBpRequirement(requirement)
                && isRequirementValidTypeSafe((BpRequirement) requirement, proceeding);
    }

    /**
     * Checks if a safeguard is valid when modelling in an IT network. The
     * method checks whether the set proceeding of the safeguard matches the
     * proceeding in the IT network.
     *
     * @param safeguard
     *            A CnATreeElement that should be a safeguard
     * @param itNetwork
     *            An IT network
     * @return true if the CnATreeElement is valid, false if not
     */
    public static boolean isSafeguardValid(CnATreeElement safeguard,
            Proceeding proceeding) {
        return Safeguard.isSafeguard(safeguard)
                && isSafeguardValidTypeSafe((Safeguard) safeguard, proceeding);
    }

    private static boolean isSafeguardValidTypeSafe(Safeguard requirement,
            Proceeding proceeding) {
        String proceedingOfSafeguard = requirement.getEntity()
                .getRawPropertyValue(Safeguard.PROP_QUALIFIER);
        if (proceeding == null) {
            return true;
        }

        switch (proceeding) {
        case BASIC:
            return Safeguard.PROP_QUALIFIER_BASIC.equals(proceedingOfSafeguard);
        case STANDARD:
            return true;
        case CORE:
            return true;
        default: {
            // Proceeding is unknown, accept the requirement anyway
            LOG.info("Unknown proceeding of securing: " + proceeding);
            return true;
        }
        }
    }

    private static boolean isRequirementValidTypeSafe(BpRequirement requirement,
            Proceeding proceeding) {
        String proceedingOfRequirement = requirement.getEntity()
                .getRawPropertyValue(BpRequirement.PROP_QUALIFIER);
        if (proceeding == null) {
            return true;
        }

        switch (proceeding) {
        case BASIC:
            return BpRequirement.PROP_QUALIFIER_BASIC.equals(proceedingOfRequirement);
        case STANDARD:
            return true;
        case CORE:
            return true;
        default: {
            // Proceeding is unknown, accept the requirement anyway
            LOG.info("Unknown proceeding of securing: " + proceeding);
            return true;
            }
        }
    }
}
