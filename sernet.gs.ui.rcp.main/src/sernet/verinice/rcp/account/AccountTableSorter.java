package sernet.verinice.rcp.account;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

class AccountTableSorter extends ViewerSorter {
    private int propertyIndex;
    private static final int DEFAULT_SORT_COLUMN = 0;
    private static final int DESCENDING = 1;
    private static final int ASCENDING = 0;
    private int direction = ASCENDING;
    
    NumericStringComparator nsc = new NumericStringComparator();

    public AccountTableSorter() {
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
        Configuration a1 = (Configuration) e1;
        Configuration a2 = (Configuration) e2;
        int rc = 0;
        if (e1 == null && e2 != null) {
            rc = 1;
        } else if (e2 == null && e1 != null) {
            rc = -1;
        } else {
            // e1 and e2 != null
            rc = compareNullSafe(a1, a2);
        }
        // If descending order, flip the direction
        if (direction == DESCENDING) {
            rc = -rc;
        }
        return rc;
    }

    private int compareNullSafe(Configuration a1, Configuration a2) {
        int rc = 0;
        switch (propertyIndex) {
        case 0:   
            rc = nsc.compare(a1.getUser().toLowerCase(), a2.getUser().toLowerCase());
            break;
        case 1:
            PersonIso p1 = (PersonIso) a1.getPerson();
            PersonIso p2 = (PersonIso) a2.getPerson();
            // use lowercase here, to avoid separation of lowercase and uppercase words
            rc = nsc.compare(p1.getName().toLowerCase(), p2.getName().toLowerCase());
            break;
        default:
            rc = 0;
        }
        return rc;
    }
}