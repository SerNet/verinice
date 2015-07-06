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

import org.eclipse.jface.preference.IPreferenceStore;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * Used for sorting columns in {@link ColumnStore}
 *
 * The default columns {@link IconColumn}, {@link TitleColumn} and
 * {@link OccurenceColumn} are always the first columns, since the rank value is
 * hardcoded. The {@link PropertyTypeColumn} is either sort by the
 * {@link NumericStringComparator} or by the order defined in the SNCA.xml
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
final class ColumnSNCAComparator extends ColumnComparator {

    private static final NumericStringComparator NSC = new NumericStringComparator();

    @Override
    public int compare(IColumn o1, IColumn o2) {
        if (arePropertyColumns(o1, o2)) {
            if (shouldDoSortingByNumericStringCompare(o1, o2)) {
                return NSC.compare(o1.getTitle(), o2.getTitle());
            }
        }

        return o1.getRank() - o2.getRank();
    }

    private boolean shouldDoSortingByNumericStringCompare(IColumn o1, IColumn o2) {
        return sortAlphabetically() || rankIsEqual(o1, o2);
    }

    private boolean rankIsEqual(IColumn o1, IColumn o2) {
        return o1.getRank() == o2.getRank();
    }

    private boolean sortAlphabetically() {
        return getSortPrefs().equals(PreferenceConstants.SEARCH_SORT_COLUMN_BY_ALPHABET);
    }

    private String getSortPrefs() {
        return getPreferenceStore().getString(PreferenceConstants.SEARCH_SORT_COLUMN_EDITOR_PREFERENCES);
    }

    private IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }
}