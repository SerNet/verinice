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
import org.eclipse.jface.viewers.TreeViewer;

import com.sun.xml.messaging.saaj.util.LogDomainConstants;

import sernet.gs.ui.rcp.main.bsi.views.ThreadSafeViewerUpdate;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27KModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class ISO27KModelViewUpdate implements IISO27KModelListener {

    private static final Logger LOG = Logger.getLogger(ISO27KModelViewUpdate.class);

    private TreeViewer viewer;
    private TreeViewerCache cache;
    private ThreadSafeViewerUpdate updater;

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
            LOG.error("Error while updating treeview", e);
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
    public void childChanged(CnATreeElement category, CnATreeElement child) {
        try {
            CnATreeElement cachedObject = cache.getCachedObject(child);
            if (cachedObject == null) {
                return; // not currently displayed or already changed object
                        // itself so nothing to update
            }
            if (cachedObject != child) {
                // update entity of cached object:
                CnAElementHome.getInstance().refresh(cachedObject);
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
            // remove listener from currently displayed model:
            getModel(viewer.getInput()).removeISO27KModelListener(this);
            newModel.addISO27KModelListener(this);
            cache.clear();
            updater.setInput(newModel);
            updater.refresh();
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

}
