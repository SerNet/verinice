package sernet.gs.ui.rcp.main.bsi.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnALink;
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
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TableViewer) viewer;
		attachModelListener();
		modelRefresh();
	}

	private void attachModelListener() {
		BSIModel model = CnAElementFactory.getLoadedModel();
		model.removeBSIModelListener(this);
		model.addBSIModelListener(this);
	}

	public Object[] getElements(Object inputElement) {
		List<MassnahmenUmsetzung> mns = (List<MassnahmenUmsetzung>) inputElement;
		return mns.toArray(new Object[mns.size()]);
		
	}

	public void dispose() {
	}
	
	public void childAdded(CnATreeElement category, CnATreeElement child) {
		if (child instanceof MassnahmenUmsetzung) {
			updateViewer(ADD, child);
		}
	}
	
	public void linkChanged(CnALink link) {
		if (link.getDependency() instanceof Person)
			updateViewer(this.REFRESH, null);
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