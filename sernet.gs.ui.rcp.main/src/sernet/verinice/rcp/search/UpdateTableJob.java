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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

import sernet.verinice.model.search.VeriniceSearchResultTable;
import sernet.verinice.rcp.search.tables.SearchTableViewerFactory;

/**
 * Update the table according to the selection in the {@link SearchComboViewer}.
 *
 * Marking the matching string can take a while, so this is done in an extra
 * thread.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
final class UpdateTableJob extends UIJob {

    private final SearchView searchView;
    private final VeriniceSearchResultTable veriniceSearchResultTable;
    private Composite tableComposite;

    UpdateTableJob(SearchView searchView, VeriniceSearchResultTable veriniceSearchResultTable, Composite tableComposite) {
        super(Messages.UpdateTableJob_0);
        this.searchView = searchView;
        this.veriniceSearchResultTable = veriniceSearchResultTable;
        this.tableComposite = tableComposite;
    }

    /*
     * @see
     * org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {

        final TableViewer currentViewer = new SearchTableViewerFactory().getSearchResultTable(veriniceSearchResultTable, tableComposite);


        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                UpdateTableJob.this.searchView.updateTable(currentViewer, veriniceSearchResultTable);
            }
        });

        return Status.OK_STATUS;
    }
}