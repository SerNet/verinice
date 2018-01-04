/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Sebastian Hagedorn  <sh[at]sernet[dot]de> - replaced org.eclipse.swt.widget.List with
 *                                                 org.eclipse.jface.viewers.TableViewer
 ******************************************************************************/

package sernet.verinice.rcp.accountgroup;

import static sernet.verinice.interfaces.IRightsService.STANDARD_GROUPS;

import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.Arrays;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.helper.UpdateConfigurationHelper;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.account.AccountWizard;
import sernet.verinice.service.account.AccountLoader;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class AccountGroupView extends RightsEnabledView
        implements SelectionListener, KeyListener, MouseListener, IModelLoadListener {

    private static final Logger LOG = Logger.getLogger(AccountGroupView.class);  
    private static final Collator COLLATOR = Collator.getInstance();
    
    public static final String ID = "sernet.verinice.rcp.accountgroup.GroupView";
    
    IAccountGroupViewDataService accountGroupDataService;
    IAccountService accountService;
    
    private String[] accountGroups;   
    private Set<String> accountsInGroup = new TreeSet<>(COLLATOR);
    private String[] accounts;
    
    private IModelLoadListener modelLoadListener;
    private AccountLabelProvider accountLabelProvider;
    private AccountLabelProvider groupLabelProvider;
    
    // SWT & JFace
    static final Point ADD_REMOVE_BUTTON_SIZE = new Point(30, 30);
    static final Point MARGINS = new Point(5, 5);
    static final Point SPACING = new Point(5, 5);
    static final int GRID_COLUMNS = 4;
    
    private static final String LEFTWARDS_ARROW = "←";
    private static final String RIGHTWARDS_ARROW = "→";
    private static final String LEFTWARDS_PAIRED_ARROWS = "⇇";
    private static final String RIGHTWARDS_PAIRED_ARROWS = "⇉";
    
    private static final String EMPTY_STRING = "";

    Composite parent;
    private Composite container;
    private Text accountGroupsFilter;
    private TableViewer tableAccountGroups;
    private TableViewer tableAccountsInGroup;
    private TableViewer tableAccounts;
    private Button addButton;
    private Button addAllButton;
    private Button removeButton;
    private Button removeAllButton;
    private Button editAccountInAccountsListButton;
    private Button editAccountInAccountsInGroupListButton;
    private Action newGroup;
    private Action deleteGroup;
    private Action editGroup;
    
    public AccountGroupView() {
        super();
        if (CnAElementFactory.isIsoModelLoaded()) {
            initDataService();
        } else if (modelLoadListener == null) {
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

                initDataService();
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

    @Override
    public void createPartControl(Composite parent) {

        super.createPartControl(parent);
        this.parent = parent;
        this.accountService = ServiceFactory.lookupAccountService();
        setupView();
        makeActions();
        fillLocalToolBar();
    }

    private void setupView() {

        container = new Composite(parent, SWT.FILL);
        createAccountGroupsColumn();
        createAccountsInGroupColumn();
        createButtonsColumn();
        createAccountsColumn();
        GridLayoutFactory.fillDefaults().numColumns(GRID_COLUMNS).margins(MARGINS).spacing(SPACING)
                .generateLayout(container);

        switchButtons(false);
        updateAllLists();
    }

    private void createAccountGroupsColumn() {

        Group accountGroupsColumn = new Group(container, SWT.NONE);
        accountGroupsColumn.setText(Messages.GroupView_2);

        accountGroupsFilter = new Text(accountGroupsColumn, SWT.BORDER);
        accountGroupsFilter.addKeyListener(this);

        tableAccountGroups = new TableViewer(accountGroupsColumn, SWT.SINGLE | SWT.BORDER);
        tableAccountGroups.setUseHashlookup(true);
        tableAccountGroups.setContentProvider(new AccountContentProvider(tableAccountGroups));
        tableAccountGroups.setLabelProvider(new AccountLabelProvider());
        tableAccountGroups.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {

                SelectionEventHandler handler = new SelectionEventHandler(event);
                handler.handleSelection(event);
            }
        });
        tableAccountGroups.setInput(new PlaceHolder(Messages.GroupView_41));
        tableAccountGroups.refresh(true);

        GridLayoutFactory.fillDefaults().margins(MARGINS).spacing(SPACING)
                .generateLayout(accountGroupsColumn);
    }

    private void createAccountsInGroupColumn() {

        Group accountsInGroupColumn = new Group(container, SWT.NONE);
        accountsInGroupColumn.setText(Messages.GroupView_3);

        tableAccountsInGroup = new TableViewer(accountsInGroupColumn);
        tableAccountsInGroup.setContentProvider(new AccountContentProvider(tableAccountsInGroup));
        groupLabelProvider = new AccountLabelProvider();
        tableAccountsInGroup.setLabelProvider(groupLabelProvider);
        tableAccountsInGroup.setComparator(new AccountComparator());
        tableAccountsInGroup.refresh(true);

        editAccountInAccountsInGroupListButton = new Button(accountsInGroupColumn, SWT.PUSH);
        editAccountInAccountsInGroupListButton.setText(Messages.GroupView_9);
        editAccountInAccountsInGroupListButton.addSelectionListener(this);

        GridLayoutFactory.fillDefaults().margins(MARGINS).spacing(SPACING)
                .generateLayout(accountsInGroupColumn);
    }

    private void createButtonsColumn() {

        Composite buttonsColumn = new Composite(container, SWT.NONE);

        addButton = new Button(buttonsColumn, SWT.PUSH);
        addButton.setText(LEFTWARDS_ARROW);
        addButton.setToolTipText(Messages.GroupView_5);
        addButton.addSelectionListener(this);
        GridDataFactory.fillDefaults().hint(ADD_REMOVE_BUTTON_SIZE).applyTo(addButton);

        removeButton = new Button(buttonsColumn, SWT.PUSH);
        removeButton.setText(RIGHTWARDS_ARROW);
        removeButton.setToolTipText(Messages.GroupView_7);
        removeButton.addSelectionListener(this);
        GridDataFactory.fillDefaults().hint(ADD_REMOVE_BUTTON_SIZE).applyTo(removeButton);

        addAllButton = new Button(buttonsColumn, SWT.PUSH);
        addAllButton.setText(LEFTWARDS_PAIRED_ARROWS);
        addAllButton.setToolTipText(Messages.GroupView_6);
        addAllButton.addSelectionListener(this);
        GridDataFactory.fillDefaults().hint(ADD_REMOVE_BUTTON_SIZE).applyTo(addAllButton);

        removeAllButton = new Button(buttonsColumn, SWT.PUSH);
        removeAllButton.setText(RIGHTWARDS_PAIRED_ARROWS);
        removeAllButton.setToolTipText(Messages.GroupView_8);
        removeAllButton.addSelectionListener(this);
        GridDataFactory.fillDefaults().hint(ADD_REMOVE_BUTTON_SIZE).applyTo(removeAllButton);

        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(buttonsColumn);
        GridLayoutFactory.fillDefaults().margins(MARGINS).spacing(SPACING)
                .generateLayout(buttonsColumn);
    }
    
    private void createAccountsColumn() {

        Group accountsColumn = new Group(container, SWT.NONE);
        accountsColumn.setText(Messages.GroupView_4);

        tableAccounts = new TableViewer(accountsColumn);
        tableAccounts.setUseHashlookup(true);
        tableAccounts.setContentProvider(new AccountContentProvider(tableAccounts));
        accountLabelProvider = new AccountLabelProvider();
        tableAccounts.setLabelProvider(accountLabelProvider);
        tableAccounts.setComparator(new AccountComparator());
        tableAccounts.refresh(true);

        editAccountInAccountsListButton = new Button(accountsColumn, SWT.PUSH);
        editAccountInAccountsListButton.setText(Messages.GroupView_9);
        editAccountInAccountsListButton.addSelectionListener(this);

        GridLayoutFactory.fillDefaults().margins(MARGINS).spacing(SPACING)
                .generateLayout(accountsColumn);
    }

    private void makeActions() {

        newGroup = new NewGroupAction();
        newGroup.setText(Messages.GroupView_10);
        newGroup.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.GROUP_ADD));

        deleteGroup = new DeleteGroupAction(this);
        deleteGroup.setText(Messages.GroupView_11);
        deleteGroup.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.GROUP_DEL));

        editGroup = new EditGroupAction();
        editGroup.setText(Messages.GroupView_12);
        editGroup.setImageDescriptor(
                ImageCache.getInstance().getImageDescriptor(ImageCache.GROUP_EDIT));
    }

    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();

        if (newGroup != null) {
            manager.add(newGroup);
        }
        if (editGroup != null) {
            manager.add(editGroup);
        }
        if (deleteGroup != null) {
            manager.add(deleteGroup);
        }
    }

    @Override
    public void setFocus() {
        if (container != null) {
            container.setFocus();
        }
    }

    @Override
    public void widgetSelected(final SelectionEvent event) {
        WorkspaceJob updateGroups = new UpdateGroupsJob(Messages.GroupView_13, event);
        JobScheduler.scheduleInitJob(updateGroups);
    }

    private void removeAccounts(String[] accounts) {

        String[] items = accountGroupDataService.deleteAccountGroupData(getSelectedGroup(),
                accounts);
        for (String i : items) {
            accountsInGroup.remove(i);
        }
    }

    private void removeAllAccounts(String[] items) {

        int result = new MessageDialog(parent.getShell(), Messages.GroupView_38, null,
                Messages.GroupView_36, MessageDialog.QUESTION,
                new String[] { Messages.GroupView_37, Messages.GroupView_27 }, 0).open();
        if (result == 0) {
            removeAccounts(items);
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
        // do nothing
    }

    private final class SelectionEventHandler implements Runnable {

        private final EventObject event;

        private SelectionEventHandler(SelectionEvent event) {
            this.event = event;
        }

        private SelectionEventHandler(SelectionChangedEvent event) {
            this.event = event;
        }

        @Override
        public void run() {

            try {
                switchButtons(false);
                handleSelection(event);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                LOG.error(String.format("problems with updating group view: %s",
                        ex.getLocalizedMessage()), ex);
            } finally {
                switchButtons(true);
            }
        }

        private void handleSelection(final EventObject event) {

            if (isGroupSelected()) {
                switchButtons(true);
                if (event.getSource() == tableAccountGroups) {
                    String[] accountNames = accountGroupDataService
                            .getAccountNamesForGroup(getSelectedGroup());
                    accountsInGroup.clear();
                    accountsInGroup.addAll(Arrays.asList(accountNames));
                } else if (event.getSource() == tableAccountsInGroup) {
                    tableAccounts.setSelection(new StructuredSelection());
                } else if (event.getSource() == tableAccounts) {
                    tableAccountsInGroup.setSelection(new StructuredSelection());
                } else if (event.getSource() == addButton) {
                    IStructuredSelection structuredSelection = (IStructuredSelection) tableAccounts
                            .getSelection();
                    addAccounts(Arrays.copyOf(structuredSelection.toArray(),
                            structuredSelection.toArray().length, String[].class));
                } else if (event.getSource() == addAllButton) {
                    addAllAccounts(accounts);
                } else if (event.getSource() == removeButton) {
                    IStructuredSelection selection = (IStructuredSelection) tableAccountsInGroup
                            .getSelection();
                    removeAccounts(Arrays.copyOf(selection.toArray(), selection.toArray().length,
                            String[].class));
                } else if (event.getSource() == removeAllButton) {
                    Set<?> set = (Set<?>) tableAccountsInGroup.getInput();
                    removeAllAccounts(Arrays.copyOf(set.toArray(), set.size(), String[].class));
                }
                updateAllLists();
            } else {
                switchButtons(false);
            }

            if (event.getSource() == editAccountInAccountsListButton
                    || event.getSource() == editAccountInAccountsInGroupListButton) {
                initAccountWizard(event);
            }
        }

        private void addAllAccounts(String[] items) {

            int result = new MessageDialog(parent.getShell(), Messages.GroupView_33, null,
                    Messages.GroupView_34, MessageDialog.QUESTION,
                    new String[] { Messages.GroupView_32, Messages.GroupView_27 }, 0).open();
            if (result == 0) {
                addAccounts(items);
            }
        }

        private void addAccounts(String[] selectedAccounts) {

            String[] accountGroupData = accountGroupDataService
                    .saveAccountGroupData(getSelectedGroup(), selectedAccounts);
            for (String account : accountGroupData) {
                if (!accountsInGroup.contains(account)) {
                    accountsInGroup.add(account);
                }
            }
        }
    }

    private final class UpdateGroupsJob extends WorkspaceJob {

        private final SelectionEvent event;

        private UpdateGroupsJob(String name, SelectionEvent event) {
            super(name);
            this.event = event;
        }

        @Override
        public IStatus runInWorkspace(final IProgressMonitor monitor) {

            IStatus status = Status.OK_STATUS;
            try {

                monitor.beginTask(Messages.GroupView_13, IProgressMonitor.UNKNOWN);
                Activator.inheritVeriniceContextState();

                updateGroups(event);

            } catch (Exception e) {
                LOG.error(Messages.GroupView_1, e);
                status = new Status(IStatus.ERROR, "sernet.gs.ui.rcp.main", Messages.GroupView_1,
                        e);
            } finally {
                monitor.done();
            }
            return status;
        }

        private void updateGroups(final SelectionEvent event) {

            getDisplay().syncExec(new SelectionEventHandler(event));
        }
    }

    void switchButtons(boolean enabled) {
        addButton.setEnabled(enabled);
        addAllButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
        removeAllButton.setEnabled(enabled);
        editAccountInAccountsListButton.setEnabled(enabled);
    }
    
    private void updateAllLists() {

        try {
            if (accountGroupDataService != null) {
                updateGroupList();
                updateGroupToAccountList();
                updateAccountList();
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Dataservice not Initialized, cannot update lists");
                }
            }
            tableAccounts.refresh(true);
            tableAccountGroups.refresh(true);
            tableAccountsInGroup.refresh(true);
        } catch (Exception e) {
            LOG.error("Error while setting data to ui widgets", e);
        }
    }

    private void updateAccountList() {

        if (accountGroupDataService != null && accountGroupDataService.getAllAccounts() != null) {
            accounts = accountGroupDataService.getAllAccounts();
            // remove accounts that are enlisted in tableToGroupAccounts
            for (String account : accountsInGroup) {
                if (ArrayUtils.contains(accounts, account)) {
                    accounts = (String[]) ArrayUtils.remove(accounts,
                            ArrayUtils.indexOf(accounts, account));
                }
            }
            Arrays.sort(accounts, COLLATOR);
            tableAccounts.setInput(accounts);
        }
    }

    private void updateGroupToAccountList() {
        if (isGroupSelected()) {
            accountsInGroup.clear();
            String group = getSelectedGroup();
            String[] names = accountGroupDataService.getAccountNamesForGroup(group);
            accountsInGroup.addAll(Arrays.asList(names));                  
            tableAccountsInGroup.setInput(accountsInGroup);
        }
    }

    private void updateGroupList() {

        String text = accountGroupsFilter.getText();
        if (accountGroupDataService != null && accountGroupDataService.getAccountGroups() != null) {
            applyFilterToAccountGroups(text);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Dataservice not Initialized, cannot update group list");
            }
        }
    }

    private void applyFilterToAccountGroups(String text) {
        String[] allAccountGroups;
        boolean isLocalAdmin = getAuthService().currentUserHasRole(new String[] { ApplicationRoles.ROLE_LOCAL_ADMIN });
        if (isLocalAdmin) {
            List<String> groupNamesForLocalAdmin = AccountLoader.loadGroupNamesForLocalAdmin();
            allAccountGroups = groupNamesForLocalAdmin.toArray(new String[groupNamesForLocalAdmin.size()]);
        } else {
            allAccountGroups = accountGroupDataService.getAccountGroups();
        }

        if (text == null || text.isEmpty()) {
            accountGroups = allAccountGroups;
        } else {
            List<String> filteredList = new LinkedList<>();
            for (String group : allAccountGroups) {
                if (group.toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(group);
                }
            }
            accountGroups = filteredList.toArray(new String[filteredList.size()]);
        }
        Arrays.sort(accountGroups, COLLATOR);
        tableAccountGroups.setInput(accountGroups);
    }

    private class NewGroupAction extends Action {

        @Override
        public void run() {

            NewGroupDialog newGroupDialog = new NewGroupDialog(AccountGroupView.this,
                    parent.getShell(), Messages.GroupView_18);
            newGroupDialog.open();
        }
    }

    private class EditGroupAction extends Action {

        @Override
        public void run() {

            EditGroupDialog dialog = new EditGroupDialog(AccountGroupView.this, parent.getShell(),
                    Messages.GroupView_19);
            dialog.open();
        }
    }

    String getSelectedGroup() {
        return (String)((StructuredSelection)tableAccountGroups.getSelection()).toList().get(0);
    }

    boolean isGroupSelected() {
        return !((StructuredSelection)tableAccountGroups.getSelection()).toList().isEmpty();
    }
    
    String[] getAllGroupsFromTable() {

        return (String[]) tableAccountGroups.getInput();
    }

    @Override
    public void keyReleased(KeyEvent event) {
        updateGroupList();
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

    static Display getDisplay() {
        Display display = Display.getCurrent();
        // may be null if outside the UI thread
        if (display == null) {
            display = Display.getDefault();
        }
        return display;
    }

    private class UpdateConfigurationCallbackHelper extends UpdateConfigurationHelper {

        public UpdateConfigurationCallbackHelper(Configuration configuration) {

            super(configuration);
        }

        @Override
        public void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {

            super.run(monitor);
            refreshView();
        }
    }

    void openStandardGroupWarningDialog(final String message) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            MessageDialog.openWarning(parent.getShell(), Messages.GroupView_23,
                                    message);
                        } catch (Exception ex) {
                            LOG.warn("error while deleting group", ex);
                        }
                    }
                });

            }
        }).start();
    }

    boolean isStandardGroup() {
        return ArrayUtils.contains(STANDARD_GROUPS, getSelectedGroup());
    }
    
    private void initAccountWizard(EventObject event) {

        String selectedAccountName = EMPTY_STRING;
        if (event.getSource() == editAccountInAccountsListButton) {
            selectedAccountName = getAccountSelectedInAccountsList();
        } else if (event.getSource() == editAccountInAccountsInGroupListButton) {
            selectedAccountName = getAccountSelectedInAccountsInGroupList();
        }

        if (!selectedAccountName.isEmpty()) {

            Configuration configuration = accountService.getAccountByName(selectedAccountName);
            WizardDialog accountDialog = createWizard(configuration);

            if (accountDialog.open() != Window.OK) {
                return;
            }
            try {
                PlatformUI.getWorkbench().getProgressService()
                        .busyCursorWhile(new UpdateConfigurationCallbackHelper(configuration));
            } catch (Exception e) {
                LOG.error(Messages.GroupView_15, e);
            }

        } else {
            MessageDialog.openWarning(parent.getShell(), Messages.GroupView_16,
                    Messages.GroupView_17);
        }
    }

    private static WizardDialog createWizard(Configuration configuration) {

        AccountWizard wizard = new AccountWizard(configuration);
        return new WizardDialog(getDisplay().getActiveShell(), wizard);
    }

    private String getAccountSelectedInAccountsList() {

        IStructuredSelection selection;

        if (tableAccounts.getSelection() != null) {
            selection = (IStructuredSelection) tableAccounts.getSelection();
            if (!selection.isEmpty()) {
                return (String) selection.getFirstElement();
            }
        }
        return EMPTY_STRING;
    }

    private String getAccountSelectedInAccountsInGroupList() {

        IStructuredSelection selection;

        if (tableAccountsInGroup.getSelection() != null) {
            selection = (IStructuredSelection) tableAccountsInGroup.getSelection();
            if (!selection.isEmpty()) {
                return (String) selection.getFirstElement();
            }
        }

        return EMPTY_STRING;
    }

    @Override
    public void mouseDoubleClick(MouseEvent event) {

        if (event.getSource() == tableAccountGroups) {
            EditGroupDialog dialog = new EditGroupDialog(this, parent.getShell(),
                    Messages.GroupView_19);
            dialog.open();
        }

        if (event.getSource() == tableAccounts || event.getSource() == tableAccountsInGroup) {
            initAccountWizard(event);
        }
    }
    
    @Override
    public void mouseDown(MouseEvent event) {
        // do nothing
    }

    @Override
    public void mouseUp(MouseEvent event) {
        // do nothing
    }

    @Override
    public void keyPressed(KeyEvent event) {
        // do nothing
    }

    private void initDataService() {

        if (accountGroupDataService == null && !Activator.getDefault().isStandalone()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Initializing DataService");
            }
            accountGroupDataService = new AccountGroupDataService(this);
        }
    }

    void passServiceToLabelProvider() {

        if (accountGroupDataService != null) {
            if (accountLabelProvider != null) {
                accountLabelProvider.setDataService(accountGroupDataService);
            }
            if (groupLabelProvider != null) {
                groupLabelProvider.setDataService(accountGroupDataService);
            }
        }
    }
    
    @Override
    public void loaded(BSIModel model) {
        //do nothing
    }

    @Override
    public void loaded(ISO27KModel model) {
        initDataService();
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.common.model.IModelLoadListener#loaded(sernet.verinice.model.bp.elements.BpModel)
     */
    @Override
    public void loaded(BpModel model) {
        // do nothing
    }

    @Override
    public void closed(BSIModel model) {
        // do nothing
    }
        
    void refreshView() {

        updateAllLists();
    }
   
    private class AccountComparator extends ViewerComparator {
        
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {

            String t1 = accountGroupDataService.getPrettyPrintAccountName((String) e1);
            String t2 = accountGroupDataService.getPrettyPrintAccountName((String) e2);
            if (t1 != null && t2 != null) {
                t1 = t1.toLowerCase();
                t2 = t2.toLowerCase();
                return t1.compareTo(t2);
            }
            return 0;
        }
    }

    public void setActionsEnabled(boolean enabled) {
        newGroup.setEnabled(enabled);
        deleteGroup.setEnabled(enabled);
        editGroup.setEnabled(enabled);
    }

    private IAuthService getAuthService() {
        return ServiceFactory.lookupAuthService();
    }

    @Override
    public void loaded(CatalogModel model) {
        // nothing to do
    }

}
