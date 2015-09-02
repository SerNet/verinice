package sernet.verinice.rcp;

import java.text.Collator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.ProfileRef;

public class ProfileTableComparator extends ViewerComparator {
   
    private int propertyIndex;
    private static final int ASCENDING = 0;
    private static final int DESCENDING = 1;
    private int direction = ASCENDING;
    private Collator collator = Collator.getInstance();
    
    private IRightsServiceClient rightsService;

    public ProfileTableComparator() {
        this.propertyIndex = 0;
        direction = ASCENDING;
    }

    public int getDirection() {
        return direction == 1 ? SWT.DOWN : SWT.UP;
    }

    public void setColumn(int column) {
        if (column == this.propertyIndex) {
            // Same column as last sort; toggle the direction
            direction = 1 - direction;
        } else {
            // New column; do an ascending sort
            this.propertyIndex = column;
            direction = DESCENDING;
        }
    }

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        String name1=null,name2=null;
        if(e1 instanceof ProfileRef) {
            name1 = ((ProfileRef) e1).getName();
        }
        if(e1 instanceof Action) {
            name1 = ((Action) e1).getId();
        }
        if(e2 instanceof ProfileRef) {
            name2 = ((ProfileRef) e2).getName();
        }
        if(e2 instanceof Action) {
            name2 = ((Action) e2).getId();
        }
        name1 = getRightService().getMessage(name1);
        name2 = getRightService().getMessage(name2);
        int rc = 0;
        switch (propertyIndex) {
        case 0:            
            rc = collator.compare(name1, name2);
            break;
        default:
            rc = 0;
        }
        // If descending order, flip the direction
        if (direction == DESCENDING) {
            rc = -rc;
        }
        return rc;
    }
    
    IRightsServiceClient getRightService() {
        if (rightsService == null) {
            rightsService = (IRightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }
}