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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.helper.UpdateConfigurationHelper;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.account.AccountWizard;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class GroupView extends RightsEnabledView implements SelectionListener, KeyListener, MouseListener, IModelLoadListener {

    private static final Logger LOG = Logger.getLogger(GroupView.class);  
    private static final String EMPTY_STRING = "";
    private static final Collator COLLATOR = Collator.getInstance();
    
    public static final String ID = "sernet.verinice.rcp.accountgroup.GroupView";   
    
    IAccountGroupViewDataService accountGroupDataService;
    IAccountService accountService;
    
    private Action newGroup;
    private Action deleteGroup;
    private Action editGroup;
    
    private String[] accountGroupArray;   
    private String[] accountArray;  
    private Set<String> accountinGroupSet = new TreeSet<String>(COLLATOR);
    
    Composite parent;
    private Composite groupViewComposite;
    private TableViewer tableAccounts;
    private TableViewer tableGroupToAccounts;    
    TableViewer tableGroups;
    private Button addBtn;
    private Button addAllBtn;
    private Button removeBtn;
    private Button removeAllBtn;
    private Button editAccountBtn;
    private Text quickFilter;  

    @Override
    public void createPartControl(Composite parent) {

        super.createPartControl(parent);
        this.parent = parent;
        this.accountService = ServiceFactory.lookupAccountService();

        if (CnAElementFactory.isModelLoaded()) {
            initData();
        } else{
            CnAElementFactory.getInstance().addLoadListener((IModelLoadListener) this);
        }
        
        setupView();
        makeActions();
        fillLocalToolBar();

    }

    void initData() {
        WorkspaceJob initDataJob = new InitDataJob(Messages.GroupView_0);
        initDataJob.addJobChangeListener(new JobChangeAdapter(){
            @Override
            public void done(IJobChangeEvent event){
                long start_time = System.currentTimeMillis();
                if(event.getResult().isOK()){
                    final long MAX_DURATION = 15000; // wait max 15 secss before canceling 
                    while(tableAccounts == null && ((System.currentTimeMillis() - start_time) < MAX_DURATION )){
                        // do nothing, wait until tableAccounts is initialized
                    }
                    if(System.currentTimeMillis() - start_time > MAX_DURATION){
                        ExceptionUtil.log(new RuntimeException(Messages.GroupView_39), Messages.GroupView_40);
                    } else {
                        updateAllLists();
                    }
                }
            }
        });
        JobScheduler.scheduleInitJob(initDataJob);
    }

    private void setupView() {

        initMainComposite();

        initLabelForAccountGroupList();

        initQuickFilter();

        initLists();
    }

    private void initLabelForAccountGroupList() {
        final int horizontalLabelSpan = 4;
        Label groupLabel = new Label(groupViewComposite, SWT.NULL);
        groupLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ((GridData) groupLabel.getLayoutData()).horizontalSpan = horizontalLabelSpan;
        groupLabel.setText(Messages.GroupView_2);
    }

    private void initQuickFilter() {
        quickFilter = new Text(groupViewComposite, SWT.SINGLE | SWT.BORDER);
        GridData fastFilterGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        quickFilter.setLayoutData(fastFilterGridData);
        quickFilter.addKeyListener(this);
        
        Label placeholder = new Label(groupViewComposite, SWT.NULL);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;
        placeholder.setLayoutData(gridData);
    }

    private void initMainComposite() {

        final int gridColumns = 4;

        groupViewComposite = new Composite(parent, SWT.FILL);
        GridLayout gridLayout = new GridLayout();
        groupViewComposite.setLayout(gridLayout);
        gridLayout.numColumns = gridColumns;
        groupViewComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private Group initButtonGroupComposite() {
        final int gridColumns = 1;
        Group connectGroupsWithAccounts = new Group(groupViewComposite, SWT.NULL);
        connectGroupsWithAccounts.setLayout(new GridLayout());
        ((GridLayout) connectGroupsWithAccounts.getLayout()).numColumns = gridColumns;
        connectGroupsWithAccounts.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        return connectGroupsWithAccounts;
    }

    private void initLists() {
        
        initDataService();

        initGroupList();

        initGroupToAccountList();

        Group buttonGroupComposite = initButtonGroupComposite();
        initButtons(buttonGroupComposite);

        initAccountList();
    }

    private void initAccountList() {
        Composite leftComposite = new Composite(groupViewComposite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 40;
        leftComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout(gridLayout);        
        
        tableAccounts = createTable(leftComposite, Messages.GroupView_4);
        
        tableAccounts.setContentProvider(new ArrayContentProvider());
        tableAccounts.setLabelProvider(new AccountLabelProvider(accountGroupDataService));
        
        tableAccounts.refresh(true);
    }

    private void initGroupToAccountList() {
        Composite leftComposite = new Composite(groupViewComposite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 40;
        leftComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout(gridLayout);        
        
        tableGroupToAccounts = createTable(leftComposite, Messages.GroupView_3);
        
        tableGroupToAccounts.setContentProvider(new ArrayContentProvider());
        tableGroupToAccounts.setLabelProvider(new AccountLabelProvider(accountGroupDataService));
        
        tableGroupToAccounts.refresh(true);
        
    }

    private void initGroupList() {
        Composite leftComposite = new Composite(groupViewComposite, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 40;
        leftComposite.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        leftComposite.setLayout(gridLayout);        
        
        tableGroups = createTable(leftComposite, EMPTY_STRING);
        
        tableGroups.setContentProvider(new ArrayContentProvider());
        tableGroups.setLabelProvider(new AccountLabelProvider(null));
        
        tableGroups.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent e) {
                SelectionEventHandler h = new SelectionEventHandler(e);
                h.handleSelection(e);
            }
        });
        
        tableGroups.refresh(true);
        
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

        deleteGroup = new DeleteGroupAction(this);
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
            if(!accountinGroupSet.contains(account)){
                accountinGroupSet.add(account);
            }
        }
    }

    private void removeAccounts(String[] accounts) {
        String[] items = accountGroupDataService.deleteAccountGroupData(getSelectedGroup(), accounts);
        for (String i : items) {
            accountinGroupSet.remove(i);
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

    private final class SelectionEventHandler implements Runnable {

        private final EventObject e;
        

        private SelectionEventHandler(SelectionEvent e) {
            this.e = e;
        }
        
        private SelectionEventHandler(SelectionChangedEvent e){
            this.e = e;
        }

        @Override
        public void run() {
            try {
                switchButtons(false);
                handleSelection(e);

            } catch (RuntimeException ex) {
                throw (RuntimeException) ex;
            } catch (Exception ex) {
                LOG.error(String.format("problems with updating group view: %s", ex.getLocalizedMessage()), ex);
            } finally {
                switchButtons(true);
            }
        }

        private void handleSelection(final EventObject e) {
            if (isGroupSelected()) {

                if (e.getSource() == tableGroups) {
                    String[] accounts = accountGroupDataService.getAccountNamesForGroup(getSelectedGroup());
                    accountinGroupSet.clear();
                    accountinGroupSet.addAll(Arrays.asList(accounts));
                }

                else if (e.getSource() == tableGroupToAccounts) {
                    tableAccounts.setSelection(new StructuredSelection());
                }

                else if (e.getSource() == tableAccounts) {
                    tableGroupToAccounts.setSelection(new StructuredSelection());
                }

                else if (e.getSource() == addBtn) {
                    IStructuredSelection structuredSelection = (IStructuredSelection)tableAccounts.getSelection();
                    addAccounts(Arrays.copyOf(structuredSelection.toArray(), structuredSelection.toArray().length, String[].class));
                }

                else if (e.getSource() == addAllBtn) {
                    addAllAccounts(accountArray);
                }

                else if (e.getSource() == removeBtn) {
                    IStructuredSelection selection = (IStructuredSelection)tableGroupToAccounts.getSelection();
                    removeAccounts(Arrays.copyOf(selection.toArray(), selection.toArray().length, String[].class));
                }

                else if (e.getSource() == removeAllBtn) {
                    Set set = (Set)tableGroupToAccounts.getInput();
                    removeAllAccounts(Arrays.copyOf(set.toArray(), set.size(), String[].class));
                }
                updateAllLists();
            }

            if (e.getSource() == editAccountBtn) {
                updateConfiguration();
            }
        }
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
                status = new Status(IStatus.ERROR, "sernet.gs.ui.rcp.main", Messages.GroupView_1, e);
            } finally {
                monitor.done();
            }
            return status;
        }
    }

    private void updateGroups(final SelectionEvent e) {
        getDisplay().syncExec(new SelectionEventHandler(e));
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

            } catch (Exception e) {
                LOG.error(Messages.GroupView_1, e);
                status = new Status(IStatus.ERROR, "sernet.gs.ui.rcp.main", Messages.GroupView_1, e);
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
                updateAccountGroupList();

                if (isGroupSelected()) {
                    accountinGroupSet.clear();
                    String group = getSelectedGroup();
                    String[] names = accountGroupDataService.getAccountNamesForGroup(group);
                    accountinGroupSet.addAll(Arrays.asList(names));                  
                    tableGroupToAccounts.setInput(accountinGroupSet);
                }
                accountArray = accountGroupDataService.getAllAccounts();
				// remove accounts that are enlisted in tableToGroupAccounts
                for(String account : accountinGroupSet){
				    if(ArrayUtils.contains(accountArray, account)){
				        accountArray = (String[])ArrayUtils.remove(accountArray, ArrayUtils.indexOf(accountArray, account));
				    }
				}
				Arrays.sort(accountArray, COLLATOR);
                tableAccounts.setInput(accountArray);
            }
        });
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                tableAccounts.refresh(true);
                tableGroups.refresh(true);
                tableGroupToAccounts.refresh(true);
            }
        });
    }
    
    private void updateAccountGroupList() {
        String text = quickFilter.getText();
        String[] allAccountGroups = accountGroupDataService.getAccountGroups();      
        if (text==null || text.isEmpty()) {
            accountGroupArray= allAccountGroups;
        } else {
            List<String> filteredList = new LinkedList<String>();
            for (String group : allAccountGroups) {
                if (group.toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(group);
                }
            }
            accountGroupArray =  filteredList.toArray(new String[filteredList.size()]);
        }        
        Arrays.sort(accountGroupArray, COLLATOR);
        tableGroups.setInput(accountGroupArray);
    }

    private class NewGroupAction extends Action {

        @Override
        public void run() {
            NewGroupDialog newGroupDialog = new NewGroupDialog(GroupView.this, parent.getShell(), Messages.GroupView_18);
            newGroupDialog.open();
        }
    }

    private class EditGroupAction extends Action {

        @Override
        public void run() {
            EditGroupDialog dialog = new EditGroupDialog(GroupView.this, parent.getShell(), Messages.GroupView_19);
            dialog.open();
        }
    }

    String getSelectedGroup() {
        return (String)((StructuredSelection)tableGroups.getSelection()).toList().get(0);
    }

    boolean isGroupSelected() {
        return ((StructuredSelection)tableGroups.getSelection()).toList().size() > 0;
    }
    
    String[] getAllGroupsFromTable(){
        return (String[]) tableGroups.getInput();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        updateAccountGroupList();
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
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            super.run(monitor);
            initData();
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
                            MessageDialog.openWarning(parent.getShell(), Messages.GroupView_23, message);
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

    @Override
    public void mouseDoubleClick(MouseEvent e) {
        if (e.getSource() == tableGroups) {
            EditGroupDialog dialog = new EditGroupDialog(this, parent.getShell(), Messages.GroupView_19);
            dialog.open();
        }

        if (e.getSource() == tableAccounts || e.getSource() == tableGroupToAccounts) {
            updateConfiguration();
        }
    }

    private void updateConfiguration() {
        String selectedAccountName = getSelectedAccount();
        if (!"".equals(selectedAccountName)) {

            Configuration configuration = accountService.getAccountByName(getSelectedAccount());
            WizardDialog accountDialog = createWizard(configuration);

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

    private WizardDialog createWizard(Configuration configuration) {
       AccountWizard wizard = new AccountWizard(configuration);
       return new WizardDialog(getDisplay().getActiveShell(), wizard);
    }

    private String getSelectedAccount() {

        if(tableAccounts.getSelection() != null){
            IStructuredSelection selection = (IStructuredSelection) tableAccounts.getSelection();
            java.util.List selectionList = selection.toList();
            if(selectionList.size() > 0){
                return (String)selectionList.get(0);
            }
        } else if (((StructuredSelection)tableGroupToAccounts.getSelection()).toList().size()   > 0) {
            return accountinGroupSet.toArray(new String[accountinGroupSet.size()])[0];
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
        if(accountGroupDataService == null){
            accountGroupDataService = new AccountGroupDataService();
        }
    }


    @Override
    public void loaded(BSIModel model) {
    }

    @Override
    public void loaded(ISO27KModel model) {
        initData();
    }

    @Override
    public void closed(BSIModel model) {

    }
    
    private TableViewer createTable(Composite parent, String title) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(title);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        
        int style = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL;

        TableViewer internalTable = new TableViewer(parent, style | SWT.MULTI);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true,true);
        internalTable.getControl().setLayoutData(gd);

        internalTable.setUseHashlookup(true);

        return internalTable;
    }
}
