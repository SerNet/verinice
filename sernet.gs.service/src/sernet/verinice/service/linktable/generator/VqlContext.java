/*******************************************************************************
 * Copyright (c) 2016 Benjamin Weißenfels.
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
package sernet.verinice.service.linktable.generator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.event.TraversalListener;

import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.TraversalFilter;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.generator.mergepath.VqlAst;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge.EdgeType;
import sernet.verinice.service.linktable.generator.mergepath.VqlNode;

/**
 * Provides some methods for navigate through a {@link VqlAst} data structure.
 *
 * From a given input given by the {@link TraversalFilter} or
 * {@link TraversalListener} the context can make e decision if there are valid
 * nodes/edges to traverse left within a {@link VeriniceGraph}.
 *
 * The context has also a pointer which stores the current position within a
 * {@link VqlAst}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
final class VqlContext {

    VqlAst vqlAst;

    VqlNode currentNode;

    Deque<VqlNode> history;

    private static final Logger LOG = Logger.getLogger(VqlContext.class);

    VqlContext(VqlAst vqlAst) {
        this.vqlAst = vqlAst;
        this.currentNode = vqlAst.getRoot();
        this.history = new ArrayDeque<>();
    }

    void setCurrentNode(VqlNode nextNode) {
        history.push(currentNode);
        currentNode = nextNode;
    }

    /**
     * Checks if this edge is also defined in the query tree.
     */
    boolean isValideEdge(CnATreeElement source, Edge edge, CnATreeElement target) {
        return getNextNode(source, edge, target) != null;
    }

    /**
     * Returns a valid next {@link VqlNode} if the source, edget and target can
     * be mapped to the {@link VqlAst} datastructure.
     *
     * @return is null if no valid next node is found.
     */
    VqlNode getNextNode(CnATreeElement source, Edge edge, CnATreeElement target) {

        Set<VqlEdge> outgoingEdges = vqlAst.getOutgoingEdges(currentNode);

        for (VqlEdge vqlEdge : outgoingEdges) {

            VqlNode edgeTarget = vqlAst.getVqlGraph().getEdgeTarget(vqlEdge);

            // no more nodes available
            if (edgeTarget == null) {
                return null;
            }

            boolean isMappedTargetTyp = vqlNodeTypeIdEqualCnaTreeElementTypeId(target, edgeTarget);

            if (isRelative(edge)) {
                if (isChildRelation(source, edge, vqlEdge, isMappedTargetTyp) || isParentRelation(source, edge, vqlEdge, isMappedTargetTyp)) {
                    return edgeTarget;
                }
            } else if (isMappedTargetTyp) {
                return edgeTarget;
            }
        }

        return null;
    }

    /**
     * Determines if the next possible relation is a parent relation
     * (<code>></code>).
     *
     * Therefore there must exist a child relation within the {@link VqlAst} and
     * child relation within the {@link VeriniceGraph}. This is kind of
     * problematic because there is no difference between a child and parent
     * relation modeled in {@link VeriniceGraph}. So we have to detect them by
     * our own. Because we know the {@link CnATreeElement} we came from and the
     * {@link Edge} we are traversing we can determine if this is meant to be a
     * child or parent relation.
     *
     * @param source
     *            The node the traversal algorithm came frome.
     * @param edge
     *            The edge we are traversing.
     * @param vqlEdge
     *            The edge from the {@link VqlAst}.
     * @param isMappedTargetTyp
     *            if the target type of {@link VqlNode} and
     *            {@link CnATreeElement} matched.
     * @return True if this the {@link Edge} and the type id of the target
     *         {@link CnATreeElement} have an corresponding {@link VqlEdge} in
     *         {@link VqlAst}.
     */
    private boolean isParentRelation(CnATreeElement source, Edge edge, VqlEdge vqlEdge, boolean isMappedTargetTyp) {
        return EdgeType.PARENT == vqlEdge.getEdgeType() && edge.getTarget() == source && isMappedTargetTyp;
    }

    /**
     * Determines if the next possible relation is a child relation
     * (<code>></code>).
     *
     * Therefore there must exist a child relation within the {@link VqlAst} and
     * child relation within the {@link VeriniceGraph}. This is kind of
     * problematic because there is no difference between a child and parent
     * relation modeled in {@link VeriniceGraph}. So we have to detect them by
     * our own. Because we know the {@link CnATreeElement} we came from and the
     * {@link Edge} we are traversing we can determine if this is meant to be a
     * child or parent relation.
     *
     * @param source
     *            The node the traversal algorithm came frome.
     * @param edge
     *            The edge we are traversing.
     * @param vqlEdge
     *            The edge from the {@link VqlAst}.
     * @param isMappedTargetTyp
     *            if the target type of {@link VqlNode} and
     *            {@link CnATreeElement} matched.
     * @return True if this the {@link Edge} and the type id of the target
     *         {@link CnATreeElement} have an corresponding {@link VqlEdge} in
     *         {@link VqlAst}.
     */
    private boolean isChildRelation(CnATreeElement source, Edge edge, VqlEdge vqlEdge, boolean isMappedTargetTyp) {
        return EdgeType.CHILD == vqlEdge.getEdgeType() && edge.getSource() == source && isMappedTargetTyp;
    }

    /**
     * Relative edges are parsed with <code>></code> and <code><</code>
     * operator.
     */
    private boolean isRelative(Edge edge) {
        return Edge.RELATIVES.equals(edge.getType());
    }

    private boolean vqlNodeTypeIdEqualCnaTreeElementTypeId(CnATreeElement target, VqlNode edgeTarget) {
        return edgeTarget.getTypeId().equals(target.getTypeId());
    }

    VqlNode getCurrentNode() {
        return currentNode;
    }

    void stepBack() {

        if (history.isEmpty()) {
            LOG.debug("history is empty");
            return;
        }

        this.currentNode = history.pop();
    }

    VqlEdge getCurrentEdge() {

        Set<VqlEdge> incomingEdgesOf = vqlAst.getVqlGraph().incomingEdgesOf(currentNode);

        boolean atMostOneIncomingEdge = hasOnlyOneParentatMost(incomingEdgesOf);
        assert atMostOneIncomingEdge;

        if (incomingEdgesOf.isEmpty()) {
            return null;
        }

        return incomingEdgesOf.iterator().next();
    }

    private boolean hasOnlyOneParentatMost(Set<VqlEdge> incomingEdgesOf) {
        return incomingEdgesOf != null && incomingEdgesOf.size() < 2;
    }
}
