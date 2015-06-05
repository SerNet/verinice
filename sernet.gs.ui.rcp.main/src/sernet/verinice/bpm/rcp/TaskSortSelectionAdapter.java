package sernet.verinice.bpm.rcp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.TableColumn;

class TaskSortSelectionAdapter extends SelectionAdapter {

    private TaskView view;
    private TableColumn column;
    private int index;

    public TaskSortSelectionAdapter(TaskView fileView, TableColumn column, int index) {
        super();
        this.view = fileView;
        this.column = column;
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        view.getTableSorter().setColumn(index);
        int dir = view.getViewer().getTable().getSortDirection();
        if (view.getViewer().getTable().getSortColumn() == column) {
            dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
        } else {

            dir = SWT.DOWN;
        }
        view.getViewer().getTable().setSortDirection(dir);
        view.getViewer().getTable().setSortColumn(column);
        view.getViewer().refresh();
    }
}