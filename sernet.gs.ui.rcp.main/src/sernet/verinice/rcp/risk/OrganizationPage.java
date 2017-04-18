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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.OrganizationMultiselectWidget;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.account.BaseWizardPage;

/**
 * A wizard page to select organizations for the execution of a ISO/IEC 27005 risk analysis.
 * This page belongs to the wizard class RiskAnalysisIsoWizard.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class OrganizationPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(OrganizationPage.class);
    public static final String PAGE_NAME = "risk-analysis-wizard-organization-page"; //$NON-NLS-1$

    private ITreeSelection selection;
    private CnATreeElement selectedElement;
    private List<Integer> organizationIds = new LinkedList<>();

    private OrganizationMultiselectWidget organizationWidget = null;

    public OrganizationPage() {
        this((CnATreeElement) null);
    }

    public OrganizationPage(ITreeSelection selection){
        this((CnATreeElement)null);
        this.selection = selection;
    }
    
    public OrganizationPage(CnATreeElement selectedOrganization) {
        super(PAGE_NAME);
        selectedElement = selectedOrganization;
    }


    @Override
    protected void initGui(Composite composite) {
        final int layoutMarginWidth = 10;
        final int layoutMarginHeight = layoutMarginWidth;
        
        setTitle(Messages.OrganizationPage_WizardTitle);
        setMessage(
                Messages.OrganizationPage_WizardMessage);

        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = layoutMarginWidth;
        layout.marginHeight = layoutMarginHeight;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        try {
            organizationWidget = new OrganizationMultiselectWidget(composite, selection,
                    selectedElement);

        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(Messages.OrganizationPage_ErrorMessage, IMessageProvider.ERROR);
        }
        
        
       

        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {       
                syncSelectedOrganizations();
                super.widgetSelected(e);
            } 
        };

        organizationWidget.addSelectionListener(organizationListener);
        
        syncSelectedOrganizations();
        composite.pack();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.account.BaseWizardPage#initData()
     */
    @Override
    protected void initData() throws Exception {
        // nothing to do, organizations are loaded in class ScopeMultiselectWidget
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        boolean complete = !organizationIds.isEmpty();
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }
    
    private void syncSelectedOrganizations() {
        organizationIds.clear();
        if (organizationWidget.getSelectedElementSet() != null) {
            Set<CnATreeElement> selectedOrganizations  = organizationWidget.getSelectedElementSet();
            for (CnATreeElement organization : selectedOrganizations) {
                organizationIds.add(organization.getDbId());
            }
        }
        setPageComplete(isPageComplete());
    }

    public Set<CnATreeElement> getSelectedElementSet() {
        return organizationWidget.getSelectedElementSet();
    }

    public CnATreeElement getSelectedElement() {
        return organizationWidget.getSelectedElement();
    }

    public List<Integer> getOrganizationIds() {
        return organizationIds;
    }



}
