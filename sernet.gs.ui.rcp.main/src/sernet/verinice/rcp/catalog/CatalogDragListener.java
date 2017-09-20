/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.catalog;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionModelingTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;

/**
 * This DragSourceListener class selects modules for the modeling process
 * of IT base protection while one or more modules
 * are dragged from sernet.verinice.rcp.catalog.CatalogView 
 * and dropped on an element in BaseProtectionView.
 * 
 * @see BbModelingDropPerformer
 * @see MetaDropAdapter
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class CatalogDragListener implements DragSourceListener {

    private static final Logger log = Logger.getLogger(CatalogDragListener.class);
    
    private TreeViewer treeViewer;

    public CatalogDragListener(TreeViewer viewer) {
        this.treeViewer = viewer;
    }

    /*
     * @see
     * org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.
     * DragSourceEvent)
     */
    @Override
    public void dragStart(DragSourceEvent event) {
        if (getTreeSelection().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Can not start dragging. No element in tree is selected.");
            }
            event.doit = false;      
        } else {
            boolean isValid = BaseProtectionModelingTransfer.isDraggedDataValid(getTreeSelection().toArray());
            if (log.isDebugEnabled()) {
                log.debug("Dragged data validation state: " + isValid);
            }
            event.doit = isValid;
        }
    }

    /*
     * @see
     * org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.
     * DragSourceEvent)
     */
    @Override
    public void dragSetData(DragSourceEvent event) {
        // Provide the data of the requested type.
        if (getTransfer().isSupportedType(event.dataType)) {
            event.data = getTreeSelection().toArray();
            if (log.isDebugEnabled()) {
                log.debug("Drag data set: ");
                logData((Object[]) event.data);
            }
        }
    }

    /*
     * @see
     * org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.
     * DragSourceEvent)
     */
    @Override
    public void dragFinished(DragSourceEvent event) {
        // nothing to do
    }

    protected IStructuredSelection getTreeSelection() {
        return (IStructuredSelection) treeViewer.getSelection();
    }

    protected VeriniceElementTransfer getTransfer() {
        return BaseProtectionModelingTransfer.getInstance();
    }
    
    private void logData(Object[] dataArray) {
        for (Object object : dataArray) {
            log.debug(object);
        }
    }

}
