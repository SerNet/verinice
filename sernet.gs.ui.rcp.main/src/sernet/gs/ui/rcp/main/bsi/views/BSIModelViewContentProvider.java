package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

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
			if (parent instanceof CnATreeElement) {
				CnATreeElement el = (CnATreeElement) parent;
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
			} else if (parent instanceof LinkKategorie) {
				return ((LinkKategorie) parent).getChildren().toArray();
			}
			return null;
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
			// FIXME server: take care of lazyinitialization, esp after reload!
			if (parent instanceof CnATreeElement) {
				CnATreeElement el = (CnATreeElement) parent;
				return el.getChildren().size() > 0
						|| el.getLinksDown().size() > 0;
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
