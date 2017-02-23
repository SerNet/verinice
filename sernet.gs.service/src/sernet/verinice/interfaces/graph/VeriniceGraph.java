/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.interfaces.graph;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.Pseudograph;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Result and data-transfer object of {@link GraphService}.
 * 
 * This class provides helper methods to get verinice links and parent child
 * relations from the graph.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class VeriniceGraph implements Serializable {

    private static final long serialVersionUID = 4518569408986129815L;

    private transient Logger log = Logger.getLogger(VeriniceGraph.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(VeriniceGraph.class);
        }
        return log;
    }

    private Pseudograph<CnATreeElement, Edge> graph;

    public VeriniceGraph() {
        super();
        graph = new Pseudograph<CnATreeElement, Edge>(Edge.class);
    }

    public void addVertex(CnATreeElement element) {
        getGraph().addVertex(element);
    }

    public void addEdge(Edge edge) {
        getGraph().addEdge(edge.getSource(), edge.getTarget(), edge);
    }

    /**
     * Returns all elements
     * 
     * @return All elements
     */
    public Set<CnATreeElement> getElements() {
        return getGraph().vertexSet();
    }

    /**
     * Returns all elements of type "typeId".
     * 
     * @param typeId
     *            Type of returned elements
     * @return All elements of type "typeId".
     */
    public Set<CnATreeElement> getElements(String typeId) {
        Set<CnATreeElement> elements = new HashSet<CnATreeElement>();
        Set<CnATreeElement> allElements = getElements();

        if (typeId == null || allElements == null) {
            return elements;
        }

        for (CnATreeElement element : allElements) {
            if (typeId.equals(element.getTypeId())) {
                elements.add(element);
            }
        }
        return elements;
    }

    public CnATreeElement getElement(String uuid) {
        CnATreeElement result = null;
        Set<CnATreeElement> allElements = getElements();
        if (uuid == null || allElements == null) {
            return result;
        }

        for (CnATreeElement element : allElements) {
            if (uuid.equals(element.getUuid())) {
                result = element;
                break;
            }
        }

        return result;
    }

    public CnATreeElement getElement(Integer dbId) {
        CnATreeElement result = null;
        Set<CnATreeElement> allElements = getElements();
        if (dbId == null || allElements == null) {
            return result;
        }

        for (CnATreeElement element : allElements) {
            if (dbId.equals(element.getDbId())) {
                result = element;
                break;
            }
        }

        return result;
    }

    /**
     * Returns the parent of an element. If parent is not found in grapg, null
     * is returned.
     * 
     * @param element
     *            A verinice element
     * @return The parent of the element or null if parent is not found
     */
    public CnATreeElement getParent(CnATreeElement element) {
        CnATreeElement parent = null;
        int parentId = element.getParentId();
        Set<CnATreeElement> relatives = getLinkTargets(element, Edge.RELATIVES);
        for (CnATreeElement parentOrChild : relatives) {
            if (parentId == parentOrChild.getDbId()) {
                parent = parentOrChild;
                break;
            }
        }
        return parent;
    }

    /**
     * Returns all children of an element. If no children are found an empty Set
     * is returned.
     * 
     * @param element
     *            A verinice element
     * @return The children of the element
     */
    public Set<CnATreeElement> getChildren(CnATreeElement element) {
        return getChildren(element, null);
    }

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
    public Set<CnATreeElement> getChildren(CnATreeElement element, String elementTypeId) {
        Set<CnATreeElement> children = new HashSet<>();
        int parentId = element.getParentId();
        Set<CnATreeElement> relatives = getLinkTargets(element, Edge.RELATIVES);
        for (CnATreeElement parentOrChild : relatives) {
            if (parentId != parentOrChild.getDbId()) {
                if (elementTypeId == null || elementTypeId.equals(parentOrChild.getTypeId())) {
                    children.add(parentOrChild);
                }
            }
        }
        return children;
    }

    /**
     * Returns all link targets of an source element (defined by its UUID)
     * including parent and children. If there are no link targets, an empty Set
     * is returned.
     * 
     * @param uuid
     *            UUID of an source element
     * @return A set of target elements
     */
    public Set<CnATreeElement> getLinkTargets(String uuid) {
        return getLinkTargets(getElement(uuid), null);
    }

    /**
     * Returns all link targets of an source element (defined by its database
     * id) including parent and children. If there are no link targets, an empty
     * Set is returned.
     * 
     * @param dbId
     *            Database id of an source element
     * @return A set of target elements
     */
    public Set<CnATreeElement> getLinkTargets(Integer dbId) {
        return getLinkTargets(getElement(dbId), null);
    }

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
    public Set<CnATreeElement> getLinkTargets(String uuid, String typeId) {
        return getLinkTargets(getElement(uuid), typeId);
    }

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
    public Set<CnATreeElement> getLinkTargets(Integer dbId, String typeId) {
        return getLinkTargets(getElement(dbId), typeId);
    }

    /**
     * Returns all link targets of an source element including parent and
     * children. If there are no link targets, an empty Set is returned.
     * 
     * @param source
     *            Source element
     * @return A set of target elements
     */
    public Set<CnATreeElement> getLinkTargets(CnATreeElement source) {
        return getLinkTargets(source, null);
    }

    /**
     * Returns link targets of an source element. Returned links are of type
     * "linkTypeId". If there are no link targets of this type, an empty list is
     * returned.
     * 
     * @param source
     *            Source element
     * @param linkTypeId
     *            Type of returned links
     * @return A set of target elements
     */
    public Set<CnATreeElement> getLinkTargets(CnATreeElement source, String linkTypeId) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Returning link targets of element: " + source.getTitle() + ", link type is: " + linkTypeId + "...");
        }
        Set<Edge> edgeList = getGraph().edgesOf(source);
        Set<CnATreeElement> linkTargets = new HashSet<>();
        for (Edge edge : edgeList) {
            if (linkTypeId == null || linkTypeId.equals(edge.getType())) {
                CnATreeElement edgeSource = edge.getSource();
                CnATreeElement edgeTarget = edge.getTarget();
                CnATreeElement target = edgeSource.equals(source) ? edgeTarget : edgeSource;
                linkTargets.add(target);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Link found, source: " + source.getTitle() + ", target: " + target.getTitle() + ", link type: " + linkTypeId);
                }
            }
        }
        return linkTargets;
    }

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
    public Set<Edge> getEdgesByElementType(CnATreeElement source, String elementTypeId) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Returning edges of element: " + source.getTitle() + ", target type is: " + elementTypeId + "...");
        }
        Set<Edge> allEdgeSet = getGraph().edgesOf(source);
        Set<Edge> edgeSet = new HashSet<>();
        for (Edge edge : allEdgeSet) {
            CnATreeElement edgeSource = edge.getSource();
            CnATreeElement edgeTarget = edge.getTarget();
            CnATreeElement target = edgeSource.equals(source) ? edgeTarget : edgeSource;
            if (elementTypeId == null || elementTypeId.equals(target.getTypeId())) {
                edgeSet.add(edge);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Edge found, source: " + source.getTitle() + ", target: " + target.getTitle() + ", edge type: " + edge.getType());
                }
            }
        }
        return edgeSet;
    }

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
    public Set<CnATreeElement> getLinkTargetsByElementType(CnATreeElement source, String elementTypeId) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Returning link targets of element: " + source.getTitle() + ", target type is: " + elementTypeId + "...");
        }
        Set<Edge> edgeList = getGraph().edgesOf(source);
        Set<CnATreeElement> linkTargets = new HashSet<>();
        for (Edge edge : edgeList) {
            CnATreeElement edgeSource = edge.getSource();
            CnATreeElement edgeTarget = edge.getTarget();
            CnATreeElement target = edgeSource.equals(source) ? edgeTarget : edgeSource;
            if (elementTypeId == null || elementTypeId.equals(target.getTypeId())) {
                linkTargets.add(target);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Link found, source: " + source.getTitle() + ", target: " + target.getTitle() + ", target type: " + elementTypeId);
                }
            }
        }
        return linkTargets;
    }

    public void log() {
        if (getLog().isInfoEnabled()) {
            logStatistics();
        }
        if (getLog().isDebugEnabled()) {
            logVertices();
        }
    }

    private void logStatistics() {
        if (getGraph() != null) {
            getLog().info("Number vertices: " + getGraph().vertexSet().size());
            getLog().info("Number edges: " + getGraph().edgeSet().size());
        }

    }

    private void logVertices() {
        if (getGraph() != null) {
            for (CnATreeElement element : getGraph().vertexSet()) {
                getLog().debug(element.getTitle());
                Set<Edge> edges = getGraph().edgesOf(element);
                for (Edge edge : edges) {
                    CnATreeElement target = edge.getTarget();
                    if (target.equals(element)) {
                        target = edge.getSource();
                    }
                    getLog().debug("  |-" + edge.getType() + "-> " + target.getTitle());
                }
            }
        }
    }

    public Graph<CnATreeElement, Edge> getGraph() {
        return graph;
    }

    /**
     * Returns a subset of all vertex in {@link VeriniceGraph}.
     */
    public Set<CnATreeElement> filter(VeriniceGraphFilter graphFilter) {

        Set<CnATreeElement> elements = new HashSet<>();

        for (CnATreeElement e : graph.vertexSet()) {
            if (graphFilter.filter(e)) {
                elements.add(e);
            }
        }

        return elements;
    }

}
