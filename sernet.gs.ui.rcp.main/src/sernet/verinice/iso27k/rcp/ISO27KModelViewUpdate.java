/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import org.eclipse.jface.viewers.TreeViewer;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.views.ThreadSafeViewerUpdate;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.verinice.iso27k.model.IISO27KModelListener;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public class ISO27KModelViewUpdate implements IISO27KModelListener {

	private TreeViewerCache cache;
	private ThreadSafeViewerUpdate updater;
	
	public ISO27KModelViewUpdate(TreeViewer viewer, TreeViewerCache cache) {
		super();
		this.cache = cache;
		this.updater = new ThreadSafeViewerUpdate(viewer);
	}


	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildRemoved(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void databaseChildRemoved(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		CnATreeElement cachedChild = cache.getCachedObject(child);

		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
			cachedParent.removeChild(cachedChild);
		}
		updater.refresh();
	}


	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#childAdded(sernet.gs.ui.rcp.main.common.model.CnATreeElement, sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void childAdded(CnATreeElement category, CnATreeElement child) {
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildAdded(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void databaseChildAdded(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
			cachedParent.addChild(child);
		}
		updater.refresh();	
	}


	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#modelRefresh(java.lang.Object)
	 */
	public void modelRefresh(Object object) {
		updater.refresh();
	}


	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#childChanged(sernet.gs.ui.rcp.main.common.model.CnATreeElement, sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void childChanged(CnATreeElement category, CnATreeElement child) {
		CnATreeElement cachedObject = cache.getCachedObject(child);
		if (cachedObject == null)
			return; // not currently displayed or already changed object itself so nothing to update

		if (cachedObject != child) {
			// update entity of cached object:
			try {
				CnAElementHome.getInstance().refresh(cachedObject);
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler beim Aktualisieren der Baumansicht.");
			}
		}
		updater.refresh();
		
	}


	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#childRemoved(sernet.gs.ui.rcp.main.common.model.CnATreeElement, sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void childRemoved(CnATreeElement category, CnATreeElement child) {
	}


	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildChanged(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void databaseChildChanged(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		CnATreeElement cachedChild = cache.getCachedObject(child);
		
		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
		}
		if (cachedChild != null) {
			cachedChild.setEntity(child.getEntity());
		}
		updater.refresh();	
	}


	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildRemoved(sernet.gs.ui.rcp.main.common.model.ChangeLogEntry)
	 */
	public void databaseChildRemoved(ChangeLogEntry entry) {
		CnATreeElement cachedChild = cache.getCachedObjectById(entry.getElementId());
		if (cachedChild != null) {
			CnATreeElement cachedParent = cachedChild.getParent();
			if (cachedParent != null) {
				cachedParent.setChildrenLoaded(false);
			}
			updater.refresh();
		}	
	}


	

}
