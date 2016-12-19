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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;

import sernet.verinice.model.common.CnATreeElement;

/**
 * Traverses a {@link VeriniceGraph} in a depth-first manner.
 * 
 * <p>
 * The algorithm does not make any cycle detection, so it is up to you to
 * implement a {@link TraversalFilter}, which makes the decision, if a node is
 * traversed or not.<br/>
 * 
 * There is also a default {@link TraversalFilter} with
 * {@link DefaultTraversalFilter}, if you want to make a simple depth first
 * iteration over the graph.
 * </p>
 * 
 * <p>
 * For processing nodes you may implement a {@link TraversalListener}. The
 * default one {@link DefaultTraversalListener} logs the traversed nodes.</p>
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public final class DepthFirstConditionalSearchPathes {

    private static final Logger LOG = Logger.getLogger(DepthFirstConditionalSearchPathes.class);

    private final Graph<CnATreeElement, Edge> g;

    private TraversalFilter traversalFilter;

    private CnATreeElement root;

    private TraversalListener traversalListener;

    private DepthFirstConditionalSearchPathes(VeriniceGraph graph, CnATreeElement root, TraversalFilter filter, TraversalListener listener) {
        this.g = graph.getGraph();
        this.root = root;
        this.traversalFilter = filter == null ? new DefaultTraversalFilter() : filter;
        this.traversalListener = listener == null ? new DefaultTraversalListener() : listener;
    }

    /**
     * Starts a depth first search on a {@link VeriniceGraph}.
     *
     * @param graph
     *            The graph may not be null.
     * @param root
     *            The starting point for the traversal.
     * @param filter
     *            If null a default implementation is used.
     * @param listener
     *            If null a default implemention is used.
     */
    public static void traverse(VeriniceGraph graph, CnATreeElement root, TraversalFilter filter, TraversalListener listener) {
        DepthFirstConditionalSearchPathes depthFirst = new DepthFirstConditionalSearchPathes(graph, root, filter, listener);
        depthFirst.dfs();
    }

    private void dfs() {
        dfs(root, null, 0);
    }

    private void dfs(CnATreeElement node, Edge incomingEdge, int depth) {

        if (traversalFilter.nodeFilter(node, incomingEdge, depth)) {

            traversalListener.nodeTraversed(node, incomingEdge, depth);

            for (Edge e : g.edgesOf(node)) {

                LOG.debug("found edge: " + e);

                // determine the next target node
                CnATreeElement target = e.getSource() == node? e.getTarget() : e.getSource();
                CnATreeElement source = e.getSource() != node? e.getTarget() : e.getSource();

                if (traversalFilter.edgeFilter(e, source, target, depth)) {
                    traversalListener.edgeTraversed(source, target, e, depth);
                    dfs(target, e, depth + 1);
                }
            }

            traversalListener.nodeFinished(node, depth);
        }
    }

    /**
     * Detects cycles within a {@link VeriniceGraph} and builds a spanning tree.
     * 
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     *
     */
    public final class DefaultTraversalFilter implements TraversalFilter {

        private Map<CnATreeElement, Boolean> marked;

        public DefaultTraversalFilter() {
            marked = initMarkedVertices();
        }

        private Map<CnATreeElement, Boolean> initMarkedVertices() {
            Set<CnATreeElement> vertexSet = g.vertexSet();
            marked = new HashMap<>();
            for (CnATreeElement e : vertexSet) {
                marked.put(e, false);
            }

            return marked;
        }

        @Override
        public boolean edgeFilter(Edge e, CnATreeElement source, CnATreeElement target, int depth) {
            return true;
        }

        @Override
        public boolean nodeFilter(CnATreeElement target, Edge incoming, int depth) {
            if (!isVisited(target)) {
                marked.put(target, true);
                return true;
            }

            return false;
        }

        private boolean isVisited(CnATreeElement target) {
            return marked.get(target);
        }
    }

    /**
     * Logs the node which is traversed with log4j in debug mode.
     * 
     * 
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     *
     */
    public static final class DefaultTraversalListener implements TraversalListener {

        private static final Logger LOG = Logger.getLogger(DepthFirstConditionalSearchPathes.DefaultTraversalListener.class);

        @Override
        public void nodeTraversed(CnATreeElement node, Edge incoming, int depth) {
            LOG.info("traversed node: " + node + " incoming edge: " + incoming.getType() + " depth: " + depth);
        }

        @Override
        public void nodeFinished(CnATreeElement node, int depth) {
            LOG.info("finished node: " + node + " depth: " + depth);
        }

        @Override
        public void edgeTraversed(CnATreeElement source, CnATreeElement target, Edge edge, int depth) {
            LOG.info("traversed edge: " + edge + " depth: " + depth);
        }
    }
}
