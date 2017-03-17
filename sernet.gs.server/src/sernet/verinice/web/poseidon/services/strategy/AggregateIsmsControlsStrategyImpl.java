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
package sernet.verinice.web.poseidon.services.strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.traverse.BreadthFirstIterator;

import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 * Accumulates implementation states.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class AggregateIsmsControlsStrategyImpl implements AggregateIsmsControlsStrategy {

    private List<ControlGroup> catalogs;
    private VeriniceGraph veriniceGraph;

    public AggregateIsmsControlsStrategyImpl(VeriniceGraph veriniceGraph, List<ControlGroup> catalogs) {
        this.veriniceGraph = veriniceGraph;
        this.catalogs = catalogs;
    }

    public AggregateIsmsControlsStrategyImpl(VeriniceGraph veriniceGraph, ControlGroup catalog) {
        this(veriniceGraph, Arrays.asList(new ControlGroup[] { catalog }));
    }

    @Override
    public Map<String, Number> getData() {

        return calculateData();

    }

    private Map<String, Number> calculateData() {

        Map<String, Number> data = new HashMap<>();

        for (ControlGroup root : catalogs) {
            sumImplemationStates(data, root);
        }

        return data;
    }

    private void sumImplemationStates(Map<String, Number> data, ControlGroup root) {
        BreadthFirstIterator<CnATreeElement, Edge> breadthFirstIterator = new BreadthFirstIterator<>(veriniceGraph.getGraph(), root);
        while (breadthFirstIterator.hasNext()) {
            CnATreeElement element = breadthFirstIterator.next();
            if (element instanceof Control) {
                Control control = (Control) element;
                Number i = data.get(control.getImplementation()) == null ? 1 : data.get(control.getImplementation()).intValue() + 1;
                data.put(control.getImplementation(), i);
            }
        }
    }

}
