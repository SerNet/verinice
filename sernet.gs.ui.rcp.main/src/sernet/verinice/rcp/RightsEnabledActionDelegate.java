/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;

/**
 * Base class for rights enabled {@link ActionDelegate}.
 * This ActionDelegate enables or disables corresponding action
 * depending on the user rights.
 * 
 * User rights are checked before the action is executed in method run.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class RightsEnabledActionDelegate extends ActionDelegate implements RightEnabledUserInteraction {
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionDelegate#init(org.eclipse.jface.action.IAction)
     */
    @Override
    public void init(final IAction action) {
        if(!isServerRunning()){
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
                        action.setEnabled(checkRights());
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            action.setEnabled(checkRights());
        }
    }
    
    /*
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public final void run(IAction action) {
        if(checkRights()) {
            doRun(action);         
        } else {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.RightsEnabledActionDelegate_0, Messages.RightsEnabledActionDelegate_1);
        }
            
    }
    
    /**
     * @param action
     */
    public abstract void doRun(IAction action);

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        Activator.inheritVeriniceContextState();
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /**
     * @return false if operation mode is standalone and internal server is not running
     */
    protected boolean isServerRunning() {
        return !(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning());
    }

}
