package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Helper to execute viewer updates from any thread.
 * Avoids the overhead of synchronizing if access is from the same thread.
 * 
 * @author koderman@sernet.de
 *
 */
public class ThreadSafeViewerUpdate {

	private TreeViewer viewer;

	public ThreadSafeViewerUpdate(TreeViewer viewer) {
		this.viewer = viewer;
	}
	
	public void add(final Object parent, final Object child) {
		if (Display.getCurrent() != null) {
			viewer.add(parent, child);
		}
		else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.add(parent, child);
				}
			});
			
		}
		
	}
	
	public void refresh(final Object child) {
		if (Display.getCurrent() != null) {
			viewer.refresh(child);
		}
		else {
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.refresh(child);
				}
			});
		}
	}
	
	public void remove(final Object child) {
		if (Display.getCurrent() != null) {
			viewer.remove(child);
		}
		else {
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.remove(child);
				}
			});
		}
	}	
	
	public void refresh() {
		if (Display.getCurrent() != null) {
			viewer.refresh();
		}
		else {
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (viewer != null && !viewer.getControl().isDisposed())
						viewer.refresh();
				}
			});
		}
	}
	
	public void reveal(final Object child) {
		if (Display.getCurrent() != null) {
			if (!(child instanceof MassnahmenUmsetzung))
				viewer.reveal(child);
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!(child instanceof MassnahmenUmsetzung))
					viewer.reveal(child);
			}
		});
	}

}
