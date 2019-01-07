/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.model.common;

import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Utility class to retrieve domain-specific model information
 */
public final class DomainSpecificElementUtil {

    /**
     * Get the typeId for person type elements in the given {@link Domain}
     */
    public static String getPersonTypeIdFromDomain(Domain domain) {
        switch (domain) {
        case BASE_PROTECTION:
            return BpPerson.TYPE_ID;
        case BASE_PROTECTION_OLD:
            return Person.TYPE_ID;
        case ISM:
            return PersonIso.TYPE_ID;
        default:
            throw new IllegalStateException("Unhandled domain type " + domain);
        }
    }

    private DomainSpecificElementUtil() {

    }
}
