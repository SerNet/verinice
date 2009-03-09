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
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.Set;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLinkRoot;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Content Provider for document view.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DocumentContentProvider implements ITreeContentProvider, IBSIModelListener {



	private TreeViewer viewer;

	public DocumentContentProvider(TreeViewer viewer) {
		this.viewer = viewer;
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DocumentLink) {
			DocumentLink doclink = (DocumentLink) parentElement;
			Set<DocumentReference> children = doclink.getChildren();
			return (DocumentReference[]) children
					.toArray(new DocumentReference[children.size()]);
		}
		else if (parentElement instanceof DocumentLinkRoot) {
			DocumentLinkRoot root = (DocumentLinkRoot) parentElement;
			return root.getChildren();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		if (element instanceof DocumentReference) {
			DocumentReference ref = (DocumentReference) element;
			return ref.getParent();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof DocumentLink) {
			DocumentLink doclink = (DocumentLink) element;
			return doclink.getChildren().size() > 0;
		}
		return false;
	}

	public void dispose() {
		if (CnAElementFactory.getLoadedModel() != null)
			CnAElementFactory.getLoadedModel().removeBSIModelListener(this);
	}

	public void inputChanged(Viewer viewer, Object oldInput,
			Object newInput) {
		if (CnAElementFactory.getLoadedModel() != null) {
			CnAElementFactory.getLoadedModel().removeBSIModelListener(this);
			CnAElementFactory.getLoadedModel().addBSIModelListener(this);
		}
		modelRefresh(null);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void childAdded(CnATreeElement category, CnATreeElement child) {
		modelRefresh(null);
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		modelRefresh(null);
	}
	
	public void linkChanged(CnALink link) {
		// do nothing
	}
	
	public void linkRemoved(CnALink link) {
		// do nothing
		
	}
	
	public void linkAdded(CnALink link) {
		// do nothing
	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		modelRefresh(null);
	}

	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	public void modelRefresh() {
		modelRefresh(null);
	}

	public void modelRefresh(Object source) {
		if (Display.getCurrent() != null) {
			viewer.refresh();
		}
		else {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
		}
	}

	public void databaseChildAdded(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildChanged(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void databaseChildRemoved(CnATreeElement child) {
		// TODO Auto-generated method stub
		
	}

	public void modelReload(BSIModel newModel) {
		// TODO Auto-generated method stub
		
	}
	


}
