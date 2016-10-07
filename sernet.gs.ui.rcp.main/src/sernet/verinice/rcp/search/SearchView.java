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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.progress.UIJob;

import sernet.gs.ui.rcp.main.*;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.bsi.dnd.SearchViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.SearchViewDropListener;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.*;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.search.*;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.search.tables.TableMenuListener;
import sernet.verinice.service.csv.CsvExportException;

/**
 * Provides input fields for searching the verinice databases and renders the
 * result into a table view.
 * <p>
 * The main form composite contains a 4 column grid, and the sub composites gets
 * their position with the {@link GridData#horizontalSpan} field.
 *
 * <pre>
 * | 1.        | 2.        | 3.        | 4.        |
 * |-----------------------------------------------|
 * | Query Title           | Limit Title           | {@link #createTitlesForSearchForm(Composite)}
 * |-----------------------------------------------|
 * |         query         | limit     | searchbtn | {@link #createSearchForm(Composite)}
 * |-----------------------------------------------|
 * |         combobox for selecting result         | {@link #createResultCombobox(Composite)}
 * |-----------------------------------------------|
 * |         result table with flexibel columns    | {@link #createTableComposite()}
 * |-----------------------------------------------|
 * </pre>
 *
 * </p>
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * @author Sebastian Hagedorn <sh@sernet.de>
 */
@SuppressWarnings("restriction")
public class SearchView extends RightsEnabledView {

    static final Logger LOG = Logger.getLogger(SearchView.class);

    public static final String ID = "sernet.verinice.rcp.search.SearchView"; //$NON-NLS-1$

    private Text queryText;

    private Text limitText;

    private Button searchButton;

    private SearchComboViewer resultsByTypeCombo;

    TableViewer currentViewer;

    private Composite parent;

    private RightsEnabledAction export2CSV;

    private Action editMode;

    private IDoubleClickListener doubleClickListener;

    private RightsEnabledAction reindex;

    private Composite searchComposite;

    private Composite tableComposite;

    public SearchView() {
        super();
    }

    @Override
    public void createPartControl(Composite parent) {
        try {
            super.createPartControl(parent);
            this.parent = parent;
            initView(parent);
        } catch (Exception e) {
            LOG.error("Error while creating control", e); //$NON-NLS-1$
            ExceptionUtil.log(e, "Something went wrong here"); //$NON-NLS-1$
        }
    }

    @Override
    public void setFocus() {
        queryText.setFocus();
    };

    private void initView(Composite parent) {
        parent.setLayout(new FillLayout());
        createComposite(parent);

        makeActions();
        fillLocalToolBar();
        initDoubleClickListener();
    }

    private void makeActions() {

        export2CSV = new RightsEnabledAction(ActionRightIDs.SEARCHEXPORT) {
            @Override
            public void doRun() {
                try {
                    doCsvExport();
                } catch (Exception e) {
                    LOG.error("Error during CSV export", e); //$NON-NLS-1$
                    MessageDialog.openError(getShell(), Messages.SearchView_19, Messages.SearchView_20 + e.getMessage());
                }
            }
        };

        export2CSV.setText(Messages.SearchView_0);
        export2CSV.setToolTipText(Messages.SearchView_0);
        export2CSV.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPORT_ICON));
        export2CSV.setEnabled(false);

        reindex = new RightsEnabledAction(ActionRightIDs.SEARCHREINDEX) {
            @Override
            public void doRun() {
                try {
                    reindex();
                } catch (Exception e) {
                    LOG.error("Error while creating search index", e); //$NON-NLS-1$
                    MessageDialog.openError(getShell(), Messages.SearchView_22, Messages.SearchView_23 + e.getMessage());
                }
            }
        };

        reindex.setText(Messages.SearchView_2);
        reindex.setToolTipText(Messages.SearchView_2);
        reindex.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));

        switchButtonsToSearchEnabled();
    }

    private void initDoubleClickListener() {
        doubleClickListener = new TableDoubleClickListener(this);
    }

    private void doCsvExport() throws CsvExportException {
        StructuredSelection selection = ((StructuredSelection) resultsByTypeCombo.getSelection());
        if (selection != null && !selection.isEmpty()) {
            VeriniceSearchResultTable result = (VeriniceSearchResultTable) selection.getFirstElement();
            CsvExportHandler handler = new CsvExportHandler(result, getShell());
            handler.run();
        }
    }

    private void fillLocalToolBar() {

        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();

        if (export2CSV != null) {
            manager.add(export2CSV);
        }

        if (editMode != null) {
            manager.add(editMode);
        }

        if (reindex != null) {
            manager.add(reindex);
        }
    }

    private void createComposite(Composite parent) {

        Composite composite = createContainerComposite(parent);
        searchComposite = createSearchComposite(composite);

        createTitlesForSearchForm(searchComposite);
        createSearchForm(searchComposite);
        createResultCombobox(searchComposite);
        createTableComposite();
    }

    private Composite createContainerComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }

    private Composite createSearchComposite(Composite composite) {

        Composite comboComposite = new Composite(composite, SWT.NONE);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        comboComposite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(4, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        return comboComposite;
    }

    /**
     * Sets labels for query and limit input field.
     */
    private void createTitlesForSearchForm(Composite searchComposite) {
        Label queryTextLabel = new Label(searchComposite, SWT.NONE);
        queryTextLabel.setText(Messages.SearchView_9);
        GridData queryTextLabelData = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
        queryTextLabelData.horizontalSpan = 2;
        queryTextLabel.setLayoutData(queryTextLabelData);

        Label limitTextLabel = new Label(searchComposite, SWT.NONE);
        limitTextLabel.setText(Messages.SearchView_10);
        GridData limitTextLabelData = new GridData(SWT.BEGINNING, SWT.FILL, true, false);
        limitTextLabelData.horizontalSpan = 2;
        limitTextLabel.setLayoutData(limitTextLabelData);
    }

    private void createSearchForm(Composite searchComposite) {

        createInputFields(searchComposite);

        searchButton = new Button(searchComposite, SWT.BUTTON1);
        searchButton.setText(Messages.SearchView_3);
        searchButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                search();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        GridData searchButtonGridData = new GridData();
        searchButtonGridData.horizontalAlignment = SWT.FILL;
        searchButton.setLayoutData(searchButtonGridData);
    }

    private void createResultCombobox(Composite searchComposite) {
        // next line in search view
        resultsByTypeCombo = new SearchComboViewer(searchComposite, this);
        Combo resultCombo = resultsByTypeCombo.getCombo();

        GridData resultsByTypeComboGridData = new GridData();
        resultsByTypeComboGridData.horizontalAlignment = SWT.FILL;
        resultsByTypeComboGridData.horizontalSpan = 4;
        resultCombo.setLayoutData(resultsByTypeComboGridData);
    }

    private void createInputFields(Composite searchComposite) {
        createQueryInputField(searchComposite);
        createLimitInputField(searchComposite);
    }

    private void createQueryInputField(Composite searchComposite) {
        // init query textfield
        queryText = new Text(searchComposite, SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.minimumWidth = 30;
        gridData.horizontalSpan = 2;
        queryText.setLayoutData(gridData);
        queryText.addKeyListener(new InputFieldsListener());
    }

    private void createLimitInputField(Composite searchComposite) {
        limitText = new Text(searchComposite, SWT.SINGLE | SWT.BORDER);
        GridData gridLimitData = new GridData(SWT.FILL, SWT.NONE, true, false);
        limitText.setLayoutData(gridLimitData);
        limitText.setText(String.valueOf(VeriniceQuery.DEFAULT_LIMIT));
        limitText.addKeyListener(new InputFieldsListener());
        limitText.addListener(SWT.Verify, new Listener() {

            @Override
            public void handleEvent(Event e) {
                String string = e.text;
                char[] chars = new char[string.length()];
                string.getChars(0, chars.length, chars, 0);
                for (int i = 0; i < chars.length; i++) {
                    if (!('0' <= chars[i] && chars[i] <= '9')) {
                        e.doit = false;
                        return;
                    }
                }
            }
        });

    }

    private void createTableComposite() {
        tableComposite = new Composite(searchComposite, SWT.SINGLE | SWT.NONE);
        tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        tableComposite.setLayout(new FillLayout());
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.SEARCHVIEW;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }

    public void updateResultCombobox(VeriniceSearchResult veriniceSearchResult) {

        resultsByTypeCombo.setInput(veriniceSearchResult);

        if (isResultEmpty(veriniceSearchResult)) {
            currentViewer.getTable().dispose();
        }

        if (!isResultEmpty(veriniceSearchResult)) {
            selectedFirstEntry(veriniceSearchResult);
        }

        // resultsByTypeCombo.getCombo().select(0);
        resultsByTypeCombo.refresh();
    }

    private void selectedFirstEntry(VeriniceSearchResult veriniceSearchResult) {
        VeriniceSearchResultTable[] result = new VeriniceSearchResultTable[veriniceSearchResult.getAllVeriniceSearchTables().size()];
        result = veriniceSearchResult.getAllVeriniceSearchTables().toArray(result);
        Arrays.sort(result, new VeriniceSearchResultComparator());

        resultsByTypeCombo.setSelection(new StructuredSelection(result[0]));
    }

    private boolean isResultEmpty(VeriniceSearchResult veriniceSearchResult) {
        return veriniceSearchResult.getHits() == 0;
    }

    public Composite getParent() {
        return parent;
    }

    public void setTableViewer(final VeriniceSearchResultTable veriniceSearchResultTable) {
        UIJob updateTable = new UpdateTableJob(this, veriniceSearchResultTable, tableComposite);
        updateTable.schedule();
    }

    void updateTable(TableViewer table, VeriniceSearchResultTable veriniceSearchResultTable) {
        try {
            if (currentViewer != null) {
                currentViewer.getTable().dispose();
            }

            currentViewer = table;

            addTableColumnContextMenu(veriniceSearchResultTable);

            currentViewer.addDoubleClickListener(doubleClickListener);

            Transfer[] dragTypes = new Transfer[] { SearchViewElementTransfer.getInstance(),
            };
            Transfer[] dropTypes = new Transfer[] { SearchViewElementTransfer.getInstance(),
                    IBSIStrukturElementTransfer.getInstance(),
                    ISO27kElementTransfer.getInstance()
            };

            int operations = DND.DROP_COPY | DND.DROP_MOVE;

            currentViewer.addDragSupport(operations, dragTypes,
                    new SearchViewDragListener(currentViewer));

            currentViewer.addDropSupport(operations, dropTypes,
                    new SearchViewDropListener(currentViewer));

            // repaint the table and rearranged the dimensions
            tableComposite.layout();

        } catch (Exception ex) {
            LOG.error("table rendering failed", ex); //$NON-NLS-1$
            showError(Messages.SearchView_25, ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }
    }

    private void addTableColumnContextMenu(VeriniceSearchResultTable veriniceSearchResultTable) {
        MenuManager menuMgr = new MenuManager("#ContextMenu"); //$NON-NLS-1$
        Menu menu = menuMgr.createContextMenu(currentViewer.getControl());
        menuMgr.addMenuListener(new TableMenuListener(this, veriniceSearchResultTable));

        currentViewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, currentViewer);
    }

    private void search() {
        if (isSearchConfirmed()) {
            validateAndCorrectLimit();
            VeriniceQuery veriniceQuery = new VeriniceQuery(queryText.getText(), Integer.valueOf(limitText.getText()));
            WorkspaceJob job = new SearchJob(veriniceQuery, this);
            job.schedule();
        }
    }

    private boolean isSearchConfirmed() {
        final int ok = 0;
        int result = ok;

        if (isQueryEmpty()) {
            MessageDialog dialog = new MessageDialog(
                    getShell(), 
                    Messages.SearchView_29, 
                    null, 
                    Messages.SearchView_16, 
                    MessageDialog.WARNING, 
                    new String[] { Messages.SearchView_27, Messages.SearchView_28 }, 
                    0);
            result = dialog.open();
        }

        return result == ok;
    }

    private void validateAndCorrectLimit() {
        try {
            int limit = Integer.valueOf(limitText.getText());
            if (limit <= 0) {
                limitText.setText(String.valueOf(VeriniceQuery.MAX_LIMIT));
            }
        } catch (NumberFormatException e) {
            limitText.setText(String.valueOf(VeriniceQuery.MAX_LIMIT));
        }
    }

    private boolean isQueryEmpty() {
        return null == queryText.getText() || StringUtils.EMPTY.equals(queryText.getText());
    }

    private void reindex() {
        if (isReindexConfirmed()) {
            WorkspaceJob job = new ReIndexJob(reindex);
            job.addJobChangeListener(new IJobChangeListener() {

                @Override
                public void scheduled(IJobChangeEvent arg0) {
                    ServiceFactory.lookupSearchService().setReindexRunning(true);
                }

                @Override
                public void done(IJobChangeEvent arg0) {
                    ServiceFactory.lookupSearchService().setReindexRunning(false);
                }

                @Override
                public void aboutToRun(IJobChangeEvent arg0) {
                    // do nothing
                }

                @Override
                public void awake(IJobChangeEvent arg0) {
                    // do nothing
                }

                @Override
                public void running(IJobChangeEvent arg0) {
                    // do nothing
                }

                @Override
                public void sleeping(IJobChangeEvent arg0) {
                    // do nothing
                }

            });
            job.schedule();
            Activator.getDefault().setReindexJob(job);
        }
    }
    
    private boolean isReindexConfirmed() {
        final int ok = 0;
        if (ServiceFactory.lookupSearchService().isReindexRunning()) {
            MessageDialog.openWarning(Display.getDefault().getActiveShell(), Messages.SearchView_33, Messages.SearchView_34);
            return false;
        }
        MessageDialog dialog = new MessageDialog(
                getShell(), 
                Messages.SearchView_29, 
                null, 
                Messages.SearchView_30, 
                MessageDialog.WARNING, 
                new String[] { Messages.SearchView_27, Messages.SearchView_28 }, 
                0);
        return dialog.open() == ok;
    }

    public TableViewer getCurrentViewer() {
        return currentViewer;
    }

    private static Shell getShell() {
        return getDisplay().getActiveShell();
    }

    static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    protected void showError(final String title, final String message) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(getShell(), title, message);
            }
        });
    }

    public void enableExport2CSVAction(boolean enable) {
        if (export2CSV.checkRights()) {
            export2CSV.setEnabled(enable);
        }
    }

    void disableSearch() {
        limitText.setEnabled(false);
        queryText.setEnabled(false);
        searchButton.setEnabled(false);
    }

    void enableSearch() {
        limitText.setEnabled(true);
        queryText.setEnabled(true);
        searchButton.setEnabled(true);
    }

    /**
     * Executes search, if the focus is on one of the search input fields and
     * the return key is pressed.
     *
     * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
     */
    private final class InputFieldsListener implements KeyListener {
        @Override
        public void keyReleased(KeyEvent e) {
            if (e.character == SWT.CR) {
                searchButton.setEnabled(false);
                enableExport2CSVAction(false);
                search();
                searchButton.setEnabled(true);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {

        }
    }

    private void switchButtonsToSearchEnabled() {
        if(isSearchEnabled()) {
            enableSearch();
            reindex.setEnabled(true);
        } else {
            disableSearch();
            reindex.setEnabled(false);
        }
    }

    private boolean isSearchEnabled() {
        if(Activator.getDefault().isStandalone()){
            return !Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.SEARCH_DISABLE);
        } else {
            return ServiceFactory.lookupSearchService().getImplementationtype() == ISearchService.ES_IMPLEMENTATION_TYPE_REAL;
        }
    }
}
