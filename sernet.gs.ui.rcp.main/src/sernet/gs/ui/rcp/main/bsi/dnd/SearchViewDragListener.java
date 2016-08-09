/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.*;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.SearchViewElementTransfer;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.service.commands.LoadElementsByUuid;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class SearchViewDragListener implements DragSourceListener {

    private static final Logger LOG = Logger.getLogger(SearchViewDragListener.class);
    TableViewer viewer;

    public SearchViewDragListener(TableViewer viewer) {
        this.viewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragStart(DragSourceEvent event) {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        if (selection == null) {
            event.doit = false;
            return;
        }
        for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (!(o instanceof VeriniceSearchResultRow)) {
                event.doit = false;
                return;
            }
        }
        event.doit = true;
    }



    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragSetData(DragSourceEvent event) {
        if (validateDrag(event)) {
            IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
            CnATreeElement[] elements = getElements(selection);
            event.data = DNDHelper.castDataArray(elements);

        }

    }

    private CnATreeElement[] getElements(IStructuredSelection selection) {

        HashSet<CnATreeElement> elements = new HashSet<>();
        LoadElementsByUuid<CnATreeElement> command;
        ArrayList<String> uuidList = new ArrayList<>();
        for (Object object : selection.toList()) {
            if (object instanceof VeriniceSearchResultRow) {
                VeriniceSearchResultRow resultRow = (VeriniceSearchResultRow) object;
                uuidList.add(resultRow.getIdentifier());
            }
        }
        command = new LoadElementsByUuid<>(uuidList, RetrieveInfo.getPropertyInstance());
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            elements.addAll(command.getElements());
        } catch (CommandException e) {
            LOG.error("Error while loading elements.", e);
        }
        return elements.toArray(new CnATreeElement[elements.size()]);
    }

    private boolean validateDrag(DragSourceEvent event) {
        return SearchViewElementTransfer.getInstance().isSupportedType(event.dataType);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
     */
    @Override
    public void dragFinished(DragSourceEvent event) {
        // nothing to do

    }

}
