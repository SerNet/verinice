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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.springframework.dao.DataIntegrityViolationException;

import sernet.gs.ui.rcp.main.bsi.views.Messages;

class NewGroupDialog extends CRUDAccountGroupDialog {

    private static final Logger LOG = Logger.getLogger(NewGroupDialog.class);
    
    private final AccountGroupView groupView;

    public NewGroupDialog(AccountGroupView groupView, Shell parent, String title) {
        super(groupView, parent, title);
        this.groupView = groupView;
    }

    @Override
    protected void okPressed() {
        try {

            if (textInputField.getText() == null || textInputField.getText().equals("")) {
                return;
            }

            this.groupView.accountGroupDataService.addAccountGroup(textInputField.getText());
            super.okPressed();
        } catch (DataIntegrityViolationException ex) {
            MessageDialog.openError(this.groupView.parent.getShell(), Messages.GroupView_23, Messages.GroupView_24);
        } catch (Exception ex) {
            MessageDialog.openError(this.groupView.parent.getShell(), Messages.GroupView_23, ex.getLocalizedMessage());
            LOG.error("adding group failed", ex);
        }
    }

    @Override
    protected boolean isEditable() {
        return true;
    }
}