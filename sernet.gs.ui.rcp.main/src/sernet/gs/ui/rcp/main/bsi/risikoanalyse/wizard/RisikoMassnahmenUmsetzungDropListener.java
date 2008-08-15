/**
 * 
 */
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.w3c.dom.Node;

import sernet.gs.scraper.GSScraper;
import sernet.gs.scraper.IGSSource;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * @author ahanekop@sernet.de
 *
 */
public class RisikoMassnahmenUmsetzungDropListener extends ViewerDropAdapter {
	
	private GefaehrdungsUmsetzung parent;
	private RisikoMassnahmenUmsetzung child;
	private TreeViewer viewer;
	
	public RisikoMassnahmenUmsetzungDropListener(TreeViewer newViewer) {
		super(newViewer);
		viewer = newViewer;
	}

	/**
	 * Adds a RiskoMassnahmenUmsetzung, which is being dropped onto a
	 * RiskoGefaehrdungsMassnahme.
	 */
	@Override
	public boolean performDrop(Object data) {
		Logger.getLogger(this.getClass()).debug("drop finish - performDrop()");

		/* get Object on which the drop is being applied */
		Object receiver = getCurrentTarget();

		/* get Object, which is beeing dropped */
		Object toDrop = DNDItems.getItems().get(0);

		try {
			parent = (GefaehrdungsUmsetzung) receiver;
			child = (RisikoMassnahmenUmsetzung) toDrop;

			if (child != null && child instanceof RisikoMassnahmenUmsetzung
					&& parent != null
					&& parent instanceof GefaehrdungsUmsetzung) {
				child.setGefaehrdungsBaumParent(parent);
				parent.addGefaehrdungsBaumChild(child);
				viewer.refresh();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).debug(e.toString());
			return false;
		}
	}

	/**
	 * retruns true, if drop is allowed. Which is only the case if
	 * the target is a GefaehrdungsUmsetzung.
	 */
	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		Logger.getLogger(this.getClass()).debug("drop finish - validateDrop()");
		if (target == null || ! (target instanceof GefaehrdungsUmsetzung)) {
			return false;
		} else {
			return true;
		}
	}
}
