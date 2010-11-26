/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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

import java.util.HashSet;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationViewContentProvider implements IStructuredContentProvider, IBSIModelListener, IISO27KModelListener {


	private IRelationTable view;
	private TableViewer viewer;

	public RelationViewContentProvider(IRelationTable view, TableViewer viewer) {
		this.view = view;
		this.viewer = viewer;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		if (newInput instanceof PlaceHolder)
			return;
		CnATreeElement inputElmt = (CnATreeElement) newInput;
		view.setInputElmt(inputElmt);
		viewer.refresh();
	}

	public void dispose() {
	}

	public Object[] getElements(Object obj) {
		if (obj instanceof PlaceHolder) {
			return new Object[] { obj };
		}

		if (view == null || view.getInputElmt() == null) {
		    return new Object[] {};
		}
		
		HashSet<CnALink> result = new HashSet<CnALink>();
		result.addAll(view.getInputElmt().getLinksDown());
		result.addAll(view.getInputElmt().getLinksUp());
		return (CnALink[]) result.toArray(new CnALink[result.size()]);
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#childAdded(sernet.gs.ui.rcp.main.common.model.CnATreeElement, sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void childAdded(CnATreeElement category, CnATreeElement child) {
		// only react to link changes
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#childChanged(sernet.gs.ui.rcp.main.common.model.CnATreeElement, sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void childChanged(CnATreeElement category, CnATreeElement child) {
	 // only react to link changes
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#childRemoved(sernet.gs.ui.rcp.main.common.model.CnATreeElement, sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void childRemoved(CnATreeElement category, CnATreeElement child) {
	 // only react to link changes
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildAdded(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void databaseChildAdded(CnATreeElement child) {
	 // only react to link changes
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildChanged(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void databaseChildChanged(CnATreeElement child) {
	 // only react to link changes
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildRemoved(sernet.gs.ui.rcp.main.common.model.CnATreeElement)
	 */
	public void databaseChildRemoved(CnATreeElement child) {
	 // only react to link changes
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#databaseChildRemoved(sernet.gs.ui.rcp.main.common.model.ChangeLogEntry)
	 */
	public void databaseChildRemoved(ChangeLogEntry entry) {
	 // only react to link changes
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#linkAdded(sernet.gs.ui.rcp.main.common.model.CnALink)
	 */
	public void linkAdded(CnALink link) {
		view.reloadAll();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#linkChanged(sernet.gs.ui.rcp.main.common.model.CnALink)
	 */
	public void linkChanged(CnALink old, CnALink link, Object source) {
	    if (view.equals(source)) {
	        view.reload(old, link);
	    } else {
	        view.reloadAll();
	    }
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#linkRemoved(sernet.gs.ui.rcp.main.common.model.CnALink)
	 */
	public void linkRemoved(CnALink link) {
		view.reloadAll();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#modelRefresh()
	 */
	public void modelRefresh() {
	 // only react to link changes
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#modelRefresh(java.lang.Object)
	 */
	public void modelRefresh(Object source) {
	    view.reloadAll();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener#modelReload(sernet.gs.ui.rcp.main.bsi.model.BSIModel)
	 */
	public void modelReload(BSIModel newModel) {
	    view.reloadAll();
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.model.IISO27KModelListener#modelReload(sernet.verinice.iso27k.model.ISO27KModel)
	 */
	public void modelReload(ISO27KModel newModel) {
	    view.reloadAll();
	}
}
