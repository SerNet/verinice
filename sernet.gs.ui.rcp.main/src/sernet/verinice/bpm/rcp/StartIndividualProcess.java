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
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
import sernet.verinice.interfaces.bpm.IIndividualService;
import sernet.verinice.interfaces.bpm.IProcessStartInformation;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.InfoDialogWithShowToggle;

/**
 * RCP Action to start jBPM process "individual-task" defined in individual-task.jpdl.xml.
 * Action opens a wizard {@link IndividualProcessWizard} to get process parameter.
 * Parameter are send to the {@link IIndividualService} to start the process.
 * 
 * This action in configured in plugin.xml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class StartIndividualProcess implements IObjectActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(StartIndividualProcess.class);
    
    private List<String> selectedUuids = new LinkedList<String>();
    
    private List<String> selectedTitles = new LinkedList<String>();

    private List<String> selectedTypeIds = new LinkedList<String>();
    
    private String personTypeId = PersonIso.TYPE_ID;
    
    private IndividualServiceParameter parameter = new IndividualServiceParameter();
    
    private int numberOfProcess = 0;
    
    private Boolean isActive = null;
    
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
        try {
            if(!selectedUuids.isEmpty() && isValid()) {
                IndividualProcessWizard wizard = new IndividualProcessWizard(selectedUuids, selectedTitles.get(0),selectedTypeIds.get(0));                 
                wizard.setPersonTypeId(personTypeId);
                WizardDialog wizardDialog = new NonModalWizardDialog(Display.getCurrent().getActiveShell(),wizard);
                if (wizardDialog.open() == Window.OK) {
                    wizard.saveTemplate();
                    parameter = wizard.getParameter();                                 
                    startProcess(wizard.getUuids());
                }
            }
        } catch( Exception e) {
            LOG.error("Error while starting individual task.", e); //$NON-NLS-1$
        }
    }
    
    /**
     * @return
     */
    private boolean isValid() {
        boolean valid = true;
        if(!selectedTypeIds.isEmpty()) {
            valid = isOfTheSameType();
            if(valid) {
                valid = isValidType();
            }
        }
        return valid;
    }

    private boolean isOfTheSameType() {
        boolean valid = true;
        String lastTypeId = null;
        for (String typeId : selectedTypeIds) {
            if(lastTypeId!=null && !lastTypeId.equals(typeId)) {
                MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.StartIndividualProcess_1, Messages.StartIndividualProcess_2);
                valid = false;
                break;
            }
            lastTypeId = typeId;
        }
        return valid;
    }
    
    private boolean isValidType() {
        boolean valid = true;
        if(!selectedTypeIds.isEmpty()) {
           String type = selectedTypeIds.get(0);
           if(ImportBsiGroup.TYPE_ID.equals(type) || ImportIsoGroup.TYPE_ID.equals(type)) {
               MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.StartIndividualProcess_1, Messages.StartIndividualProcess_3);
               valid = false;              
           }
        }
        return valid;
    }

    private void startProcess(final List<String> uuids) {
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();       
        try {
            progressService.run(true, true, new IRunnableWithProgress() {  
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    numberOfProcess=0;
                    IProcessStartInformation info = null;
                    if(uuids!=null && !uuids.isEmpty() ) {
                        for (String uuid : uuids) {
                            parameter.setUuid(uuid);
                            info = ServiceFactory.lookupIndividualService().startProcess(parameter);
                            if(info!=null) {
                                numberOfProcess+=info.getNumber();
                            }
                        }
                                   
                    }
                                    
                }
            });
            if(numberOfProcess > 0) {
                TaskChangeRegistry.tasksAdded();
            }
            InfoDialogWithShowToggle.openInformation(
                    Messages.StartIsaProcess_0,  
                    Messages.bind(Messages.StartIndividualProcess_0, numberOfProcess),
                    Messages.StartIsaProcess_3,
                    PreferenceConstants.INFO_PROCESSES_STARTED);
        } catch (Exception t) {
            LOG.error("Error while creating tasks.",t); //$NON-NLS-1$
            ExceptionUtil.log(t, sernet.verinice.bpm.rcp.Messages.StartIsaProcess_5); 
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
                selectedUuids.clear();
                selectedTitles.clear();
                selectedTypeIds.clear();
                for (Iterator iterator = treeSelection.iterator(); iterator.hasNext();) {
                    Object selectedElement = iterator.next();         
                    if(selectedElement instanceof CnATreeElement) {
                        CnATreeElement element = (CnATreeElement) selectedElement;
                        selectedUuids.add(element.getUuid());
                        selectedTitles.add(element.getTitle());
                        selectedTypeIds.add(element.getTypeId());
                    }
                    if(isGrundschutzElement(selectedElement)) {
                        personTypeId = Person.TYPE_ID;
                    } else {
                        personTypeId = PersonIso.TYPE_ID;
                    }
                }
                
            }
        } else {
            action.setEnabled(false);
        }       
    }

    /**
     * @param selectedElement
     * @return
     */
    private boolean isGrundschutzElement(Object selectedElement) {
        return (selectedElement instanceof IBSIStrukturElement)
                || (selectedElement instanceof BausteinUmsetzung)
                || (selectedElement instanceof MassnahmenUmsetzung);
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
        return ActionRightIDs.CREATE_INDIVIDUAL_TASKS;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
    }
    
    class NonModalWizardDialog extends WizardDialog {
        public NonModalWizardDialog(Shell parentShell, IWizard newWizard) {
            super(parentShell, newWizard);
            int style = SWT.CLOSE | SWT.MAX | SWT.TITLE;
            style = style | SWT.BORDER | SWT.RESIZE;
            setShellStyle(style | getDefaultOrientation());         
        }
    }
}


