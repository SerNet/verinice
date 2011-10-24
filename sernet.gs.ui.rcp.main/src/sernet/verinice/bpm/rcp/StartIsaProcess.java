/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.bpm.rcp;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.rcp.InfoDialogWithShowToggle;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class StartIsaProcess implements IObjectActionDelegate {

    private static final Logger LOG = Logger.getLogger(StartIsaProcess.class);
    
    private Audit selectedAudit;
    
    int numberOfProcess = 0;
    
    Boolean isActive = null;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override  
    public void run(IAction action) {
        if(selectedAudit!=null) {
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            
            try {
                progressService.run(true, true, new IRunnableWithProgress() {  
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        Activator.inheritVeriniceContextState();
                        IProcessStartInformation info = ServiceFactory.lookupProcessService().startProcessForIsa(selectedAudit.getUuid());           
                        numberOfProcess=0;
                        if(info!=null) {
                            numberOfProcess=info.getNumber();
                        }
                    }
                });
                InfoDialogWithShowToggle.openInformation(
                        Messages.StartIsaProcess_0,  
                        Messages.bind(Messages.StartIsaProcess_1, numberOfProcess),// "Succesfully created " + numberOfProcess + " tasks.",; 
                        Messages.StartIsaProcess_3,
                        PreferenceConstants.INFO_PROCESSES_STARTED);
            } catch (Throwable t) {
                LOG.error("Error while creating tasks.",t); //$NON-NLS-1$
                ExceptionUtil.log(t, sernet.verinice.bpm.rcp.Messages.StartIsaProcess_5); 
            }   
        }   
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if(isActive()) {
            if(selection instanceof ITreeSelection) {
                ITreeSelection treeSelection = (ITreeSelection) selection;
                Object selectedElement = treeSelection.getFirstElement();
                if(selectedElement instanceof Audit) {
                    selectedAudit = (Audit) selectedElement;
                }
            }
        } else {
            action.setEnabled(false);
        }
        
    }
    
    private boolean isActive() {
        if(isActive==null) {
            isActive = ServiceFactory.lookupProcessService().isActive();
        }
        return isActive.booleanValue();
    }

}
