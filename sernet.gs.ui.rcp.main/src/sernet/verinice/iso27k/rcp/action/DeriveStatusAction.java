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
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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

import sernet.gs.service.RetrieveInfo;
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
import sernet.verinice.iso27k.rcp.action.Messages;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.service.commands.DeriveStatusCommand;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 *
 */
public class DeriveStatusAction extends ActionDelegate implements IViewActionDelegate, IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {
    
    public static final String ID = "sernet.verinice.iso27k.rcp.action.DeriveStatusAction"; //$NON-NLS-1$
    private static final Logger LOG = Logger.getLogger(DeriveStatusAction.class);
    
    private boolean serverIsRunning = true;
    
    private IViewPart view;
    
    private ControlGroup selectedControlgroup;
    
    private static ISchedulingRule iSchedulingRule = new Mutex();
    
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
        this.view = arg0;
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
            boolean confirm = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.getString("DeriveStatus.1"), NLS.bind(Messages.getString("DeriveStatus.2"), title));
            if (!confirm)
                return;
            try {
                PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.beginTask(Messages.getString("DeriveStatus.3"), IProgressMonitor.UNKNOWN);
                        derivateStatus();
                        monitor.done();
                    }
                });
                InfoDialogWithShowToggle.openInformation(Messages.getString("DeriveStatus.1"), NLS.bind(Messages.getString("DeriveStatus.4"), new Object[]{measureCount, samtCount}), Messages.getString("DeriveStatus.6"), PreferenceConstants.INFO_STATUS_DERIVED);
//                MessageDialog.openInformation(Display.getDefault().getActiveShell(),  NLS.bind(Messages.getString("DeriveStatus.4"), new Object[]{measureCount, samtCount}));
            } catch (Throwable e) {
                LOG.error("Error while derivating status.", e); //$NON-NLS-1$
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
        }
    }

    private CnATreeElement loadChildren(CnATreeElement element, RetrieveInfo info) {
        try {
            LoadElementByUuid command = new LoadElementByUuid(element.getUuid(), info);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            return command.getElement();
        } catch (CommandException e) {
            LOG.error("Error while retrieving children", e);
            return null;
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

    @SuppressWarnings("restriction")
    private void updateModel(List<CnATreeElement> changedElementList) {
        if (changedElementList != null && !changedElementList.isEmpty()) {
            for (CnATreeElement cnATreeElement : changedElementList) {
//                avoid lazy exceptions here and use childChanged/databaseChildChanged again
//                CnAElementFactory.getModel(cnATreeElement).childChanged(cnATreeElement.getParent(), cnATreeElement);
//                CnAElementFactory.getModel(cnATreeElement).databaseChildChanged(cnATreeElement);
            }
            CnAElementFactory.getInstance().reloadModelFromDatabase();
        }
    }
}
