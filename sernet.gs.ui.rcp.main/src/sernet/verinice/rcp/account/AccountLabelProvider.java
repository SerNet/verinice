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
            switch (columnIndex) {
            case 1:
                return account.getUser(); //$NON-NLS-1$
            case 2:
                return ((PersonIso)account.getPerson()).getName(); //$NON-NLS-1$
            
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