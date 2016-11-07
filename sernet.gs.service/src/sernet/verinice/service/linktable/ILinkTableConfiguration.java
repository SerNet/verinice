/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable;

import java.io.Serializable;
import java.util.Set;

/**
 * This interface provides all information you need to create a Link Table.
 * See {@link LinkTableService} for an introduction to Link Tables.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public interface ILinkTableConfiguration extends Serializable {

    /**
     * @return The VQL column paths for the Link Table
     */
    Set<String> getColumnPaths();

    /**
     * Object types are defined in SNCA.xml.
     * 
     * @return A set with all objects type ids in this link table configuration
     */
    Set<String> getObjectTypeIds();
    
    /**
     * Property types are defined in SNCA.xml.
     * 
     * @return A set with all property type ids in this link table configuration
     */
    Set<String> getPropertyTypeIds();

    /**
     * Link types are defined in SNCA.xml.
     * 
     * @return A set with all link (or relation) ids in this link table configuration
     */
    Set<String> getLinkTypeIds();

    /**
     * @return An array of scope ids for this Link Table
     */
    Integer[] getScopeIdArray();
    
    /**
     * Adds a scope id to this configuration
     * 
     * @param scopeId The database id of an organization or IT network
     */
    void addScopeId(Integer scopeId);
    
    /**
     * Removes all scope ids of this configuration. 
     */
    void removeAllScopeIds();
}
