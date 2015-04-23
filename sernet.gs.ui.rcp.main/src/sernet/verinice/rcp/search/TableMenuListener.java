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

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;

import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.rcp.search.column.ColumnStoreFactory;
import sernet.verinice.rcp.search.column.IColumn;
import sernet.verinice.rcp.search.column.IColumnStore;

final class TableMenuListener implements IMenuListener {

    private final SearchView searchView;

    private IColumnStore columnStore;

    private VeriniceSearchResultObject vSearchResultObject;

    public TableMenuListener(SearchView searchView, VeriniceSearchResultObject vSearchResultObject) {
        this.searchView = searchView;
        columnStore = ColumnStoreFactory.getColumnStore(vSearchResultObject.getEntityTypeId());
        this.vSearchResultObject = vSearchResultObject;
    }

    @Override
    public void menuAboutToShow(IMenuManager manager) {
        if (hasNoColumnEntries(manager)) {
            for (IColumn col : columnStore.getAllColumns()) {
                manager.add(new ColumnContribution(this.searchView, col, vSearchResultObject));
            }
        }
    }

    private boolean hasNoColumnEntries(IMenuManager manager) {
        return manager.getItems() != null && manager.getItems().length == 0;
    }
}