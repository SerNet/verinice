package sernet.verinice.rcp.account;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;

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

    @Override
    public void widgetSelected(SelectionEvent e) {
        fileView.tableSorter.setColumn(index);
        int dir = fileView.viewer.getTable().getSortDirection();
        if (fileView.viewer.getTable().getSortColumn() == column) {
            dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
        } else {

            dir = SWT.DOWN;
        }
        fileView.viewer.getTable().setSortDirection(dir);
        fileView.viewer.getTable().setSortColumn(column);
        fileView.viewer.refresh();
    }
}