/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;

/**
 *
 */
public class SearchSortSelectionAdapter extends SelectionAdapter {
    
    private SearchView searchView;
    private TableColumn column;
    private int index;
    
    public SearchSortSelectionAdapter(SearchView view, TableColumn column, int index){
        this.searchView = view;
        this.column = column;
        this.index = index;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        searchView.getTableSorter().setColumn(index);
        int dir = searchView.getCurrentViewer().getTable().getSortDirection();
        if (searchView.getCurrentViewer().getTable().getSortColumn() == column) {
            dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
        } else {

            dir = SWT.DOWN;
        }
        searchView.getCurrentViewer().getTable().setSortDirection(dir);
        searchView.getCurrentViewer().getTable().setSortColumn(column);
        searchView.getCurrentViewer().refresh();
    }

}
