/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.verinice.iso27k.model.IISO27kElement;

public class LinkDropper {
	public boolean dropLink(final List<CnATreeElement> toDrop,
			final CnATreeElement target) {
			
			// Prevent creation of new link when parent is not allowed to be modified. 
			if (!CnAElementHome.getInstance().isWriteAllowed(target))
				return false;
			
			try {
				// close all editors first:
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /* ask save */);
				
				Job dropJob = new Job(Messages.getString("LinkDropper.0")) { //$NON-NLS-1$
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							createLink(target, toDrop);
						} catch (Exception e) {
							Logger.getLogger(this.getClass()).error("Drop failed", e); //$NON-NLS-1$
							return Status.CANCEL_STATUS;
						}
						DNDItems.clear();
						return Status.OK_STATUS;
					}
				};
				dropJob.schedule();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(Messages.getString("LinkDropper.2"), e); //$NON-NLS-1$
				return false;
			}
			return true;
		}

		private void createLink(final CnATreeElement dropTarget, final List<CnATreeElement> toDrop) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					List<CnALink> newLinks = new ArrayList<CnALink>();
					for (CnATreeElement dragged : toDrop) {
						try {
							CnALink link = CnAElementHome.getInstance().createLink(dropTarget, dragged);
							newLinks.add(link);
						} catch (Exception e) {
							Logger.getLogger(this.getClass()).debug("Saving link failed.", e); //$NON-NLS-1$
						}
					}
					for (CnALink link : newLinks) {
						if (link.getDependant() instanceof ITVerbund) {
							CnAElementFactory.getInstance().reloadModelFromDatabase();
							return;
						}
						else {
							if(link.getDependant() instanceof IBSIStrukturElement
							   || link.getDependency() instanceof IBSIStrukturElement) {
								CnAElementFactory.getLoadedModel().linkChanged(link);
							}
							if(link.getDependant() instanceof IISO27kElement
							   || link.getDependency() instanceof IISO27kElement) {
								CnAElementFactory.getInstance().getISO27kModel().linkChanged(link);
							}
						}
					}
				}
			});
		}


}
