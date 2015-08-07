/*******************************************************************************
 * Copyright (c) 2015 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search.tables;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.search.VeriniceSearchResultTable;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.column.IColumn;
import sernet.verinice.rcp.search.column.IColumnStore;
import sernet.verinice.rcp.search.column.IconColumn;

/**
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class SearchResultsTableViewer extends TableViewer implements IStructuredContentProvider {

    private final class ListenerImplementation implements Listener {

        private final TableViewerColumn columnViewer;
        private final IColumn col;


        private ListenerImplementation(TableViewerColumn columnViewer, IColumn col) {
            this.columnViewer = columnViewer;
            this.col = col;
        }

        @Override
        public void handleEvent(Event event) {
             col.setWidth(columnViewer.getColumn().getWidth());
        }
    }

    private IColumnStore columnStore;

    private SearchTableComparator searchTableComparator;

    private Table table;

    public SearchResultsTableViewer(Composite parent, IColumnStore columnStore, VeriniceSearchResultTable veriniceSearchResultTable) {

        super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

        table = getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        searchTableComparator = new SearchTableComparator();
        setComparator(searchTableComparator);

        this.columnStore = columnStore;

        createColumns();

        setContentProvider(this);
        setInput(veriniceSearchResultTable);

        defaultSort(columnStore);
    }

    private void createColumns() {

        for (final IColumn col : columnStore.getColumns()) {

            final TableViewerColumn columnViewer = new TableViewerColumn(this, SWT.NONE);
            columnViewer.getColumn().setText(col.getTitle());
            columnViewer.getColumn().setMoveable(false);
            columnViewer.getColumn().setResizable(true);
            columnViewer.getColumn().setWidth(col.getWidth());
            columnViewer.getColumn().addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    SearchResultsTableViewer.this.searchTableComparator.setColumn(col);
                    SearchResultsTableViewer.this.getTable().setSortColumn(columnViewer.getColumn());
                    SearchResultsTableViewer.this.refresh();
                }
            });

            if (col instanceof IconColumn) {
                columnViewer.getColumn().setText("");
            }

            columnViewer.setLabelProvider(new SearchTableColumnLabelProvider(col));
            columnViewer.getColumn().addListener(SWT.Resize, new ListenerImplementation(columnViewer, col));
        }
    }

    /**
     * Default sort by the first column which is not an icon column.
     *
     */
    private void defaultSort(IColumnStore columnStore) {
        for (IColumn column : columnStore.getAllColumns()) {
            if (!(column instanceof IconColumn)) {
                searchTableComparator.setColumn(column);
                refresh();
                return;
            }
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof VeriniceSearchResultTable) {
            VeriniceSearchResultTable object = (VeriniceSearchResultTable) inputElement;
            VeriniceSearchResultRow[] elements = new VeriniceSearchResultRow[object.getRows().size()];
            return object.getRows().toArray(elements);
        }

        return new Object[] { new PlaceHolder("loading") };
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
     * .viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub
    }
}
