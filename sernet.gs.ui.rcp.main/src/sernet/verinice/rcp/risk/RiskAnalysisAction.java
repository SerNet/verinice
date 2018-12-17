/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman - initial API and implementation
 *     Daniel Murygin 
 ******************************************************************************/
package sernet.verinice.rcp.risk;

import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.NonModalWizardDialog;

/**
 * This action class runs a ISO/IEC 27005 risk analysis
 * on data in one or more organizations.
 *
 * @see RiskAnalysisActionDelegate
 * @author Alexander Koderman
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class RiskAnalysisAction extends RightsEnabledAction implements ISelectionListener  {

    public static final String ID = "sernet.gs.ui.rcp.main.runriskanalysisaction"; //$NON-NLS-1$
    
    private CnATreeElement selectedOrganization;
    
    public RiskAnalysisAction(IWorkbenchWindow window) {
        super(ActionRightIDs.RISKANALYSIS, Messages.RiskAnalysisAction_Text);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ISO27K_RISK));
        addLoadListener();
        window.getSelectionService().addSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
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

    private TitleAreaDialog openWizard() {
        RiskAnalysisIsoWizard wizard = new RiskAnalysisIsoWizard(selectedOrganization);                 
        return new NonModalWizardDialog(getShell(),wizard);
        
    }

    private void addLoadListener() {
        CnAElementFactory.getInstance().addLoadListener(new IModelLoadListener() {
            @Override
            public void closed(BSIModel model) {
                setEnabled(false);
            }
            @Override
            public void loaded(BSIModel model) {
                // Nothing to do, this action is for ISO/IEC 27005 risk analysis
            }
            @Override
            public void loaded(ISO27KModel model) {
                setEnabled(checkRights());
            }
            @Override
            public void loaded(BpModel model) {
                // Nothing to do, this action is for ISO/IEC 27005 risk analysis
            }
            @Override
            public void loaded(CatalogModel model) {
                // nothing to do
            }
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart arg0, ISelection selection) {
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
        return Display.getCurrent().getActiveShell();
    }

}
