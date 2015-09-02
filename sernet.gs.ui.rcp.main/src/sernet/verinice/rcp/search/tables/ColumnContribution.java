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

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import sernet.verinice.model.search.VeriniceSearchResultTable;
import sernet.verinice.rcp.search.SearchView;
import sernet.verinice.rcp.search.column.IColumn;

public class ColumnContribution extends ContributionItem {

    private final SearchView searchView;

    private IColumn column;

    final private VeriniceSearchResultTable vSearchResultObject;

    public ColumnContribution(SearchView searchView, IColumn column, VeriniceSearchResultTable vSearchResultObject) {
        super(column.getTitle());
        this.searchView = searchView;
        this.column = column;
        this.vSearchResultObject = vSearchResultObject;
    }

    @Override
    public void fill(Menu menu, int index) {
        final MenuItem menuItem = new MenuItem(menu, SWT.CHECK);
        menuItem.setText(column.getTitle());
        menuItem.setSelection(column.isVisible());

        menuItem.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateColumnVisibility(menuItem, e);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                updateColumnVisibility(menuItem, e);
            }

            private void updateColumnVisibility(final MenuItem menuItem, SelectionEvent e) {
                if (e.getSource() == menuItem) {
                    column.setVisible(!column.isVisible());
                    menuItem.setSelection(column.isVisible());
                    searchView.setTableViewer(vSearchResultObject);
                }
            }
        });
    }
}