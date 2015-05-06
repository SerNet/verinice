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

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.tables.SearchResultsTableViewer;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * Opens a row from the {@link SearchResultsTableViewer} with
 * {@link EditorFactory}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
final class TableDoubleClickListener implements IDoubleClickListener {

    private final SearchView searchView;

    /**
     * @param searchView
     */
    TableDoubleClickListener(SearchView searchView) {
        this.searchView = searchView;
    }

    private ICommandService commandService;

    @SuppressWarnings("rawtypes")
    @Override
    public void doubleClick(DoubleClickEvent event) {
        if (isTableEnabled() && isRowSelected()) {
            try {
                VeriniceSearchResultRow row = getRow();
                LoadElementByUuid loadElementByUuid = loadCnATreeElementFromRDBM(row);
                openElementInEditor(loadElementByUuid);
            } catch (Exception t) {
                SearchView.LOG.error("Error while opening control.", t); //$NON-NLS-1$
            }
        }

    }

    @SuppressWarnings("rawtypes")
    private void openElementInEditor(LoadElementByUuid loadElementByUuid) {
        if (loadElementByUuid.getElement() != null) {
            EditorFactory.getInstance().updateAndOpenObject(loadElementByUuid.getElement());
        } else {
            this.searchView.showError(Messages.SearchView_8, Messages.SearchView_7); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("rawtypes")
    private LoadElementByUuid loadCnATreeElementFromRDBM(VeriniceSearchResultRow row) throws CommandException {
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        LoadElementByUuid loadElementByUuid = new LoadElementByUuid(row.getValueFromResultString("uuid"), ri);
        loadElementByUuid = getCommandService().executeCommand(loadElementByUuid);
        return loadElementByUuid;
    }

    private VeriniceSearchResultRow getRow() {
        VeriniceSearchResultRow row = (VeriniceSearchResultRow) ((IStructuredSelection) this.searchView.currentViewer.getSelection()).getFirstElement();
        return row;
    }

    private boolean isTableEnabled() {
        return this.searchView.currentViewer != null && this.searchView.currentViewer.getControl().isVisible();
    }

    private boolean isRowSelected() {
        return this.searchView.currentViewer.getSelection() instanceof IStructuredSelection && ((IStructuredSelection) this.searchView.currentViewer.getSelection()).getFirstElement() instanceof VeriniceSearchResultRow;
    }

    private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }
}