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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ConfigurationAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.IComboModelLabelProvider;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.BpPersonGroup;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.rcp.ElementTitleCache;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.TextEventAdapter;
import sernet.verinice.service.account.AccountLoader;
import sernet.verinice.service.account.AccountSearchParameter;
import sernet.verinice.service.commands.LoadCnAElementByEntityTypeId;

/**
 * View to find, edit, create and delete accounts. Actions are executed by
 * account Service, see {@link IAccountService}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AccountView extends RightsEnabledView {

    private static final Logger LOG = Logger.getLogger(AccountView.class);

    public static final String ID = "sernet.verinice.rcp.account.AccountView"; //$NON-NLS-1$

    private static final int COMBO_INDEX_BOTH = 0;
    private static final int COMBO_INDEX_YES = 1;
    private static final int COMBO_INDEX_NO = 2;
    private static final int TEXT_COLUMN_WIDTH = 150;
    private static final int BOOLEAN_COLUMN_WIDTH = 70;
    private static final int MIN_WIDTH_TEXT = 100;

    private static final int MAX_LMCOLUMN_HEADER_LENGTH = 8;
    private static final String LMCOLUM_HEADER_EXTENSION = "...";

    private IAccountSearchParameter parameter = new AccountSearchParameter();

    private Action searchAction;
    private ConfigurationAction editAction;
    private Action runEditAction;
    private Action removeAction;
    private Action createAction;

    private IModelLoadListener modelLoadListener;
    private IAccountService accountService;
    private ICommandService commandService;

    private Text textLogin;
    private Text textFirstName;
    private Text textFamilyName;

    private ComboModel<CnATreeElement> comboModel;
    private Combo comboOrganization;

    private Combo comboAdmin;
    private Combo comboLocalAdmin;
    private Combo comboScopeOnly;

    private TableViewer viewer;
    private AccountTableSorter tableSorter = new AccountTableSorter();
    private AccountContentProvider contentProvider = new AccountContentProvider();
    private ISelectionListener selectionListener;
    private WorkspaceJob initDataJob;

    private Map<Integer, LicenseMessageInfos> lmColumnsMap;

    public AccountView() {
        super();
        initDataJob = new WorkspaceJob(Messages.AccountView_0) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.AccountView_0, IProgressMonitor.UNKNOWN);
                    Activator.inheritVeriniceContextState();
                    init();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", //$NON-NLS-1$
                            "Error while loading data.", e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        lmColumnsMap = new HashMap<>();
    }

    private void init() throws CommandException {
        ElementTitleCache.load(new String[] { ITVerbund.TYPE_ID_HIBERNATE, Organization.TYPE_ID,
                ItNetwork.TYPE_ID, PersonGroup.TYPE_ID, PersonenKategorie.TYPE_ID_HIBERNATE,
                BpPersonGroup.TYPE_ID });
        findAccounts();
        loadScopes();
        initCombos();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.rcp.RightsEnabledView#createPartControl(org.eclipse.swt.
     * widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        try {
            super.createPartControl(parent);
            initView(parent);
            hookPageSelection();
        } catch (Exception e) {
            LOG.error("Error while creating control", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.AccountView_2);
        }
    }

    private void initView(Composite parent) {
        parent.setLayout(new FillLayout());
        createComposite(parent);
        comboModel = new ComboModel<>(new IComboModelLabelProvider<CnATreeElement>() {
            @Override
            public String getLabel(CnATreeElement element) {
                return element.getTitle();
            }
        });
        makeActions();
        fillLocalToolBar();
        startInitDataJob();
    }

    private void createComposite(Composite parent) {
        Composite composite = createContainerComposite(parent);
        Composite searchComposite = createSearchComposite(composite);
        createSearchForm(searchComposite);
        Composite tableComposite = createTableComposite(composite);
        createTable(tableComposite);
        getSite().setSelectionProvider(viewer);
        viewer.setInput(new PlaceHolder(Messages.AccountView_3));
    }

    private Composite createContainerComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }

    private Composite createSearchComposite(Composite composite) {
        Composite comboComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        comboComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(7, true);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comboComposite.setLayout(gridLayout);
        return comboComposite;
    }

    private Composite createTableComposite(Composite composite) {
        Composite tableComposite = new Composite(composite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        tableComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        tableComposite.setLayout(gridLayout);
        return tableComposite;
    }

    private void createSearchForm(Composite searchComposite) {
        Label label = new Label(searchComposite, SWT.WRAP);
        label.setText(Messages.AccountView_4);
        label = new Label(searchComposite, SWT.WRAP);
        label.setText(Messages.AccountView_5);
        label = new Label(searchComposite, SWT.WRAP);
        label.setText(Messages.AccountView_6);
        label = new Label(searchComposite, SWT.WRAP);
        label.setText(Messages.AccountView_7);
        label = new Label(searchComposite, SWT.WRAP);
        label.setText(Messages.AccountView_8);
        label = new Label(searchComposite, SWT.WRAP);
        label.setText(Messages.AccountView_39);
        label = new Label(searchComposite, SWT.WRAP);
        label.setText(Messages.AccountView_9);

        textLogin = new Text(searchComposite, SWT.BORDER);
        GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
        gridData.minimumWidth = MIN_WIDTH_TEXT;
        textLogin.setLayoutData(gridData);

        textLogin.addFocusListener(new TextEventAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                AccountView.this.parameter.setLogin(getInput(textLogin));
                findAccounts();
            }
        });

        textFirstName = new Text(searchComposite, SWT.BORDER);
        textFirstName.setLayoutData(gridData);
        textFirstName.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                AccountView.this.parameter.setFirstName(getInput(textFirstName));
                findAccounts();
            }

            @Override
            public void focusGained(FocusEvent e) {
                // nothing to do
            }
        });

        textFamilyName = new Text(searchComposite, SWT.BORDER);
        textFamilyName.setLayoutData(gridData);
        textFamilyName.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                AccountView.this.parameter.setFamilyName(getInput(textFamilyName));
                findAccounts();
            }

            @Override
            public void focusGained(FocusEvent e) {
                // nothing to do
            }
        });

        comboOrganization = new Combo(searchComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboOrganization.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboOrganization.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModel.setSelectedIndex(comboOrganization.getSelectionIndex());
                Integer dbId = null;
                CnATreeElement scope = comboModel.getSelectedObject();
                if (scope != null) {
                    dbId = scope.getDbId();
                }
                AccountView.this.parameter.setScopeId(dbId);
                findAccounts();
            }
        });

        comboAdmin = new Combo(searchComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboAdmin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboAdmin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (COMBO_INDEX_YES == comboAdmin.getSelectionIndex()) {
                    AccountView.this.parameter.setIsAdmin(true);
                }
                if (COMBO_INDEX_NO == comboAdmin.getSelectionIndex()) {
                    AccountView.this.parameter.setIsAdmin(false);
                }
                if (COMBO_INDEX_BOTH == comboAdmin.getSelectionIndex()) {
                    AccountView.this.parameter.setIsAdmin(null);
                }
                findAccounts();
            }
        });

        comboLocalAdmin = new Combo(searchComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboLocalAdmin.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboLocalAdmin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (COMBO_INDEX_YES == comboLocalAdmin.getSelectionIndex()) {
                    AccountView.this.parameter.setIsLocalAdmin(true);
                }
                if (COMBO_INDEX_NO == comboLocalAdmin.getSelectionIndex()) {
                    AccountView.this.parameter.setIsLocalAdmin(false);
                }
                if (COMBO_INDEX_BOTH == comboLocalAdmin.getSelectionIndex()) {
                    AccountView.this.parameter.setIsLocalAdmin(null);
                }
                findAccounts();
            }
        });

        comboScopeOnly = new Combo(searchComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboScopeOnly.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboScopeOnly.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (COMBO_INDEX_YES == comboScopeOnly.getSelectionIndex()) {
                    AccountView.this.parameter.setIsScopeOnly(true);
                }
                if (COMBO_INDEX_NO == comboScopeOnly.getSelectionIndex()) {
                    AccountView.this.parameter.setIsScopeOnly(false);
                }
                if (COMBO_INDEX_BOTH == comboScopeOnly.getSelectionIndex()) {
                    AccountView.this.parameter.setIsScopeOnly(null);
                }
                findAccounts();
            }
        });

        final Button searchButton = new Button(searchComposite, SWT.PUSH);
        searchButton.setText(Messages.AccountView_10);
        searchButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                findAccounts();
            }
        });

    }

    private void createTable(Composite tableComposite) {
        viewer = new TableViewer(tableComposite,
                SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        viewer.getControl().setLayoutData(gd);

        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new AccountLabelProvider(lmColumnsMap, viewer));
        Table table = viewer.getTable();

        int columnIndex = 0;

        createTableColumn(Messages.AccountView_12, TEXT_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_13, TEXT_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_14, TEXT_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_15, TEXT_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_16, TEXT_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_17, BOOLEAN_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_38, BOOLEAN_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_18, BOOLEAN_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_19, BOOLEAN_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_20, BOOLEAN_COLUMN_WIDTH, columnIndex++, "");
        createTableColumn(Messages.AccountView_21, BOOLEAN_COLUMN_WIDTH, columnIndex++, "");

        try {
            columnIndex = creatLMColumns(columnIndex);
        } catch (LicenseManagementException e) {
            String msg = "Error creating license-mgmt-Colums";
            ExceptionUtil.log(e, msg);
            LOG.error(msg, e);
        }

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        viewer.setSorter(tableSorter);
        // ensure initial table sorting (by filename)
        ((AccountTableSorter) viewer.getSorter()).setColumn(2);
    }

    /**
     * @param columnIndex
     * @return
     * @throws LicenseManagementException
     */
    private int creatLMColumns(int columnIndex)
            throws LicenseManagementException {
        List<LicenseMessageInfos> licenseInfos = new ArrayList<>();
        licenseInfos.addAll(getLMService().getAllLicenseMessageInfos());

        Collections.sort(licenseInfos, new Comparator<LicenseMessageInfos>() {

            @Override
            public int compare(LicenseMessageInfos infos0, LicenseMessageInfos infos1) {
                return infos0.getContentId().compareToIgnoreCase(infos1.getContentId());
            }

        });

        for (int index = 0; index < licenseInfos.size(); index++) {

            LicenseMessageInfos infos = licenseInfos.get(index);
            if (infos != null) {
                infos.setAccountWizardLabel(
                        LicenseMgmtPage.getLicenseLabelString(infos.getLicenseId()));
                infos.setAccountViewColumnHeader(
                        getLMColumnHeader(infos.getContentId(), index + 1));
                lmColumnsMap.put(columnIndex, infos);

                createTableColumn(infos.getAccountViewColumnHeader(), BOOLEAN_COLUMN_WIDTH,
                        columnIndex++, infos.getAccountWizardLabel());
            }
        }
        return columnIndex;
    }

    private String getLMColumnHeader(String contentId, int index) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(index));
        sb.append(". ");
        if (contentId.length() >= MAX_LMCOLUMN_HEADER_LENGTH) {
            sb.append(contentId.substring(0, MAX_LMCOLUMN_HEADER_LENGTH));
            sb.append(LMCOLUM_HEADER_EXTENSION);
        } else {
            sb.append(contentId);
        }

        return sb.toString();
    }

    private void createTableColumn(String title, int width, int index, String tooltip) {
        TableColumn scopeColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
        scopeColumn.setText(title);
        scopeColumn.setWidth(width);
        if (StringUtils.isEmpty(tooltip)) {
            scopeColumn.setToolTipText(title);
        } else {
            scopeColumn.setToolTipText(tooltip);
        }
        scopeColumn.addSelectionListener(new AccountSortSelectionAdapter(this, scopeColumn, index));
        scopeColumn.pack();
    }

    private void makeActions() {
        editAction = new ConfigurationAction();
        editAction.setText(Messages.AccountView_23);
        editAction.setToolTipText(Messages.AccountView_24);
        editAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EDIT));

        searchAction = new Action() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.jface.action.Action#run()
             */
            @Override
            public void run() {
                findAccounts();
            }
        };
        searchAction.setText(Messages.AccountView_26);
        searchAction.setToolTipText(Messages.AccountView_27);
        searchAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.SEARCH));

        runEditAction = new Action() {
            @Override
            public void run() {
                if (getViewer().getSelection() instanceof IStructuredSelection
                        && ((IStructuredSelection) getViewer().getSelection())
                                .getFirstElement() instanceof Configuration) {
                    try {
                        Configuration account = (Configuration) ((IStructuredSelection) getViewer()
                                .getSelection()).getFirstElement();
                        if (AccountLoader.isEditAllowed(account)) {
                            account = getAccountService().getAccountById(account.getDbId());
                            editAction.setConfiguration(account);
                            editAction.run();
                            findAccounts();
                        }
                    } catch (Exception t) {
                        LOG.error("Error while opening control.", t); //$NON-NLS-1$
                    }
                }
            }
        };
        runEditAction.setText(Messages.AccountView_28);
        runEditAction.setToolTipText(Messages.AccountView_29);
        runEditAction
                .setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EDIT));
        runEditAction.setEnabled(false);

        removeAction = new Action() {
            @Override
            public void run() {
                if (getViewer().getSelection() instanceof IStructuredSelection
                        && ((IStructuredSelection) getViewer().getSelection())
                                .getFirstElement() instanceof Configuration) {
                    try {
                        Configuration account = (Configuration) ((IStructuredSelection) getViewer()
                                .getSelection()).getFirstElement();
                        if (AccountLoader.isEditAllowed(account)) {
                            deactivateAccount(account);
                            findAccounts();
                        }
                    } catch (Exception t) {
                        LOG.error("Error while opening control.", t); //$NON-NLS-1$
                    }
                }
            }
        };
        removeAction.setText(Messages.AccountView_30);
        removeAction.setToolTipText(Messages.AccountView_31);
        removeAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.USER_DISABLED));
        removeAction.setEnabled(false);

        createAction = new Action() {
            @Override
            public void run() {
                Configuration account = Configuration.createDefaultAccount();
                editAction.setConfiguration(account);
                editAction.run();
                findAccounts();
            }
        };
        createAction.setText("New...");
        createAction.setToolTipText("New Account...");
        createAction.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.USER_ADD));

        getViewer().addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                Configuration account = (Configuration) ((IStructuredSelection) event
                        .getSelection()).getFirstElement();
                if (AccountLoader.isEditAllowed(account)) {
                    runEditAction.run();
                }
            }
        });

        textLogin.addKeyListener(
                new EnterKeylistener(textLogin, IAccountSearchParameter.LOGIN, searchAction));
        textFirstName.addKeyListener(new EnterKeylistener(textFirstName,
                IAccountSearchParameter.FIRST_NAME, searchAction));
        textFamilyName.addKeyListener(new EnterKeylistener(textFamilyName,
                IAccountSearchParameter.FAMILY_NAME, searchAction));
    }

    private void hookPageSelection() {
        selectionListener = new ISelectionListener() {
            @Override
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                pageSelectionChanged(selection);
            }
        };
        getSite().getPage().addPostSelectionListener(selectionListener);
        getSite().setSelectionProvider(viewer);
    }

    protected void pageSelectionChanged(ISelection selection) {
        if (!(selection instanceof IStructuredSelection)) {
            return;
        }
        if (((IStructuredSelection) selection).size() != 1) {
            runEditAction.setEnabled(false);
            removeAction.setEnabled(false);
            return;
        }
        Object element = ((IStructuredSelection) selection).getFirstElement();
        if (element instanceof Configuration
                && AccountLoader.isEditAllowed((Configuration) element)) {
            runEditAction.setEnabled(true);
            removeAction.setEnabled(true);
        } else {
            runEditAction.setEnabled(false);
            removeAction.setEnabled(false);
        }
    }

    protected void findAccounts() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("findAccounts called..."); //$NON-NLS-1$
        }
        final List<Configuration> accountList = getAccountService().findAccounts(parameter);
        getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                viewer.setInput(accountList);
            }
        });
    }

    protected void deactivateAccount(Configuration account) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAccount called..."); //$NON-NLS-1$
        }
        if (MessageDialog.openQuestion(getDisplay().getActiveShell(), Messages.AccountView_32,
                NLS.bind(Messages.AccountView_33, account.getUser()))) {
            getAccountService().deactivate(account);
        }
    }

    private List<CnATreeElement> loadEntitiesByTypeId(String typeId) throws CommandException {
        LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(typeId);
        return getCommandService().executeCommand(command).getElements();
    }

    private void loadScopes() throws CommandException {
        comboModel.clear();
        comboModel.addAll(loadEntitiesByTypeId(Organization.TYPE_ID));
        comboModel.addAll(loadEntitiesByTypeId(ITVerbund.TYPE_ID_HIBERNATE));
        comboModel.addAll(loadEntitiesByTypeId(ItNetwork.TYPE_ID));
        comboModel.sort();
        comboModel.addNoSelectionObject(Messages.AccountView_34);
        getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                comboOrganization.setItems(comboModel.getLabelArray());
                comboOrganization.select(0);
                comboModel.setSelectedIndex(comboOrganization.getSelectionIndex());
            }
        });
    }

    protected void startInitDataJob() {
        if (CnAElementFactory.isIsoModelLoaded()) {
            JobScheduler.scheduleInitJob(initDataJob);
        } else if (modelLoadListener == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No model loaded, adding model load listener."); //$NON-NLS-1$
            }
            createModelLoadListener();
        }
    }

    protected void initCombos() {
        getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                initYesNoCombo(comboAdmin);
                initYesNoCombo(comboLocalAdmin);
                initYesNoCombo(comboScopeOnly);
            }
        });
    }

    private void initYesNoCombo(Combo combo) {
        combo.add(Messages.AccountView_35, COMBO_INDEX_BOTH);
        combo.add(Messages.AccountView_36, COMBO_INDEX_YES);
        combo.add(Messages.AccountView_37, COMBO_INDEX_NO);
        combo.select(0);
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
                JobScheduler.scheduleInitJob(initDataJob);
                CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
            }

            @Override
            public void loaded(BpModel model) {
                // nothing to do
            }

            @Override
            public void loaded(CatalogModel model) {
                // nothing to do
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

    private String getInput(final Text textFirstName) {
        String input = textFirstName.getText();
        if (input != null) {
            input = input.trim();
            if (input.isEmpty()) {
                input = null;
            }
        }
        return input;
    }

    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.createAction);
        manager.add(this.removeAction);
        manager.add(this.runEditAction);
        manager.add(this.searchAction);
    }

    private static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
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

    public TableViewer getViewer() {
        return this.viewer;
    }

    public AccountTableSorter getTableSorter() {
        return tableSorter;
    }

    public void setTableSorter(AccountTableSorter tableSorter) {
        this.tableSorter = tableSorter;
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
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
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
        getSite().getPage().removePostSelectionListener(selectionListener);
        super.dispose();
    }

    class EnterKeylistener implements KeyListener {

        private Text textField;
        private Action enterAction;
        private String parameter;

        public EnterKeylistener(Text textField, String parameter, Action enterAction) {
            super();
            this.textField = textField;
            this.enterAction = enterAction;
            this.parameter = parameter;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            AccountView.this.parameter.setParameter(this.parameter, this.getInput(textField));
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if ('\r' == e.character) {
                AccountView.this.parameter.setParameter(this.parameter, this.getInput(textField));
                enterAction.run();
            }
        }

        private String getInput(final Text textFirstName) {
            String input = textFirstName.getText();
            if (input != null) {
                input = input.trim();
                if (input.isEmpty()) {
                    input = null;
                }
            }
            return input;
        }
    }

    private ILicenseManagementService getLMService() {
        return ServiceFactory.lookupLicenseManagementService();
    }
}
