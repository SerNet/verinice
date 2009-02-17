package sernet.gs.ui.rcp.main.bsi.views;

import org.apache.derby.iapi.services.property.PersistentSet;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.hibernate.collection.AbstractPersistentCollection;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.NullModel;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;

/**
 * Content provider for BSI model elements.
 * 
 * @author koderman@sernet.de
 * 
 */
public class BSIModelViewContentProvider implements ITreeContentProvider {
	
	public BSIModelViewContentProvider(TreeViewerCache cache) {
		super();
		this.cache = cache;
	}

	private TreeViewerCache cache;
	

		public void dispose() {
		}

		public Object[] getChildren(Object parent) {
			Logger.getLogger(this.getClass()).debug("getChildren " +parent);
			
			// replace object in event with the one actually displayed in the tree:
			Object cachedObject = cache.getCachedObject(parent);
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
					CnATreeElement[] children = el.getChildrenAsArray();
					if (el.getLinksDown().size() > 0) {
						// add linkkategorie object:
						Object[] result = new Object[children.length + 1];
						System.arraycopy(children, 0, result, 0, children.length);
						result[children.length] = el.getLinks();
						return result;
					} else {
						return children;
					}
				} catch (CommandException e) {
					ExceptionUtil.log(e, "Konnte untergeordnete Objekte nicht laden.");
				}
			} 
			
			else if (parent instanceof LinkKategorie) {
				return ((LinkKategorie) parent).getChildren().toArray();
			}
			
			return null;
		}

		private CnATreeElement loadChildren(CnATreeElement el) throws CommandException {
			if (el.isChildrenLoaded())
				return el;
			
			LoadChildrenForExpansion command = new LoadChildrenForExpansion(el);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			CnATreeElement newElement = command.getElementWithChildren();
			newElement.setChildrenLoaded(true);
			
			// replace with loaded object in cache:
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
			} else if (child instanceof LinkKategorie) {
				LinkKategorie kat = (LinkKategorie) child;
				return kat.getParent();
			} else if (child instanceof CnALink) {
				CnALink link = (CnALink) child;
				return link.getParent();
			}
			return null;
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof MassnahmenUmsetzung)
				return false;
			
			if (parent instanceof CnATreeElement) {
				try {
					CnATreeElement el = (CnATreeElement) parent;
					boolean hasChildren = el.getChildren().size() > 0
						|| el.getLinksDown().size() > 0;
					return hasChildren;
				} catch (Exception e) {
					//Logger.getLogger(this.getClass()).error(e);
					return true;
				}
				
			}
			if (parent instanceof LinkKategorie) {
				LinkKategorie kat = (LinkKategorie) parent;
				return kat.getChildren().size() > 0;
			}
			return false;
		}

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			cache.clear();
		}


}
