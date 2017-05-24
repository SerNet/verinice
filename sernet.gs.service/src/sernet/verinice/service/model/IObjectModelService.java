/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Interface for a service to get information about related elements.
 *
 * @see HUIObjectModelLoader
 * @see HUIObjectModelService
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public interface IObjectModelService extends Serializable {


    void init();

    /**
     * Returns a set of relation IDs possible between fromEntityTypeID and
     * toEntityTypeID.
     */
    Set<String> getRelations(String fromEntityTypeID, String toEntityTypeID);

    /**
     * Returns the typeIDs of the elements which can be linked to typeID.
     */
    Set<String> getPossibleRelationPartners(String typeID);

    /**
     * Returns all possible typeIDs.
     */
    Set<String> getAllTypeIDs();

    /**
     * Returns a set of property-IDs beloning to the entity of the typeID.
     */
    Set<String> getPossibleProperties(String typeID);

    /**
     * Returns a localized label of the id.
     * 
     * @see #getRelationLabel(String)
     */
    String getLabel(String id);

    /**
     * 
     * Returns a localized label of the id.
     * 
     * @see #getLabel(String)
     */
    String getRelationLabel(String id);

    /**
     * Returns a set of typeIDs which can be child to the given typeID.
     */
    Set<String> getPossibleChildren(String typeID);

    /**
     * Returns a set of typeIDs which the given typeID can be child of.
     */
    Set<String> getPossibleParents(String typeID);

    /**
     * Returns a predefined map with all typeIDs and the typeIDs of the possible
     * children.
     */
    Map<String, Set<String>> getAllPossibleChildren();

    /**
     * Returns a predefined map with all typeIDs and the typeIDs of the possible
     * parents.
     */
    Map<String, Set<String>> getAllPossibleParents();

    /**
     * Returns a container for all possible content used in this interface for
     * performance reasons.
     */
    ObjectModelContainer loadAll();

    
    boolean isValidTypeId(String typeID);

    boolean isValidRelationId(String relationID);
}
