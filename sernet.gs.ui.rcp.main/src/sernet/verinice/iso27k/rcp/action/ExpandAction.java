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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;

import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * This action expands all elements in a tree below the selectedElement.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ExpandAction extends Action implements ISelectionChangedListener {

    private static final Logger LOG = Logger.getLogger(ExpandAction.class);

    private TreeViewer viewer;
    private CnATreeElement selectedElement;
    private ITreeContentProvider contentProvider;

    public ExpandAction(TreeViewer viewer, ITreeContentProvider contentProvider) {
        this.viewer = viewer;
        this.contentProvider = contentProvider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        List<Object> expandedElements = new ArrayList<>();

        // add all elements form selection to organization
        CnATreeElement element = selectedElement;
        expandedElements.add(element);
        if (!isScope(element)) {
            while (element.getParent() != null && !(parentIsScope(element))) {
                element = element.getParent();
                expandedElements.add(element);
            }
            expandedElements.add(element.getParent());
        }

        // add all children
        element = selectedElement;
        addChildren(element, expandedElements);

        viewer.setExpandedElements(expandedElements.toArray());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.
     * eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            if (sel instanceof CnATreeElement) {
                this.selectedElement = (CnATreeElement) sel;
            }
        }
    }

    private void addChildren(CnATreeElement element, List<Object> expandedElements) {
        Object[] children = contentProvider.getChildren(element);
        if (children != null && children.length > 0) {
            expandedElements.addAll(Arrays.asList(children));
            for (Object child : children) {
                if (child instanceof CnATreeElement) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("child: " + ((CnATreeElement) child).getTitle());
                    }
                    addChildren((CnATreeElement) child, expandedElements);
                }
            }
        }
    }

    private boolean parentIsScope(CnATreeElement element) {
        if (element == null || element.getParent() == null) {
            return false;
        }
        CnATreeElement parent = element.getParent();
        return isScope(parent);
    }

    /**
     * Checks if the given element is a scope. Aka root node.
     */
    private boolean isScope(CnATreeElement element) {
        return element instanceof Organization 
                || element instanceof ItNetwork 
                || element instanceof ITVerbund 
                || element instanceof ImportBsiGroup 
                || element instanceof ImportIsoGroup 
                || element instanceof ImportBpGroup;
    }

}
