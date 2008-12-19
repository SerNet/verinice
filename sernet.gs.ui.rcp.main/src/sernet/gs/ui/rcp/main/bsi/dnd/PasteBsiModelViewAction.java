package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.dialogs.SanityCheckDialog;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Verknüpfe kopierte Bausteine mit selektierten Strukturobjekten.
 * 
 * @author koderman@sernet.de
 * 
 */
public class PasteBsiModelViewAction extends Action {

	private TreeViewer view;

	public PasteBsiModelViewAction(TreeViewer view, String text) {
		super(text);
		this.view = view;
		setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		setToolTipText(Messages.getString("PasteBsiModelViewAction.0")); //$NON-NLS-1$
	}

	@Override
	public void run() {
		final List items = CnPItems.getItems();
		if ( items.size() == 0 )
			return;
		
		// insert Bausteine (modules):
		if ((items.get(0) instanceof Baustein)) {
			// check if only Bausteine present:
			for (Object item : items) {
				if (!(item instanceof Baustein))
					return;
			}
			try {
				final IStructuredSelection sel = (IStructuredSelection) view
				.getSelection();
				
				if (!checkLayers(items, sel))
					return;
				
				Job dropJob = bausteinDropJob(sel);
				dropJob.setUser(true);
				dropJob.schedule();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(Messages.getString("PasteBsiModelViewAction.1"), e); //$NON-NLS-1$
			}
			
		}
		
		if (items.get(0) instanceof IBSIStrukturElement) {
			// all items must be objects that can be linked together:
			for (Object object : items) {
				if (!(object instanceof IBSIStrukturElement))
					return;
			}
			final IStructuredSelection targets = (IStructuredSelection) view
			.getSelection();
			LinkDropper dropper = new LinkDropper();
			for (Iterator iter = targets.iterator(); iter.hasNext();) {
				Object target =  iter.next();
				if (!(target instanceof IBSIStrukturElement))
					continue;
				dropper.dropLink(items, (CnATreeElement) target);
			}
		}
		
		
	}

	private Job bausteinDropJob(final IStructuredSelection targets) {
		Job dropJob = new Job(Messages.getString("PasteBsiModelViewAction.2")) { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.getString("PasteBsiModelViewAction.3"), targets.size()); //$NON-NLS-1$
				try {
					for (Iterator iter = targets.iterator(); iter.hasNext();) {
						Object o = (Object) iter.next();
						if (o instanceof CnATreeElement) {
							CnATreeElement target = (CnATreeElement) o;
							pasteBausteine(target, monitor);
						}
						monitor.worked(1);
					}
				} catch (Exception e) {
					Logger.getLogger(this.getClass()).error("Drop failed", //$NON-NLS-1$
							e);
					return Status.CANCEL_STATUS;
				}
				monitor.done();
				DNDItems.clear();
				return Status.OK_STATUS;
			}
		};
		return dropJob;
	}

	private boolean checkLayers(final List items, final IStructuredSelection sel) {
		Check: for (Iterator iter = sel.iterator(); iter.hasNext();) {
			Object o = (Object) iter.next();
			if (o instanceof CnATreeElement) {
				CnATreeElement target = (CnATreeElement) o;
			
				for (Iterator iter2 = items.iterator(); iter2.hasNext();) {
					Object sourceObject = iter2.next();
					int targetSchicht = 0;
					if (target instanceof IBSIStrukturElement)
						targetSchicht = ((IBSIStrukturElement) target)
								.getSchicht();

					if (sourceObject instanceof Baustein 
							&& target.canContain(sourceObject)) {
						Baustein sourceBst = (Baustein) sourceObject;

						if (sourceBst.getSchicht() != targetSchicht) {
							if (!SanityCheckDialog.checkLayer(view
									.getControl().getShell(), sourceBst
									.getSchicht(), targetSchicht))
								return false;
							else
								break Check; // user says he knows what
							// he's doing, stop
							// checking.
						}
					}
				}
			}
		}
	return true;
	}

	private void pasteBausteine(CnATreeElement target, IProgressMonitor mon) {
		List items = CnPItems.getItems();
		for (Iterator iter = items.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof Baustein) {
				Baustein baustein = (Baustein) o;
				if (target.canContain(baustein)) {
					try {
						mon.subTask(baustein.getTitel());
						CnAElementFactory.getInstance().saveNew(target,
								BausteinUmsetzung.TYPE_ID,
								new BuildInput<Baustein>(baustein));
					} catch (Exception e) {
						Logger.getLogger(this.getClass()).error(
								Messages.getString("PasteBsiModelViewAction.5"), e); //$NON-NLS-1$
					}
				}
			}
		}
	}

}
