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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.IGSModelElementTransfer;

public class BSIMassnahmenViewDragListener implements DragSourceListener {

	private TreeViewer viewer;

	public BSIMassnahmenViewDragListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public void dragFinished(DragSourceEvent event) {
		// do nothing
	}
	
	public void dragSetData(DragSourceEvent event) {
	    IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
	    if(validateDrag(event)){
	        event.data = DNDHelper.castDataArray(selection.toArray());
	        
	    }
	}

	public void dragStart(DragSourceEvent event) {
        IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
        if(selection==null) {
            event.doit = false;
            return; 
        }
        List selectionList = new ArrayList(selection.size());
        
        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object o = iter.next();
            selectionList.add(o);
            if (!(o instanceof Baustein
                  || o instanceof Gefaehrdung
                  || o instanceof Massnahme)) {
                event.doit = false;
                return; 
            }
        }
        event.doit = true;
	}
	
	private boolean validateDrag(DragSourceEvent event){
	    return (IGSModelElementTransfer.getInstance().isSupportedType(event.dataType));
	}

}
