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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.web.poseidon.services.CompareByTitle;

/**
 * Counts the states of all {@link MassnahmenUmsetzung} states and group them by
 * the {@link BausteinUmsetzung#getKapitel()} value.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class GroupByStrategySum implements GroupByStrategy {

    public static final String GET_PARAM_IDENTIFIER = "accumulated";

    private static final Logger LOG = Logger.getLogger(GroupByStrategySum.class);

    @Override
    public final Map<String, Map<String, Number>> aggregateMassnahmen(VeriniceGraph g) {

        List<DataPoint> dataPoints = new ArrayList<>();

        for (MassnahmenUmsetzung maU : g.getElements(MassnahmenUmsetzung.class)) {

            dataPoints.add(new DataPoint((BausteinUmsetzung) g.getParent(maU), maU));
        }

        Map<String, List<DataPoint>> state2DataPoint = new HashMap<>();
        for (DataPoint p : dataPoints) {
            if (!state2DataPoint.containsKey(p.getState())) {
                state2DataPoint.put(p.getState(), new ArrayList<DataPoint>());
            }

            state2DataPoint.get(p.getState()).add(p);
        }

        Map<String, Map<String, Number>> data = new TreeMap<>(new CompareByTitle());
        for (Entry<String, List<DataPoint>> e : state2DataPoint.entrySet()) {
            data.put(e.getKey(), new HashMap<String, Number>());
            for (DataPoint p : e.getValue()) {
                Number number = data.get(e.getKey()).get(p.getChapter());
                number = number == null ? 1 : number.intValue() + 1;
                data.get(e.getKey()).put(p.getChapter(), number);
            }
        }

        Set<String> ticks = new HashSet<>();
        for (DataPoint d : dataPoints) {
            ticks.add(d.getChapter());
        }

        // fill up empty ticks
        for (Map<String, Number> series : data.values()) {
            for (String tick : ticks) {
                if (!series.keySet().contains(tick)) {
                    LOG.debug("fill up tick: " + tick);
                    series.put(tick, 0);
                }
            }
        }

        return data;
    }

    private static class DataPoint {

        private BausteinUmsetzung bst;

        private MassnahmenUmsetzung massnahmenUmsetzung;

        public DataPoint(BausteinUmsetzung bst, MassnahmenUmsetzung massUms) {
            this.bst = bst;
            this.massnahmenUmsetzung = massUms;
        }

        public String getState() {
            return massnahmenUmsetzung.getUmsetzung();
        }

        public String getChapter() {
            return bst.getKapitel();
        }
    }

}
