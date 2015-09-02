/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import java.util.Comparator;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.search.VeriniceSearchResultTable;

/**
 * Entries are sorted by there hits pro {@link VeriniceSearchResultTable}. When
 * hits are equal the entries are sorted by the {@link NumericStringComparator}.
 *
 * There is one interesting detail. Because in a sorted Set the smallest element
 * comes first, the sign of the {@link VeriniceSearchResultTable#getHits()} has
 * to be inverted. After that the {@link SearchComboViewer} shows the elements
 * in a descending order.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
final class VeriniceSearchResultComparator implements Comparator<VeriniceSearchResultTable> {
    NumericStringComparator comparator = new NumericStringComparator();

    @Override
    public int compare(VeriniceSearchResultTable vResultObject1, VeriniceSearchResultTable vResultObject2) {

        if (vResultObject1.getHits() == vResultObject2.getHits()) {
            return comparator.compare(vResultObject1.getEntityName(), vResultObject2.getEntityName());
        } else {
            return compare(-vResultObject1.getHits(), -vResultObject2.getHits());
        }
    }

    private int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}