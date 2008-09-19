package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.jface.viewers.TableViewer;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzungFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class RisikoMassnahmenUmsetzungDragListener implements
		DragSourceListener {

	private TableViewer viewer;
	private CnATreeElement cnaElement;

	public RisikoMassnahmenUmsetzungDragListener(TableViewer newViewer,
			CnATreeElement newCnaElement) {
		viewer = newViewer;
		cnaElement = newCnaElement;
	}

	/**
	 * nothing to do after drag completed
	 */
	public void dragFinished(DragSourceEvent event) {
		// nothing to do
	}

	public void dragSetData(DragSourceEvent event) {
		event.data = DNDItems.RISIKOMASSNAHMENUMSETZUNG;
	}

	/**
	 * starts drag if necessary
	 */
	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = ((IStructuredSelection) viewer
				.getSelection());
		ArrayList<RisikoMassnahmenUmsetzung> risikoMassnahmenUmsetzungen = new ArrayList<RisikoMassnahmenUmsetzung>();

		/* leave, if selcetion is empty */
		if (selection.size() < 1) {
			event.doit = false;
			return;
		}

		/*
		 * process RisikoMassnahmenUmsetzungen cast MassnahmenUmsetzungen to
		 * RisikoMassnahmenUmsetzungen
		 */
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object object = iter.next();
			if (!(object instanceof RisikoMassnahmenUmsetzung || object instanceof MassnahmenUmsetzung)) {
				event.doit = false;
				return;
			} else if (object instanceof RisikoMassnahmenUmsetzung) {

				/*
				 * object is of type RisikoMassnahmenUmsetzung - create instance
				 * for target object and add it
				 */
				RisikoMassnahmenUmsetzung umsetzung = RisikoMassnahmenUmsetzungFactory
						.buildFromRisikomassnahmenUmsetzung((RisikoMassnahmenUmsetzung) object,
								cnaElement, 
								null);

				risikoMassnahmenUmsetzungen.add(umsetzung);

				Logger.getLogger(this.getClass()).debug(
						"drag start - RisikoMassnahmenUmsetzung "
								+ ((RisikoMassnahmenUmsetzung) object)
										.getTitel());
			} else {/*
				 * object is of type MassnahmenUmsetzung - convert to
				 * RisikoMassnahmenUmsetzung before adding it
				 */
				RisikoMassnahmenUmsetzung umsetzung = RisikoMassnahmenUmsetzungFactory
						.buildFromMassnahmenUmsetzung((MassnahmenUmsetzung) object,
								cnaElement, 
								null);
		
				risikoMassnahmenUmsetzungen.add(umsetzung);
				Logger.getLogger(this.getClass()).debug(
						"drag start - MassnahmenUmsetzung "
								+ ((MassnahmenUmsetzung) object).getTitel());
			}

		}
		event.doit = true;
		DNDItems.setItems(risikoMassnahmenUmsetzungen);
	}
}
