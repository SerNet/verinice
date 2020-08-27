package sernet.verinice.rcp.account;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.threeten.bp.LocalDate;

import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.hui.swt.SWTResourceManager;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.rcp.ElementTitleCache;
import sernet.verinice.service.account.AccountLoader;

class AccountLabelProvider extends ColumnLabelProvider implements ITableLabelProvider {

    private static final Logger LOG = Logger.getLogger(AccountLabelProvider.class);

    boolean titleMapInitialized = false;

    private Map<Integer, LicenseMessageInfos> lmInfosMap;

    private final Set<String> currentUserRoles;

    public AccountLabelProvider(Map<Integer, LicenseMessageInfos> lmInfosMap,
            Set<String> currentUserRoles) {
        super();
        this.lmInfosMap = lmInfosMap;
        this.currentUserRoles = currentUserRoles;
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
                return ElementTitleCache.getInstance().get(scopeId);
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

    private String getLMColumnLabel(int columnIndex, Configuration account) {
        LicenseMessageInfos infos = lmInfosMap.get(columnIndex);
        String licenseId = infos.getLicenseId();
        if (infos.getValidUntil().isBefore(LocalDate.now())) {
            return Messages.AccountView_LicenseExpired;
        }
        return convertToX(account.getAssignedLicenseIds().contains(licenseId));
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
        if (o instanceof Configuration
                && !AccountLoader.isEditAllowed((Configuration) o, currentUserRoles)) {
            return SWTResourceManager.getColor(SWT.COLOR_GRAY);
        }
        return super.getForeground(o);
    }
}