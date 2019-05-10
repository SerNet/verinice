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
import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.hui.common.connect.ITitledElement;
import sernet.verinice.model.common.CnATreeElement;

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
        String title1 = o1.getTitle();
        String title2 = o2.getTitle();
        if (o1 instanceof IAbbreviatedElement && o2 instanceof IAbbreviatedElement
                && o1 instanceof ITitledElement && o2 instanceof ITitledElement) {
            NumericStringComparator numericStringComparator = new NumericStringComparator();
            
            title1 = ((IAbbreviatedElement) o1).getAbbreviation() + title1;
            title2 = ((IAbbreviatedElement) o2).getAbbreviation() + title2;
            return numericStringComparator.compare(title1, title2);
        }
        return title1.compareTo(title2);
    }


}
