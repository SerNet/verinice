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
 *     Robert Schuster <r.schuster@tarent.de> - add possibility to override config
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIConfig;
import sernet.gs.ui.rcp.main.common.model.ProgressAdapter;

/**
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class OpenCataloguesJob extends WorkspaceJob {
	
	private BSIMassnahmenModel model;
	
	/**
	 * Creates a workspace job which loads the catalues with
	 * its current configuration.
	 * 
	 * @param name
	 */
	public OpenCataloguesJob(String name) {
		this(name, null);
	}

	/**
	 * Creates a workspace job which loads the catalues with
	 * the given configuration.
	 * 
	 * <p>If the {@link IBSIConfig} instance is <code>null</code>
	 * the model's current configuration is used instead.</p>
	 * 
	 * <p>Using a non-<code>null</code> configuration object
	 * makes the catalogue model discard already loaded data.</p>
	 * 
	 * @param name
	 * @see {@link BSIMassnahmenModel#setBSIConfig(IBSIConfig)}
	 */
	public OpenCataloguesJob(String name, IBSIConfig newConfig) {
		super(name);
		
		model = GSScraperUtil.getInstance().getModel();
		
		// Using a new configuration object makes it throw
		// already loaded data.
		if (newConfig != null){
			model.setBSIConfig(newConfig);
		}
	}

	public IStatus runInWorkspace(IProgressMonitor monitor)
			throws CoreException {
		// Needed for access to 'commandService' when using a remote 
		Activator.inheritVeriniceContextState();
		
		try {
			List<Baustein> bausteine = model.loadBausteine(new ProgressAdapter(monitor));

			BSIKatalogInvisibleRoot.getInstance().setBausteine(bausteine);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					Messages.BSIMassnahmenView_1, e);
		}
		return Status.OK_STATUS;
	}
}
