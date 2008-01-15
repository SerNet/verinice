package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.widgets.TreeItem;

import sernet.gs.model.Baustein;

public class BSIMassnahmenViewDragListener implements DragSourceListener {

	private TreeViewer viewer;

	public BSIMassnahmenViewDragListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public void dragFinished(DragSourceEvent event) {
		// do nothing
	}

	public void dragSetData(DragSourceEvent event) {
		event.data = DNDItems.BAUSTEIN;
	}

	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			if (!(iter.next() instanceof Baustein)) {
				event.doit = false;
				return;	
			}
		}
		event.doit = true;
		DNDItems.setItems(selection.toList());
	}

}
