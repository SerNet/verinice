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

import java.util.SortedMap;
import java.util.TreeMap;

import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.web.poseidon.services.CompareByTitle;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public final class SimpleSumOfStates implements AggregateControlsStrategy {


    @Override
    public SortedMap<String, Number> aggregateData(Iterable<MassnahmenUmsetzung> bsiControls) {
        SortedMap<String, Number> result = new TreeMap<>(new CompareByTitle());

        for (MassnahmenUmsetzung m : bsiControls) {
            Number number = result.get(m.getUmsetzung());
            number = number == null ? 1 : number.intValue() + 1;
            result.put(m.getUmsetzung(), number);
        }

        return result;
    }
}