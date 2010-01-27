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
 *     Daniel Murygin <dm@sernet.de> - modified for ISO 27000
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;

/**
 * Content provider for BSI model elements.
 * 
 * @author koderman@sernet.de
 * @author Daniel Murygin <dm@sernet.de>
 */
public class ISMViewContentProvider implements ITreeContentProvider {

	private static final Logger log = Logger.getLogger(ISMViewContentProvider.class);

	private BSIModelElementFilter modelFilter;

	public ISMViewContentProvider(TreeViewerCache cache) {
		super();
		this.cache = cache;
	}

	private TreeViewerCache cache;

	public void dispose() {
	}

	public Object[] getChildren(Object parent) {
		Object[] children = null;

		// replace object in event with the one actually displayed in the tree:
		Object cachedObject = cache.getCachedObject(parent);
		if (cachedObject != null) {
			parent = cachedObject;
		}
			
		if(parent instanceof List) {
			children = ((List)parent).toArray();
		} else if (parent instanceof CnATreeElement) {
			CnATreeElement el = (CnATreeElement) parent;
			CnATreeElement newElement;
			try {
				newElement = loadChildren(el);
				el.replace(newElement);
				el = newElement;
				children = el.getChildrenAsArray();
			} catch (CommandException e) {
				log.error("Error while loading child elements", e);
				ExceptionUtil.log(e, "Konnte untergeordnete Objekte nicht laden.");
			}
		}

		return children;
	}

	private CnATreeElement loadChildren(CnATreeElement el) throws CommandException {
		if (el.isChildrenLoaded()) {
			return el;
		}

		Logger.getLogger(this.getClass()).debug("Loading children from DB for " + el);

		RetrieveCnATreeElement command = null;
		if(el instanceof ISO27KModel) {
			command = RetrieveCnATreeElement.getISO27KModelISMViewInstance(el.getDbId());
		} else if(el instanceof Organization) {
			command = RetrieveCnATreeElement.getOrganizationISMViewInstance(el.getDbId());
		} else if( el instanceof Group) {
			command = RetrieveCnATreeElement.getGroupISMViewInstance(el.getDbId(), (Class<Group>) el.getClass());
		} else if( el instanceof CnATreeElement) {
			command = RetrieveCnATreeElement.getElementISMViewInstance(el.getDbId(), el.getClass());
		}

		command = ServiceFactory.lookupCommandService().executeCommand(command);
		CnATreeElement newElement = command.getElement();

		// If a filter was active the tree element for which we loaded the
		// children
		// is *not* marked as if its children have been really loaded. This is
		// only
		// done when no classes have been filtered.
		// By doing this the element gets automatically reloaded (and now its
		// children
		// as well) as soon as the user disables the filter.
		if (modelFilter == null || modelFilter.isEmpty()) {
			newElement.setChildrenLoaded(true);
		}

		// replace with loaded object in cache:
		Logger.getLogger(this.getClass()).debug("Replacing in cache: " + el + " replaced with " + newElement);
		cache.clear(el);
		cache.addObject(newElement);

		// TODO ak synchronization problem?

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

	public Object getParent(Object child) {
		Object parent = null;
		if (child instanceof CnATreeElement) {
			CnATreeElement el = (CnATreeElement) child;
			parent = el.getParent();
		} 
		return parent;
	}

	public boolean hasChildren(Object parent) {
		boolean hasChildren = false;
		if (parent instanceof CnATreeElement) {
			try {
				CnATreeElement el = (CnATreeElement) parent;
				hasChildren = el.getChildren().size() > 0;
			} catch (Exception e) {
				if (parent != null) {
					log.error("Error in hasChildren, element type: " + parent.getClass().getSimpleName(), e);
				} else {
					log.error("Error in hasChildren, element is null", e);
				}
				return true;
			}
		}
		return hasChildren;
	}
	
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		cache.clear();
	}
}
