/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.search.VeriniceSearchResult;

/**
 *
 */
public class SearchTableSorter extends ViewerSorter {
    
    private int propertyIndex;
    private static final int DEFAULT_SORT_COLUMN = 0;
    private static final int DESCENDING = 1;
    private static final int ASCENDING = 0;
    private int direction = ASCENDING;
    
    private static final NumericStringComparator NSC = new NumericStringComparator();
    
    public SearchTableSorter(){
        super();
        this.propertyIndex = DEFAULT_SORT_COLUMN;
        this.direction = ASCENDING;
    }
    
    public void setColumn(int column) {
        if (column == this.propertyIndex) {
            // Same column as last sort; toggle the direction
            direction = (direction == ASCENDING) ? DESCENDING : ASCENDING;
        } else {
            // New column; do an ascending sort
            this.propertyIndex = column;
            direction = ASCENDING;
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {    
        int rc = 0;
        if (e1 == null && e2 != null) {
            rc = 1;
        } else if (e2 == null && e1 != null) {
            rc = -1;
        } else {
            // e1 and e2 != null
            VeriniceSearchResult a1 = (VeriniceSearchResult) e1;
            VeriniceSearchResult a2 = (VeriniceSearchResult) e2;
            rc = compareNullSafe(a1, a2);
        }
        // If descending order, flip the direction
        if (direction == DESCENDING) {
            rc = -rc;
        }
        return rc;
    }
    
    private int compareNullSafe(VeriniceSearchResult a1, VeriniceSearchResult a2) {
        int rc = 0;
        switch (propertyIndex) {
        case 0:  
            rc = a1.getValueFromResultString("typeId").compareTo(a2.getValueFromResultString("typeId"));
            break;
        case 1:
            // implement compare for title here
            rc = 1;
            break;
        case 2:   
            rc = NSC.compare(a1.getFieldOfOccurence(), a2.getFieldOfOccurence());
            break;
        case 3:       
            rc = a1.getValueFromResultString("uuid").compareTo(a2.getValueFromResultString("uuid"));
            break;
        
        default:
            rc = 0;
        }
        return rc;
    }

}
