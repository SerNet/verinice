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

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.verinice.rcp.IllegalSelectionException;

class EditGroupDialog extends CRUDAccountGroupDialog {

    private static final Logger LOG = Logger.getLogger(EditGroupDialog.class);
    
    private final AccountGroupView groupView;
    private String selection;

    public EditGroupDialog(AccountGroupView groupView, Shell parent, String title) {

        super(groupView, parent, title);
        this.groupView = groupView;

        if (this.groupView.isStandardGroup()) {
            this.groupView.openStandardGroupWarningDialog(Messages.GroupView_35);
            super.closeTray();
        } else if (this.groupView.isGroupSelected()) {
            this.selection = this.groupView.getSelectedGroup();
        } else {
            throw new IllegalSelectionException("an account group must be selected");
        }
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
            MessageDialog messageDialog = new MessageDialog(this.groupView.parent.getShell(), Messages.GroupView_16, null, msg, MessageDialog.QUESTION, new String[] { Messages.GroupView_30, Messages.GroupView_27 }, 0);
            int result = messageDialog.open();
            if (result == 0) {
                this.groupView.accountGroupDataService.editAccountGroupName(textInputField.getText(), selection);
            }
        } else {
            try {
                this.groupView.accountGroupDataService.editAccountGroupName(textInputField.getText(), selection);
            } catch (Exception ex) {
                MessageDialog.openError(this.groupView.parent.getShell(), "Error", ex.getLocalizedMessage());
                LOG.error("editing account group name failed", ex);
            }
        }

        super.okPressed();
    }

    private boolean existsGroup() {
        return ArrayUtils.contains(this.groupView.getAllGroupsFromTable(), textInputField.getText());
    }

}