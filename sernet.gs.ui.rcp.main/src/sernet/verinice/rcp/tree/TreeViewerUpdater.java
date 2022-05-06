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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementById;

/**
 * Helper to execute viewer updates from any thread. Avoids the overhead of
 * synchronizing if access is from the same thread.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TreeViewerUpdater {

    private static final Logger LOG = Logger.getLogger(TreeViewerUpdater.class);

    private TreeViewer viewer;

    public TreeViewerUpdater(TreeViewer viewer) {
        this.viewer = viewer;
    }

    public void add(final Object parent, final Object child) {
        if (parent != null && child != null) {
            executeInRenderThread(() -> viewer.add(parent, child));
        }
    }

    public void refresh(final Object child) {
        executeInRenderThread(() -> viewer.refresh(child));
    }

    public void remove(final Object child) {
        executeInRenderThread(() -> viewer.remove(child));
    }

    public void refresh() {
        executeInRenderThread(() -> {
            if (viewer != null) {
                viewer.refresh();
            }
        });
    }

    public void setInput(final Object newModel) {
        final Object newInput = getNewInput(newModel);
        executeInRenderThread(() -> {
            if (!viewer.getTree().isDisposed()) {
                viewer.setInput(newInput);
            }
        });
    }

    private static void executeInRenderThread(Runnable r) {
        if (Display.getCurrent() != null) {
            // the current thread is a render thread
            r.run();
        } else {
            Display.getDefault().asyncExec(r);
        }
    }

    private Object getNewInput(final Object newModel) {
        Object returnValue = newModel;
        Object oldInput = viewer.getInput();
        if (oldInput != null && !oldInput.getClass().equals(newModel.getClass())
                && oldInput instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) oldInput;
            RetrieveInfo ri = new RetrieveInfo();
            ri.setProperties(true).setPermissions(true).setParent(true).setChildren(true)
                    .setSiblings(true);
            LoadElementById<CnATreeElement> loadByUuid = new LoadElementById<>(element.getDbId(),
                    ri);
            try {
                Activator.inheritVeriniceContextState();
                loadByUuid = ServiceFactory.lookupCommandService().executeCommand(loadByUuid);
                element = loadByUuid.getElement();
                if (element.getParent() != null) {
                    element.getParent().setParent(null);
                }
                returnValue = element;
            } catch (CommandException e) {
                LOG.error("Error while reloading tree root", e);
            }
        }
        return returnValue;
    }

}
