/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;

public class BSIMassnahmenViewDragListener implements DragSourceListener {

	private TreeViewer viewer;

	private Logger LOG = Logger.getLogger(BSIMassnahmenViewDragListener.class);

	public BSIMassnahmenViewDragListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		// do nothing
	}
	
	@Override
	public void dragSetData(DragSourceEvent event) {
	    DragSource ds = (DragSource) event.widget;
	    IStructuredSelection selection = null;
	    try{
	        selection = (IStructuredSelection)viewer.getSelection();
	        if(selection.getFirstElement() instanceof Baustein){
	            event.data = (Baustein)selection.getFirstElement();
	        }
	    } catch (Throwable t){
	        LOG.error("error: ", t);
	    }
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (!(o instanceof Baustein || o instanceof Massnahme || o instanceof Gefaehrdung)) {
				event.doit = false;
				return;	
			} else {
			    event.data = o;
			}
		}
		event.doit = true;
		dragSetData(event);
	}

}
