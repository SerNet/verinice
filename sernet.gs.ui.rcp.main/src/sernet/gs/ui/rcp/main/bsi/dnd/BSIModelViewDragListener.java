package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.widgets.TreeItem;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;

public class BSIModelViewDragListener implements DragSourceListener {

	private TreeViewer viewer;

	public BSIModelViewDragListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public void dragFinished(DragSourceEvent event) {
		// do nothing
	}

	public void dragSetData(DragSourceEvent event) {
		event.data = DNDItems.CNAITEM;
	}

	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
		if (selection.size() != 1) {
			event.doit = false;
			return;
		}
			
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (!(o instanceof BausteinUmsetzung
					|| o instanceof IBSIStrukturElement)
				|| o instanceof ITVerbund) {
				event.doit = false;
				return;	
			}
		}
		event.doit = true;
		DNDItems.setItems(selection.toList());
	}

}
