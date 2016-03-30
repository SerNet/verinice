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

import java.util.Set;

/**
 * This interface provides all information you need to create a Link Table.
 * See {@link LinkTableService} for an introduction to Link Tables.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public interface ILinkTableConfiguration {

    /**
     * @return The VQL column paths for the Link Table
     */
    Set<String> getColumnPathes();

    /**
     * @return An array of path elements with one element for each VQL column path
     */
    IPathElement[] getPathElements();

    /**
     * @return A set of objects type ids for the Link Table. See SNCA.xml for all object types.
     */
    Set<String> getObjectTypeIds();

    /**
     * @return A set of relation ids for the Link Table. See SNCA.xml for all relation ids.
     */
    Set<String> getLinkTypeIds();

    /**
     * @return An array of scope ids for this Link Table
     */
    Integer[] getScopeIdArray();
}
