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
package sernet.verinice.rcp.search.tables;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.column.IColumn;
import sernet.verinice.rcp.search.column.IconColumn;
import sernet.verinice.rcp.search.column.ScopeColumn;
import sernet.verinice.rcp.search.column.TitleColumn;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class SearchTableComparator extends ViewerComparator {

    IColumn currentColumn;

    NumericStringComparator numericStringComparator;

    int direction = SWT.UP;

    public SearchTableComparator() {
        numericStringComparator = new NumericStringComparator();
    }

    public void setColumn(IColumn currentColumn) {
        detectSortDirection(currentColumn);
        this.currentColumn = currentColumn;
    }

    /**
     * If column has been already set by the selection listener, the sorting
     * direction is inverted. Otherwise the direction is always set to
     * up.
     *
     * @param currentColumn
     *            column provided by selection listener == column
     *            header clicked by user
     */
    private void detectSortDirection(IColumn currentColumn) {
        if (this.currentColumn == currentColumn) {
            inverseDirection();
        } else {
            direction = SWT.UP;
        }
    }

    private void inverseDirection() {
        if (direction == SWT.UP) {
            direction = SWT.DOWN;
        } else {
            direction = SWT.UP;
        }
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {

        if (currentColumn == null) {
            return 0;
        }

        VeriniceSearchResultRow row1 = (VeriniceSearchResultRow) e1;
        VeriniceSearchResultRow row2 = (VeriniceSearchResultRow) e2;
        String v1, v2;

        if (currentColumn instanceof TitleColumn) {
            v1 = row1.getValueFromResultString(TitleColumn.TITLE_PROPERTY_NAME);
            v2 = row2.getValueFromResultString(TitleColumn.TITLE_PROPERTY_NAME);
        } else if (currentColumn instanceof ScopeColumn) {
            v1 = row1.getValueFromResultString(ISearchService.ES_FIELD_SCOPE_TITLE);
            v2 = row2.getValueFromResultString(ISearchService.ES_FIELD_SCOPE_TITLE);
        } else if (currentColumn instanceof IconColumn) {
            v1 = TableImageProvider.getImagePath(row1);
            v2 = TableImageProvider.getImagePath(row2);
        } else {
            v1 = row1.getValueFromResultString(currentColumn.getId());
            v2 = row2.getValueFromResultString(currentColumn.getId());
        }

        if (direction == SWT.DOWN) {
            return -numericStringComparator.compare(v1, v2);
        }

        return numericStringComparator.compare(v1, v2);
    }

}
