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

import java.util.Map;

import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * Calculates the sum of every {@link MassnahmenUmsetzung} under one
 * {@link BausteinUmsetzung}.
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface GroupByStrategy {

    /**
     * Returns a map which contains the sum of the states of all
     * {@link MassnahmenUmsetzung} grouped by the {@link BausteinUmsetzung}.
     *
     * @param g
     *            Verinice graph data model.
     * @return The key of the map is the status which is the value returned by
     *         {@link MassnahmenUmsetzung#getUmsetzung()}. The Value is map of
     *         chapter names to the sum of all states of the key of the wrapping
     *         map.
     */
    Map<String, Map<String, Number>> aggregateMassnahmen(VeriniceGraph g);

}
