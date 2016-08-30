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
package sernet.verinice.service.linktable;

import java.util.Comparator;
import java.util.List;

import sernet.gs.service.NumericStringComparator;

/**
 * Compares two rows of a table by comparing
 * the first column of the table. If first column is equal
 * the comparator continues with the second [3.,4.] column.
 *
 * For comparison a {@link NumericStringComparator} is used.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class RowComparator implements Comparator<List<String>> {

    private static final NumericStringComparator NSC = new NumericStringComparator();

    @Override
    public int compare(List<String> row1, List<String> row2) {
        return compare(row1, row2, 0);
    }

    private static int compare(List<String> row1, List<String> row2, int column) {
        int value = 0;
        String s1 = row1.get(column);
        String s2 = row2.get(column);
        if(s1==null && s2!=null) {
            value = 1;
        }
        if(s1!=null && s2==null) {
            value = -1;
        }
        if(s1!=null && s2!=null) {
            value = NSC.compare(s1, s2);
        }
        if(value==0 && column+1 < row1.size()) {
            value = compare(row1, row2, column+1);
        }
        return value;
    }
}