/*******************************************************************************
 * Copyright (c) 2015 Moritz Reiter.
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
 *     Moritz Reiter - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.bsi.editors;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.views.RelationTableViewer;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.task.FindRelationsFor;

/**
 * @author Moritz Reiter
 */
final class ReloadLinksWorkspaceJob extends WorkspaceJob {

    private static final Logger LOGGER = Logger.getLogger(ReloadLinksWorkspaceJob.class);

    private final CnATreeElement inputElement;
    private final RelationTableViewer viewer;

    ReloadLinksWorkspaceJob(CnATreeElement inputElement, RelationTableViewer viewer, String name) {
        
        super(name);
        this.inputElement = inputElement;
        this.viewer = viewer;
    }

    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) {
        
        Activator.inheritVeriniceContextState();

        try {
            monitor.setTaskName(Messages.LinkMaker_11);

            FindRelationsFor command = new FindRelationsFor(inputElement);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            final CnATreeElement linkElmt = command.getElmt();

            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.setInput(linkElmt);
                }
            });
        } catch (Exception e) {
            
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    viewer.setInput(new PlaceHolder(Messages.LinkMaker_12));
                }
            });
            
            LOGGER.error("Error while searching relations", e);
            ExceptionUtil.log(e, Messages.LinkMaker_13);
        }
        return Status.OK_STATUS;
    }
}
