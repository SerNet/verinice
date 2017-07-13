/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.account;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Sort selection adapter for table in account view
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
class AccountSortSelectionAdapter extends SelectionAdapter {

    private AccountView fileView;
    private TableColumn column;
    private int index;

    public AccountSortSelectionAdapter(AccountView fileView, TableColumn column, int index) {
        super();
        this.fileView = fileView;
        this.column = column;
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        fileView.getTableSorter().setColumn(index);
        int dir = fileView.getViewer().getTable().getSortDirection();
        if (fileView.getViewer().getTable().getSortColumn() == column) {
            dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
        } else {

            dir = SWT.DOWN;
        }
        fileView.getViewer().getTable().setSortDirection(dir);
        fileView.getViewer().getTable().setSortColumn(column);
        fileView.getViewer().refresh();
    }
}