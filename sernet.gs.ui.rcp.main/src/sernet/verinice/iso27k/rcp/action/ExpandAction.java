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
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;

import sernet.verinice.iso27k.rcp.ISMViewContentProvider;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ExpandAction extends Action implements ISelectionChangedListener {

	private static final Logger LOG = Logger.getLogger(ExpandAction.class);
	
	TreeViewer viewer;
	
	CnATreeElement selectedElement;
	
	ISMViewContentProvider contentProvider;
	
	public ExpandAction(TreeViewer viewer, ISMViewContentProvider contentProvider) {
		this.viewer = viewer;
		this.contentProvider = contentProvider;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		List<Object> expandedElements = new ArrayList<Object>();
		
		// add all elements form selection to organization
		CnATreeElement element = selectedElement;
		expandedElements.add(element);
		if(!(element instanceof Organization) && !(element instanceof ImportIsoGroup)) {
			while(element.getParent()!=null && !(element.getParent() instanceof Organization) && !(element.getParent() instanceof ImportIsoGroup)) {
				element = element.getParent();
				expandedElements.add(element);
			}
			expandedElements.add(element.getParent());
		}
		
		// add all children
		element = selectedElement;
		addChildren(element,expandedElements);
		

		viewer.setExpandedElements(expandedElements.toArray());
	}

	/**
	 * @param element
	 * @param expandedElements
	 */
	private void addChildren(CnATreeElement element, List<Object> expandedElements) {
		Object[] children = contentProvider.getChildren(element);
		if(children!=null && children.length>0) {
			expandedElements.addAll(Arrays.asList(children));
			for (Object child : children) {
				Object cachedObject = contentProvider.getCachedObject(child);
				if (cachedObject == null) {
					contentProvider.addCachedObject(child);
				} else {
					child = cachedObject;
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("child: " + ((CnATreeElement)child).getTitle());
				}
				
				addChildren((CnATreeElement) child, expandedElements);
			}
		}
		
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection selection = event.getSelection();
		if(selection instanceof IStructuredSelection ) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();
			if(sel instanceof CnATreeElement) {
				this.selectedElement = (CnATreeElement) sel;
			}
		}
		
	}

}
