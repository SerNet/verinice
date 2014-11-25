/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin <dm[at]sernet[dot]de>.
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
package sernet.verinice.rcp.tree;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.validation.CnAValidation;

/**
 * TreeUpdateListener updates a {@link TreeViewer} when a {@link CnATreeElement} was added, changed or removed.
 * It handles changes of ISO 27000 and BSI elements.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TreeUpdateListener implements IISO27KModelListener,IBSIModelListener {

    private static final Logger LOG = Logger.getLogger(TreeUpdateListener.class);

    private TreeViewer viewer;
    private TreeViewerUpdater updater;
    private Object[] expandedElements = null;
    private ElementManager elementManager;

    public TreeUpdateListener(TreeViewer viewer, ElementManager elementManager) {
        super();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new TreeUpdateListener...");
        }
        this.viewer = viewer;
        this.updater = new TreeViewerUpdater(viewer);
        this.elementManager = elementManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildRemoved
     * (sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public void databaseChildRemoved(CnATreeElement child) {
        try {
            getElementManager().elementRemoved(child);
            updater.remove(child);
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
    @Override
    public void databaseChildRemoved(ChangeLogEntry entry) {
        try {
            if(entry!=null && entry.getUuid()!=null) {
                getElementManager().elementRemoved(entry.getUuid());
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
     * sernet.verinice.iso27k.model.IISO27KModelListener#childAdded(sernet.gs
     * .ui.rcp.main.common.model.CnATreeElement,
     * sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public void childAdded(CnATreeElement parent, CnATreeElement child) {
        try {
            getElementManager().elementAdded(child);
            updater.add(parent, child);
            updater.refresh();
        } catch (Exception e) {
            LOG.error("Error while updating treeview", e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildAdded(
     * sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public void databaseChildAdded(CnATreeElement child) {
         /*
          *  CnAElementFactory.saveNew calls both
          *  CnAElementFactory.getModel(child).childAdded(container, child) and
          *  CnAElementFactory.getModel(child).databaseChildAdded(child);
          *  
          *  This method is empty, action is done in childAdded
          */
    }
    
    /**
     * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
     * @see sernet.verinice.model.bsi.IBSIModelListener#modelRefresh()
     */
    @Override
    public void modelRefresh() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#modelRefresh(java.lang
     * .Object)
     */
    @Override
    public void modelRefresh(Object model) {
        try {
            updater.refresh();
        } catch (Exception e) {
            LOG.error("Error while updating treeview", e);
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
    @Override
    public void childChanged(CnATreeElement child) {
        try {
            getElementManager().elementChanged(child);
            updater.refresh(child);
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
    @Override
    public void childRemoved(CnATreeElement parent, CnATreeElement child) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.model.IISO27KModelListener#databaseChildChanged
     * (sernet.gs.ui.rcp.main.common.model.CnATreeElement)
     */
    @Override
    public void databaseChildChanged(CnATreeElement child) {
        try {
            getElementManager().elementChanged(child);
            updater.refresh(child);
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void modelReload(ISO27KModel newModel) {
        doModelReload(newModel);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.model.bsi.IBSIModelListener#modelReload(sernet.verinice.model.bsi.BSIModel)
     */
    @Override
    public void modelReload(BSIModel newModel) {
        doModelReload(newModel);      
    }
    
    @Override
    public void validationAdded(Integer scopeId){};
    
    @Override
    public void validationRemoved(Integer scopeId){};
    
    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation){};
    
    
    /**
     * @param model A ISO27KModel or BSIModel
     */
    private void doModelReload(Object model) {
        try {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    try {
                        expandedElements = viewer.getExpandedElements();
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });

            getElementManager().clearCache();

            updater.setInput(model);
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
     * @return the elementManager
     */
    protected ElementManager getElementManager() {
        return elementManager;
    }

    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     * 
     */
    private final class ExpandJobRule implements ISchedulingRule {
        @Override
        public boolean contains(ISchedulingRule rule) {
            return rule.getClass() == ExpandJobRule.class;
        }

        @Override
        public boolean isConflicting(ISchedulingRule rule) {
            return rule.getClass() == ExpandJobRule.class;
        }
    }

    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     * 
     */
    private final class ExpandJob extends Job {

        Object[] elements;

        /**
         * @param name
         */
        private ExpandJob(Object[] elements) {
            super("Expanding");
            this.elements = (elements != null) ? elements.clone() : null;
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            monitor.setTaskName("Expanding element tree...");
            Display.getDefault().asyncExec(new Runnable() {
                @Override
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
