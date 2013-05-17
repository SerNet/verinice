/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.UpdateElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IconSelectAction implements IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(IconSelectAction.class);
    
    private Shell shell;
    
    private List<CnATreeElement> selectedElments;
    
    private static ISchedulingRule iSchedulingRule = new Mutex();
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
        this.shell = window.getShell();     
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction arg0) {
        try {
            final IconSelectDialog dialog = new IconSelectDialog(shell);
            if(Dialog.OK==dialog.open() && dialog.isSomethingSelected()) {
                WorkspaceJob importJob = new WorkspaceJob(Messages.IconSelectAction_0) {
                    @Override
                    public IStatus runInWorkspace(final IProgressMonitor monitor) {
                        IStatus status = Status.OK_STATUS;
                        try {
                            monitor.setTaskName(Messages.IconSelectAction_1);
                            String iconPath = dialog.getSelectedPath();
                            if(dialog.isDefaultIcon()) {
                                iconPath = null;
                            }
                            for (CnATreeElement element : selectedElments) {                  
                                element = updateIcon(element, iconPath);
                            }
                        } catch (Exception e) {
                            LOG.error("Error while changing icons.", e); //$NON-NLS-1$
                            status = new Status(IStatus.ERROR, "sernet.verinice.rcp", Messages.IconSelectAction_3, e); //$NON-NLS-1$
                        }
                        return status;
                    }
                };
                JobScheduler.scheduleJob(importJob, iSchedulingRule);
            }
        } catch (Exception e) {
            LOG.error(Messages.IconSelectAction_4, e);
        }
    }

    private CnATreeElement updateIcon(CnATreeElement element, String iconPath) throws CommandException {
        element.setIconPath(iconPath);
        Activator.inheritVeriniceContextState();
        UpdateElement<CnATreeElement> updateCommand = new UpdateElement<CnATreeElement>(element, false, ChangeLogEntry.STATION_ID);
        getCommandService().executeCommand(updateCommand);
        // notify all views of change:
        CnAElementFactory.getModel(element).childChanged(element);
        return element;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction arg0, ISelection selection) {
        if(selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            List<Object> selectionList = treeSelection.toList();
            selectedElments = new ArrayList<CnATreeElement>(selectionList.size());
            for (Object object : selectionList) {
                if(object instanceof CnATreeElement) {
                    selectedElments.add((CnATreeElement) object);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        Activator.inheritVeriniceContextState();
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.GENERATEORGREPORT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // DO nothing, no need for an implementation        
        
    }
    
    private ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}
