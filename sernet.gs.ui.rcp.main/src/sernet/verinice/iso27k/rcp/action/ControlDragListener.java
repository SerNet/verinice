/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import sernet.gs.ui.rcp.main.bsi.dnd.DNDHelper;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.ItemTransfer;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.service.iso27k.Item;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ControlDragListener implements DragSourceListener {
	
	private static final Logger LOG = Logger.getLogger(ControlDragListener.class);
	
	TreeViewer viewer;
	
	List<IItem> dragedItemList = new ArrayList<IItem>();
	
	/**
	 * @param viewer
	 */
	public ControlDragListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void dragStart(DragSourceEvent event) {
		IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
		dragedItemList.clear();
		event.doit = true;
		
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (LOG.isDebugEnabled()) {
				LOG.debug("dragStart, selected element: " +  o);
			}
			if (!(o instanceof IItem)) {
				event.doit = false;
			} else {
				dragedItemList.add((IItem) o);
				if (LOG.isDebugEnabled()) {
					LOG.debug("dragStart, added to dragedItemList: " + ((IItem) o).getName());
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragSetData(DragSourceEvent event) {
	    try{
	        IStructuredSelection selection = ((IStructuredSelection)viewer.getSelection());
	        if(ItemTransfer.getInstance().isSupportedType(event.dataType)){
	            if(selection.getFirstElement() instanceof Item){
	                event.data = DNDHelper.castDataArray(selection.toArray());
	            } else {
	                event.data = DNDItems.CNAITEM;
	                LOG.error("Something went wrong here");
	            }
	        }
	    }catch (Exception t){
	        LOG.error("error", t);
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
	 */
	@Override
	public void dragFinished(DragSourceEvent event) {
	    dragedItemList.clear();
	}

}
