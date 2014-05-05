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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Display;
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
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.rcp.InfoDialogWithShowToggle;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class StartIsaControlFlowProcess implements IObjectActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(StartIsaControlFlowProcess.class);
    
    private List<String> selectedControlUuids = new LinkedList<String>();
    private List<String> selectedControlGroupUuids = new LinkedList<String>();
    private List<String> selectedAuditUuids = new LinkedList<String>();
    
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
        if(!selectedControlUuids.isEmpty() ||
           !selectedControlGroupUuids.isEmpty() ||
           !selectedAuditUuids.isEmpty()) {
            IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            
            try {
                progressService.run(true, true, new IRunnableWithProgress() {  
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        Activator.inheritVeriniceContextState();
                        numberOfProcess=0;
                        IProcessStartInformation info = null;
                        if(!selectedControlUuids.isEmpty() ) {
                            info = ServiceFactory.lookupIsaControlFlowService().startProcessesForControls(selectedControlUuids);           
                        }
                        if(info!=null) {
                            numberOfProcess=info.getNumber();
                        }
                        info = null;
                        if( !selectedControlGroupUuids.isEmpty()) {
                            info = ServiceFactory.lookupIsaControlFlowService().startProcessesForGroups(selectedControlGroupUuids);
                        }
                        if(info!=null) {
                            numberOfProcess=numberOfProcess + info.getNumber();
                        }
                        info = null;
                        if( !selectedAuditUuids.isEmpty()) {
                            info = ServiceFactory.lookupIsaControlFlowService().startProcessesForAudits(selectedAuditUuids);
                        }
                        if(info!=null) {
                            numberOfProcess=numberOfProcess + info.getNumber();
                        }
                    }
                });
                if(numberOfProcess > 0) {
                    TaskChangeRegistry.tasksAdded();
                }
                boolean infoDialogOpened = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.INFO_PROCESSES_STARTED);
                if(numberOfProcess > 0){
                InfoDialogWithShowToggle.openInformation(
                        Messages.StartIsaProcess_0,  
                        Messages.bind(Messages.StartIsaProcess_1, numberOfProcess),// "Succesfully created " + numberOfProcess + " tasks.",; 
                        Messages.StartIsaProcess_3,
                        PreferenceConstants.INFO_PROCESSES_STARTED);
                } else if (numberOfProcess == 0){
                    MessageDialog.openInformation(Display.getDefault().getActiveShell(), Messages.StartIsaProcess_0, Messages.StartIsaProcess_6);
                }
            } catch (Exception t) {
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
        action.setEnabled(checkRights());
        if(isActive()) {
            if(selection instanceof ITreeSelection) {
                ITreeSelection treeSelection = (ITreeSelection) selection;
                selectedControlUuids.clear();
                selectedControlGroupUuids.clear();
                for (Iterator iterator = treeSelection.iterator(); iterator.hasNext();) {
                    Object selectedElement = iterator.next();         
                    if(selectedElement instanceof Control) {
                        selectedControlUuids.add(((CnATreeElement) selectedElement).getUuid());
                    }
                    if(selectedElement instanceof ControlGroup) {
                        selectedControlGroupUuids.add(((CnATreeElement) selectedElement).getUuid());
                    }
                    if(selectedElement instanceof Audit) {
                        selectedAuditUuids.add(((CnATreeElement) selectedElement).getUuid());
                    }
                }
                
            }
        } else {
            action.setEnabled(false);
        }
        
    }
    
    private boolean isActive() {
        if(isActive==null) {
            isActive = ServiceFactory.lookupProcessServiceIsa().isActive();
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
        return ActionRightIDs.CREATEISATASKS;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
    }

}
