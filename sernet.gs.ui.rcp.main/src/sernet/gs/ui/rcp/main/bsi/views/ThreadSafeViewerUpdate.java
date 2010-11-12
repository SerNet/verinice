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
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * Helper to execute viewer updates from any thread.
 * Avoids the overhead of synchronizing if access is from the same thread.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class ThreadSafeViewerUpdate {

	private TreeViewer viewer;

	public ThreadSafeViewerUpdate(TreeViewer viewer) {
		this.viewer = viewer;
	}
	
	public void add(final Object parent, final Object child) {
		if (viewer.getControl().isDisposed())
			return;
		
		if (Display.getCurrent() != null) {
			viewer.add(parent, child);
		}
		else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.add(parent, child);
				}
			});
			
		}
		
	}
	
	public void refresh(final Object child) {
		if (viewer.getControl().isDisposed())
			return;
		
		if (Display.getCurrent() != null) {
			viewer.refresh(child);
		}
		else {
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.refresh(child);
				}
			});
		}
	}
	
	public void remove(final Object child) {
		if (viewer.getControl().isDisposed())
			return;
		
		if (Display.getCurrent() != null) {
			viewer.remove(child);
		}
		else {
			
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.remove(child);
				}
			});
		}
	}	
	
	public void refresh() {
		
		if (Display.getCurrent() != null) {
				viewer.refresh();
		}
		else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (viewer != null)
						viewer.refresh();
				}
			});
		}
	}
	
	public void reveal(final Object child) {
		if (viewer.getControl().isDisposed())
			return;
		
		if (Display.getCurrent() != null) {
			if (!(child instanceof MassnahmenUmsetzung))
				viewer.reveal(child);
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!(child instanceof MassnahmenUmsetzung))
					viewer.reveal(child);
			}
		});
	}

	public void setInput(final Object newModel) {
		//dmurygin, 2010-09-16:
	    if (viewer.getControl().isDisposed())
			return;
		
		if (Display.getCurrent() != null) {
			viewer.setInput(newModel);
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				viewer.setInput(newModel);
			}
		});
	}

}
