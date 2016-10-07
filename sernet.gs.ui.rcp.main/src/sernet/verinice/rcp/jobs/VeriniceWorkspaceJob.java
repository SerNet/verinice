/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.jobs;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.*;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public abstract class VeriniceWorkspaceJob extends WorkspaceJob {

    private static final Logger LOG = Logger.getLogger(VeriniceWorkspaceJob.class);
    String errorMessage;

    public VeriniceWorkspaceJob(String name, String errorMessage) {
        super(name);
        this.errorMessage = errorMessage;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

        IStatus status = Status.OK_STATUS;

        try {
            monitor.beginTask("Running Task \"" + this.getName() + "\"",
                    IProgressMonitor.UNKNOWN);
            doRunInWorkspace();
        } catch (Exception e) {
            LOG.error("Error while running job " + this.getName(), e); //$NON-NLS-1$
            status = new Status(Status.ERROR, "sernet.verinice.samt.rcp", //$NON-NLS-1$
                    errorMessage, e);
        } finally {
            monitor.done();
            this.done(status);
        }
        return status;
    }

    protected abstract void doRunInWorkspace();

}
