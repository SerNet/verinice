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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;

import sernet.gs.ui.rcp.main.bsi.views.Messages;

class DeleteGroupAction extends Action {

    private static final Logger LOG = Logger.getLogger(DeleteGroupAction.class);
    
    private final AccountGroupView groupView;

    DeleteGroupAction(AccountGroupView groupView) {
        this.groupView = groupView;
    }

    @Override
    public void run() {

        if (this.groupView.isStandardGroup()) {
            this.groupView.openStandardGroupWarningDialog(Messages.GroupView_22);
            return;
        }

        AccountGroupView.getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                try {
                    int connectedAccounts = DeleteGroupAction.this.groupView.accountGroupDataService.getAccountNamesForGroup(DeleteGroupAction.this.groupView.getSelectedGroup()).length;
                    long connectedObjects = DeleteGroupAction.this.groupView.accountService.countConnectObjectsForGroup(DeleteGroupAction.this.groupView.getSelectedGroup());
                    String message = String.format(Messages.GroupView_25, connectedAccounts, connectedObjects);
                    MessageDialog dialog = new MessageDialog(DeleteGroupAction.this.groupView.parent.getShell(), Messages.GroupView_16, null, message, MessageDialog.ERROR, new String[] { Messages.GroupView_26, Messages.GroupView_27 }, 0);
                    int result = dialog.open();
                    if (result == 0) {
                        if (!isGroupEmptyAndNotConnectedToObject()) {
                            openSecondWarningDialog();
                        } else {
                            deleteGroup();
                        }
                    }
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
                AccountGroupView.getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            MessageDialog dialog = new MessageDialog(DeleteGroupAction.this.groupView.parent.getShell(), Messages.GroupView_28, null, Messages.GroupView_29, MessageDialog.ERROR, new String[] { Messages.GroupView_26, Messages.GroupView_27 }, 0);
                            int result = dialog.open();
                            if (result == 0) {
                                deleteGroup();
                            }
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
        return this.groupView.accountService.countConnectObjectsForGroup(this.groupView.getSelectedGroup()) == 0;
    }

    private boolean hasAccounts() {
        return this.groupView.accountGroupDataService.getAccountNamesForGroup(this.groupView.getSelectedGroup()).length == 0;
    }

    private void deleteGroup() {
        this.groupView.accountGroupDataService.deleteAccountGroup(this.groupView.getSelectedGroup());
        this.groupView.refreshView();
    }

}