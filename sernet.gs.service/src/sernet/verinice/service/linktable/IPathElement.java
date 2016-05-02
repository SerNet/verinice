/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.linktable;

import java.util.Map;

import sernet.verinice.interfaces.graph.VeriniceGraph;

/**
 * A IPathElement is an element in a ColumnPath.
 * A ColumnPath is a description of a report column in GenericDataModel.
 * See GenericDataModel for a description of column path definitions.
 *
 * @see GenericDataModel
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IPathElement<E,C> {

    public static final char DELIMITER_LINK = '/';
    public static final char DELIMITER_LINK_TYPE = ':';
    public static final char DELIMITER_CHILD = '>';
    public static final char DELIMITER_PARENT = '<';
    public static final char DELIMITER_PROPERTY = '.';

    public static final String RESULT_KEY_SEPERATOR = ".";
    
    public enum Direction { INCOMING, OUTGOING };

    /**
     * Loads the data of this path element from the verinice graph
     * for a given parent element.
     *
     * @param parent The parent element
     * @param graph The verinice graph with all relevant elements
     */
    void load(E parent, VeriniceGraph graph);

    /**
     * Creates the result map for this path element,
     * adds it to the given map and returns it.
     *
     * @param map A map with the results of the ancestors
     * @param dbIds Db ids aof all elements in the path linked with dots
     * @return The result map
     */
    Map<String, String> createResultMap(Map<String, String> map, String dbIds);

    /**
     * Returns the all results for this path in a map.
     * Key of the map ist the db-id of the parent element.
     * Value of the map is an inner map.
     *
     * Key of the inner map is the db-id of an element, value
     * of the inner map are the results for the db-id.
     *
     * @return All results for this path
     */
    Map<String, Map<String, Object>> getResult();

    /**
     * @return The type-id of this path element (an element or relation type)
     */
    String getTypeId();

    /**
     * Sets the type-id of this path element.
     * The type-id is an element or relation type.
     *
     * @param typeId The type-id of this path element
     */
    void setTypeId(String typeId);

    /**
     * @return The next element in the path
     */
    IPathElement<C,?> getChild();

    /**
     * Sets the next element in the path
     *
     * @param nextElement The next path element
     */
    void setChild(IPathElement<C,?> nextElement);

    /**
     * Return the alias (or name) of an element. The alias is an optional field
     * which can be null.
     *
     * @return The alias (or name) of an element
     */
    String getAlias();

    /**
     * Sets the alias (or name) of an element. The alias is an optional field
     * which can be null.
     *
     * @param alias The alias (or name) of an element
     */
    void setAlias(String alias);
    
    Direction getDirection();
    
    void setDirection(Direction direction);
   

}
