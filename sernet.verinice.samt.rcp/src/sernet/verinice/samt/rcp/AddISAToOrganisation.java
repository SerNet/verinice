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
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.model.iso27k.AuditGroup;

/**
 * Action in SAMT/ISA view context menu added programmatically in {@link SamtView}.
 * Creates a new ISA in an existing organization.
 * 
 * {@link AddSelfAssessment} creates a new organization with a new ISA.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class AddISAToOrganisation extends Action implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(AddISAToOrganisation.class);
    
    public static final String ID = "sernet.verinice.samt.rcp.AddISAToOrganisation"; //$NON-NLS-1$
    
    private CreateNewSelfAssessmentService samtService = new CreateNewSelfAssessmentService();
    
    private AuditGroup auditGroup = null;
    
    private static ISchedulingRule iSchedulingRule = new Mutex();
    
    public AddISAToOrganisation(IWorkbenchWindow window) {
        super();
        setText(sernet.verinice.samt.rcp.Messages.AddISAToOrganisation_0);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOTE_NEW));
        setToolTipText(sernet.verinice.samt.rcp.Messages.AddISAToOrganisation_1);
        window.getSelectionService().addSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        WorkspaceJob importJob = new WorkspaceJob(Messages.AddISAToOrganisation_0) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.setTaskName(Messages.AddISAToOrganisation_4);
                    samtService.createSelfAssessment(AddISAToOrganisation.this.auditGroup);
                } catch (Exception e) {
                    LOG.error("Error while creating new ISA.", e); //$NON-NLS-1$
                    status = new Status(IStatus.ERROR, "sernet.verinice.samt.rcp", sernet.verinice.samt.rcp.Messages.AddISAToOrganisation_3, e); //$NON-NLS-1$
                }
                return status;
            }
        };
        JobScheduler.scheduleJob(importJob, iSchedulingRule);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {     
        boolean enabled = false;
        if (part instanceof SamtView && selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof AuditGroup) {
                this.auditGroup = (AuditGroup) element;
                enabled = true;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("enabled: " + enabled + ", audit group: " + this.auditGroup); //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.setEnabled(enabled);
    }
}
