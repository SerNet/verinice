/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class LinkTableHandler extends RightsEnabledHandler {

    private static final Logger LOG = Logger.getLogger(LinkTableHandler.class);
    
    public LinkTableHandler() {
       super(false);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.EXPORT_LINK_TABLE;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if(checkRights()){
            final VeriniceLinkTable veriniceLinkTable = createLinkTable();
            if (veriniceLinkTable != null) {

                UIJob job = new UIJob("open vlt file") {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        IStatus status = Status.OK_STATUS;

                        try {
                            monitor.beginTask("open vlt file",
                                    IProgressMonitor.UNKNOWN);
                            EditorFactory.getInstance().updateAndOpenObject(veriniceLinkTable);
                        } catch (Exception e) {
                            LOG.error("Error while running job " + this.getName(), e); //$NON-NLS-1$
                            status = new Status(Status.ERROR, "sernet.verinice.samt.rcp", //$NON-NLS-1$
                                    "Error while open vlt-file", e);
                        } finally {
                            monitor.done();
                            this.done(status);
                        }
                        return status;
                    }
                };
                job.schedule();

            }
        } else {
            setBaseEnabled(false);
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error", "You don't have the permission to perform this action.");
        }
        
        return null;
    }

    protected abstract VeriniceLinkTable createLinkTable();

}
