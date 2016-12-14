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
package sernet.verinice.interfaces.graph;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Filters subtree of {@link VeriniceGraph}.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface TraversalFilter {

    /**
     * Filter edges within a {@link DepthFirstConditionalSearchPathes}
     * traversal.
     *
     * If this method returns false the edge is not followed, which means that
     * {@link TraversalListener#edgeTraversed(CnATreeElement, CnATreeElement, Edge, int)}
     * is never called for this edge.
     *
     * @param edge
     *            The edge which connects source and target.
     * @param source
     *            The source node. Actually this is not the source node of the
     *            edge parameter. It is the source where the traversal is
     *            started from, means that:
     *
     *            <pre>
     *             {@link Edge#getTarget()} == source
     *            </pre>
     *
     *            and the other way around is possible.
     * @param target
     *            The source node. Actually this is not the source node of the
     *            edge parameter. It is the source where the traversal is
     *            started from, means that the {@link Edge#getTarget()} ==
     *            source is possible.
     * @param depth
     *            This is the depth of the traversal. Since the traversal starts
     *            from a specific root node this is also the distances to this
     *            root node.
     * @return If returns true this edge is traversed.
     */
    boolean edgeFilter(Edge edge, CnATreeElement source, CnATreeElement target, int depth);

    /**
     * If this method returns false the node is not traversed, which means that
     * {@link TraversalListener#nodeTraversed(CnATreeElement, int)} is never
     * called for this node.
     *
     * @param target
     *            The currently traversed node.
     * @param incomingEdge
     *            The edge from which the traversed node is reached.
     * @param depth
     *            This is the depth of the traversal. Since the traversal starts
     *            from a specific root node this is also the distances to this
     *            root node.
     * @return If returns true this node is traversed.
     */
    boolean nodeFilter(CnATreeElement target, Edge incomingEdge, int depth);
}
