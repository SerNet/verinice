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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Implements common verinice graph helper methods based on JGraphT graph
 * implementation.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public abstract  class AbstractVeriniceGraph implements VeriniceGraph {


    private transient Logger log = Logger.getLogger(UndirectedVeriniceGraph.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(UndirectedVeriniceGraph.class);
        }
        return log;
    }


    @Override
    public void addEdge(Edge edge) {
        getGraph().addEdge(edge.getSource(), edge.getTarget(), edge);
    }

    @Override
    public Set<CnATreeElement> getElements() {
        return getGraph().vertexSet();
    }

    @Override
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> Set<T> getElements(Class<T> clazz) {
        Set<T> elements = new HashSet<T>();
        Set<CnATreeElement> allElements = getElements();

        for (CnATreeElement element : allElements) {
            if (element.getClass().equals(clazz)) {

                elements.add((T) element);
            }
        }

        return elements;
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> getChildren(CnATreeElement element, Class<T> type) {
        Set<T> children = new HashSet<>();
        int parentId = element.getParentId();
        Set<CnATreeElement> relatives = getLinkTargets(element, Edge.RELATIVES);
        for (CnATreeElement parentOrChild : relatives) {
            if (parentId != parentOrChild.getDbId()) {
                if (type.equals(parentOrChild.getClass())) {
                    children.add((T) parentOrChild);
                }
            }
        }

        return children;
    }

    @Override
    public Set<CnATreeElement> getChildren(CnATreeElement element) {
        return getChildren(element, "");
    }

    @Override
    public Set<CnATreeElement> getChildren(CnATreeElement element, String elementTypeId) {
        Set<CnATreeElement> children = new HashSet<>();
        int parentId = element.getParentId();
        Set<CnATreeElement> relatives = getLinkTargets(element, Edge.RELATIVES);
        for (CnATreeElement parentOrChild : relatives) {
            if (parentId != parentOrChild.getDbId()) {
                if (elementTypeId == "" || elementTypeId.equals(parentOrChild.getTypeId())) {
                    children.add(parentOrChild);
                }
            }
        }
        return children;
    }

    @Override
    public Set<CnATreeElement> getLinkTargets(String uuid) {
        return getLinkTargets(getElement(uuid), null);
    }

    @Override
    public Set<CnATreeElement> getLinkTargets(Integer dbId) {
        return getLinkTargets(getElement(dbId), null);
    }

    @Override
    public Set<CnATreeElement> getLinkTargets(String uuid, String typeId) {
        return getLinkTargets(getElement(uuid), typeId);
    }

    @Override
    public Set<CnATreeElement> getLinkTargets(Integer dbId, String typeId) {
        return getLinkTargets(getElement(dbId), typeId);
    }

    @Override
    public Set<CnATreeElement> getLinkTargets(CnATreeElement source) {
        return getLinkTargets(source, null);
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
                    getLog().debug("  |-" + edge.getType() + " -> " + target.getTitle());
                }
            }
        }
    }

    @Override
    public Set<CnATreeElement> filter(VeriniceGraphFilter graphFilter) {

        Set<CnATreeElement> elements = new HashSet<>();

        for (CnATreeElement e : getGraph().vertexSet()) {
            if (graphFilter.filter(e)) {
                elements.add(e);
            }
        }

        return elements;
    }
}
