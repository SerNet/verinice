/*******************************************************************************
 * Copyright (c) 2009 Anne Hanekop <ah@sernet.de>
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Anne Hanekop <ah@sernet.de> 	- initial API and implementation
 *     ak@sernet.de					- various fixes, adapted to command layer
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;

/**
 * Defines what to do when an item is dropped into the TreeViewer.
 * 
 * @author ahanekop@sernet.de
 */
public class RisikoMassnahmenUmsetzungDropListener extends ViewerDropAdapter {

	private GefaehrdungsUmsetzung parent;
	private RisikoMassnahmenUmsetzung child;
	private TreeViewer viewer;

	/**
	 * Constructor sets the needed data.
	 * 
	 * @param newViewer the viewer to add the dropped element to
	 */
	public RisikoMassnahmenUmsetzungDropListener(TreeViewer newViewer) {
		super(newViewer);
		viewer = newViewer;
	}

	/**
	 * Adds a RiskoMassnahmenUmsetzung to the RiskoGefaehrdungsMassnahme
	 * is is dropped onto.
	 * 
	 * @param data the data to drop (not used - DNDItems instead)
	 * @return true if RiskoMassnahmenUmsetzung has been added successfully
	 *			to the GefaehrdungsUmsetzung, false else
	 */
	@Override
	public boolean performDrop(Object data) {
		
		/* get the target object */
		Object receiver = getCurrentTarget();

		/* get dropped elements*/
		for (Object toDrop : DNDItems.getItems()) {
			try {
				// class has already been validated at this point:
				parent = (GefaehrdungsUmsetzung) receiver;
				child = (RisikoMassnahmenUmsetzung) toDrop;

				List<IGefaehrdungsBaumElement> children = parent
						.getGefaehrdungsBaumChildren();

				if (child != null && child instanceof RisikoMassnahmenUmsetzung
						&& parent != null
						&& parent instanceof GefaehrdungsUmsetzung
						&& !(children.contains(child))) {
					
					parent.addGefaehrdungsBaumChild(child);
					child.setGefaehrdungsBaumParent(parent);
					
					child.setParent(parent);

					viewer.refresh();
					return true;
					
				} else {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Returns true, if drop is allowed (which is only the case if the
	 * target is a GefaehrdungsUmsetzung).
	 * 
	 * @param target the target object
	 * @param operation the current drag operation (copy, move, etc.)
	 * @param transferType the current transfer type
	 * @return true if target is a GefaehrdungsUmsetzung, false else
	 */
	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		if (target == null || !(target instanceof GefaehrdungsUmsetzung)) {
			return false;
		} else {
			return true;
		}
	}
}
