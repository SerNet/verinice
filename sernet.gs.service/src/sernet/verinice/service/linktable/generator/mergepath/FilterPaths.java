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
package sernet.verinice.service.linktable.generator.mergepath;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;

import sernet.verinice.service.linktable.generator.mergepath.Path.PathElement;

/**
 * Traverses a {@link VqlAst} in order to extract all possible pathes.
 *
 * Example:
 *
 * <pre>
 *
 *                    * assetgroup[title]
 *                    |
 *                    * asset[title, description]
 *                   / \
 *  CnaLink[title]  /   \ CnaLink
 *                 /     \
 * control[title] *       * person[name, surname]
 * </pre>
 *
 * The method returns two pathes:
 *
 * <pre>
 *      [
 *          [(assetgroup, null), (asset, child), (control, link)],
 *          [(assetgroup, null), (asset, child), (person, link)]
 *      ]
 * </pre>
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
final class FilterPaths implements TraversalListener<VqlNode, VqlEdge> {

    private final VqlAst vqlAst;
    private final Deque<PathElement> vqlStack = new LinkedList<>();
    private final Deque<EdgeTraversalEvent<VqlNode, VqlEdge>> edgeStack = new ArrayDeque<>();
    private final Set<Path> paths = new HashSet<>();

    FilterPaths(VqlAst vqlAst) {
        this.vqlAst = vqlAst;
    }


    @Override
    public void vertexTraversed(VertexTraversalEvent<VqlNode> e) {

        VqlEdge edge = getEdge(e.getVertex());
        vqlStack.addFirst(new PathElement(e.getVertex(), edge));

        if (isLeaf(e.getVertex())) {
            paths.add(createPath());
        }
    }

    private VqlEdge getEdge(VqlNode vertex) {

        Set<VqlEdge> incomingEdges = this.vqlAst.getVqlGraph().incomingEdgesOf(vertex);

        if (incomingEdges.isEmpty()) {
            return null;
        } else {
            // extract the value
            return incomingEdges.iterator().next();
        }
    }

    @Override
    public void vertexFinished(VertexTraversalEvent<VqlNode> e) {

        vqlStack.removeFirst();

        if (!edgeStack.isEmpty()) {
            edgeStack.removeFirst();
        }
    }

    @Override
    public void edgeTraversed(EdgeTraversalEvent<VqlNode, VqlEdge> e) {
        // not needed
    }

    @Override
    public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
        // not needed
    }

    @Override
    public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
        // not needed
    }

    private Path createPath() {

        Path path = new Path();
        Deque<PathElement> q = new ArrayDeque<>(vqlStack);

        while (!q.isEmpty()){
            PathElement lastElement = q.removeLast();
            path.addPathElement(lastElement.node, lastElement.edge);
        }

        return path;
    }

    private boolean isLeaf(VqlNode vertex) {
        return this.vqlAst.getVqlGraph().outgoingEdgesOf(vertex).size() == 0;
    }

    Set<Path> getPaths() {
        return paths;
    }
}