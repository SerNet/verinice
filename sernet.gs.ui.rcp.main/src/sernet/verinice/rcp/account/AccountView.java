/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.account;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.service.account.AccountSearchParameterFactory;

/**
 * View to find, edit, create and delete accounts.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AccountView extends RightsEnabledView {

    static final Logger LOG = Logger.getLogger(AccountView.class);

    public static final String ID = "sernet.verinice.rcp.account.AccountView"; //$NON-NLS-1$

    private RightsEnabledAction editAccountAction;

    TableViewer viewer;
    AccountTableSorter tableSorter = new AccountTableSorter();
    private AccountContentProvider contentProvider = new AccountContentProvider();


    private IModelLoadListener modelLoadListener;
    private IAccountService accountService;

    public AccountView() {
        super();
    }

    @Override
    public String getRightID() {
        return ActionRightIDs.ACCOUNTSETTINGS;
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

    @Override
    public void createPartControl(Composite parent) {
        try {
            super.createPartControl(parent);
            initView(parent);
            
        } catch (Exception e) {
            LOG.error("Error while creating control", e); //$NON-NLS-1$
            ExceptionUtil.log(e, "Error while opening account view.");
        }
    }

    private void initView(Composite parent) {        
        parent.setLayout(new FillLayout());
        createTable(parent);      
        makeActions();
        hookActions();
        fillLocalToolBar();   
        startInitDataJob();
    }

    private void createTable(Composite parent) {
        TableColumn fileNameColumn;
        TableColumn textColumn;

        final int filenameColumnWidth = 152;
        final int textColumnWidth = 250;

        viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new AccountLabelProvider());
        Table table = viewer.getTable();

        fileNameColumn = new TableColumn(table, SWT.LEFT);
        fileNameColumn.setText("Login");
        fileNameColumn.setWidth(filenameColumnWidth);
        fileNameColumn.addSelectionListener(new AccountSortSelectionAdapter(this, fileNameColumn, 1));

        textColumn = new TableColumn(table, SWT.LEFT);
        textColumn.setText("Name");
        textColumn.setWidth(textColumnWidth);
        textColumn.addSelectionListener(new AccountSortSelectionAdapter(this, textColumn, 3));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        viewer.setSorter(tableSorter);
        // ensure initial table sorting (by filename)
        ((AccountTableSorter) viewer.getSorter()).setColumn(1);
        
        getSite().setSelectionProvider(viewer);
        viewer.setInput(new PlaceHolder("Accounts"));
    }

    private void hookActions() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {

            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {

            }
        });
    }

    protected void startInitDataJob() {
        WorkspaceJob initDataJob = new WorkspaceJob("") {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask("", IProgressMonitor.UNKNOWN);
                    Activator.inheritVeriniceContextState();
                    initData();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data.", e); //$NON-NLS-1$ //$NON-NLS-2$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }
    
    protected void initData() { 
        if (LOG.isDebugEnabled()) {
            LOG.debug("initData called..."); //$NON-NLS-1$
        }      
        if(CnAElementFactory.isIsoModelLoaded()) {
            final List<Configuration> accountList = getAccountService().findAccounts(AccountSearchParameterFactory.create());
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.setInput(accountList);
                }
            });          
        } else if(modelLoadListener==null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No model loaded, adding model load listener."); //$NON-NLS-1$
            }
            createModelLoadListener();         
        }

    }

    private void createModelLoadListener() {
        // model is not loaded yet: add a listener to load data when it's loaded
        modelLoadListener = new IModelLoadListener() {           
            @Override
            public void closed(BSIModel model) {
                // nothing to do
            }        
            @Override
            public void loaded(BSIModel model) {
                // nothing to do
            }         
            @Override
            public void loaded(ISO27KModel model) {
                startInitDataJob();
            }             
        };
        CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
    }

    /**
     * Passing the focus request to the viewer's control.
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.editAccountAction);
    }

    private void makeActions() {
        editAccountAction = new RightsEnabledAction(ActionRightIDs.ADDFILE) {
            @Override
            public void doRun() {

            }
        };
        editAccountAction.setText("Edit");
        editAccountAction.setToolTipText("Edit selected account");
        editAccountAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EDIT));
        editAccountAction.setEnabled(false);
    }

    public static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    public TableViewer getViewer() {
        return this.viewer;
    }

    public IAccountService getAccountService() {
        if (accountService == null) {
            accountService = createAccountServive();
        }
        return accountService;
    }

    private IAccountService createAccountServive() {
        return ServiceFactory.lookupAccountService();
    }

    @Override
    public void dispose() {
        super.dispose();
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
    }
}
