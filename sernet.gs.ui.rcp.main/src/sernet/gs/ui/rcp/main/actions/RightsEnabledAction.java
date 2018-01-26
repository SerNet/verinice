/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 * Sebastian Hagedorn - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;

/**
 * JFace action that can be configured with rights profile management.
 * This action is enabled and executable if the rights ID returned by
 * getRightID() is assigned to the user account.
 * 
 * In contrast to JFace actions, the method run () must not be overwritten in
 * derived classes, but doRun ().
 *
 * @author Sebastian Hagedorn
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class RightsEnabledAction extends Action implements RightEnabledUserInteraction {

    private final String rightID;
    
    private boolean serverRunning = true;

    public RightsEnabledAction(String rightID) {
        this.rightID = rightID;
        setEnabledViaRightID();
    }

    public RightsEnabledAction(String rightID, String text) {
        super(text);
        this.rightID = rightID;
        setEnabledViaRightID();
    }

    public RightsEnabledAction(String rightID, String text, int style) {
        super(text, style);
        this.rightID = rightID;
        setEnabledViaRightID();
    }

    /**
     * Checks if rightID is assigned to the user account.
     * If it is assigned doRun() is executed if not
     * an error message is shown.
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public final void run() {
        if (checkRights()) {
            doRun();
        } else {
            MessageDialog.openError(Display.getCurrent().getActiveShell(),
                    Messages.RightsEnabledAction_0, Messages.RightsEnabledAction_1);
        }

    }

    /**
     * Overwrite to implement your action.
     */
    public abstract void doRun();

    @Override
    public final boolean checkRights() {
        if (getRightID() == null) {
            // no right management should be used
            return true;
        }
        else if (getRightID().isEmpty()) {
            // id set but empty, right not granted, action disabled
            return false;
        } else {
            // enabled / false if not
            Activator.inheritVeriniceContextState();
            RightsServiceClient service = (RightsServiceClient) VeriniceContext
                    .get(VeriniceContext.RIGHTS_SERVICE);
            return service.isEnabled(getRightID());
        }
    }

    @Override
    public final String getRightID() {
        return rightID;
    }

    private void setEnabledViaRightID() {
        if (Activator.getDefault().isStandalone()
                && !Activator.getDefault().getInternalServer().isRunning()) {
            serverRunning = false;
            addInternalServerStartListener();
        } else {
            setEnabled(checkRights());
        }
    }

    private void addInternalServerStartListener() {
        IInternalServerStartListener listener = new IInternalServerStartListener() {
            @Override
            public void statusChanged(InternalServerEvent e) {
                if (e.isStarted()) {
                    serverRunning = true;
                    setEnabled(checkRights());
                }
            }
        };
        Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
    }
    
    public final boolean isServerRunning() {
        return serverRunning;
    }

}
