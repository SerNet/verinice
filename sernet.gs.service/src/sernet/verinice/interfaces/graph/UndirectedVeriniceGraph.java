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
public class UndirectedVeriniceGraph extends AbstractVeriniceGraph implements Serializable, VeriniceGraph {

    private static final long serialVersionUID = 4518569408986129815L;

    private Pseudograph<CnATreeElement, Edge> graph;

    public UndirectedVeriniceGraph() {
        graph = new Pseudograph<CnATreeElement, Edge>(Edge.class);
    }

    @Override
    public Graph<CnATreeElement, Edge> getGraph() {
        return graph;
    }

    @Override
    public void addVertex(CnATreeElement element) {
        getGraph().addVertex(element);
    }



    @Override
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

    @Override
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

    @Override
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

}
