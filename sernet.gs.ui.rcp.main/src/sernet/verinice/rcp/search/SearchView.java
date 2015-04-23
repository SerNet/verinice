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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResult;
import sernet.verinice.model.search.VeriniceSearchResultObject;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.search.tables.SearchTableViewerFactory;

/**
 *
 */
public class SearchView extends RightsEnabledView {

    private static final Logger LOG = Logger.getLogger(SearchView.class);

    public static final String ID = "sernet.verinice.rcp.search.SearchView";

    private Text queryText;

    private Button searchButton;

    private SearchComboViewer resultsByTypeCombo;

    private TableViewer currentViewer;

    private SearchTableSorter tableSorter = new SearchTableSorter();

    private Composite parent;

    private SearchTableViewerFactory tableFactory;

    private Action export2CSV;

    private Action editMode;

    Action reindex;

    private Composite searchComposite;

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

    }

    private void makeActions() {

        export2CSV = new Action() {
            @Override
            public void run() {
                try {
                    doCsvExport();
                } catch (CsvExportException e) {
                    LOG.error("Error during CSV export", e);
                    MessageDialog.openError(getShell(), "Error", "Error during CSV export: " + e.getMessage());
                }
            }     
        };

        export2CSV.setText(Messages.SearchView_0);
        export2CSV.setToolTipText(Messages.SearchView_0);
        export2CSV.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPORT_ICON));

        editMode = new Action() {
            @Override
            public void run() {

            }
        };

        editMode.setText(Messages.SearchView_1);
        editMode.setToolTipText(Messages.SearchView_1);
        editMode.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EDIT));

        reindex = new Action() {
            @Override
            public void run() {
                reindex();
            }
        };

        reindex.setText(Messages.SearchView_2);
        reindex.setToolTipText(Messages.SearchView_2);
        reindex.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));
    }
    
    private void doCsvExport() throws CsvExportException {
        StructuredSelection selection = ((StructuredSelection)resultsByTypeCombo.getSelection());
        if(selection!=null && !selection.isEmpty()) {
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
        createSearchForm(searchComposite);
    }

    private void createSearchForm(Composite searchComposite) {

        // init query textfield
        queryText = new Text(searchComposite, SWT.SINGLE | SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.minimumWidth = 30;
        queryText.setLayoutData(gridData);
        queryText.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.character == SWT.CR) {
                    searchButton.setEnabled(false);
                    search();
                    searchButton.setEnabled(true);
                }

            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });

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

        // next line in search view
        resultsByTypeCombo = new SearchComboViewer(searchComposite, this);
        Combo resultCombo = resultsByTypeCombo.getCombo();

        GridData resultsByTypeComboGridData = new GridData();
        resultsByTypeComboGridData.horizontalAlignment = SWT.FILL;
        resultsByTypeComboGridData.horizontalSpan = 2;
        resultCombo.setLayoutData(resultsByTypeComboGridData);
    }

    private Composite createSearchComposite(Composite composite) {

        Composite comboComposite = new Composite(composite, SWT.NONE);

        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        comboComposite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(3, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        return comboComposite;
    }

    private Composite createContainerComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
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
        resultsByTypeCombo.refresh();
    }

    public Composite getParent() {
        return parent;
    }

    public void setTableViewer(VeriniceSearchResultObject veriniceSearchResultObject) {

        if (currentViewer != null) {
            currentViewer.getTable().dispose();
        }

        currentViewer = tableFactory.getSearchResultTable(veriniceSearchResultObject, searchComposite);
    }

    private void search() {
       VeriniceQuery veriniceQuery = new VeriniceQuery();
       veriniceQuery.setQuery(queryText.getText());
       WorkspaceJob job = new SearchJob(veriniceQuery, searchButton, queryText, this);
       job.schedule();
    }
    
    private void reindex() {
        WorkspaceJob job = new IndexJob(this);
        job.schedule();
    }

    public TableViewer getCurrentViewer() {
        return currentViewer;
    }

    public SearchTableSorter getTableSorter() {
        return tableSorter;
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
}
