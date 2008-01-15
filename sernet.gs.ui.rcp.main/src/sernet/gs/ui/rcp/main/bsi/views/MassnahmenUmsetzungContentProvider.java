package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Gets Massnahmen from current BSIModel and reacts to model changes.
 * 
 * Update performed in synchronized thread, but only if
 * necessary, to optimize performance.
 * 
 * @author koderman@sernet.de
 *
 */
class MassnahmenUmsetzungContentProvider implements IStructuredContentProvider, 
				IBSIModelListener {

	final int ADD     = 0;
	final int UPDATE  = 1;
	final int REMOVE  = 2;
	final int REFRESH = 3;
	
	
	private TableViewer viewer;
	private BSIModel model;
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		if (model != null)
			model.removeBSIModelListener(this);
		model = (BSIModel) newInput;
		if (model != null)
			model.addBSIModelListener(this);
		modelRefresh();
	}

	public Object[] getElements(Object inputElement) {
		if (model == null)
			return new Object[] {};
		ArrayList<MassnahmenUmsetzung> mns = model.getMassnahmen();
		return mns.toArray(new Object[mns.size()]);
		
	}

	public void dispose() {
	}
	
	public void childAdded(CnATreeElement category, CnATreeElement child) {
		if (child instanceof MassnahmenUmsetzung) {
			updateViewer(ADD, child);
		}
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		if (child instanceof MassnahmenUmsetzung)
			updateViewer(UPDATE, child);
	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		if (child instanceof MassnahmenUmsetzung) {
			updateViewer(REMOVE, child);
		}
	}
	
	public void modelRefresh() {
		updateViewer(this.REFRESH, null);
	}
	
	
	private void updateViewer(final int type, final CnATreeElement child) {
		if (Display.getCurrent() != null) {
			switch (type) {
			case ADD:
				viewer.add(child);
				return;
			case UPDATE:
				viewer.refresh(child);
				return;
			case REMOVE:
				viewer.remove(child);
				return;
			case REFRESH:
				viewer.refresh();
				return;
			}
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				switch (type) {
				case ADD:
					viewer.add(child);
					return;
				case UPDATE:
					viewer.refresh(child);
					return;
				case REMOVE:
					viewer.remove(child);
					return;
				case REFRESH:
					viewer.refresh();
					return;
				}
			}
		});
	}
}