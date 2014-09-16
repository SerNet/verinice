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

import java.sql.BatchUpdateException;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.hibernate3.HibernateJdbcException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IAccountService;
import static sernet.verinice.interfaces.IRightsService.STANDARD_GROUPS;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.rcp.IllegalSelectionException;
import sernet.verinice.rcp.RightsEnabledView;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class GroupView extends RightsEnabledView implements SelectionListener, KeyListener {

    public static final String ID = "sernet.verinice.rcp.accountgroup.GroupView";

    private final static Logger LOG = Logger.getLogger(GroupView.class);

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

        WorkspaceJob test = new WorkspaceJob("load groups") {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask("load groups", IProgressMonitor.UNKNOWN);
                    Activator.inheritVeriniceContextState();

                    accountGroupDataService = new AccountGroupDataService();

                    getDisplay().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            groupList.setItems(accountGroupDataService.getAccountGroups());

                            if (isGroupSelected())
                                groupToAccountList.setItems(accountGroupDataService.getAccountNamesForGroup(getSelectedGroup()));

                            accountList.setItems(accountGroupDataService.getAllAccounts());
                        }
                    });

                } catch (Exception e) {
                    LOG.error("Error while loading data.", e);
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data.", e);
                } finally {
                    monitor.done();
                }
                return status;
            }
        };

        JobScheduler.scheduleInitJob(test);
    }

    private void setupView() {
        groupViewComposite = new Composite(parent, SWT.FILL);
        GridLayout gridLayout = new GridLayout();
        groupViewComposite.setLayout(gridLayout);
        gridLayout.numColumns = 4;
        groupViewComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label groupLabel = new Label(groupViewComposite, SWT.NULL);
        groupLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ((GridData) groupLabel.getLayoutData()).horizontalSpan = 4;
        groupLabel.setText(Messages.GroupView_3);

        quickFilter = new Text(groupViewComposite, SWT.SINGLE | SWT.BORDER);
        GridData fastFilterGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        quickFilter.setLayoutData(fastFilterGridData);
        quickFilter.addKeyListener(this);

        Label accountsInGroup = new Label(groupViewComposite, SWT.NULL);
        accountsInGroup.setLayoutData(new GridData());
        ((GridData) accountsInGroup.getLayoutData()).horizontalSpan = 2;
        accountsInGroup.setText("Accounts in Group");

        Label accounts = new Label(groupViewComposite, SWT.NULL);
        accounts.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        accounts.setText("Accounts");

        groupList = new List(groupViewComposite, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        GridData groupListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        Rectangle trim = groupList.computeTrim(0, 0, 0, groupList.getItemHeight() * 12);
        groupListGridData.heightHint = trim.height;
        groupList.setLayoutData(groupListGridData);
        groupList.addSelectionListener(this);

        groupToAccountList = new List(groupViewComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        groupToAccountList.setItems(new String[] {});
        GridData groupAccountListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        groupListGridData.heightHint = groupToAccountList.computeTrim(0, 0, 0, groupList.getItemHeight() * 12).height;
        groupToAccountList.setLayoutData(groupAccountListGridData);
        groupToAccountList.addSelectionListener(this);

        Group connectGroupsWithAccounts = new Group(groupViewComposite, SWT.NULL);
        connectGroupsWithAccounts.setLayout(new GridLayout());
        ((GridLayout) connectGroupsWithAccounts.getLayout()).numColumns = 1;
        connectGroupsWithAccounts.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        addBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        addBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        addBtn.setText(Messages.GroupView_4);
        addBtn.addSelectionListener(this);

        addAllBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        addAllBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        addAllBtn.setText(Messages.GroupView_5);
        addAllBtn.addSelectionListener(this);

        removeBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        removeBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        removeBtn.setText(Messages.GroupView_6);
        removeBtn.addSelectionListener(this);

        removeAllBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        removeAllBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        removeAllBtn.setText(Messages.GroupView_7);
        removeAllBtn.addSelectionListener(this);

        editAccountBtn = new Button(connectGroupsWithAccounts, SWT.END);
        editAccountBtn.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
        editAccountBtn.setText(Messages.GroupView_8);
        editAccountBtn.addSelectionListener(this);

        accountList = new List(groupViewComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        accountList.setItems(new String[] {});
        GridData accountListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        accountListGridData.heightHint = accountList.computeTrim(0, 0, 0, accountList.getItemHeight() * 12).height;
        accountList.setLayoutData(accountListGridData);
        accountList.addSelectionListener(this);
    }

    private void makeActions() {
        newGroup = new NewGroupAction();
        newGroup.setText(Messages.GroupView_0);
        newGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));

        deleteGroup = new DeleteGroupAction();
        deleteGroup.setText(Messages.GroupView_1);
        deleteGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.DELETE));

        editGroup = new EditGroupAction();
        editGroup.setText(Messages.GroupView_2);
        editGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.SAVE));
    }

    private void fillLocalToolBar() {

        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();

        if (newGroup != null)
            manager.add(newGroup);
        if (editGroup != null)
            manager.add(editGroup);
        if (deleteGroup != null)
            manager.add(deleteGroup);
    }

    @Override
    public void setFocus() {
        if (groupViewComposite != null)
            groupViewComposite.setFocus();
    }

    @Override
    public void widgetSelected(final SelectionEvent e) {

        WorkspaceJob updateGroups = new WorkspaceJob("update groups") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {

                    monitor.beginTask("update groups", IProgressMonitor.UNKNOWN);

                    Activator.inheritVeriniceContextState();

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

                                    else if (e.getSource() == addBtn) {
                                        addAccounts(accountList.getSelection());
                                    }

                                    else if (e.getSource() == addAllBtn) {
                                        addAccounts(accountList.getItems());
                                    }

                                    else if (e.getSource() == removeBtn) {
                                        removeAccounts(groupToAccountList.getSelection());
                                    }

                                    else if (e.getSource() == removeAllBtn) {
                                        removeAccounts(groupToAccountList.getItems());
                                    }
                                }
                            } catch (Exception ex) {

                            } finally {
                                switchButtons(true);
                            }
                        }

                        private void switchButtons(boolean enabled) {
                            addBtn.setEnabled(enabled);
                            addAllBtn.setEnabled(enabled);
                            removeBtn.setEnabled(enabled);
                            removeAllBtn.setEnabled(enabled);
                        }
                    });

                } catch (Exception e) {
                    LOG.error("Error while loading data.", e);
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data.", e);
                } finally {
                    monitor.done();
                }
                return status;
            }
        };

        JobScheduler.scheduleInitJob(updateGroups);

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

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {

    }

    private class NewGroupAction extends Action {

        @Override
        public void run() {
            NewGroupDialog newGroupDialog = new NewGroupDialog(parent.getShell(), "create new group");
            newGroupDialog.open();
        }
    }

    private class EditGroupAction extends Action {

        @Override
        public void run() {
            EditGroupDialog dialog = new EditGroupDialog(parent.getShell(), "edit group");
            dialog.open();
        }
    }

    private class DeleteGroupAction extends Action {

        @Override
        public void run() {
            DeleteGroupDialog deleteGroupDialog = new DeleteGroupDialog(parent.getShell(), "delete group");
            deleteGroupDialog.open();
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
                groupTextLabel.setText("Group Name");

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

        protected boolean isStandardGroup() {
            return ArrayUtils.contains(STANDARD_GROUPS, getSelectedGroup());
        }

        protected void openStandardGroupWarningDialog() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getDisplay().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                MessageDialog.openWarning(parent.getShell(), "Warning", String.format("%s is a standard group and therefore cannot be deleted or edited", getSelectedGroup()));
                            } catch (Exception ex) {
                                LOG.warn("error while deleting group", ex);
                            }
                        }
                    });

                }
            }).start();
        }
    }

    private class NewGroupDialog extends CRUDAccountGroupDialog {

        public NewGroupDialog(Shell parent, String title) {
            super(parent, title);
        }

        @Override
        protected void okPressed() {
            try {
                accountGroupDataService.addAccountGroup(textInputField.getText());
                super.okPressed();
            } catch (DataIntegrityViolationException ex) {
                MessageDialog.openError(parent.getShell(), "Error", "group/role name already exists");
            } catch (Exception ex) {
                MessageDialog.openError(parent.getShell(), "Error", ex.getLocalizedMessage());
                LOG.error("adding group failed", ex);
            }
        }

        @Override
        protected boolean isEditable() {
            return true;
        }
    }

    private class DeleteGroupDialog extends CRUDAccountGroupDialog {

        String selection;

        public DeleteGroupDialog(Shell parent, String title) {
            super(parent, String.format("%s %s", title, getSelectedGroup()));

            if (isStandardGroup()) {
                openStandardGroupWarningDialog();
                super.closeTray();
            }

            if (isGroupSelected() && isStandardGroup())
                this.selection = getSelectedGroup();
            else
                throw new IllegalSelectionException("an account group must be selected");
        }

        @Override
        protected void okPressed() {

            if (isGroupEmptyAndNotConnectedToObject())
                accountGroupDataService.deleteAccountGroup(selection);
            else
                openWarningDialog();
            super.okPressed();
        }

        private void openWarningDialog() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getDisplay().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                int connectedAccounts = accountGroupDataService.getAccountNamesForGroup(selection).length;
                                long connectedObjects = accountService.countConnectObjectsForGroup(selection);
                                String message = String.format("this group is connected with %d accounts and %d objects", connectedAccounts, connectedObjects);
                                MessageDialog dialog = new MessageDialog(parent.getShell(), "Achtung", null, message, MessageDialog.ERROR, new String[] { "ok", "cancel" }, 0);
                                int result = dialog.open();
                                if (result == 0)
                                    openSecondWarningDialog();
                                else
                                    accountGroupDataService.deleteAccountGroup(selection);
                            } catch (Exception ex) {
                                LOG.error("error while deleting group", ex);
                            }
                        }
                    });

                }
            }).start();
        }

        private void openSecondWarningDialog() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getDisplay().syncExec(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                MessageDialog dialog = new MessageDialog(parent.getShell(), "Gruppe Löschen", null, "really?", MessageDialog.ERROR, new String[] { "ok", "cancel" }, 0);
                                int result = dialog.open();
                                if (result == 0)
                                    accountGroupDataService.deleteAccountGroup(selection);
                                initData();
                            } catch (Exception ex) {
                                LOG.error("error while deleting group", ex);
                            }
                        }
                    });

                }
            }).start();
        }

        @Override
        protected boolean isEditable() {
            return false;
        }

        private boolean isGroupEmptyAndNotConnectedToObject() {
            return accountGroupDataService.getAccountNamesForGroup(selection).length == 0 && accountService.countConnectObjectsForGroup(selection) == 0;
        }
    }

    private class EditGroupDialog extends CRUDAccountGroupDialog {

        private String selection;

        public EditGroupDialog(Shell parent, String title) {

            super(parent, title);

            if (isStandardGroup()) {
                openStandardGroupWarningDialog();
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
            accountGroupDataService.editAccountGroupName(textInputField.getText(), selection);
            super.okPressed();
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
    public void keyPressed(KeyEvent e) {
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.GROUPSETTINGS;
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
}
