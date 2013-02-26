/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.util.Comparator;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ISMItemComparator implements Comparator<CnATreeElement> {

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(CnATreeElement o1, CnATreeElement o2) {
        if (o1 == null || o2 == null){
            return 0;
        }
        if (o1 instanceof IISO27kElement && o2 instanceof IISO27kElement ) {
            NumericStringComparator numericStringComparator = new NumericStringComparator();
            
            IISO27kElement isoelmt1 = (IISO27kElement) o1;
            IISO27kElement isoelmt2 = (IISO27kElement) o2;
            String title1 = isoelmt1.getAbbreviation() + isoelmt1.getTitle();
            String title2 = isoelmt2.getAbbreviation() + isoelmt2.getTitle();
            return numericStringComparator.compare(title1, title2);
        }
        return o1.getTitle().compareTo(o2.getTitle());
    }


}
