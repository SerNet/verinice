/*******************************************************************************
 * Copyright (c) 2017 Benjamin Weißenfels.
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
 *     @author Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.graph;

import java.util.Set;

import org.jgrapht.Graph;

import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface VeriniceGraph {

    void addVertex(CnATreeElement element);

    void addEdge(Edge edge);

    /**
     * Returns all elements
     * 
     * @return All elements
     */
    Set<CnATreeElement> getElements();

    /**
     * Returns all elements of type "typeId".
     * 
     * @param typeId
     *            Type of returned elements
     * @return All elements of type "typeId".
     */
    Set<CnATreeElement> getElements(String typeId);

    /**
     * Returns all elements of type "typeId".
     *
     * @param typeId
     *            Type of returned elements
     * @return All elements of type "typeId".
     */
    <T> Set<T> getElements(Class<T> clazz);

    CnATreeElement getElement(String uuid);

    CnATreeElement getElement(Integer dbId);

    /**
     * Returns the parent of an element. If parent is not found in grapg, null
     * is returned.
     * 
     * @param element
     *            A verinice element
     * @return The parent of the element or null if parent is not found
     */
    CnATreeElement getParent(CnATreeElement element);

    /**
     * Returns all children of an element. If no children are found an empty Set
     * is returned.
     * 
     * @param element
     *            A verinice element
     * @param
     *
     * @return The children of the element
     */
    <T> Set<T> getChildren(CnATreeElement element, Class<T> type);

    /**
     * Returns all children of an element. If no children are found an empty Set
     * is returned.
     *
     * @param element
     *            A verinice element
     * @return The children of the element
     */
    Set<CnATreeElement> getChildren(CnATreeElement element);

    /**
     * Returns the children of an element. Returned children are of type
     * "elementTypeId". If no children are found an empty Set is returned.
     * 
     * @param element
     *            A verinice element
     * @param elementTypeId
     *            Type of returned children
     * @return The children of the element
     */
    Set<CnATreeElement> getChildren(CnATreeElement element, String elementTypeId);

    /**
     * Returns all link targets of an source element (defined by its UUID)
     * including parent and children. If there are no link targets, an empty Set
     * is returned.
     * 
     * @param uuid
     *            UUID of an source element
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargets(String uuid);

    /**
     * Returns all link targets of an source element (defined by its database
     * id) including parent and children. If there are no link targets, an empty
     * Set is returned.
     * 
     * @param dbId
     *            Database id of an source element
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargets(Integer dbId);

    /**
     * Returns all link targets of an source element (defined by its UUID)
     * including parent and children. Returned links are of type "typeId". If
     * there are no link targets, an empty Set is returned.
     * 
     * @param uuid
     *            UUID of an source element
     * @param typeId
     *            Type of returned links
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargets(String uuid, String typeId);

    /**
     * Returns all link targets of an source element (defined by its database
     * id) including parent and children. Returned links are of type "typeId".
     * If there are no link targets, an empty Set is returned.
     * 
     * @param dbId
     *            Database id of an source element
     * @param typeId
     *            Type of returned links
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargets(Integer dbId, String typeId);

    /**
     * Returns all link targets of an source element including parent and
     * children. If there are no link targets, an empty Set is returned.
     * 
     * @param source
     *            Source element
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargets(CnATreeElement source);

    /**
     * Returns link targets of an source element. Returned links are of type
     * "linkTypeId". If there are no link targets of this type, an empty list isX
     * returned.
     * 
     * @param source
     *            Source element
     * @param linkTypeId
     *            Type of returned links
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargets(CnATreeElement source, String linkTypeId);

    /**
     * Returns edges of an source element. Target types of edges are
     * "elementTypeId". If there are no edges with targets of this type, an
     * empty set is returned.
     * 
     * @param source
     *            Source element
     * @param elementTypeId
     *            Type of edge targets
     * @return A set of edges
     */
    Set<Edge> getEdgesByElementType(CnATreeElement source, String elementTypeId);

    /**
     * Returns link targets of an source element. Returned targets are of type
     * "elementTypeId". If there are no link targets of this type, an empty list
     * is returned.
     * 
     * @param source
     *            Source element
     * @param elementTypeId
     *            Type of returned targets
     * @return A set of target elements
     */
    Set<CnATreeElement> getLinkTargetsByElementType(CnATreeElement source, String elementTypeId);

    Graph<CnATreeElement, Edge> getGraph();

    /**
     * Returns a subset of all vertex in {@link UndirectedVeriniceGraph}.
     */
    Set<CnATreeElement> filter(VeriniceGraphFilter graphFilter);

    /**
     * Prints statistics about the graph to stdout.
     */
    void log();

}