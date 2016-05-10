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
 * A IPathElement represents on element in a column path.
 * A column path is a description of a column in a link table.
 *
 * @see LinkTableDataModel
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @param <P> The type of the parent object represented by this element in the path
 * @param <C> The type of the child object represented by this element in the path
 */
public interface IPathElement<P,C> {

    public static final char DELIMITER_LINK = '/';
    public static final char DELIMITER_LINK_TYPE = ':';
    public static final char DELIMITER_CHILD = '>';
    public static final char DELIMITER_PARENT = '<';
    public static final char DELIMITER_PROPERTY = '.';

    public static final String RESULT_KEY_SEPERATOR = ".";
    
    public enum Direction { INCOMING, OUTGOING };

    /**
     * Loads the result of this path element from a verinice graph
     * and a given parent element.
     *
     * @param parent The object represented by this element
     * @param graph The verinice graph with all relevant elements
     */
    void load(P parent, VeriniceGraph graph);

    /**
     * Add a single result to the map with all results
     *
     * @param map A map with all results
     * @param key The key of the result
     * @return A map with all results including the single result
     */
    Map<String, String> addResultToMap(Map<String, String> map, String key);

    /**
     * Returns all results for this path in a map.
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
    
    /**
     * @return The direction of the path element (relevant for links)
     */
    Direction getDirection();
    
    /**
     * Sets the direction of the path element (relevant for links)
     * 
     * @param direction
     */
    void setDirection(Direction direction);
   

}
