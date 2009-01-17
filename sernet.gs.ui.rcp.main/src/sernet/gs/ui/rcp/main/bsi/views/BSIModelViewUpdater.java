package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.TreeViewer;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.gs.ui.rcp.main.service.crudcommands.RefreshElement;

/**
 * Check for model changes and update our display.
 */
public class BSIModelViewUpdater implements IBSIModelListener {
	
	private TreeViewer viewer;
	private TreeViewerCache cache;
	private ThreadSafeViewerUpdate updater;

	BSIModelViewUpdater(TreeViewer viewer, TreeViewerCache cache) {
		this.viewer = viewer;
		this.cache = cache;
		 this.updater = new ThreadSafeViewerUpdate(viewer);
	}
	

		public void childAdded(CnATreeElement category, CnATreeElement child) {
			updater.add(category, child);
		}

		public void childChanged(CnATreeElement category, CnATreeElement child) {
			CnATreeElement cachedObject = cache.getCachedObject(child);
			if (cachedObject == null) 
				return; // not currently displayed, so nothing to update
				
			try {
				CnAElementHome.getInstance().refresh(cachedObject);
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler beim Aktualisieren der Baumansicht.");
			}
			updater.refresh(cachedObject);
		}

		public void childRemoved(CnATreeElement category, CnATreeElement child) {
			updater.refresh();
		}

		public void modelRefresh() {
			updater.refresh();
		}

		public void linkChanged(CnALink link) {
			// is top element visible?
			CnATreeElement oldElement = cache.getCachedObject(link.getParent().getParent());
			if (oldElement != null) {
				// load and add linkkategory:
				oldElement.setLinks(link.getParent());
				try {
					replaceElement(oldElement);
					updater.refresh(oldElement.getParent());
				} catch (CommandException e) {
					ExceptionUtil.log(e.getCause(), "Fehler beim Aktualisiewren der Verknüpfung.");
				}
			}
		}


		private void replaceElement(CnATreeElement oldElement) throws CommandException {
			RefreshElement command = new RefreshElement(oldElement, true);
			command = ServiceFactory.lookupCommandService().executeCommand(
					command);
			CnATreeElement newElement = command.getElement();
			
			cache.clear(oldElement);
			newElement.setParent(oldElement.getParent());
			oldElement.getParent().replaceChild(newElement);
		}
}
