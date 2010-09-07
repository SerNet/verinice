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

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.filter.BSIModelElementFilter;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Content provider for BSI model elements.
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class BSIModelViewContentProvider implements ITreeContentProvider {

	private static final Logger log = Logger.getLogger(BSIModelViewContentProvider.class);

	private BSIModelElementFilter modelFilter;

	public BSIModelViewContentProvider(TreeViewerCache cache) {
		super();
		this.cache = cache;
	}

	private TreeViewerCache cache;

	public void dispose() {
	}

	public Object[] getChildren(Object parent) {
		// Logger.getLogger(this.getClass()).debug("getChildren " +parent);

		// replace object in event with the one actually displayed in the tree:
		Object cachedObject = cache.getCachedObject(parent);
		// Logger.getLogger(this.getClass()).debug("Retrieved from view cache: "
		// + cachedObject);
		if (cachedObject != null)
			parent = cachedObject;

		if (parent instanceof NullModel) {
			NullModel model = (NullModel) parent;
			return model.getChildrenAsArray();
		}

		if (parent instanceof CnATreeElement) {
			CnATreeElement el = (CnATreeElement) parent;
			CnATreeElement newElement;
			try {
				newElement = loadChildren(el);
				el.replace(newElement);
				el = newElement;
				return el.getChildrenAsArray();
			} catch (CommandException e) {
				log.error("Error while loading child elements", e);
				ExceptionUtil.log(e, "Konnte untergeordnete Objekte nicht laden.");
			}
		}
		return null;
	}

	private CnATreeElement loadChildren(CnATreeElement el) throws CommandException {
		if (el.isChildrenLoaded()) {
			// Logger.getLogger(this.getClass()).debug("NOT loading children because of positive flag on parent "
			// + el);
			return el;
		}
		
		if (log.isDebugEnabled()) {
		    log.debug("Loading children from DB for " + el);
        }
		
		LoadChildrenForExpansion command;
		if (modelFilter != null) {
			command = new LoadChildrenForExpansion(el, modelFilter.getFilteredClasses());
		} else {
			command = new LoadChildrenForExpansion(el);
		}

		command = ServiceFactory.lookupCommandService().executeCommand(command);
		CnATreeElement newElement = command.getElementWithChildren();

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
		if (log.isDebugEnabled()) {
		    log.debug("Replacing in cache: " + el + " replaced with " + newElement);
        }
		
		cache.clear(el);
		cache.addObject(newElement);

		return newElement;
	}

	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof CnATreeElement) {
			CnATreeElement el = (CnATreeElement) child;
			return el.getParent();
		} 
		return null;
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof MassnahmenUmsetzung)
			return false;

		if (parent instanceof CnATreeElement) {
			try {
				CnATreeElement el = (CnATreeElement) parent;
				boolean hasChildren = el.getChildren().size() > 0;
				return hasChildren;
			} catch (Exception e) {
				if (parent != null) {
					log.error("Error in hasChildren, element type: " + parent.getClass().getSimpleName(), e);
				} else {
					log.error("Error in hasChildren, element is null", e);
				}
				return true;
			}

		}
		return false;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		cache.clear();
		cache.addObject(newInput);
	}

	void setModelElementFilter(BSIModelElementFilter modelFilter) {
		this.modelFilter = modelFilter;
	}

}
