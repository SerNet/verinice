/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah[at]sernet[dot]de>
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Anne Hanekop <ah[at]sernet[dot]de> 	- initial API and implementation
 *     ak[at]sernet[dot]de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzungFactory;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Defines what to do, when a Massnahme is dragged from the TableViewer to the
 * TreeViewer.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class RisikoMassnahmenUmsetzungDragListener implements DragSourceListener {

    private static final Logger LOG = Logger.getLogger(RisikoMassnahmenUmsetzungDragListener.class);
    
    private TableViewer viewer;
    private CnATreeElement cnaElement;


    ArrayList<RisikoMassnahmenUmsetzung> risikoMassnahmenUmsetzungen;
    
    /**
     * Constructor sets the needed data.
     * 
     * @param newViewer
     *            the viewer containing the dragged element
     * @param newCnaElement
     *            the element to drag
     */
    public RisikoMassnahmenUmsetzungDragListener(TableViewer newViewer, CnATreeElement newCnaElement) {
        viewer = newViewer;
        cnaElement = newCnaElement;
    }

    /**
     * starts drag if necessary
     */
    public void dragStart(DragSourceEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("dragStart...");
        }
        try {
            IStructuredSelection selection = ((IStructuredSelection) viewer.getSelection());
            risikoMassnahmenUmsetzungen = new ArrayList<RisikoMassnahmenUmsetzung>();
    
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
    
                if (!(object instanceof RisikoMassnahmenUmsetzung || object instanceof MassnahmenUmsetzung)) {            
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("dragStart, wrong object");
                    }
                    
                    event.doit = false;
                    return;
    
                } else if (object instanceof RisikoMassnahmenUmsetzung) {
                    
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("dragStart, RisikoMassnahmenUmsetzung");
                    }
                    
                    /*
                     * object is of type RisikoMassnahmenUmsetzung - create instance
                     * for target object and add it
                     */
                    RisikoMassnahmenUmsetzung umsetzung = RisikoMassnahmenUmsetzungFactory.buildFromRisikomassnahmenUmsetzung((RisikoMassnahmenUmsetzung) object, cnaElement, null);
                    risikoMassnahmenUmsetzungen.add(umsetzung);
    
                } else {
                    
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("dragStart, MassnahmenUmsetzung");
                    }
                    /*
                     * object is of type MassnahmenUmsetzung - create instance for
                     * target object and add it
                     */
                    RisikoMassnahmenUmsetzung umsetzung = RisikoMassnahmenUmsetzungFactory.buildFromMassnahmenUmsetzung((MassnahmenUmsetzung) object, cnaElement, null);
                    risikoMassnahmenUmsetzungen.add(umsetzung);
                }
            }
            event.doit = true;
            DNDItems.setItems(risikoMassnahmenUmsetzungen);
            if (LOG.isDebugEnabled()) {
                LOG.debug("dragStart finished");
            }
        } catch( Throwable t) {
            LOG.error("Error in dragStart", t);
        }
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */
    public void dragSetData(DragSourceEvent event) {
        event.data = DNDItems.RISIKOMASSNAHMENUMSETZUNG;
        DNDItems.setItems(risikoMassnahmenUmsetzungen);
    }

    /**
     * Nothing to do after drag completed. Must be implemented due to
     * DragSourceListener.
     * 
     * @param event
     *            the information associated with the drag finished event
     */
    public void dragFinished(DragSourceEvent event) {
        risikoMassnahmenUmsetzungen.clear();
    }
}
