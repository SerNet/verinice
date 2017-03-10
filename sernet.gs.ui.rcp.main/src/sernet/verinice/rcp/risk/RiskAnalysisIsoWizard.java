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

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import sernet.verinice.model.common.CnATreeElement;

/**
 * A wizard to configure and run a ISO/IEC 27005 risk analysis.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RiskAnalysisIsoWizard extends Wizard {
    
    private CnATreeElement selectedOrganization = null;
    
    private OrganizationPage organizationPage;

    
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
        setWindowTitle("ISO/IEC 27005 Risk Analysis");
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
        return true;
        // return organizationPage.isPageComplete();
    }
    
    @Override
    public IWizardPage getStartingPage() {
        return organizationPage;
    }

    /**
     * @return
     */
    public List<Integer> getOrganizationIds() {
        return organizationPage.getOrganizationIds();
    }
    

}
