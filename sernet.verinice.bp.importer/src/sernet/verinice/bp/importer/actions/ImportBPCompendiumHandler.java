/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.importer.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.bp.importer.preferences.PreferenceConstants;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.rcp.linktable.Messages;
import sernet.verinice.service.commands.ImportBPCompendium;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ImportBPCompendiumHandler extends RightsEnabledHandler {
    
    public static final Logger LOG = Logger.getLogger(ImportBPCompendiumHandler.class);
    
    public static final String ID = "sernet.verinice.bp.importer.importaction"; //$NON-NLS-1$

    public ImportBPCompendiumHandler() {
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.BPIMPORTER;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if(checkRights()){

                UIJob job = new UIJob(PlatformUI.getWorkbench().getDisplay(),
                        Messages.LinkTableHandler_0) {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        IStatus status = Status.OK_STATUS;

                        try {
                            monitor.beginTask("Import GS-Compendium",
                                    IProgressMonitor.UNKNOWN);
                            doImport();

                        } catch (Exception e) {

                            LOG.error("Error while running job " + this.getName(), e);
                            status = new Status(Status.ERROR, "sernet.verinice.bp.importer.actions",
                                    "Error importing BSI-GS-Compendium", e);
                        } finally {
                            monitor.done();
                            this.done(status);
                        }
                        return status;
                    }

                };
                job.schedule();

        } else {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error", //$NON-NLS-1$
                    Messages.ExportLinkTableHandler_1);
        }
        
        return null;

    }
    
    private void doImport() {
        String xmlRoot = Activator.getDefault().getPreferenceStore().
                getString(PreferenceConstants.XML_ROOT_DIRECTORY);
        try {
            ImportBPCompendium command = new ImportBPCompendium(xmlRoot);
            ServiceFactory.lookupCommandService().executeCommand(command);
        } catch (CommandException e) {
            ExceptionUtil.log(e, "Something went wrong importing BSI BP-Compendium from " + xmlRoot);
        } 
    }
}
