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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.ui.rcp.main.bsi.views.Messages;

abstract class CRUDAccountGroupDialog extends TitleAreaDialog {

    private final AccountGroupView groupView;

    protected Text textInputField;

    protected Composite container;

    private String title;

    public CRUDAccountGroupDialog(AccountGroupView groupView, Shell parent, String title) {
        super(parent);
        this.groupView = groupView;
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
        this.groupView.refreshView();
        super.okPressed();
    }
}