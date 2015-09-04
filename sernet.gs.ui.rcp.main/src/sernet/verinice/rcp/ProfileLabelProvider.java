package sernet.verinice.rcp;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.ProfileRef;

public class ProfileLabelProvider extends ColumnLabelProvider {
    
    private IRightsServiceClient rightsService;

    public ProfileLabelProvider() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        String text = Messages.UserprofileDialog_17;
        if (element instanceof ProfileRef) {
            text = ((ProfileRef) element).getName();
        }
        if (element instanceof Action) {
            text = ((Action) element).getId();
        }
        // get lacalized message
        text = getRightService().getMessage(text);
        return text;
    }
    
    
    IRightsServiceClient getRightService() {
        if (rightsService == null) {
            rightsService = (IRightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }
}