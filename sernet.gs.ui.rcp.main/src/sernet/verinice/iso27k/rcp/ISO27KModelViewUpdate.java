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
package sernet.verinice.iso27k.rcp;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.ui.rcp.main.bsi.views.ThreadSafeViewerUpdate;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.validation.CnAValidation;
import sernet.verinice.rcp.tree.TreeUpdateListener;

/**
 * @deprecated {@link TreeUpdateListener}
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class ISO27KModelViewUpdate implements IISO27KModelListener {

    private static final Logger LOG = Logger.getLogger(ISO27KModelViewUpdate.class);
    
    private static final String DEFAULT_ERR_MSG = "Error while updating treeview";

    private TreeViewer viewer;
    private TreeViewerCache cache;
    private ThreadSafeViewerUpdate updater;
    private Object[] expandedElements = null;

    public ISO27KModelViewUpdate(TreeViewer viewer, TreeViewerCache cache) {
        super();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating ISO27k model view updater.");
        }
        this.viewer = viewer;
        this.cache = cache;
        this.updater = new ThreadSafeViewerUpdate(viewer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildRemoved
     * (sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    public void databaseChildRemoved(CnATreeElement child) {
        try {
            // cause reload of children list of parent if currently displayed:
            CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
            CnATreeElement cachedChild = cache.getCachedObject(child);
            if (cachedParent != null) {
                cachedParent.setChildrenLoaded(false);
                if(cachedChild!=null) {
                    cachedParent.removeChild(cachedChild);
                } else {
                    cachedParent.removeChild(child);
                }
            }
            if (cachedParent instanceof ISO27KModel) {
                updater.setInput(cachedParent);
            }
            updater.refresh();
        } catch (Exception e) {
            LOG.error(DEFAULT_ERR_MSG, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#childAdded(sernet.gs
     * .ui.rcp.main.common.model.CnATreeElement,
     * sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    public void childAdded(CnATreeElement category, CnATreeElement child) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildAdded(
     * sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    public void databaseChildAdded(CnATreeElement child) {
        try {
            // cause reload of children list of parent if currently displayed:
            CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
            if (cachedParent != null) {
                cachedParent.setChildrenLoaded(false);
                cachedParent.addChild(child);
            }
            updater.refresh();
        } catch (Exception e) {
            LOG.error(DEFAULT_ERR_MSG, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#modelRefresh(java.lang
     * .Object)
     */
    public void modelRefresh(Object object) {
        try {
            updater.refresh();
        } catch (Exception e) {
            LOG.error(DEFAULT_ERR_MSG, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#childChanged(sernet
     * .gs.ui.rcp.main.common.model.CnATreeElement,
     * sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    public void childChanged(CnATreeElement child) {
        try {
            CnATreeElement cachedObject = cache.getCachedObject(child);
            if (cachedObject == null) {
                return; // not currently displayed or already changed object
                        // itself so nothing to update
            }
            if (!cachedObject.equals(child)) {
                // update entity of cached object:
                CnAElementHome.getInstance().refresh(cachedObject);
                child = cachedObject; 
            }
            updater.refresh(child);
            if(child.getParent()!=null && child.getParent().getParent()!=null) {
                childChanged(child.getParent());
            }
        } catch (Exception e) {
            LOG.error("Error while updating treeview", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#childRemoved(sernet
     * .gs.ui.rcp.main.common.model.CnATreeElement,
     * sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    public void childRemoved(CnATreeElement category, CnATreeElement child) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildChanged
     * (sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    public void databaseChildChanged(CnATreeElement child) {
        try {
            // cause reload of children list of parent if currently displayed:
            CnATreeElement cachedParent = cache.getCachedObject(child.getParent());
            CnATreeElement cachedChild = cache.getCachedObject(child);
            if (cachedParent != null) {
                cachedParent.setChildrenLoaded(false);
            }
            if (cachedChild != null) {
                cachedChild.setEntity(child.getEntity());
                cachedChild.setChildrenLoaded(false);
            }
            updater.refresh();
        } catch (Exception e) {
            LOG.error("Error while updating treeview", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildRemoved
     * (sernet.gs.ui.rcp.main.common.model.ChangeLogEntry)
     */
    public void databaseChildRemoved(ChangeLogEntry entry) {
        try {
            CnATreeElement cachedChild = cache.getCachedObjectById(entry.getElementId());
            if (cachedChild != null) {
                CnATreeElement cachedParent = cachedChild.getParent();
                if (cachedParent != null) {
                    cachedParent.setChildrenLoaded(false);
                }
                updater.refresh();
            }
        } catch (Exception e) {
            LOG.error("Error while updating treeview", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#linkChanged(sernet.
     * gs.ui.rcp.main.common.model.CnALink)
     */
    public void linkChanged(CnALink old, CnALink link, Object source) {
        // nothing to do, since links are displayed in relation view
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#linkAdded(sernet.gs
     * .ui.rcp.main.common.model.CnALink)
     */
    public void linkAdded(CnALink link) {
        // nothing to do, since links are displayed in relation view
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#linkRemoved(sernet.
     * gs.ui.rcp.main.common.model.CnALink)
     */
    public void linkRemoved(CnALink link) {
        // nothing to do, since links are displayed in relation view
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#modelReload(sernet.
     * gs.ui.rcp.main.bsi.model.BSIModel)
     */
    public void modelReload(ISO27KModel newModel) {
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
            
            // remove listener from currently displayed model:
            ISO27KModel model = getModel(viewer.getInput());
            if(model!=null) {
                model.removeISO27KModelListener(this);
            }
            newModel.addISO27KModelListener(this);
            cache.clear();
            updater.setInput(newModel);
            updater.refresh();
            cache.addObject(newModel);
            
            // Expand elements in background
            Job job = new ExpandJob(expandedElements);
            job.setRule(new ExpandJobRule());
            job.schedule(Job.DECORATE);
        } catch (Exception e) {
            LOG.error("Error while updating treeview", e);
        }
    }

    /**
     * Get model, may be current viewer input or the root of the currently
     * displayed element.
     * 
     * @param input
     * @return
     */
    private ISO27KModel getModel(Object input) {
        if (input instanceof ISO27KModel) {
            return (ISO27KModel) input;
        }

        if (input instanceof CnATreeElement) {
            CnATreeElement elmt = (CnATreeElement) input;
            return getModel(elmt.getParent());
        }

        // input is not part of a proper tree / no BSIModel object could be
        // found as parent:
        return null;
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
        
        private Object [] elements;
        
        /**
         * @param name
         */
        private ExpandJob(Object [] elements) {
            super("Expanding");
            this.elements = (elements != null) ? elements.clone() : null;
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
    
    @Override
    public void validationAdded(Integer scopeId){};
    
    @Override
    public void validationRemoved(Integer scopeId){};
    
    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation){};

}
