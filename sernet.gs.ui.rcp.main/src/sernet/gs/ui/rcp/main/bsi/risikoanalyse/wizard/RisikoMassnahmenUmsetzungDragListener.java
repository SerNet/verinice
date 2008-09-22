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

/**
 * Defines what to do, when a Massnahme is dragged from the TableViewer
 * to the TreeViewer.
 * 
 * @author ahanekop@sernet.de
 */
public class RisikoMassnahmenUmsetzungDragListener implements
		DragSourceListener {

	private TableViewer viewer;
	private CnATreeElement cnaElement;

	/**
	 * Constructor sets the needed data.
	 * 
	 * @param newViewer the viewer containing the dragged element
	 * @param newCnaElement the element to drag
	 */
	public RisikoMassnahmenUmsetzungDragListener(TableViewer newViewer,
			CnATreeElement newCnaElement) {
		viewer = newViewer;
		cnaElement = newCnaElement;
	}
	
	/**
	 * Set information about the event.
	 * 
	 * @param event the event information
	 */
	public void dragSetData(DragSourceEvent event) {
		event.data = DNDItems.RISIKOMASSNAHMENUMSETZUNG;
	}

	/**
	 * starts drag if necessary
	 */
	public void dragStart(DragSourceEvent event) {
		
		IStructuredSelection selection = ((IStructuredSelection) viewer
				.getSelection());
		ArrayList<RisikoMassnahmenUmsetzung> risikoMassnahmenUmsetzungen =
				new ArrayList<RisikoMassnahmenUmsetzung>();

		/* leave, if selection is empty */
		if (selection.size() < 1) {
			event.doit = false;
			return;
		}

		/*
		 * process RisikoMassnahmenUmsetzungen or cast MassnahmenUmsetzungen to
		 * RisikoMassnahmenUmsetzungen
		 */
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			
			Object object = iter.next();
			
			if (!(object instanceof RisikoMassnahmenUmsetzung ||
					object instanceof MassnahmenUmsetzung)) {
				event.doit = false;
				return;
				
			} else if (object instanceof RisikoMassnahmenUmsetzung) {
				/*
				 * object is of type RisikoMassnahmenUmsetzung - create
				 * instance for target object and add it
				 */
				RisikoMassnahmenUmsetzung umsetzung =
						RisikoMassnahmenUmsetzungFactory
								.buildFromRisikomassnahmenUmsetzung(
										(RisikoMassnahmenUmsetzung) object,
										cnaElement,
										null);
				risikoMassnahmenUmsetzungen.add(umsetzung);
				
			} else {
				/*
				 * object is of type MassnahmenUmsetzung - create
				 * instance for target object and add it
				 */
				RisikoMassnahmenUmsetzung umsetzung =
						RisikoMassnahmenUmsetzungFactory
								.buildFromMassnahmenUmsetzung(
										(MassnahmenUmsetzung) object,
										cnaElement,
										null);
				risikoMassnahmenUmsetzungen.add(umsetzung);
			}
		}
		event.doit = true;
		DNDItems.setItems(risikoMassnahmenUmsetzungen);
	}
	
	/**
	 * Nothing to do after drag completed.
	 * Must be implemented due to DragSourceListener.
	 * 
	 * @param event the information associated with the drag finished event
	 */
	public void dragFinished(DragSourceEvent event) {}
}
