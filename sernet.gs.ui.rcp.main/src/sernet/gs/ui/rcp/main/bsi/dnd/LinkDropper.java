package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class LinkDropper {
	public boolean dropLink(final List<CnATreeElement> toDrop,
			final CnATreeElement target) {
			
			
			try {
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

		private void createLink(final CnATreeElement dropTarget, 
				final List<CnATreeElement> toDrop) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					for (CnATreeElement dragged : toDrop) {
						CnALink link = new CnALink(dropTarget, dragged);
						try {
							CnAElementHome.getInstance().save(link);
						} catch (Exception e) {
							Logger.getLogger(this.getClass()).debug("Saving link failed."); //$NON-NLS-1$
							link.remove();
						}
					}
				}
			});
		}


}
