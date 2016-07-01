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

import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.TraversalFilter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.linktable.generator.mergepath.Path;
import sernet.verinice.service.linktable.generator.mergepath.Path.PathElement;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge;
import sernet.verinice.service.linktable.generator.mergepath.VqlEdge.EdgeType;

/**
 * Controls a traversal, so that only egdes and nodes are traversed which follow
 * a given {@link Path}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
final class LtrTraversalFilter implements TraversalFilter {

    private final Path path;

    
    public LtrTraversalFilter(Path path) {
        this.path = path;
    }

    @Override
    public boolean edgeFilter(Edge e, CnATreeElement node, int depth) {

        if(hasNextElementInPath(depth)){
            return false;
        }

        PathElement pathElement = getNextElementInQueryPath(depth);
        VqlEdge vqlEdge = pathElement.edge;

        if(Edge.RELATIVES.equals(e.getType())){

            if(EdgeType.CHILD == vqlEdge.getEdgeType()){
                return e.getSource() == node;
            }

            if(EdgeType.PARENT == vqlEdge.getEdgeType()){
                return e.getTarget() == node;
            }
        } else {
            CnATreeElement target = e.getTarget() == node ? e.getSource() : e.getTarget();
            return target.getTypeId().equals(pathElement.node.getTypeId());
        }

        return true;
    }

    private boolean hasNextElementInPath(int depth) {
        return path.getPathElements().size() < depth + 2;
    }

    private boolean reachedEndOfPath(int depth) {
        return depth >= path.getPathElements().size();
    }

    private PathElement getNextElementInQueryPath(int depth) {
        return path.getPathElements().get(depth + 1);
    }

    @Override
    public boolean nodeFilter(CnATreeElement target, int depth) {
        return isProperNode(target, depth);
    }

    private boolean isProperNode(CnATreeElement target, int depth) {
        if (reachedEndOfPath(depth)) {
            return false;
        }

        String targetTypeId = target.getTypeId();
        String pathElementTypeId = path.getPathElements().get(depth).node.getTypeId();

        return targetTypeId.equals(pathElementTypeId);
    }
}
