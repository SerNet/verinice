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
package sernet.verinice.iso27k.rcp;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;

/**
 * A diialog to start the execution of a ISO/IEC 27005 risk analysis.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class RiskAnalysisDialog extends TitleAreaDialog {

    private static final Logger LOG = Logger.getLogger(RiskAnalysisDialog.class);

    private ITreeSelection selection;
    private CnATreeElement selectedElement;
    private List<Integer> organizationIds = new LinkedList<>();

    private ElementMultiselectWidget organizationWidget = null;

    public RiskAnalysisDialog(Shell activeShell) {
        this(activeShell, (CnATreeElement) null);
    }

    public RiskAnalysisDialog(Shell activeShell, ITreeSelection selection){
        this(activeShell, (CnATreeElement)null);
        this.selection = selection;
    }
    
    public RiskAnalysisDialog(Shell activeShell, CnATreeElement selectedOrganization) {
        super(activeShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        selectedElement = selectedOrganization;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final int layoutMarginWidth = 10;
        final int layoutMarginHeight = layoutMarginWidth;

        /*
         * Dialog title, message and layout:
         */

        setTitle("ISO/IEC 27005 Risk Analysis");
        setMessage(
                "Select at one or more organizations. Click OK to run the risk analysis on this organizations.");

        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = (GridLayout) composite.getLayout();
        layout.marginWidth = layoutMarginWidth;
        layout.marginHeight = layoutMarginHeight;
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gd);

        try {
            organizationWidget = new ElementMultiselectWidget(composite, selection,
                    selectedElement);

        } catch (CommandException ex) {
            LOG.error("Error while loading organizations", ex); //$NON-NLS-1$
            setMessage(Messages.SamtExportDialog_4, IMessageProvider.ERROR);
            return null;
        }

        SelectionListener organizationListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button checkbox = (Button) e.getSource();
                if (checkbox.getSelection()) {

                }
                super.widgetSelected(e);
            }
        };

        organizationWidget.addSelectionLiustener(organizationListener);

        

        composite.pack();
        return composite;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        organizationIds.clear();
        if (organizationWidget.getSelectedElementSet() != null) {
            Set<CnATreeElement> selectedOrganizations  = organizationWidget.getSelectedElementSet();
            for (CnATreeElement organization : selectedOrganizations) {
                organizationIds.add(organization.getDbId());
            }
        }
        super.okPressed();
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
