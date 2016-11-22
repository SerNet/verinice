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

import java.nio.file.Path;

import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.TraversalFilter;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Controls a traversal, so that only egdes and nodes are traversed which follow
 * a given {@link Path}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
final class LtrTraversalFilter implements TraversalFilter {

    private VqlContext vqlNavigator;
    
    public LtrTraversalFilter(VqlContext vqlNavigator) {
        this.vqlNavigator = vqlNavigator;
    }

    @Override
    public boolean edgeFilter(Edge e, CnATreeElement source, CnATreeElement target, int depth) {
        return vqlNavigator.isValideEdge(source, e, target);
    }

    @Override
    public boolean nodeFilter(CnATreeElement target, Edge incomingEdge, int depth) {
       return true;
    }

}
