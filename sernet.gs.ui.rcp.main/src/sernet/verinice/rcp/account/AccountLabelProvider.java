package sernet.verinice.rcp.account;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.ElementTitleCache;

class AccountLabelProvider extends LabelProvider implements ITableLabelProvider {           

    private static final Logger LOG = Logger.getLogger(AccountLabelProvider.class);
    
    boolean titleMapInitialized = false;
    
    @Override
    public String getColumnText(Object element, int columnIndex) {
        try {
            if (element instanceof PlaceHolder) {
                return getPlaceHolderText(element, columnIndex);
            }
            Configuration account = (Configuration) element;
            GenericPerson person = new GenericPerson(account.getPerson());
            Integer scopeId = account.getPerson().getScopeId();
            switch (columnIndex) { 
                case 0:
                    return ElementTitleCache.get(scopeId);
                case 1:               
                    return person.getParentName();
                case 2:   
                    return account.getUser();
                case 3:
                    return person.getName();
                case 4:
                    return account.getEmail();
                case 5:
                    return convertToX(account.isAdminUser());
                case 6:
                    return convertToX(account.isScopeOnly()); 
                case 7:
                    return convertToX(account.isWebUser());
                case 8:
                    return convertToX(account.isRcpUser());
                case 9:
                    return convertToX(account.isDeactivatedUser());  
                
                default:
                    return null;
            }
        } catch (Exception e) {
            LOG.error("Error while getting column text", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    private String getPlaceHolderText(Object element, int columnIndex) {
        if (columnIndex == 1) {
            PlaceHolder ph = (PlaceHolder) element;
            return ph.getTitle();
        }
        return ""; //$NON-NLS-1$
    }
    
    public String convertToX(boolean value) {
        return (value) ? "X" : "";
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }
    
    


}