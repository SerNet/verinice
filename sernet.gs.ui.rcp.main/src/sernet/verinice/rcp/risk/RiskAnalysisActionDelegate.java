/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.risk;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.NonModalWizardDialog;
import sernet.verinice.rcp.RightsEnabledActionDelegate;

/**
 * This action delegate class runs a ISO/IEC 27005 risk analysis
 * on data in one or more organizations.
 * 
 * This action delegate class is configured in plugin.xml of bundle
 * sernet.gs.ui.rcp.main. See extension point: "org.eclipse.ui.popupMenus".
 *
 * @see RiskAnalysisAction
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class RiskAnalysisActionDelegate extends RightsEnabledActionDelegate
        implements IWorkbenchWindowActionDelegate {

    private static final Logger log = Logger.getLogger(RiskAnalysisActionDelegate.class);
    
    private CnATreeElement selectedOrganization;
    
    private Shell shell;
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledActionDelegate#doRun(org.eclipse.jface.action.IAction)
     */
    @Override
    public void doRun(IAction action) {
        try {
                // Close editors to avoid that stale values remain in an open editor
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /* ask save */); 
            TitleAreaDialog wizardDialog = openWizard(); 
            if (wizardDialog.open() == Window.OK) {
                MessageDialog.openInformation(getShell(), Messages.RiskAnalysisAction_FinishDialogTitle, Messages.RiskAnalysisAction_FinishDialogMessage);
            }
        } catch (Exception e) {
            MessageDialog.openError(getShell(), Messages.RiskAnalysisAction_ErrorDialogTitle, Messages.RiskAnalysisAction_ErrorDialogMessage);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.RISKANALYSIS;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
        try {
            shell = window.getShell();
        } catch(Exception t) {
            log.error("Error creating RiskAnalysisActionDelegate", t); //$NON-NLS-1$
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof ITreeSelection) {
            selectedOrganization = null;
            ITreeSelection selectionCurrent = (ITreeSelection) selection;
            for (Iterator<?> iter = selectionCurrent.iterator(); iter.hasNext();) {
                Object selectedObject = iter.next();
                if (isOrganization(selectedObject)) {
                    selectedOrganization = (CnATreeElement) selectedObject;     
                }
            }
        }  
    }
    private boolean isOrganization(Object element) {
        return element instanceof Organization;
    }
    
    private Shell getShell() {
        return shell;
    }

    private TitleAreaDialog openWizard() {
        RiskAnalysisIsoWizard wizard = new RiskAnalysisIsoWizard(selectedOrganization);                 
        return new NonModalWizardDialog(getShell(),wizard);
        
    }

}
