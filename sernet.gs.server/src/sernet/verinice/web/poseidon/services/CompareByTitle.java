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
package sernet.verinice.web.poseidon.services;

import static sernet.verinice.web.poseidon.view.charts.ChartUtils.getLabel;

import java.util.Comparator;

import sernet.gs.service.NumericStringComparator;

/**
 * Sorts with the {@link NumericStringComparator} by the messages of a property
 * id.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public final class CompareByTitle implements Comparator<String> {

    private static final NumericStringComparator numericStringComparator = new NumericStringComparator();

    @Override
    public int compare(String o1, String o2) {
        return numericStringComparator.compare(getLabel(o1), getLabel(o2));
    }
}