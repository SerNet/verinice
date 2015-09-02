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
package sernet.verinice.rcp.search.column;

import sernet.gs.service.NumericStringComparator;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class ColumnComparator implements IColumnComparator {

    private static final NumericStringComparator NSC = new NumericStringComparator();

    @Override
    public int compare(IColumn o1, IColumn o2) {
        if (arePropertyColumns(o1, o2)) {
            return NSC.compare(o1.getTitle(), o2.getTitle());
        } else {
            return o1.getRank() - o2.getRank();
        }
    }

    protected boolean arePropertyColumns(IColumn o1, IColumn o2) {
        return o1 instanceof PropertyTypeColumn && o2 instanceof PropertyTypeColumn;
    }
}
