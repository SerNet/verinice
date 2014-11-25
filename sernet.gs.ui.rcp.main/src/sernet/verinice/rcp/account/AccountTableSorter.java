package sernet.verinice.rcp.account;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.ElementTitleCache;

class AccountTableSorter extends ViewerSorter {
    private int propertyIndex;
    private static final int DEFAULT_SORT_COLUMN = 0;
    private static final int DESCENDING = 1;
    private static final int ASCENDING = 0;
    private int direction = ASCENDING;
    
    private static final NumericStringComparator nsc = new NumericStringComparator();

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
        int rc = 0;
        if (e1 == null && e2 != null) {
            rc = 1;
        } else if (e2 == null && e1 != null) {
            rc = -1;
        } else {
            // e1 and e2 != null
            Configuration a1 = (Configuration) e1;
            Configuration a2 = (Configuration) e2;
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
        CnATreeElement p1 = a1.getPerson();
        CnATreeElement p2 = a2.getPerson();
        PersonAdapter adapter = new PersonAdapter(p1, p2);
        switch (propertyIndex) {
        case 0:  
            rc = compareTitle(p1, p2);
            break;
        case 1:
            rc = adapter.compareParentName();
            break;
        case 2:   
            rc = nsc.compare(a1.getUser().toLowerCase(), a2.getUser().toLowerCase());
            break;
        case 3:       
            rc = adapter.compareName();
            break;
        case 4:   
            rc = nsc.compare(a1.getEmail().toLowerCase(), a2.getEmail().toLowerCase());
            break;
        case 5:
            rc = Boolean.valueOf(a1.isAdminUser()).compareTo(a2.isAdminUser());
            break; 
        case 6:
            rc = Boolean.valueOf(a1.isScopeOnly()).compareTo(a2.isScopeOnly());
            break;  
        case 7:
            rc = Boolean.valueOf(a1.isWebUser()).compareTo(a2.isWebUser());
            break; 
        case 8:
            rc = Boolean.valueOf(a1.isRcpUser()).compareTo(a2.isRcpUser());
            break; 
        case 9:
            rc = Boolean.valueOf(a1.isDeactivatedUser()).compareTo(a2.isDeactivatedUser());
            break;   
        
        default:
            rc = 0;
        }
        return rc;
    }

    private int compareTitle(CnATreeElement p1, CnATreeElement p2) {
        int rc = 0;
        String title1 = ElementTitleCache.get(p1.getScopeId());
        String title2 = ElementTitleCache.get(p2.getScopeId());
        if(title1!=null && title2!=null) {
            rc = nsc.compare(title1.toLowerCase(), title2.toLowerCase());
        }
        return rc;
    }
    
    class PersonAdapter {
        
        GenericPerson p1;
        GenericPerson p2;
        
        NumericStringComparator nsc = new NumericStringComparator();
        
        public PersonAdapter(CnATreeElement p1, CnATreeElement p2) {
            super();
            this.p1 = new GenericPerson(p1);
            this.p2 = new GenericPerson(p2);
        }

        public int compareName() {
            String name1 = p1.getName();
            if(name1!=null) {
                name1 = name1.toLowerCase();
            }
            String name2 = p2.getName();
            if(name2!=null) {
                name2 = name2.toLowerCase();
            }
            return nsc.compare(name1, name2);
        }
        
        public int compareParentName() {
            String name1 = p1.getParentName();
            if(name1!=null) {
                name1 = name1.toLowerCase();
            }
            String name2 = p2.getParentName();
            if(name2!=null) {
                name2 = name2.toLowerCase();
            }
            return nsc.compare(name1, name2);
        }

     
    }
}