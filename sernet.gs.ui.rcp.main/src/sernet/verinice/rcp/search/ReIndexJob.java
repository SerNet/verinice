/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin
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
package sernet.verinice.rcp.search;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;

/**
 * Creates the search index in background without blocking the UI thread.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ReIndexJob extends WorkspaceJob {

    private final Action reindex;

    private final Logger LOG = Logger.getLogger(ReIndexJob.class);

    private IProgressMonitor mon;

    public ReIndexJob(Action reindex) {
        super(Messages.IndexJob_0);
        this.reindex = reindex;
    }

    @Override
    public IStatus runInWorkspace(IProgressMonitor mon) throws CoreException {

        this.mon = mon;

        try {
            deactivateReindexButton();

            Activator.inheritVeriniceContextState();

            ServiceFactory.lookupSearchService().reindex();
            mon.done();

        } finally {
            activateReIndexButton();
        }

        return Status.OK_STATUS;
    }

    private void activateReIndexButton() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                reindex.setEnabled(true);

            }
        });
    }

    private void deactivateReindexButton() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                reindex.setEnabled(false);
            }
        });
    }


    /**
     * This does not canceling the server side reindexing.
     */
    @Override
    protected void canceling() {
        mon.setCanceled(true);
        getThread().interrupt();
        LOG.debug("reindexing was cancelled");
    }

}
