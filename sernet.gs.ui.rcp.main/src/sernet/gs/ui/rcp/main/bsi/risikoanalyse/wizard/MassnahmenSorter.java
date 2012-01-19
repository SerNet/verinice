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

import sernet.gs.model.Massnahme;
import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

/**
 * Sorts Massnahmen in table viewer.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class MassnahmenSorter extends ViewerSorter {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        NumericStringComparator numComp = new NumericStringComparator();
        if (e1 instanceof Massnahme && e2 instanceof Massnahme) {
            // sort chapters correctly by converting 2.45, 2.221, 3.42
            // to 2045, 2221, 3024
            
//            return (Integer.valueOf(((Massnahme) e1).getKapitelValue()).compareTo(((Massnahme) e2).getKapitelValue()));
            return numComp.compare(((Massnahme)e1).getId(), ((Massnahme)e2).getId());

        }

        if (e1 instanceof MassnahmenUmsetzung && e2 instanceof MassnahmenUmsetzung) {
            // sort chapters correctly by converting 2.45, 2.221, 3.42
            // to 2045, 2221, 3024
            return numComp.compare(((MassnahmenUmsetzung)e1).getEntity().getSimpleValue(MassnahmenUmsetzung.P_KAPITEL), ((MassnahmenUmsetzung)e2).getEntity().getSimpleValue(MassnahmenUmsetzung.P_KAPITEL));
        }
        
        
        

        // else, consider elements as equal:
        return 0;

    }
}
