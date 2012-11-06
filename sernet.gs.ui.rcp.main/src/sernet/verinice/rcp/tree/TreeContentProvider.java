/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.tree;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.hibernate.mapping.Set;

import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementComparator;
import sernet.verinice.model.common.ITitleAdaptor;

/**
 * TreeContentProvider provides the TreeViewer in {@link ISMView} with information on 
 * how to transform a domain object ({@link CnATreeElement}) into an item in the UI tree.
 * 
 * When you ask the tree viewer for the selected objects, it will answer with the 
 * domain objects - not the underlying UI resources.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class TreeContentProvider implements ITreeContentProvider {
    
    private static final Logger LOG = Logger.getLogger(TreeContentProvider.class);
    
    private static final ElementComparator<CnATreeElement> COMPARATOR = new ElementComparator<CnATreeElement>(new ITitleAdaptor<CnATreeElement>() {
        @Override
        public String getTitle(CnATreeElement element) {
            return element.getTitle();
        }
    });
    
    private ElementManager elementManager;
    
    /**
     * @param elementManager
     */
    public TreeContentProvider(ElementManager elementManager) {
        super();
        this.elementManager = elementManager;
    }

    /**
     * This is the method invoked by calling the setInput method on the tree viewer. 
     * In fact, the getElements method is called only in response to the tree viewer's 
     * setInput method and should answer with the appropriate domain objects of the 
     * inputElement. The getElements and getChildren methods operate in a similar way.
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    /**
     * Returns the child elements of the given parent element.
     * <p>
     * The difference between this method and <code>IStructuredContentProvider.getElements</code> 
     * is that <code>getElements</code> is called to obtain the 
     * tree viewer's root elements, whereas <code>getChildren</code> is used
     * to obtain the children of a given parent element in the tree (including a root).
     * </p>
     * The result is not modified by the viewer.
     * 
     * Calling this method might change parameter parentElement.
     * Children {@link Set} in parentElement is replaced by return value.
     *
     * @param parentElement the parent element
     * @return an array of child elements
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        CnATreeElement[] children = new CnATreeElement[]{};
        if(parentElement==null) {
            LOG.warn("Can not load children. Parent element is null.");
        } else if(!(parentElement instanceof CnATreeElement)) {
            LOG.warn("Can not load children. Parent element is no a CnATreeElement. Element class is: " + parentElement.getClass());
        } else {
            children = getElementManager().getChildren((CnATreeElement) parentElement);
        } 
        Arrays.sort(children, COMPARATOR);
        return children;
    }
    
    /**
     * Returns whether the given element has children.
     * <p>
     * Intended as an optimization for when the viewer does not
     * need the actual children.  Clients may be able to implement
     * this more efficiently than <code>getChildren</code>.
     * </p>
     *
     * @param element the element
     * @return <code>true</code> if the given element has children,
     *  and <code>false</code> if it has no children
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        boolean hasChildren = false;
        if(element==null) {
            LOG.warn("Can not determine if elemant has children. Element is null.");
        } else if(!(element instanceof CnATreeElement)) {
            LOG.warn("Can not determine if elemant has children. Element is no a CnATreeElement. Element class is: " + element.getClass());
        } else {
            hasChildren = getElementManager().hasChildren((CnATreeElement) element);
        }
        return hasChildren;
    }

    /** 
     * Returns the parent for the given element, or <code>null</code> 
     * indicating that the parent can't be computed. 
     * In this case the tree-structured viewer can't expand
     * a given node correctly if requested.
     *
     * @param element the element
     * @return the parent element, or <code>null</code> if it
     *   has none or if the parent cannot be computed
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {      
        if(element instanceof CnATreeElement) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getParent called for uuid: " + ((CnATreeElement) element).getUuid());
            }
            CnATreeElement elementWithParent = Retriever.checkRetrieveParent((CnATreeElement) element);
            if(elementWithParent==null) {
                return null;
            } else {
                return elementWithParent.getParent();
            }
        } else {
            return null;
        }
    }

    /**
     * Notifies this content provider that the given viewer's input
     * has been switched to a different element.
     * <p>
     * A typical use for this method is registering the content provider as a listener
     * to changes on the new input (using model-specific means), and deregistering the viewer 
     * from the old input. In response to these change notifications, the content provider
     * should update the viewer (see the add, remove, update and refresh methods on the viewers).
     * </p>
     * <p>
     * The viewer should not be updated during this call, as it might be in the process
     * of being disposed.
     * </p>
     *
     * @param viewer the viewer
     * @param oldInput the old input element, or <code>null</code> if the viewer
     *   did not previously have an input
     * @param newInput the new input element, or <code>null</code> if the viewer
     *   does not have an input
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // TODO Auto-generated method stub

    }
    
    /**
     * @return the elementManager
     */
    protected ElementManager getElementManager() {
        return elementManager;
    }

    /**
     * Disposes of this content provider.  
     * This is called by the viewer when it is disposed.
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}
