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
package sernet.gs.ui.rcp.main.bsi.views;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;

/**
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class OpenCataloguesJob extends WorkspaceJob {
	
	public OpenCataloguesJob(String name) {
		super(name);
	}

	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		Activator.inheritVeriniceContextState();
		
		try {
			BSIKatalogInvisibleRoot.getInstance().loadModel(monitor);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					Messages.BSIMassnahmenView_1, e);
		}
		return Status.OK_STATUS;
	}
}
