package sernet.verinice.rcp.account;

import java.text.DateFormat;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

class AccountLabelProvider extends LabelProvider implements ITableLabelProvider {           

    @Override
    public String getColumnText(Object element, int columnIndex) {
        try {
            if (element instanceof PlaceHolder) {
                if (columnIndex == 1) {
                    PlaceHolder ph = (PlaceHolder) element;
                    return ph.getTitle();
                }
                return ""; //$NON-NLS-1$
            }
            Configuration account = (Configuration) element;
            GenericPerson person = new GenericPerson(account.getPerson());
            switch (columnIndex) {
                case 0:
                    return account.getUser();
                case 1:
                    return person.getName();
                case 2:
                    return account.getEmail();
                case 3:
                    return (account.isAdminUser()) ? "X" : "";
                case 4:
                    return (account.isScopeOnly()) ? "X" : ""; 
                case 5:
                    return (account.isWebUser()) ? "X" : "";
                case 6:
                    return (account.isRcpUser()) ? "X" : "";
                case 7:
                    return (account.isDeactivatedUser()) ? "X" : "";  
                
                default:
                    return null;
            }
        } catch (Exception e) {
            AccountView.LOG.error("Error while getting column text", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }


}