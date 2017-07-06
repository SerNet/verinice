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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.model;

import java.util.Map;

/**
 * This service provides methods to get the number of elements in the database.
 *
 * In this service the term element stands for CnATreeElement.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public interface ICountElementService {

    /**
     * @return The total number of elements in the database
     */
    public long getNumber();

    /**
     * Returns the number of elements of all types in the database. The result
     * is returned in a map: the key is the type ID the value is the number of
     * elements for the type ID.
     *
     * @return A map: the key is the type ID the value is the number of elements
     *         for the type ID
     */
    public Map<String, Long> getNumberOfAllTypes();

    /**
     * Return the number of elements of a given type ID.
     *
     * @param typeID
     *            A type ID of elements
     * @return The number of elements of a given type ID
     */
    public long getNumber(String typeID);

    /**
     * Returns a limit on how many elements can be loaded at once. This method
     * returns a default value for all type. A return values -1 disables the
     * limit. Method getLimit(String typeID) returns a limit for a given type ID
     * which overrides the default vales for this type ID.
     *
     * @return A limit on how many elements can be loaded at once
     */
    public Integer getLimit();

    /**
     * Returns a limit on how many elements of a given type ID can be loaded at
     * once. A return values -1 disables the limit. This method overrides the
     * default value returned by method getLimit() for this type ID.
     *
     * If no limit is found for the given type ID the default value is returned
     * (method getLimit()).
     *
     * @return A limit on how many elements of a given type ID can be loaded at
     *         once
     */
    public Integer getLimit(String typeID);
}
