/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin.
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
package sernet.verinice.rcp.risk;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.risk.RiskAnalysisConfiguration;
import sernet.verinice.service.risk.RiskAnalysisService;

/**
 * A wizard to configure and run an ISO/IEC 27005 risk analysis.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RiskAnalysisIsoWizard extends Wizard {
    
    private static final Logger log = Logger.getLogger(RiskAnalysisIsoWizard.class);
    
    private RiskAnalysisConfiguration configuration;
    
    private CnATreeElement selectedOrganization = null;
    
    private OrganizationPage organizationPage;
    
    private RiskAnalysisService riskAnalysisService;

    
    public RiskAnalysisIsoWizard() {
        super(); 
        init();
    }
    public RiskAnalysisIsoWizard(CnATreeElement selectedOrganization) {
        this(); 
        this.selectedOrganization = selectedOrganization;
    }
    
    private void init() {
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.RiskAnalysisIsoWizard_WindowTitle);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        organizationPage = new OrganizationPage(selectedOrganization);             
        addPage(organizationPage);    
    }

    /* (non-Javadoc)    
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {       
        try {
            organizationPage.setMessage(Messages.RiskAnalysisIsoWizard_IsRunningMessage, DialogPage.INFORMATION);
            createConfiguration();
            runRiskAnalysis();
        } catch (InvocationTargetException | InterruptedException e) {
            log.error("InvocationTargetException or InterruptedException while running ISO/IEC 27005 risk analysis", e); //$NON-NLS-1$
            organizationPage.setMessage(Messages.RiskAnalysisIsoWizard_ErrorMessage, DialogPage.ERROR);
            return false;
        } 
        return true;
    }
    private void createConfiguration() {
        configuration = new RiskAnalysisConfiguration();
        List<Integer> organizationIds = organizationPage.getOrganizationIds(); 
        Integer[] organizationIdArray = organizationIds.toArray(new Integer[organizationIds.size()]);
        configuration.setOrganizationDbIds(organizationIdArray);
    }
    
    private void runRiskAnalysis() throws InvocationTargetException, InterruptedException {
        getContainer().run(true, false, new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor progressMonitor)
                    throws InvocationTargetException, InterruptedException {
                progressMonitor.beginTask(Messages.RiskAnalysisIsoWizard_IsRunningTaskMessage, IProgressMonitor.UNKNOWN);
                getRiskAnalysisService().runRiskAnalysis(configuration);  
                progressMonitor.done();
            }
        });            
    }
    
    @Override
    public IWizardPage getStartingPage() {
        return organizationPage;
    }

   
    public RiskAnalysisConfiguration getConfiguration() {
        return configuration;
    }
    public void setConfiguration(RiskAnalysisConfiguration configuration) {
        this.configuration = configuration;
    }
    
    private RiskAnalysisService getRiskAnalysisService() {
        if(riskAnalysisService==null) {
            riskAnalysisService = (RiskAnalysisService) VeriniceContext.get(VeriniceContext.RISK_ANALYSIS_SERVICE);
        }
        return riskAnalysisService;
    }

}
