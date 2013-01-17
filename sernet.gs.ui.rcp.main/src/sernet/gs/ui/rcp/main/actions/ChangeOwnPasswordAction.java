/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.PasswordDialog;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.ChangeOwnPassword;
import sernet.verinice.interfaces.CommandException;

/**
 * Action to allow users to change their own password if stored in the verinice DB.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ChangeOwnPasswordAction extends Action  {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.changeownpasswordaction"; //$NON-NLS-1$
    private final IWorkbenchWindow window;

    
    public ChangeOwnPasswordAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        //setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.PERSON));
        setToolTipText(Messages.ChangeOwnPasswordAction_0);
        setEnabled(true);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        
        // if the user clicks this action as the first thing he does in verinice, the auth service, might not have been enabled
        // because no object was accessed yet. Make sure it is there:
        ServiceFactory.lookupAuthService();
        if (!ServiceFactory.isPermissionHandlingNeeded()) {
            setEnabled(false);
            return;
        }
        
        // this action works for normal users, admins are supposed to change their password differently, since admin acounts can also be defined in the config file
        // where they cannot be edited from within the application.
        // (admins can change the passwords for anybody, this action here only works for the currently logged in user)
        boolean isAdmin = AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN });
        if (isAdmin) {
            MessageDialog.openInformation(window.getShell(), Messages.ChangeOwnPasswordAction_1, Messages.ChangeOwnPasswordAction_2);
            return;
        }
        
        Activator.inheritVeriniceContextState();
        PasswordDialog passwordDialog = new PasswordDialog(this.window.getShell());
        if (passwordDialog.open() == Window.OK) {
            ChangeOwnPassword command = new ChangeOwnPassword(passwordDialog.getPassword());
            try {
                ServiceFactory.lookupCommandService().executeCommand(command);
            } catch (CommandException e) {
                ExceptionUtil.log(e, Messages.ChangeOwnPasswordAction_3);
            }
        }
    }


}
