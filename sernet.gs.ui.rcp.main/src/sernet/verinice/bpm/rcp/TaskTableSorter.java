/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import java.util.Date;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.bpm.ITask;


/**
 * Table sorter for table in task view
 * 
 * @see sernet.verinice.bpm.rcp.TaskView
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
class TaskTableSorter extends ViewerSorter {
    
    private static final Logger LOG = Logger.getLogger(TaskTableSorter.class);
    
    private int propertyIndex;
    private static final int DEFAULT_SORT_COLUMN = 0;
    private static final int DESCENDING = 1;
    private static final int ASCENDING = 0;
    private int direction = ASCENDING;
    
    private static final NumericStringComparator NSC = new NumericStringComparator();

    public TaskTableSorter() {
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
            ITask a1 = (ITask) e1;
            ITask a2 = (ITask) e2;
            rc = compareNullSafe(a1, a2);
        }
        // If descending order, flip the direction
        if (direction == DESCENDING) {
            rc = -rc;
        }
        return rc;
    }

    private int compareNullSafe(ITask a1, ITask a2) {
        int rc = 0;
        switch (propertyIndex) {
        case 0:  
            // prio           
            rc = NSC.compare(a1.getPriority(), a2.getPriority());
            break;
        case 1:  
            // Group           
            rc = NSC.compare(a1.getGroupTitle(), a2.getGroupTitle());
            break;
        case 2:
            // Object
            rc = NSC.compare(a1.getElementTitle(), a2.getElementTitle());
            break;
        case 3:  
            // Process
            rc = compareStringNullSave(a1.getProcessName(), a2.getProcessName());
            break;
        case 4:    
            // Task type
            rc = compareStringNullSave(a1.getName(), a2.getName());
            break;
        case 5:   
            // Assignee
            rc = compareStringNullSave(a1.getAssignee(), a2.getAssignee());
            break;
        case 6:
            // due date
            rc = compareDateNullSave(a1.getDueDate(), a2.getDueDate());
            break; 
        default:
            rc = 0;
        }
        return rc;
    }

    private int compareStringNullSave(String s1, String s2) {
        if(s1==null) {
            if(s2==null) {
                return 0;
            } else {
                return 1;
            }
        }
        int result = s1.compareToIgnoreCase(s2);
        if (LOG.isDebugEnabled()) {
            LOG.debug(result + " " + s1 + " - " + s2 + " (compare result)");
        }
        return result;
    }
    
    private int compareDateNullSave(Date d1, Date d2) {
        if(d1==null) {
            if(d2==null) {
                return 0;
            } else {
                return 1;
            }
        }
        return d1.compareTo(d2);
    }

    
    
}