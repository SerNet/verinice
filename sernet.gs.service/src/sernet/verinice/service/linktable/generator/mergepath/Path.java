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

import java.util.LinkedList;
import java.util.List;

/**
 * A Path describes a traversal through a {@link VqlAst} graph. It is composed
 * of list of {@link PathElement} which consist of a node and an incoming edge.
 * The first node has no incoming edge, so this value is null.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class Path {

    private final List<PathElement> pathElements;

    public Path() {
        pathElements = new LinkedList<>();
    }

    public void addPathElement(VqlNode n, VqlEdge e) {
        pathElements.add(new PathElement(n, e));
    }

    public List<PathElement> getPathElements() {
        return pathElements;
    }

    @Override
    public String toString() {
        return "Path [pathElements=" + pathElements + "]";
    }

    public int getSize() {
        return pathElements.size();
    }

    public static class PathElement {

        public final VqlNode node;
        public final VqlEdge edge;

        public PathElement(VqlNode node, VqlEdge e) {
            this.node = node;
            this.edge = e;
        }

        @Override
        public String toString() {
            return "PathElement [node=" + node + ", edge=" + edge + "]";
        }
    }
}
