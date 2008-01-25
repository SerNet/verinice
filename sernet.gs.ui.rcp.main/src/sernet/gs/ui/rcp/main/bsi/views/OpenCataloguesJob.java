package sernet.gs.ui.rcp.main.bsi.views;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

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
		try {
			BSIKatalogInvisibleRoot.getInstance().loadModel(monitor);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					Messages.BSIMassnahmenView_1, e);
		}
		return Status.OK_STATUS;
	}
}
