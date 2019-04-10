/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin.
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
 *     Daniel Murygin - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp.converter;

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

import sernet.gs.ui.rcp.main.ItNetworkMultiselectWidget;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.account.BaseWizardPage;

/**
 * A wizard page to select IT networks for converting these IT networks to new
 * IT base protection. This page belongs to the wizard class
 * ItNetworkConverterWizard.
 * 
 * @author Daniel Murygin
 */
public class ItNetworkPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(ItNetworkPage.class);
    public static final String PAGE_NAME = "bp-converter-wizard-it-network-page"; //$NON-NLS-1$

    private ITreeSelection selection;
    private CnATreeElement selectedElement;
    private List<Integer> itNetworkIds = new LinkedList<>();

    private ItNetworkMultiselectWidget itNetworkWidget = null;

    public ItNetworkPage() {
        this((CnATreeElement) null);
    }

    public ItNetworkPage(ITreeSelection selection) {
        this((CnATreeElement) null);
        this.selection = selection;
    }

    public ItNetworkPage(CnATreeElement selectedItNetwork) {
        super(PAGE_NAME);
        selectedElement = selectedItNetwork;
    }

    @Override
    protected void initGui(Composite composite) {
        final int layoutMarginWidth = 10;
        final int layoutMarginHeight = layoutMarginWidth;
        setTitle(Messages.ItNetworkPage_Title);
        setMessage(Messages.ItNetworkPage_Message);

        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = layoutMarginWidth;
        layout.marginHeight = layoutMarginHeight;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        try {
            itNetworkWidget = new ItNetworkMultiselectWidget(composite, selection, selectedElement);
        } catch (CommandException ex) {
            LOG.error("Error while loading IT networks", ex); //$NON-NLS-1$
            setMessage(Messages.ItNetworkPage_Error, IMessageProvider.ERROR);
        }

        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                syncSelectedItNetworks();
                super.widgetSelected(e);
            }
        };
        itNetworkWidget.addSelectionListener(organizationListener);
        syncSelectedItNetworks();
        composite.pack();
    }

    @Override
    protected void initData() throws Exception {
        // nothing to do, IT networks are loaded in class ScopeMultiselectWidget
    }

    @Override
    public boolean isPageComplete() {
        boolean complete = !itNetworkIds.isEmpty();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }

    private void syncSelectedItNetworks() {
        itNetworkIds.clear();
        if (itNetworkWidget.getSelectedElementSet() != null) {
            Set<CnATreeElement> selectedOrganizations = itNetworkWidget.getSelectedElementSet();
            for (CnATreeElement organization : selectedOrganizations) {
                itNetworkIds.add(organization.getDbId());
            }
        }
        setPageComplete(isPageComplete());
    }

    public Set<CnATreeElement> getSelectedElementSet() {
        return itNetworkWidget.getSelectedElementSet();
    }

    public CnATreeElement getSelectedElement() {
        return itNetworkWidget.getSelectedElement();
    }

    public List<Integer> getItNetworkIds() {
        return itNetworkIds;
    }
}
