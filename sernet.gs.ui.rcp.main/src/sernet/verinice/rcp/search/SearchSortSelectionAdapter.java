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
    
    private SearchView view;
    private TableColumn column;
    private int index;
    
    public SearchSortSelectionAdapter(SearchView view, TableColumn column, int index){
        this.view = view;
        this.column = column;
        this.index = index;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        view.tableSorter.setColumn(index);
        int dir = view.viewer.getTable().getSortDirection();
        if (view.viewer.getTable().getSortColumn() == column) {
            dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
        } else {

            dir = SWT.DOWN;
        }
        view.viewer.getTable().setSortDirection(dir);
        view.viewer.getTable().setSortColumn(column);
        view.viewer.refresh();
    }

}
