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
 * Gets fired if a node/edge is traversed or removed from the traversal.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface TraversalListener {

    /**
     * Is called if a node is traversed. Therefore the node has to pass the
     * {@link TraversalFilter#nodeFilter(CnATreeElement, int)} and the
     * {@link TraversalFilter#edgeFilter(Edge, CnATreeElement, CnATreeElement, int)}.
     *
     * @param node
     *            The node which is currently traversed.
     * @param incomingEdge
     *            The edge we reached the node.
     * @param depth
     *            This is the depth of the traversal. Since the traversal starts
     *            from a specific root node this is also the distances to this
     *            root node.
     */
    void nodeTraversed(CnATreeElement node, Edge incomingEdge, int depth);

    /**
     * Is called if the sub tree is finally processed.
     *
     * @param node
     *            The finished node. This means that all child nodes where
     *            traversed and finished.
     * @param depth
     *            This is the depth of the traversal. Since the traversal starts
     *            from a specific root node this is also the distances to this
     *            root node.
     */
    void nodeFinished(CnATreeElement node, int depth);

    /**
     * Is called if a edge is traversed. Therefore the edge has to pass the
     * {@link TraversalFilter#edgeFilter(Edge, CnATreeElement, CnATreeElement, int)}.
     *
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
     *            The target node. Actually this is not the source node of the
     *            edge parameter. It is the source where the traversal is
     *            started from, means that:
     *
     *            <pre>
     *             {@link Edge#getSource()} == target
     *            </pre>
     *
     *            and the other way around is possible.
     * @param edge
     *            The edge which is currently traversed.
     * @param depth
     *            This is the depth of the traversal. Since the traversal starts
     *            from a specific root node this is also the distances to this
     *            root node.
     */
    void edgeTraversed(CnATreeElement source, CnATreeElement target, Edge edge, int depth);

}
