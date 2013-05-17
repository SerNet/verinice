/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.common.CnATreeElement;

public class LinkDropper {

	private static final Logger LOG = Logger.getLogger(LinkDropper.class);
	

	public boolean dropLink(final List<CnATreeElement> toDrop, final CnATreeElement target) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("dropLink...");
		}
		// Prevent creation of new link when parent is not allowed to be
		// modified.
		if (!CnAElementHome.getInstance().isWriteAllowed(target)){
			return false;
		}
		try {
			// close all editors first:
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /*
																									 * ask
																									 * save
																									 */);

			Job dropJob = new Job(Messages.getString("LinkDropper.0")) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						Activator.inheritVeriniceContextState();
						CnAElementHome.getInstance().createLinksAccordingToBusinessLogic(target, toDrop);
					} catch (Exception e) {
						LOG.error("Drop failed", e); //$NON-NLS-1$
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			};
			dropJob.schedule();
		} catch (Exception e) {
			LOG.error(Messages.getString("LinkDropper.2"), e); //$NON-NLS-1$
			return false;
		}
		return true;
	}

	

}
