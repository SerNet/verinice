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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bsi.BausteinUmsetzung;

/**
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class GroupByStrategyNormalized implements GroupByStrategy{

    public static final String GET_PARAM_IDENTIFIER = "normalized";

    private Map<String, Map<String, Number>> aggregateMassnahmen;
    private VeriniceGraph graph;
    private Map<String,Integer> modul2Occurences;

    @Override
    public Map<String, Map<String, Number>> aggregateMassnahmen(VeriniceGraph g) {
        graph = g;
        GroupByStrategy groupByStrategySum = new GroupByStrategySum();
        aggregateMassnahmen = groupByStrategySum.aggregateMassnahmen(g);
        counterOccurenceOfChapter();
        normalize();
        return aggregateMassnahmen;
    }

    private void normalize() {
      for(Map<String,Number> chapter2Massnahmen : aggregateMassnahmen.values()) {
          for(Entry<String,Number> e : chapter2Massnahmen.entrySet()){
             int chapterOccurences =  modul2Occurences.get(e.getKey());
             int numberOfStates = e.getValue().intValue();
             chapter2Massnahmen.put(e.getKey(), numberOfStates / chapterOccurences);
          }
      }
    }

    private void counterOccurenceOfChapter() {
        modul2Occurences = new HashMap<>();
        for(BausteinUmsetzung bauU : graph.getElements(BausteinUmsetzung.class)){
            if(!modul2Occurences.containsKey(bauU.getKapitel())){
                modul2Occurences.put(bauU.getKapitel(), 1);
            } else {
                int count = modul2Occurences.get(bauU.getKapitel()) + 1;
                modul2Occurences.put(bauU.getKapitel(), count);
            }
        }
    }

}
