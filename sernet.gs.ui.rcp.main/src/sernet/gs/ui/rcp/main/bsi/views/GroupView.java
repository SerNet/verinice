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
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.accountgroup.AccountGroup;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class GroupView extends ViewPart {

    public static final String ID = "sernet.gs.ui.rcp.main.bsi.views.groupview";

    private Composite parent;

    private Composite groupViewComposite;

    private Action newGroup;

    private Action deleteGroup;

    private Action saveGroup;

    private IAccountService accountService;

    private List groupList;

    private java.util.List<AccountGroup> groups;

    @Override
    public void createPartControl(Composite parent) {

        this.parent = parent;

        makeActions();
        fillLocalToolBar();
        setupView();

        initData();
    }

    private void initData() {
        this.accountService = (IAccountService) VeriniceContext.get(VeriniceContext.ACCOUNT_SERVICE);
        this.groups = accountService.listGroups();
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

        Text quickFilter = new Text(groupViewComposite, SWT.SINGLE | SWT.BORDER);
        GridData fastFilterGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        quickFilter.setLayoutData(fastFilterGridData);

        Label accountsInGroup = new Label(groupViewComposite, SWT.NULL);
        accountsInGroup.setLayoutData(new GridData());
        ((GridData) accountsInGroup.getLayoutData()).horizontalSpan = 2;
        accountsInGroup.setText("Accounts in Group");

        Label accounts = new Label(groupViewComposite, SWT.NULL);
        accounts.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        accounts.setText("Accounts");

        groupList = new List(groupViewComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        GridData groupListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        Rectangle trim = groupList.computeTrim(0, 0, 0, groupList.getItemHeight() * 12);
        groupListGridData.heightHint = trim.height;
        groupList.setLayoutData(groupListGridData);

        List groupAccountList = new List(groupViewComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        groupAccountList.setItems(new String[] { "account_1", "account_2", "account_3" });
        GridData groupAccountListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
        groupListGridData.heightHint = groupAccountList.computeTrim(0, 0, 0, groupList.getItemHeight() * 12).height;
        groupAccountList.setLayoutData(groupAccountListGridData);

        Group connectGroupsWithAccounts = new Group(groupViewComposite, SWT.NULL);
        connectGroupsWithAccounts.setLayout(new GridLayout());
        ((GridLayout) connectGroupsWithAccounts.getLayout()).numColumns = 1;
        connectGroupsWithAccounts.setLayoutData(new GridData(GridData.FILL_VERTICAL));

        Button addBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        addBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        addBtn.setText(Messages.GroupView_4);

        Button addAllBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        addAllBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        addAllBtn.setText(Messages.GroupView_5);

        Button removeBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        removeBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        removeBtn.setText(Messages.GroupView_6);

        Button removeAllBtn = new Button(connectGroupsWithAccounts, SWT.NULL);
        removeAllBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        removeAllBtn.setText(Messages.GroupView_7);

        Button editAccountBtn = new Button(connectGroupsWithAccounts, SWT.END);
        editAccountBtn.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
        editAccountBtn.setText(Messages.GroupView_8);

        List accountList = new List(groupViewComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        accountList.setItems(new String[] { "account_1", "account_2", "account_3", "account_4", "account_5", "account_6" });
        GridData accountListGridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        accountListGridData.heightHint = accountList.computeTrim(0, 0, 0, accountList.getItemHeight() * 12).height;
        accountList.setLayoutData(accountListGridData);

    }

    private void makeActions() {

        newGroup = new Action() {

        };

        newGroup.setText(Messages.GroupView_0);
        newGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));

        this.deleteGroup = new Action() {
        };

        deleteGroup.setText(Messages.GroupView_1);
        deleteGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.DELETE));

        this.saveGroup = new Action() {
        };

        saveGroup.setText(Messages.GroupView_2);
        saveGroup.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.SAVE));
    }

    private void setupCRUDButtons() {

        Group buttonGroup = new Group(groupViewComposite, SWT.NULL);
        buttonGroup.setText("Edit Groups");

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        buttonGroup.setLayout(gridLayout);

        GridData gridData = new GridData();
        gridData.horizontalSpan = 3;

        buttonGroup.setLayoutData(gridData);

        Button newBtn = new Button(buttonGroup, SWT.PUSH);
        newBtn.setText(Messages.GroupView_0);

        Button deleteBtn = new Button(buttonGroup, SWT.PUSH);
        deleteBtn.setText(Messages.GroupView_1);

        Button saveBtn = new Button(buttonGroup, SWT.PUSH);
        saveBtn.setText(Messages.GroupView_2);
    }

    private void fillLocalToolBar() {

        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();

        if (saveGroup != null)
            manager.add(saveGroup);
        if (deleteGroup != null)
            manager.add(deleteGroup);
        if (newGroup != null)
            manager.add(newGroup);
    }

    @Override
    public void setFocus() {
        if (groupViewComposite != null) groupViewComposite.setFocus();
    }
}
