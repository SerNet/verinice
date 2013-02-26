/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.service.commands.DeriveStatusCommand;

/**
 * This {@link ActionDelegate} determines all generic and specific measures (controls) that are
 * linked to an isa question (samttopic) and transfers the maturity of the question
 * to the measures, if maturity not unset or NA.
 * Execution is is done by remote call on the server in {@link DeriveStatusCommand}.
 * 
 * @see DeriveStatusCommand
 */
@SuppressWarnings("restriction")
public class DeriveStatusAction extends ActionDelegate implements IViewActionDelegate, IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {
    
    private static final Logger LOG = Logger.getLogger(DeriveStatusAction.class);
    
    public static final String ID = "sernet.verinice.iso27k.rcp.action.DeriveStatusAction"; //$NON-NLS-1$
    
    private boolean serverIsRunning = true;
    
    private ControlGroup selectedControlgroup;
    
    private int samtCount = 0;
    
    private int measureCount = 0;

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.DERIVESTATUS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java
     * .lang.String)
     */
    @Override
    public void setRightID(String rightID) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.
     * IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow arg0) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart arg0) {
    }

    @Override
    public void init(final IAction action) {
        if (Activator.getDefault().isStandalone() && !Activator.getDefault().getInternalServer().isRunning()) {
            serverIsRunning = false;
            IInternalServerStartListener listener = new IInternalServerStartListener() {
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if (e.isStarted()) {
                        serverIsRunning = true;
                        action.setEnabled(checkRights());
                    }
                }
            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            action.setEnabled(checkRights());
        }
    }

    @Override
    public void run(IAction action) {
        if (selectedControlgroup != null) {
            String title = selectedControlgroup.getTitle();
            boolean confirm = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.getString("DeriveStatus.1"), NLS.bind(Messages.getString("DeriveStatus.2"), title)); //$NON-NLS-1$ //$NON-NLS-2$
            if (!confirm){
                return;
            }
            try {
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.beginTask(Messages.getString("DeriveStatus.3"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                        derivateStatus();
                        monitor.done();
                    }
                });
                InfoDialogWithShowToggle.openInformation(Messages.getString("DeriveStatus.1"), NLS.bind(Messages.getString("DeriveStatus.4"), new Object[]{measureCount, samtCount}), Messages.getString("DeriveStatus.6"), PreferenceConstants.INFO_STATUS_DERIVED); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } catch (Exception e) {
                final String message = Messages.getString("DeriveStatusAction.6"); //$NON-NLS-1$
                LOG.error(message, e); //$NON-NLS-1$
                MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.getString("DeriveStatusAction.7"), message); //$NON-NLS-1$
            }
        }
    }

    private void derivateStatus() {
        Activator.inheritVeriniceContextState();
        DeriveStatusCommand command = new DeriveStatusCommand(selectedControlgroup);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            updateModel(command.getChangedElements());
            samtCount = command.getSamtTopicCount();
            measureCount = command.getMeasureCount();
        } catch (CommandException e) {
            LOG.error("Error while derivating status.", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
     * .IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (serverIsRunning) {
            action.setEnabled(checkRights());
        }
        if (selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            Object selectedElement = treeSelection.getFirstElement();
            if (selectedElement instanceof ControlGroup) {
                selectedControlgroup = (ControlGroup) selectedElement;
            }
        }
    }

    private void updateModel(List<CnATreeElement> changedElementList) {
        if (changedElementList != null && !changedElementList.isEmpty()) {
            CnAElementFactory.getInstance().reloadModelFromDatabase();
        }
    }
}
