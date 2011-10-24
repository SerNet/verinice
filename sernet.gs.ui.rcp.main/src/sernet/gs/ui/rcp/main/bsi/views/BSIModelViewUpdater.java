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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Listener to check for model changes and update the connected tree viewer.
 * 
 * Uses cache to resolve objects being updated. This cache is shared between ContentProvider, LabelProvider and 
 * Updater (this class) to update only actually displayed objects based on object identity 
 * (as defined by the objects' equals() method.
 * 
 * This is necessary because objects loaded from the database may be instantiated multiple times in memory.
 * Also, loaded objects may not be fully initialised or have less elements loaded than the ones in the cache
 * that are already displayed.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class BSIModelViewUpdater implements IBSIModelListener {

    private static final Logger LOG = Logger.getLogger(BSIModelViewUpdater.class);
    
	private TreeViewer viewer;
	private ThreadSafeViewerUpdate updater;
    Object[] expandedElements = null;
	
	// cache to figure out if an element is currently displayed in the tree or not
	private TreeViewerCache cache;

	BSIModelViewUpdater(TreeViewer viewer, TreeViewerCache cache) {
		this.viewer = viewer;
		this.cache = cache;
		this.updater = new ThreadSafeViewerUpdate(viewer);
	}

	public void childAdded(CnATreeElement category, CnATreeElement child) {
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		
		CnATreeElement cachedObject = cache.getCachedObject(child);
		if (cachedObject == null)
			return; // not currently displayed or already changed object itself so nothing to update

		if (cachedObject != child) {
			// update entity of cached object:
			try {
				CnAElementHome.getInstance().refresh(cachedObject);
			} catch (CommandException e) {
				ExceptionUtil.log(e, "Fehler beim Aktualisieren der Baumansicht.");
			}
		}
		updater.refresh();
	}

	public void childRemoved(CnATreeElement category, CnATreeElement child) {
	}

	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	public void modelRefresh() {
		modelRefresh(null);
	}

	public void modelRefresh(Object source) {
		updater.refresh();
	}
	
	public void modelReload(BSIModel newModel) {
	    
	    try {
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    try {
                        expandedElements = viewer.getExpandedElements();
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            BSIModel model = getModel(viewer.getInput());
            if(model!=null) {
                // remove listener from currently displayed model:
                model.removeBSIModelListener(this);
            }
            newModel.addBSIModelListener(this);
            cache.clear();
            updater.setInput(newModel);
            updater.refresh();
            // Expand elements in background
            Job job = new ExpandJob(expandedElements);
            job.setRule(new ExpandJobRule());
            job.schedule(Job.DECORATE);
        } catch (Exception e) {
            LOG.error("Error while updating treeview", e);
        }
	}

	/**
	 * Get model, may be current viewer input or the root of the currently displayed
	 * element.
	 * 
	 * @param input
	 * @return
	 */
	private BSIModel getModel(Object input) {
		if (input instanceof BSIModel)
			return (BSIModel) input;
		
		if (input instanceof CnATreeElement) {
			CnATreeElement elmt = (CnATreeElement) input;
			return getModel(elmt.getParent());
		}
		
		// input is not part of a proper tree / no BSIModel object could be found as parent:
		return null;
	}

	public void linkChanged(CnALink old, CnALink link, Object source) {
	    // do nothing
	}
	
	public void linkAdded(CnALink link) {
		// TODO akoderman do nothing, li nks displayed in relationview now 
	}
	
	
	public void linkRemoved(CnALink link) {
		// TODO akoderman do nothing, li nks displayed in relationview now 
//		// is link visible?
//		CnALink oldElement = cache.getCachedObject(link);
//		
//		if (oldElement != null) {
//			oldElement.getParent().getParent().removeLinkDown(oldElement);
//			updater.remove(oldElement);
//		}
	}

	public void databaseChildAdded(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
			cachedParent.addChild(child);
		}
		updater.refresh();
	}

	public void databaseChildChanged(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		CnATreeElement cachedChild = cache.getCachedObject(child);
		
		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
		}
		if (cachedChild != null) {
			cachedChild.setEntity(child.getEntity());
		}
		updater.refresh();
	}

	public void databaseChildRemoved(CnATreeElement child) {
		// cause reload of children list of parent if currently displayed:
		CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
		CnATreeElement cachedChild = cache.getCachedObject(child);

		if (cachedParent != null) {
			cachedParent.setChildrenLoaded(false);
			cachedParent.removeChild(cachedChild);
		}
		updater.refresh();
	}
	
	public void databaseChildRemoved(ChangeLogEntry entry) {
		CnATreeElement cachedChild = cache.getCachedObjectById(entry.getElementId());
		if (cachedChild != null) {
			CnATreeElement cachedParent = cachedChild.getParent();
			if (cachedParent != null) {
				cachedParent.setChildrenLoaded(false);
			}
			updater.refresh();
		}
	}
	
	/**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     * 
     */
    private final class ExpandJobRule implements ISchedulingRule {
        public boolean contains(ISchedulingRule rule) {
            return rule.getClass() == ExpandJobRule.class;
        }

        public boolean isConflicting(ISchedulingRule rule) {
            return rule.getClass() == ExpandJobRule.class;
        }
    }

    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     * 
     */
    private final class ExpandJob extends Job {
        
        Object [] elements;
        
        /**
         * @param name
         */
        private ExpandJob(Object [] elements) {
            super("Expanding");
            this.elements = elements;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            monitor.setTaskName("Expanding element tree...");
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    try {
                        viewer.setExpandedElements(elements);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });         
            return Status.OK_STATUS;
        }
    }
	
}
