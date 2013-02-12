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
package sernet.verinice.rcp.gsm;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

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
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.bpm.rcp.TaskChangeRegistry;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.InfoDialogWithShowToggle;

/**
 * RCP Action to start jBPM process "gsm-ism-execute" defined in gsm-ism-execute.jpdl.xml.
 * 
 * This action in configured in plugin.xml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class StartGsmExecuteProcess implements IObjectActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(StartGsmExecuteProcess.class);
   
    private Integer orgId;
    
    private int numberOfProcess = 0;
    
    private Boolean isActive = null;

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override  
    public void run(IAction action) {
        try {
            if(orgId!=null) {
                startProcess();
            }
        } catch( Exception e) {
            LOG.error("Error while starting individual task.", e); //$NON-NLS-1$
        }
    }

    private void startProcess() {
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();       
        try {
            progressService.run(true, true, new IRunnableWithProgress() {  
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    IProcessStartInformation info = ServiceFactory.lookupGsmService().startProcessesForOrganization(orgId);
                    numberOfProcess = info.getNumber();                
                }
            });           
            InfoDialogWithShowToggle.openInformation(
                    Messages.StartGsmExecuteProcess_0,  
                    Messages.bind(Messages.StartGsmExecuteProcess_1, numberOfProcess),
                    Messages.StartGsmExecuteProcess_2,
                    PreferenceConstants.INFO_PROCESSES_STARTED);
            if(numberOfProcess > 0) {
                TaskChangeRegistry.tasksAdded();
            }
        } catch (Exception t) {
            LOG.error("Error while creating process.",t); //$NON-NLS-1$
            ExceptionUtil.log(t, Messages.StartGsmExecuteProcess_3); 
        }
    } 

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(checkRights());
        if(isActive()) {
            if(selection instanceof ITreeSelection) {
                ITreeSelection treeSelection = (ITreeSelection) selection;
                for (Iterator<?> iterator = treeSelection.iterator(); iterator.hasNext();) {
                    Object selectedElement = iterator.next();         
                    if(selectedElement instanceof Organization) {
                        orgId = ((Organization) selectedElement).getDbId();
                    }
                }             
            }
        } else {
            action.setEnabled(false);
        }       
    }
    
    private boolean isActive() {
        if(isActive==null) {
            isActive = ServiceFactory.lookupGsmService().isActive();
        }
        return isActive.booleanValue();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.CREATE_INDIVIDUAL_TASKS;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
    }
    
   
}


