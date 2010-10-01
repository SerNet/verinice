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
 *     Daniel Murygin <dm[at]sernet[dot]de> - modified for ISO 27000
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Content provider for BSI model elements.
 * 
 * @author koderman[at]sernet[dot]de
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ISMViewContentProvider implements ITreeContentProvider {

    private static final Logger log = Logger.getLogger(ISMViewContentProvider.class);

    private final ElementComparator comparator = new ElementComparator();

    private BSIModelElementFilter modelFilter;

    private TreeViewerCache cache;

    private List<ViewerFilter> filterList = new ArrayList<ViewerFilter>();

    private IParentLoader parentLoader;

    private IContentCommandFactory commandFactory;

    public ISMViewContentProvider(TreeViewerCache cache) {
        super();
        this.cache = cache;
        commandFactory = new DefaultCommandFactory();
        parentLoader = new DefaultParentLoader();
    }

    public ISMViewContentProvider(TreeViewerCache cache, IContentCommandFactory commandFactory, IParentLoader parentLoader) {
        super();
        this.cache = cache;
        if (commandFactory != null) {
            this.commandFactory = commandFactory;
        } else {
            commandFactory = new DefaultCommandFactory();
        }
        if (parentLoader != null) {
            this.parentLoader = parentLoader;
        } else {
            parentLoader = new DefaultParentLoader();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object element) {
        CnATreeElement[] children = new CnATreeElement[] {};

        // replace object in event with the one actually displayed in the tree:
        Object cachedObject = cache.getCachedObject(element);
        if (cachedObject != null) {
            element = cachedObject;
        }
        try {
            if (element instanceof List<?>) {
                List<CnATreeElement> list = (List<CnATreeElement>) element;
                children = new CnATreeElement[list.size()];
                int i = 0;
                for (Iterator<CnATreeElement> iterator = list.iterator(); iterator.hasNext();) {
                    CnATreeElement cnATreeElement = iterator.next();
                    children[i] = loadChildren(cnATreeElement, true);
                    i++;
                }
            } else if (element instanceof CnATreeElement) {
                CnATreeElement el = (CnATreeElement) element;
                CnATreeElement newElement;

                if (!el.isChildrenLoaded()) {
                    newElement = loadChildren(el);
                    if (newElement != null) {
                        el.replace(newElement);
                        el = newElement;
                        children = el.getChildrenAsArray();
                    }
                } else {
                    children = el.getChildrenAsArray();
                }
                Arrays.sort(children, comparator);
            }
        } catch (CommandException e) {
            log.error("Error while loading child elements", e);
            ExceptionUtil.log(e, "Konnte untergeordnete Objekte nicht laden.");
        }
        return children;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object parent) {
        boolean hasChildren = false;
        if (parent instanceof CnATreeElement) {
            try {
                CnATreeElement el = Retriever.checkRetrieveChildren((CnATreeElement) parent);
                Set<CnATreeElement> children = el.getChildren();
                if(children!=null) {
                    hasChildren = !children.isEmpty();
                }
                // to be correct we have to check by tree filter if children are correctly displayed
                // this is extremly inperformant
                /*
                Set<CnATreeElement> filteredList = children;
                if (filterList.isEmpty()) {
                    filteredList = children;
                } else {
                    filteredList = new HashSet<CnATreeElement>(children.size());
                    for (CnATreeElement cnATreeElement : children) {
                        if(getCachedObject(cnATreeElement)!=null) {
                            cnATreeElement = (CnATreeElement) getCachedObject(cnATreeElement);
                        }
                        //cnATreeElement = Retriever.checkRetrieveChildren((CnATreeElement) cnATreeElement);
                        for (ViewerFilter filter : filterList) {
                            if (filter.select(null, null, cnATreeElement)) {
                                filteredList.add(cnATreeElement);
                            }
                        }
                    }
                }
                */               
            } catch (Exception e) {
                if (parent != null) {
                    log.error("Error in hasChildren, element type: " + parent.getClass().getSimpleName(), e);
                } else {
                    log.error("Error in hasChildren, element is null", e);
                }
                hasChildren = true;
            }
        }
        return hasChildren;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object child) {
        Object parent = null;
        if (child instanceof CnATreeElement) {
            parent = getParentLoader().getParent((CnATreeElement) child);
            addParentToCache((CnATreeElement) parent);
        }
        return parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    @Override
    public void dispose() {
    }

    private CnATreeElement loadChildren(CnATreeElement el) throws CommandException {
        return loadChildren(el, false);
    }

    /**
     * @param el
     * @param loadParent
     * @return
     * @throws CommandException
     */
    private CnATreeElement loadChildren(CnATreeElement el, boolean loadParent) throws CommandException {
        if (el.isChildrenLoaded()) {
            return el;
        }

        Logger.getLogger(this.getClass()).debug("Loading children from DB for " + el);

        RetrieveCnATreeElement command = commandFactory.createCommand(el, loadParent);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        CnATreeElement newElement = command.getElement();

        if (newElement != null) {
            // If a filter was active the tree element for which we loaded the  children
            // is *not* marked as if its children have been really loaded. 
            // This is only done when no classes have been filtered.
            // By doing this the element gets automatically reloaded 
            // (and now its children as well) as soon as the user disables the filter.

            if (modelFilter == null || modelFilter.isEmpty()) {
                newElement.setChildrenLoaded(true);
            }

            // replace with loaded object in cache:
            Logger.getLogger(this.getClass()).debug("Replacing in cache: " + el + " replaced with " + newElement);
            cache.addObject(newElement);
            if (loadParent && newElement.getParent() != null) {
                cache.addObject(newElement.getParent());
            }
        }
        return newElement;
    }

    public Object getCachedObject(Object o) {
        return cache.getCachedObject(o);
    }

    public void addCachedObject(Object o) {
        cache.addObject(o);
    }

    private void addParentToCache(CnATreeElement element) {
        if (element != null) {
            cache.addObject(element);
            addParentToCache(element.getParent());
        }
    }

    /**
     * @return
     */
    private IParentLoader getParentLoader() {
        return parentLoader;
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        cache.clear();
        cache.addObject(newInput);
    }

    public void addFilter(ViewerFilter filter) {
        filterList.add(filter);
    }

    class ElementComparator implements Comparator<CnATreeElement> {
        NumericStringComparator numericStringComparator = new NumericStringComparator();

        public int compare(CnATreeElement o1, CnATreeElement o2) {
            int FIRST_IS_LESS = -1;
            int EQUAL = 0;
            int FIRST_IS_GREATER = 1;
            int result = FIRST_IS_LESS;
            if (o1 != null && o1.getTitle() != null) {
                if (o2 != null && o2.getTitle() != null) {
                    result = numericStringComparator.compare(o1.getTitle().toLowerCase(), o2.getTitle().toLowerCase());
                } else {
                    result = FIRST_IS_GREATER;
                }
            } else if (o2 == null) {
                result = EQUAL;
            }
            return result;
        }

    }

    class DefaultParentLoader implements IParentLoader {

        /*
         * (non-Javadoc)
         * 
         * @see
         * sernet.verinice.iso27k.rcp.IParentLoader#getParent(sernet.verinice
         * .model.common.CnATreeElement)
         */
        @Override
        public CnATreeElement getParent(CnATreeElement child) {
            return (child != null) ? child.getParent() : null;
        }

    }

}
