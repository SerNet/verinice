/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah[at]sernet[dot]de>
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
 *     Anne Hanekop <ah[at]sernet[dot]de> 	- initial API and implementation
 *     ak[at]sernet[dot]de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.AddMassnahmeToGefaherdung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.IGefaehrdungsBaumElement;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;

/**
 * Defines what to do when an item is dropped into the TreeViewer.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class RisikoMassnahmenUmsetzungDropListener extends ViewerDropAdapter {

    private static final Logger LOG = Logger.getLogger(RisikoMassnahmenUmsetzungDropListener.class);

    private TreeViewer viewer;

    /**
     * Constructor sets the needed data.
     * 
     * @param newViewer
     *            the viewer to add the dropped element to
     */
    public RisikoMassnahmenUmsetzungDropListener(TreeViewer newViewer) {
        super(newViewer);
        viewer = newViewer;
    }

    /**
     * Adds a RiskoMassnahmenUmsetzung to the RiskoGefaehrdungsMassnahme is is
     * dropped onto.
     * 
     * @param data
     *            the data to drop (not used - DNDItems instead)
     * @return true if RiskoMassnahmenUmsetzung has been added successfully to
     *         the GefaehrdungsUmsetzung, false else
     */
    @Override
    public boolean performDrop(Object data) {
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("performDrop...");
        }
        
        /* get the target object */
        Object receiver = getCurrentTarget();
        Object selectedData = null;
        if(data == null){
            selectedData = ((IStructuredSelection)this.getViewer().getSelection()).toArray();
        } else {
            selectedData = data;
        }

        /* get dropped elements */
        for (Object toDrop : DNDHelper.arrayToList(selectedData)) {
            try {
                // class has already been validated at this point:
                GefaehrdungsUmsetzung parent = (GefaehrdungsUmsetzung) receiver;
                RisikoMassnahmenUmsetzung child = (RisikoMassnahmenUmsetzung) toDrop;

                List<IGefaehrdungsBaumElement> children = parent.getGefaehrdungsBaumChildren();

                if (child instanceof RisikoMassnahmenUmsetzung 
                        && parent instanceof GefaehrdungsUmsetzung 
                        && !(children.contains(child))) {

                    AddMassnahmeToGefaherdung command = new AddMassnahmeToGefaherdung(parent, child);
                    command = ServiceFactory.lookupCommandService().executeCommand(command);
                    child = command.getChild();

                    // add for viewer:
                    parent.addChild(child);
                    viewer.refresh();
                    viewer.setExpandedState(parent, true);
                    return true;

                } else {
                    return false;
                }
            } catch (Exception e) {
                ExceptionUtil.log(e, Messages.RisikoMassnahmenUmsetzungDropListener_0);
                return false;
            }
        }
        return false;
    }

    /**
     * Returns true, if drop is allowed (which is only the case if the target is
     * a GefaehrdungsUmsetzung).
     * 
     * @param target
     *            the target object
     * @param operation
     *            the current drag operation (copy, move, etc.)
     * @param transferType
     *            the current transfer type
     * @return true if target is a GefaehrdungsUmsetzung, false else
     */
    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (!(target instanceof GefaehrdungsUmsetzung)) {
            return false;
        } 
        return true;
    }
}
