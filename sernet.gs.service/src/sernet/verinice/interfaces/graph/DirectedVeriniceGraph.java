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

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Implements the verinice data model with an underlying directed graph.
 *
 * It amkes
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class DirectedVeriniceGraph extends AbstractVeriniceGraph {

    DirectedGraph<CnATreeElement, Edge>  directedGraph;

    public DirectedVeriniceGraph() {
        directedGraph = new DirectedMultigraph<>(Edge.class);
    }

    @Override
    public void addVertex(CnATreeElement element) {
        directedGraph.addVertex(element);
    }

    @Override
    public Graph<CnATreeElement, Edge> getGraph() {
        return directedGraph;
    }

    @Override
    public Set<CnATreeElement> getLinkTargets(CnATreeElement source, String linkTypeId) {

        if (getLog().isDebugEnabled()) {
            getLog().debug("Returning link targets of element: " + source.getTitle() + ", link type is: " + linkTypeId + "...");
        }

        Set<Edge> edgeList = directedGraph.outgoingEdgesOf(source);
        Set<CnATreeElement> linkTargets = new HashSet<>();
        for (Edge edge : edgeList) {
            if (linkTypeId == null || linkTypeId.equals(edge.getType())) {
                linkTargets.add(edge.getTarget());
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Link found, source: " + source.getTitle() + ", target: " + edge.getTarget().getTitle() + ", link type: " + linkTypeId);
                }
            }
        }
        return linkTargets;
    }

    @Override
    public Set<CnATreeElement> getLinkTargetsByElementType(CnATreeElement source, String elementTypeId) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Returning link targets of element: " + source.getTitle() + ", target type is: " + elementTypeId + "...");
        }
        Set<Edge> edgeList = getGraph().edgesOf(source);
        Set<CnATreeElement> linkTargets = new HashSet<>();
        for (Edge edge : edgeList) {
            if (elementTypeId == null || elementTypeId.equals(edge.getTarget().getTypeId())) {
                linkTargets.add(edge.getTarget());
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Link found, source: " + source.getTitle() + ", target: " + edge.getTarget().getTitle() + ", target type: " + elementTypeId);
                }
            }
        }
        return linkTargets;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.graph.VeriniceGraphInterface#getEdgesByElementType(sernet.verinice.model.common.CnATreeElement, java.lang.String)
     */
    @Override
    public Set<Edge> getEdgesByElementType(CnATreeElement source, String elementTypeId) {
        if (getLog().isDebugEnabled()) {
            getLog().debug("Returning edges of element: " + source.getTitle() + ", target type is: " + elementTypeId + "...");
        }
        Set<Edge> allEdgeSet = directedGraph.outgoingEdgesOf(source);
        Set<Edge> edgeSet = new HashSet<>();
        for (Edge edge : allEdgeSet) {
            if (elementTypeId == null || elementTypeId.equals(edge.getTarget().getTypeId())) {
                edgeSet.add(edge);
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Edge found, source: " + source.getTitle() + ", target: " + edge.getTarget().getTitle() + ", edge type: " + edge.getType());
                }
            }
        }
        return edgeSet;
    }
}
