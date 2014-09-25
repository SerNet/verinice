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
 ******************************************************************************/
package sernet.verinice.rcp.accountgroup;

import static sernet.verinice.interfaces.IRightsService.STANDARD_GROUPS;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.springframework.dao.DataIntegrityViolationException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.helper.UpdateConfigurationHelper;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.IllegalSelectionException;
import sernet.verinice.rcp.NonModalWizardDialog;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.account.AccountWizard;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class GroupView extends RightsEnabledView implements SelectionListener, KeyListener, MouseListener {

    public static final String ID = "sernet.verinice.rcp.accountgroup.GroupView";

    private static final Logger LOG = Logger.getLogger(GroupView.class);

    private Composite parent;

    private Composite groupViewComposite;

    private Action newGroup;

    private Action deleteGroup;

    private Action editGroup;

    private List groupList;

    private List groupToAccountList;

    private List accountList;

    private Button addBtn;

    private Button addAllBtn;

    private Button removeBtn;

    private Button removeAllBtn;

    private Button editAccountBtn;

    private IAccountGroupViewDataService accountGroupDataService;

    private Text quickFilter;

    private IAccountService accountService;

    @Override
    public void createPartControl(Composite parent) {

        super.createPartControl(parent);
        this.parent = parent;
        this.accountService = ServiceFactory.lookupAccountService();

        setupView();
        makeActions();
        fillLocalToolBar();
        initData();
    }

    private void initData() {
        WorkspaceJob initDataJob = new InitDataJob(Messages.GroupView_0);
        JobScheduler.scheduleInitJob(initDataJob);
    }

    private void setupView() {

        initMainComposite();

        initLabelForAccountGroupList();

        initQuickFilter();

        initLabelsForAccountLists();

        initLists();
    }

    private void initLabelsForAccountLists() {
        Label accountsInGroup = new Label(groupViewComposite, SWT.NULL);
        accountsInGroup.setLayoutData(new GridData());
        ((GridData) accountsInGroup.getLayoutData()).horizontalSpan = 2;
        accountsInGroup.setText(Messages.GroupView_3);

        Label accounts = new Label(groupViewComposite, SWT.NULL);
        accounts.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        accounts.setText(Messages.GroupView_4);
    }

    private void initLabelForAccountGroupList() {
        Label groupLabel = new Label(groupViewComposite, SWT.NULL);
        groupLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ((GridData) groupLabel.getLayoutData()).horizontalSpan = 4;
        groupLabel.setText(Messages.GroupView_2);
    }

    private void initQuickFilter() {
        quickFilter = new Text(groupViewComposite, SWT.SINGLE | SWT.BORDER);
        GridData fastFilterGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        quickFilter.setLayoutData(fastFilterGridData);
        quickFilter.addKeyListener(this);
    }

    private void initMainComposite() {
        groupViewComposite = new Composite(parent, SWT.FILL);
        GridLayout gridLayout = new GridLayout();
        groupViewComposite.setLayout(gridLayout);
        gridLayout.numColumns = 4;
        groupViewComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private Group initButtonGroupComposite() {
        Group connectGroupsWithAccounts = new Group(groupViewComposite, SWT.NULL);
        connectGroupsWithAccounts.setLayout(new GridLayout());
        ((GridLayout) connectGroupsWithAccounts.getLayout()).numColumns = 1;
        connectGroupsWithAccounts.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        return connectGroupsWithAccounts;
    }

    private void initLists() {

        initGroupList();

        initGroupToAccountList();

        Group buttonGroupComposite = initButtonGroupComposite();
        initButtons(buttonGroupComposite);

        initAccountList();
    }

    private void initAccountList() {
        accountList = new List(groupViewComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        accountList.setItems(new String[] {});
        GridData accountListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        accountListGridData.heightHint = accountList.computeTrim(0, 0, 0, accountList.getItemHeight() * 12).height;
        accountList.setLayoutData(accountListGridData);
        accountList.addSelectionListener(this);
        accountList.addMouseListener(this);
    }

    private void initGroupToAccountList() {
        groupToAccountList = new List(groupViewComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        groupToAccountList.setItems(new String[] {});
        GridData groupAccountListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        groupAccountListGridData.heightHint = groupToAccountList.computeTrim(0, 0, 0, groupList.getItemHeight() * 12).height;
        groupToAccountList.setLayoutData(groupAccountListGridData);
        groupToAccountList.addSelectionListener(this);
        groupToAccountList.addMouseListener(this);
    }

    private GridData initGroupList() {
        groupList = new List(groupViewComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        GridData groupListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        Rectangle trim = groupList.computeTrim(0, 0, 0, groupList.getItemHeight() * 12);
        groupListGridData.heightHint = trim.height;
        groupList.setLayoutData(groupListGridData);
        groupList.addSelectionListener(this);
        groupList.addMouseListener(this);
        return groupListGridData;
    }

    private void initButtons(Group connectGroupsWithAccounts) {
        addBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        addBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        addBtn.setText(Messages.GroupView_5);
        addBtn.addSelectionListener(this);

        addAllBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        addAllBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        addAllBtn.setText(Messages.GroupView_6);
        addAllBtn.addSelectionListener(this);

        removeBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        removeBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        removeBtn.setText(Messages.GroupView_7);
        removeBtn.addSelectionListener(this);

        removeAllBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        removeAllBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        removeAllBtn.setText(Messages.GroupView_8);
        removeAllBtn.addSelectionListener(this);

        editAccountBtn = new Button(connectGroupsWithAccounts, SWT.END);
        editAccountBtn.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
        editAccountBtn.setText(Messages.GroupView_9);
        editAccountBtn.addSelectionListener(this);
    }

    private void makeActions() {
        newGroup = new NewGroupAction();
        newGroup.setText(Messages.GroupView_10);
        newGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.GROUP_ADD));

        deleteGroup = new DeleteGroupAction();
        deleteGroup.setText(Messages.GroupView_11);
        deleteGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.GROUP_DEL));

        editGroup = new EditGroupAction();
        editGroup.setText(Messages.GroupView_12);
        editGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.GROUP_EDIT));
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
        if (groupViewComposite != null) {
            groupViewComposite.setFocus();
        }
    }

    @Override
    public void widgetSelected(final SelectionEvent e) {
        WorkspaceJob updateGroups = new UpdateGroupsJob(Messages.GroupView_13, e);
        JobScheduler.scheduleInitJob(updateGroups);
    }

    private void addAllAccounts(String[] items) {
        int result = new MessageDialog(parent.getShell(), Messages.GroupView_33, null, Messages.GroupView_34, MessageDialog.QUESTION, new String[] { Messages.GroupView_32, Messages.GroupView_27 }, 0).open();
        if (result == 0) {
            addAccounts(items);
        }
    }

    private void addAccounts(String[] selectedAccounts) {
        String[] accounts = accountGroupDataService.saveAccountGroupData(getSelectedGroup(), selectedAccounts);
        for (String account : accounts) {
            if (!ArrayUtils.contains(groupToAccountList.getItems(), account)) {
                groupToAccountList.add(account);
            }
        }
    }

    private void removeAccounts(String[] accounts) {
        String[] items = accountGroupDataService.deleteAccountGroupData(getSelectedGroup(), accounts);
        for (String i : items) {
            groupToAccountList.remove(i);
        }
    }

    private void removeAllAccounts(String[] items) {
        int result = new MessageDialog(parent.getShell(), Messages.GroupView_38, null, Messages.GroupView_36, MessageDialog.QUESTION, new String[] { Messages.GroupView_37, Messages.GroupView_27 }, 0).open();
        if (result == 0) {
            removeAccounts(items);
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {

    }

    private final class UpdateGroupsJob extends WorkspaceJob {

        private final SelectionEvent e;

        private UpdateGroupsJob(String name, SelectionEvent e) {
            super(name);
            this.e = e;
        }

        @Override
        public IStatus runInWorkspace(final IProgressMonitor monitor) {
            IStatus status = Status.OK_STATUS;
            try {

                monitor.beginTask(Messages.GroupView_13, IProgressMonitor.UNKNOWN);
                Activator.inheritVeriniceContextState();

                updateGroups(e);

            } catch (Exception e) {
                LOG.error(Messages.GroupView_1, e);
                status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.GroupView_1, e);
            } finally {
                monitor.done();
            }
            return status;
        }
    }

    private void updateGroups(final SelectionEvent e) {
        getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                try {

                    switchButtons(false);

                    if (isGroupSelected()) {

                        if (e.getSource() == groupList) {
                            String[] accounts = accountGroupDataService.getAccountNamesForGroup(getSelectedGroup());
                            groupToAccountList.setItems(accounts);
                        }

                        else if (e.getSource() == groupToAccountList) {
                            accountList.deselectAll();
                        }

                        else if (e.getSource() == accountList) {
                            groupToAccountList.deselectAll();
                        }

                        else if (e.getSource() == addBtn) {
                            addAccounts(accountList.getSelection());
                        }

                        else if (e.getSource() == addAllBtn) {
                            addAllAccounts(accountList.getItems());
                        }

                        else if (e.getSource() == removeBtn) {
                            removeAccounts(groupToAccountList.getSelection());
                        }

                        else if (e.getSource() == removeAllBtn) {
                            removeAllAccounts(groupToAccountList.getItems());
                        }
                    }

                    if (e.getSource() == editAccountBtn) {
                        updateConfiguration();
                    }
                } catch (Exception ex) {

                    LOG.error(String.format("problems with updating group view: %s", ex.getLocalizedMessage()), ex);

                    if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    }
                } finally {
                    switchButtons(true);
                }
            }

        });
    }

    private void switchButtons(boolean enabled) {
        addBtn.setEnabled(enabled);
        addAllBtn.setEnabled(enabled);
        removeBtn.setEnabled(enabled);
        removeAllBtn.setEnabled(enabled);
        editAccountBtn.setEnabled(enabled);
    }

    public final class InitDataJob extends WorkspaceJob {

        public InitDataJob(String name) {
            super(name);
        }

        @Override
        public IStatus runInWorkspace(final IProgressMonitor monitor) {
            IStatus status = Status.OK_STATUS;
            try {
                monitor.beginTask(Messages.GroupView_0, IProgressMonitor.UNKNOWN);
                Activator.inheritVeriniceContextState();

                initDataService();
                updateAllLists();

            } catch (Exception e) {
                LOG.error(Messages.GroupView_1, e);
                status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.GroupView_1, e);
            } finally {
                monitor.done();
            }
            return status;
        }
    }

    private void updateAllLists() {
        getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                groupList.setItems(accountGroupDataService.getAccountGroups());

                if (isGroupSelected()) {
                    groupToAccountList.setItems(accountGroupDataService.getAccountNamesForGroup(getSelectedGroup()));
                }

                accountList.setItems(accountGroupDataService.getAllAccounts());
            }
        });
    }

    private class NewGroupAction extends Action {

        @Override
        public void run() {
            NewGroupDialog newGroupDialog = new NewGroupDialog(parent.getShell(), Messages.GroupView_18);
            newGroupDialog.open();
        }
    }

    private class EditGroupAction extends Action {

        @Override
        public void run() {
            EditGroupDialog dialog = new EditGroupDialog(parent.getShell(), Messages.GroupView_19);
            dialog.open();
        }
    }

    private class DeleteGroupAction extends Action {

        @Override
        public void run() {

            if (isStandardGroup()) {
                openStandardGroupWarningDialog(Messages.GroupView_22);
                return;
            }

            getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    try {
                        int connectedAccounts = accountGroupDataService.getAccountNamesForGroup(getSelectedGroup()).length;
                        long connectedObjects = accountService.countConnectObjectsForGroup(getSelectedGroup());
                        String message = String.format(Messages.GroupView_25, connectedAccounts, connectedObjects);
                        MessageDialog dialog = new MessageDialog(parent.getShell(), Messages.GroupView_16, null, message, MessageDialog.ERROR, new String[] { Messages.GroupView_26, Messages.GroupView_27 }, 0);
                        int result = dialog.open();
                        if (result == 0)
                            if (!isGroupEmptyAndNotConnectedToObject())
                                openSecondWarningDialog();
                            else
                                deleteGroup();
                    } catch (Exception ex) {
                        LOG.error("error while deleting group", ex);
                    }
                }
            });

        }

        private void openSecondWarningDialog() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getDisplay().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                MessageDialog dialog = new MessageDialog(parent.getShell(), Messages.GroupView_28, null, Messages.GroupView_29, MessageDialog.ERROR, new String[] { Messages.GroupView_26, Messages.GroupView_27 }, 0);
                                int result = dialog.open();
                                if (result == 0)
                                    deleteGroup();
                            } catch (Exception ex) {
                                LOG.error("error while deleting group", ex);
                            }
                        }
                    });

                }
            }).start();
        }

        private boolean isGroupEmptyAndNotConnectedToObject() {
            return hasAccounts() && hasObjects();
        }

        private boolean hasObjects() {
            return accountService.countConnectObjectsForGroup(getSelectedGroup()) == 0;
        }

        private boolean hasAccounts() {
            return accountGroupDataService.getAccountNamesForGroup(getSelectedGroup()).length == 0;
        }

        private void deleteGroup() {
            accountGroupDataService.deleteAccountGroup(getSelectedGroup());
            initData();
        }

    }

    private abstract class CRUDAccountGroupDialog extends TitleAreaDialog {

        protected Text textInputField;

        protected Composite container;

        private String title;

        public CRUDAccountGroupDialog(Shell parent, String title) {
            super(parent);
            this.title = title;
        }

        @Override
        protected Control createDialogArea(Composite parent) {

            Composite area = (Composite) super.createDialogArea(parent);
            container = new Composite(area, SWT.NONE);
            container.setLayoutData(new GridData(GridData.FILL_BOTH));
            GridLayout layout = new GridLayout(2, false);
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);

            container.setLayoutData(gridData);
            container.setLayout(layout);

            if (isEditable()) {
                GridData dataGroupName = new GridData();
                dataGroupName.grabExcessHorizontalSpace = true;

                dataGroupName.horizontalAlignment = GridData.FILL;

                Label groupTextLabel = new Label(container, SWT.NONE);
                groupTextLabel.setText(Messages.GroupView_21);

                textInputField = new Text(container, SWT.BORDER);
                textInputField.setLayoutData(dataGroupName);
            }

            return area;
        }

        protected abstract boolean isEditable();

        @Override
        public void create() {
            super.create();
            setTitle(title);
        }

        @Override
        protected boolean isResizable() {
            return true;
        }

        @Override
        protected void okPressed() {
            initData();
            super.okPressed();
        }
    }

    private class NewGroupDialog extends CRUDAccountGroupDialog {

        public NewGroupDialog(Shell parent, String title) {
            super(parent, title);
        }

        @Override
        protected void okPressed() {
            try {

                if (textInputField.getText() == null || textInputField.getText().equals("")) {
                    return;
                }

                accountGroupDataService.addAccountGroup(textInputField.getText());
                super.okPressed();
            } catch (DataIntegrityViolationException ex) {
                MessageDialog.openError(parent.getShell(), Messages.GroupView_23, Messages.GroupView_24);
            } catch (Exception ex) {
                MessageDialog.openError(parent.getShell(), Messages.GroupView_23, ex.getLocalizedMessage());
                LOG.error("adding group failed", ex);
            }
        }

        @Override
        protected boolean isEditable() {
            return true;
        }
    }

    private class EditGroupDialog extends CRUDAccountGroupDialog {

        private String selection;

        public EditGroupDialog(Shell parent, String title) {

            super(parent, title);

            if (isStandardGroup()) {
                openStandardGroupWarningDialog(Messages.GroupView_35);
                super.closeTray();
            } else if (isGroupSelected())
                this.selection = getSelectedGroup();
            else
                throw new IllegalSelectionException("an account group must be selected");
        }

        @Override
        public void create() {
            super.create();
            textInputField.setText(selection);

        }

        @Override
        protected boolean isEditable() {
            return true;
        }

        @Override
        protected void okPressed() {

            if (textInputField.getText() == null || textInputField.getText().equals("")) {
                return;
            }

            if (textInputField.getText().equals(selection)) {
                super.okPressed();
                return;
            }

            if (existsGroup()) {
                String msg = String.format(Messages.GroupView_31, textInputField.getText());
                MessageDialog messageDialog = new MessageDialog(parent.getShell(), Messages.GroupView_16, null, msg, MessageDialog.QUESTION, new String[] { Messages.GroupView_30, Messages.GroupView_27 }, 0);
                int result = messageDialog.open();
                if (result == 0) {
                    accountGroupDataService.editAccountGroupName(textInputField.getText(), selection);
                }
            } else {
                try {
                    accountGroupDataService.editAccountGroupName(textInputField.getText(), selection);
                } catch (Exception ex) {
                    MessageDialog.openError(parent.getShell(), "Error", ex.getLocalizedMessage());
                    LOG.error("editing account group name failed", ex);
                }
            }

            super.okPressed();
        }

        private boolean existsGroup() {
            return ArrayUtils.contains(groupList.getItems(), textInputField.getText());
        }

    }

    private String getSelectedGroup() {
        return groupList.getSelection()[0];
    }

    private boolean isGroupSelected() {
        return groupList.getSelectionCount() > 0;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        String text = quickFilter.getText();
        String[] allAccountGroups = accountGroupDataService.getAccountGroups();
        if (("").equals(text)) {
            groupList.setItems(allAccountGroups);
        } else {
            groupList.removeAll();
            for (String group : allAccountGroups) {
                if (group.contains(text)) {
                    groupList.add(group);
                }
            }
        }
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

    private static Display getDisplay() {
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

        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            super.run(monitor);
            initData();
        }
    }

    private void openStandardGroupWarningDialog(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getDisplay().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            MessageDialog.openWarning(parent.getShell(), Messages.GroupView_23, message);
                        } catch (Exception ex) {
                            LOG.warn("error while deleting group", ex);
                        }
                    }
                });

            }
        }).start();
    }

    private boolean isStandardGroup() {
        return ArrayUtils.contains(STANDARD_GROUPS, getSelectedGroup());
    }

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        if (e.getSource() == groupList) {
            EditGroupDialog dialog = new EditGroupDialog(parent.getShell(), Messages.GroupView_19);
            dialog.open();
        }

        if (e.getSource() == accountList || e.getSource() == groupToAccountList) {
            updateConfiguration();
        }
    }

    private void updateConfiguration() {
        String selectedAccountName = getSelectedAccount();
        if (!"".equals(selectedAccountName)) {

            Configuration configuration = accountService.getAccountByName(getSelectedAccount());
            TitleAreaDialog accountDialog = createWizard(configuration);

            if (accountDialog.open() != Window.OK) {
                return;
            }
            
            try {
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new UpdateConfigurationCallbackHelper(configuration));
            } catch (Exception e) {
                LOG.error(Messages.GroupView_15, e);
            } finally {
                configuration = null;
            }

        } else {
            MessageDialog.openWarning(parent.getShell(), Messages.GroupView_16, Messages.GroupView_17);
        }
    }

    private TitleAreaDialog createWizard(Configuration configuration) {
        AccountWizard wizard = new AccountWizard(configuration);
        return new NonModalWizardDialog(Display.getCurrent().getActiveShell(), wizard);
    }

    private String getSelectedAccount() {

        if (accountList.getSelectionCount() > 0) {
            return accountList.getSelection()[0];
        } else if (groupToAccountList.getSelectionCount() > 0) {
            return groupToAccountList.getSelection()[0];
        }

        return "";
    }

    @Override
    public void mouseDown(MouseEvent e) {
    }

    @Override
    public void mouseUp(MouseEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    private void initDataService() {
        accountGroupDataService = new AccountGroupDataService();
    }
}
