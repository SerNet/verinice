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
package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.actions.Messages;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.samt.audit.rcp.ElementView;

/**
 * Action in SAMT/ISA view context menu added programmatically in {@link SamtView}.
 * Creates a new ISA in an existing organization.
 * 
 * {@link AddSelfAssessment} creates a new organization with a new ISA.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("restriction")
public class AddISAToOrganisation extends Action implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(AddISAToOrganisation.class);
    
    public static final String ID = "sernet.verinice.samt.rcp.AddISAToOrganisation"; //$NON-NLS-1$
    
    private CreateNewSelfAssessmentService samtService = new CreateNewSelfAssessmentService();
    
    private AuditGroup auditGroup = null;
    
    public AddISAToOrganisation(IWorkbenchWindow window) {
        super();
        setText(sernet.verinice.samt.rcp.Messages.AddISAToOrganisation_0);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
        setToolTipText(sernet.verinice.samt.rcp.Messages.AddISAToOrganisation_1);
        window.getSelectionService().addSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        try {
            samtService.createSelfAssessment(this.auditGroup);
        } catch (Exception e) {
            LOG.error("Error while naturalizing element", e); //$NON-NLS-1$
            ExceptionUtil.log(e, sernet.verinice.samt.rcp.Messages.AddISAToOrganisation_3);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {     
        boolean enabled = false;
        if (part instanceof SamtView) {
            if (selection instanceof IStructuredSelection) {
                Object element = ((IStructuredSelection) selection).getFirstElement();
                if (element instanceof AuditGroup) {
                    this.auditGroup = (AuditGroup) element;
                    enabled = true;
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("enabled: " + enabled + ", audit group: " + this.auditGroup); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.setEnabled(enabled);
    }
}
