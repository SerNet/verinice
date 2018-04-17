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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.*;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * 
 * Class for drag and drop between SearchView and {@link ISMView} /
 * {@link BSIModelView}
 * 
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class SearchViewDropListener extends ViewerDropAdapter
        implements DropPerformer, RightEnabledUserInteraction {


    private transient Logger log = Logger.getLogger(SearchViewDropListener.class);

    private boolean isActive = false;
    private Object target = null;

    public SearchViewDropListener(TableViewer viewer) {
        super(viewer);
    }

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SearchViewDropListener.class);
        }
        return log;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient) VeriniceContext
                .get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.TREEDND;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (getLog().isDebugEnabled()) {
            getLog().debug(
                    "validateDrop, \n\t transfer type class: " + transferType.getClass().getName());
        }
        if (!checkRights()) {
            return false;
        }
        if (target == null) {
            if (log.isDebugEnabled()) {
                getLog().debug("target null - false");
            }
            isActive = false;
            return isActive;
        } else {
            this.target = target;
        }
        isActive = target instanceof VeriniceSearchResultRow;
        if (log.isDebugEnabled()) {
            getLog().debug("validation returns " + isActive);
        }
        return isActive;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
     */
    @Override
    public boolean isActive() {
        return isActive;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    @Override
    public boolean performDrop(Object toDrop) {
        Object[] dataToDrop = (Object[]) toDrop;
        if (dataToDrop == null || dataToDrop.length == 0) {
            getLog().error("data missing");
            return false;
        }
        if (getLog().isDebugEnabled()) {
            getLog().debug("performDrop");
        }
        Object firstObject = dataToDrop[0];
        if (isActive()) {
            return handleDrop(dataToDrop);
        } else {
            if (getLog().isDebugEnabled()) {
                getLog().debug(firstObject + " not supported element");
            }
            return false;
        }
    }

    private boolean handleDrop(Object[] data) {

        if (getLog().isDebugEnabled()) {
            getLog().debug("BSI");
        }

        ArrayList<CnATreeElement> toDrop = new ArrayList<>(data.length);
        for (Object object : data) {
            toDrop.add((CnATreeElement) object);
        }

        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<>(
                ((VeriniceSearchResultRow) target).getIdentifier());
        CnATreeElement element;
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            element = command.getElement();

        } catch (CommandException e) {

            getLog().error(e);
            return false;
        }
        LinkDropper dropper = new LinkDropper();

        return dropper.dropLink(toDrop, element);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.
     * Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        return performDrop(data);
    }

    @Override
    public void drop(DropTargetEvent event) {
        getLog().debug("entered drop(DropTargetEvent event) with event:" + event.toString());
        target = determineTarget(event);
        super.drop(event);
    }

}
