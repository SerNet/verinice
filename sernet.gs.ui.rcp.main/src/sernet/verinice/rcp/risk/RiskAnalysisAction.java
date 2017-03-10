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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.Messages;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.NonModalWizardDialog;
import sernet.verinice.service.risk.RiskAnalysisConfiguration;
import sernet.verinice.service.risk.RiskAnalysisService;

/**
 * This action class runs a ISO/IEC 27005 risk analysis
 * on data in one or more.
 *
 * @author Alexander Koderman
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class RiskAnalysisAction extends RightsEnabledAction implements ISelectionListener  {

    public static final String ID = "sernet.gs.ui.rcp.main.runriskanalysisaction"; //$NON-NLS-1$
    
    private CnATreeElement selectedOrganization;
    
    RiskAnalysisIsoWizard wizard;
    private RiskAnalysisService riskAnalysisService;
    
    public RiskAnalysisAction(IWorkbenchWindow window) {
        setText(Messages.RunRiskAnalysisAction_0);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ISO27K_RISK));
        setRightID(ActionRightIDs.RISKANALYSIS);
        addLoadListener();
        window.getSelectionService().addSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        final RiskAnalysisConfiguration configuration = createConfiguration();
        if(isNoOrganizationInConfiguration(configuration)) {
            return;
        }
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {             
                    runRiskAnalysis(configuration);                 
                }
            });
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.RunRiskAnalysisAction_2);
        }
        
    }

    private boolean isNoOrganizationInConfiguration(final RiskAnalysisConfiguration configuration) {
        return configuration==null || configuration.getOrganizationDbIds()==null || configuration.getOrganizationDbIds().length==0;
    }

    private void runRiskAnalysis(RiskAnalysisConfiguration configuration) {
        getRiskAnalysisService().runRiskAnalysis(configuration);
    }
    
   

    private RiskAnalysisConfiguration createConfiguration() {
        RiskAnalysisConfiguration configuration = null;
        
        final TitleAreaDialog dialog = createWizard();
        if (dialog.open() == Window.OK) {
            List<Integer> organizationIds = wizard.getOrganizationIds(); 
            Integer[] organizationIdArray = organizationIds.toArray(new Integer[organizationIds.size()]);
            configuration = new RiskAnalysisConfiguration(organizationIdArray);
        }
        
        return configuration;
    }
    
    private TitleAreaDialog createWizard() {
        wizard = new RiskAnalysisIsoWizard(selectedOrganization);                 
        return new NonModalWizardDialog(Display.getCurrent().getActiveShell(),wizard);
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
        });
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart arg0, ISelection input) {
        if (input instanceof ITreeSelection) {
            selectedOrganization = null;
            ITreeSelection selectionCurrent = (ITreeSelection) input;
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
    
    private RiskAnalysisService getRiskAnalysisService() {
        if(riskAnalysisService==null) {
            riskAnalysisService = (RiskAnalysisService) VeriniceContext.get(VeriniceContext.RISK_ANALYSIS_SERVICE);
        }
        return riskAnalysisService;
    }

}
