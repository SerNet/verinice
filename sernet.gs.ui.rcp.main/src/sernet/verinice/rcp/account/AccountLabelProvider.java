package sernet.verinice.rcp.account;

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.threeten.bp.LocalDate;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.rcp.ElementTitleCache;
import sernet.verinice.service.account.AccountLoader;

class AccountLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {

    private static final Logger LOG = Logger.getLogger(AccountLabelProvider.class);
    
    boolean titleMapInitialized = false;
    
    private Map<Integer, LicenseMessageInfos> lmInfosMap;
    
    private TableViewer viewer;
    
    public AccountLabelProvider(Map<Integer, LicenseMessageInfos> lmInfosMap,
            TableViewer viewer) {
        super();
        this.lmInfosMap = lmInfosMap;
        this.viewer = viewer;
    }
    
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
                return convertToX(account.isLocalAdminUser());
            case 7:
                return convertToX(account.isScopeOnly()); 
            case 8:
                return convertToX(account.isWebUser());
            case 9:
                return convertToX(account.isRcpUser());
            case 10:
                return convertToX(account.isDeactivatedUser());
            case 11:
                return getLMColumnLabel(11, account);

            default:
                if (columnIndex > 10) {
                    return getLMColumnLabel(columnIndex, account);
                }
                return null;
            }
        } catch (Exception e) {
            LOG.error("Error while getting column text", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    private String getLMColumnLabel(int columnIndex, Configuration account){
        LicenseMessageInfos infos = lmInfosMap.get(columnIndex);
        int newCount = 
                ServiceFactory.lookupLicenseManagementService().
                getLicenseIdAllocationCount(infos.getLicenseId());
        String licenseId = infos.getLicenseId();
        if (infos.getAssignedUsers()!=newCount){
            refreshColumnTooltip(columnIndex, newCount);
        }
        if(infos.getValidUntil().isBefore(LocalDate.now())){
            return Messages.AccountView_LicenseExpired;
        }
        return convertToX(
                account.getAssignedLicenseIds().
                contains(licenseId));
    }

    /**
     * refreshes assignmentcount in columntooltip after
     * new license has been assigned to user
     * 
     * @param columnIndex
     * @param newCount
     */
    private void refreshColumnTooltip(int columnIndex, int newCount) {
        String oldColumnTooltip = 
                viewer.getTable().getColumn(columnIndex).getToolTipText();
        StringBuilder replacement = new StringBuilder();
        replacement.append("- (");
        replacement.append(String.valueOf(newCount));
        replacement.append("/");
        String newColumnTooltip = 
                oldColumnTooltip.replaceFirst("- \\(\\d/", replacement.toString());
        viewer.getTable().getColumn(columnIndex).setToolTipText(newColumnTooltip);
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    @Override
    public Color getForeground(Object o) {
        if (o instanceof Configuration && !AccountLoader.isEditAllowed((Configuration) o)) {
            return Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
        }
        return super.getForeground(o);
    }
}