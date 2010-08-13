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
package sernet.verinice.samt.audit.rcp;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

/**
 * Content provider for BSI model elements.
 * 
 * @author koderman[at]sernet[dot]de
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementViewContentProvider implements ITreeContentProvider {

	private static final Logger log = Logger.getLogger(ElementViewContentProvider.class);

	private final ElementComparator comparator = new ElementComparator();
	
	private BSIModelElementFilter modelFilter;

	public ElementViewContentProvider(TreeViewerCache cache) {
		super();
		this.cache = cache;
	}

	private TreeViewerCache cache;

	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		CnATreeElement[] children = new CnATreeElement[]{};

		// replace object in event with the one actually displayed in the tree:
		Object cachedObject = cache.getCachedObject(parent);
		if (cachedObject != null) {
			parent = cachedObject;
		}
		try {
    		if(parent instanceof List) {
    			List<CnATreeElement> list = (List<CnATreeElement>)parent;
    			children = new CnATreeElement[list.size()];
    			int i = 0;
    			for (Iterator<CnATreeElement> iterator = list.iterator(); iterator.hasNext();) {			 
                    CnATreeElement cnATreeElement = iterator.next();
                    children[i]=loadChildren(cnATreeElement);
                    i++;
                }
    		} 
		} catch (CommandException e) {
            log.error("Error while loading child elements", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ElementViewContentProvider_1);
        }
		return children;
	}

	private CnATreeElement loadChildren(CnATreeElement el) throws CommandException {
		if (el.isChildrenLoaded()) {
			return el;
		}
		
        if (log.isDebugEnabled()) {
            log.debug("Loading children from DB for " + el); //$NON-NLS-1$
        }

		RetrieveCnATreeElement command = null;
		if(el instanceof ISO27KModel) {
			command = RetrieveCnATreeElement.getISO27KModelISMViewInstance(el.getDbId());
		} else if(el instanceof Organization) {
			command = RetrieveCnATreeElement.getOrganizationISMViewInstance(el.getDbId());
		} else if( el instanceof IISO27kGroup ) {
			command = RetrieveCnATreeElement.getGroupISMViewInstance(el.getDbId(), el.getTypeId());
		} else if( el instanceof CnATreeElement) {
			command = RetrieveCnATreeElement.getElementISMViewInstance(el.getDbId(), el.getTypeId());
		}

		command = ServiceFactory.lookupCommandService().executeCommand(command);
		CnATreeElement newElement = command.getElement();

		if (newElement!=null) {
			// If a filter was active the tree element for which we loaded the
			// children
			// is *not* marked as if its children have been really loaded. This is
			// only
			// done when no classes have been filtered.
			// By doing this the element gets automatically reloaded (and now its
			// children
			// as well) as soon as the user disables the filter.
			
			if(modelFilter == null || modelFilter.isEmpty()) {
				newElement.setChildrenLoaded(true);
			}
	
			// replace with loaded object in cache:
			if (log.isDebugEnabled()) {
			    log.debug("Replacing in cache: " + el + " replaced with " + newElement); //$NON-NLS-1$ //$NON-NLS-2$
	        }
			
			cache.clear(el);
			cache.addObject(newElement);
		}
		return newElement;
	}
	
	public Object getCachedObject(Object o) {
		return cache.getCachedObject(o);
	}
	
	public void addCachedObject(Object o) {
		cache.addObject(o);
	}

	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object child) {
		return null;
	}

	public boolean hasChildren(Object parent) {
		return false;
	}
	
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		cache.clear();
		cache.addObject(newInput);
	}
	
	class ElementComparator implements Comparator<CnATreeElement> {
		NumericStringComparator numericStringComparator = new NumericStringComparator(); 
		public int compare(CnATreeElement o1, CnATreeElement o2) {
			int FIRST_IS_LESS = -1;
			int EQUAL = 0;
			int FIRST_IS_GREATER = 1;
			int result = FIRST_IS_LESS;
			if(o1!=null && o1.getTitle()!=null) {
				if(o2!=null && o2.getTitle()!=null) {
					result = numericStringComparator.compare(o1.getTitle().toLowerCase(), o2.getTitle().toLowerCase());
				} else {
					result = FIRST_IS_GREATER;
				}
			} else if(o2==null) {
				result = EQUAL;
			}
			return result;
		}
		
	}
}
