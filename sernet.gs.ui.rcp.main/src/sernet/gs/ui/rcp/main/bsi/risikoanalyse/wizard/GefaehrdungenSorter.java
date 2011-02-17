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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;

/**
 * Sorts the Gefaehrdungen.
 * 
 * @author ahanekop[at]sernet[dot]de
 */
public class GefaehrdungenSorter extends ViewerSorter {

	/**
	 * Compares two Gefaehrdungen and returns them sorted.
	 * Numbers are taken into account as well as characters.
	 * 
	 * @param viewer the viewer
     * @param e1 the first element
     * @param e2 the second element
     * @return a negative number if the first element is less than the 
     *  second element; the value <code>0</code> if the first element is
     *  equal to the second element; and a positive number if the first
     *  element is greater than the second element
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if ((e1 instanceof Gefaehrdung
				&& e2 instanceof Gefaehrdung)
				) {
			Gefaehrdung gef1 = (Gefaehrdung) e1;
			Gefaehrdung gef2 = (Gefaehrdung) e2;
			
			NumericStringComparator numComp = new NumericStringComparator();
			return numComp.compare(gef1.getId(), gef2.getId());
		}

		if ((e1 instanceof GefaehrdungsUmsetzung
				&& e2 instanceof GefaehrdungsUmsetzung)
		) {
			GefaehrdungsUmsetzung gef1 = (GefaehrdungsUmsetzung) e1;
			GefaehrdungsUmsetzung gef2 = (GefaehrdungsUmsetzung) e2;
			
			NumericStringComparator numComp = new NumericStringComparator();
			return numComp.compare(gef1.getId(), gef2.getId());
		}
		
		// else, consider elements as equal:
		return 0;
		
	}
}
