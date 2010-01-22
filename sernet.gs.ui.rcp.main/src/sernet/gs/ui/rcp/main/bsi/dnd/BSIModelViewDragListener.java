/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.Iterator;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.verinice.iso27k.model.IISO27kElement;

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
			// check if only structure elements are selected:
			Iterator iterator = selection.iterator();
			while (iterator.hasNext()) {
				if (! (iterator.next() instanceof IBSIStrukturElement)) {
					event.doit = false;
					return;
				}
			}
		}
			
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (!(o instanceof BausteinUmsetzung
				  || o instanceof IBSIStrukturElement
				  || o instanceof IISO27kElement)
				  || o instanceof ITVerbund) {
				event.doit = false;
				return;	
			}
		}
		event.doit = true;
		DNDItems.setItems(selection.toList());
	}

}
