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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.search.tables.SearchTableViewerFactory;
import sernet.verinice.rcp.search.tables.TableMenuListener;

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
public class SearchView extends RightsEnabledView {

    static final Logger LOG = Logger.getLogger(SearchView.class);

    public static final String ID = "sernet.verinice.rcp.search.SearchView";

    private Text queryText;

    private Text limitText;

    private Button searchButton;

    private SearchComboViewer resultsByTypeCombo;

    TableViewer currentViewer;

    private Composite parent;

    private SearchTableViewerFactory tableFactory;

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
            this.tableFactory = new SearchTableViewerFactory();
            initView(parent);
        } catch (Exception e) {
            LOG.error("Error while creating control", e); //$NON-NLS-1$
            ExceptionUtil.log(e, "Something went wrong here");
        }
    }

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
                    LOG.error("Error during CSV export", e);
                    MessageDialog.openError(getShell(), "Error", "Error during CSV export: " + e.getMessage());
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
                    LOG.error("Error while creating search index", e);
                    MessageDialog.openError(getShell(), "Error", "Error while creating search index: " + e.getMessage());
                }
            }
        };

        reindex.setText(Messages.SearchView_2);
        reindex.setToolTipText(Messages.SearchView_2);
        reindex.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));
    }

    private void initDoubleClickListener() {
        doubleClickListener = new TableDoubleClickListener(this);
    }

    private void doCsvExport() throws CsvExportException {
        StructuredSelection selection = ((StructuredSelection) resultsByTypeCombo.getSelection());
        if (selection != null && !selection.isEmpty()) {
            VeriniceSearchResultObject result = (VeriniceSearchResultObject) selection.getFirstElement();
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
        limitText.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                validateLimit();
            }
            @Override
            public void focusGained(FocusEvent e) {

            }
        });
    }

    private boolean validateLimit() {
        try {
            int limit = Integer.parseInt(limitText.getText());
            if(limit<0) {
                limitInputError();
                return false;
            }
        } catch (NumberFormatException nfe) {
            limitInputError();
            return false;
        }
        return true;
    }
    
    private void limitInputError() {
        limitText.setText(String.valueOf(VeriniceQuery.DEFAULT_LIMIT));
        limitText.setFocus();
        limitText.selectAll();     
        MessageDialog.openError(getShell(), Messages.SearchView_12, NLS.bind(Messages.SearchView_13, Messages.SearchView_10));
        
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
        VeriniceSearchResultObject[] result = new VeriniceSearchResultObject[veriniceSearchResult.getAllVeriniceSearchObjects().size()];
        result = veriniceSearchResult.getAllVeriniceSearchObjects().toArray(result);
        Arrays.sort(result, new VeriniceSearchResultComparator());

        resultsByTypeCombo.setSelection(new StructuredSelection(result[0]));
    }

    private boolean isResultEmpty(VeriniceSearchResult veriniceSearchResult) {
        return veriniceSearchResult.getHits() == 0;
    }

    public Composite getParent() {
        return parent;
    }

    public void setTableViewer(VeriniceSearchResultObject veriniceSearchResultObject) {
        try {

            if (currentViewer != null) {
                currentViewer.getTable().dispose();
            }

            currentViewer = tableFactory.getSearchResultTable(veriniceSearchResultObject, tableComposite);

            addTableColumnContextMenu(veriniceSearchResultObject);

            currentViewer.addDoubleClickListener(doubleClickListener);
            tableComposite.layout();
        } catch (Exception ex) {
            LOG.error("table rendering failed", ex);
            showError("Table rendering faile", ex.getLocalizedMessage());
            throw new RuntimeException(ex);
        }
    }

    private void addTableColumnContextMenu(VeriniceSearchResultObject veriniceSearchResultObject) {
        MenuManager menuMgr = new MenuManager("#ContextMenu");
        Menu menu = menuMgr.createContextMenu(currentViewer.getControl());

        menuMgr.addMenuListener(new TableMenuListener(this, veriniceSearchResultObject));

        currentViewer.getControl().setMenu(menu);

        getSite().registerContextMenu(menuMgr, currentViewer);
    }

    private void search() {
        validateLimit();
        VeriniceQuery veriniceQuery = new VeriniceQuery(queryText.getText(), Integer.valueOf(limitText.getText()));
        WorkspaceJob job = new SearchJob(veriniceQuery, this);
        job.schedule();
    }

    private void reindex() {
        WorkspaceJob job = new ReIndexJob(reindex);
        job.schedule();
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
        export2CSV.setEnabled(enable);
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
                validateLimit();
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
}
