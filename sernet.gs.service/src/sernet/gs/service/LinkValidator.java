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
package sernet.gs.service;

import java.util.Set;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A utility class to validate links
 */
public final class LinkValidator {

    /**
     * gets all possible relations outgoing from entityType of sourceElement
     * targeting the entityType of destinationElement. If one of those equals
     * linkType, return true else return false (relationtype for source and
     * destination is not defined in SNCA.xml )
     */
    public static boolean isRelationValid(CnATreeElement sourceElement,
            CnATreeElement destinationElement, String relationType) {
        if (CnALink.Id.NO_TYPE.equals(relationType)) { // special dnd itgs case
                                                       // which is allowed
                                                       // always
            return true;
        }
        // special handling for generic links without an explicit link type
        // between elements of old ITBP model
        if (sourceElement instanceof IBSIStrukturElement
                && destinationElement instanceof IBSIStrukturElement) {
            return true;
        }

        Set<HuiRelation> relationsFromSNCA = HUITypeFactory.getInstance().getPossibleRelations(
                sourceElement.getEntityType().getId(), destinationElement.getEntityType().getId());

        return relationsFromSNCA.stream().anyMatch(item -> relationType.equals(item.getId()));
    }

    private LinkValidator() {
    }

}