/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.account;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.LicenseManagementException;

/**
 * Page for {@link AccountWizard} that enables to configure
 * license-management-properties (assigned license-IdD and automatic e-mail
 * notification) for a single instance of {@link Configuration}
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseMgmtPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(LicenseMgmtPage.class);
    private static final String BASIC_LICENSE_FORMAT = "%s - %s - (%s/%s)"; //$NON-NLS-1$
    public static final String PAGE_NAME = "account-wizard-license-mgmt-page"; //$NON-NLS-1$

    /**
     * Hold the license data.
     */
    private static class LicenseEntry {
        private static final String LICENSE_FORMAT = "%d. %s - %s - (%d/%d) %s"; //$NON-NLS-1$
        private Integer index;
        private Integer currentUser;
        private String plainLicenseId;
        private LocalDate validUntil;
        private Integer validUser;
        private String contentId;
        private String validMsg;

        private String toLicenseLabel(Integer offset) {
            return String.format(LICENSE_FORMAT, index, contentId, validUntil, currentUser + offset,
                    validUser, validMsg);
        }
    }

    ILicenseManagementService licenseService;

    private String user;

    private Set<String> assignedLicenseIds;
    private Set<Button> checkboxes;
    private boolean sendEmail;
    private Configuration account;

    public LicenseMgmtPage(Configuration account) {
        super(PAGE_NAME);
        this.licenseService = ServiceFactory.lookupLicenseManagementService();
        this.checkboxes = new HashSet<>();
        this.account = account;
    }

    @Override
    protected void initGui(Composite composite) {
        GridData containerGridData = new GridData();
        containerGridData.horizontalAlignment = SWT.LEFT;
        containerGridData.verticalAlignment = SWT.CENTER;
        containerGridData.grabExcessHorizontalSpace = true;
        containerGridData.grabExcessVerticalSpace = true;

        composite.setLayoutData(containerGridData);
        composite.setEnabled(!AccountWizard.isCurrentUserLocalAdmin());

        setTitle(Messages.LicenseMgmtPage_Title);
        setMessage(Messages.LicenseMgmtPage_Text);

        try {
            Set<LicenseManagementEntry> licenseList = licenseService.getExistingLicenses();
            List<LicenseEntry> lEntries = licenseList.stream().map(this::createLicenseEntry)
                    .sorted((o1, o2) -> o1.toLicenseLabel(0).compareTo(o2.toLicenseLabel(0)))
                    .collect(Collectors.toList());
            lEntries.stream().forEach(le -> {
                le.index = lEntries.indexOf(le) + 1;
                createCheckbox(composite, le);
            });
            if (lEntries.isEmpty()) {
                Label label = new Label(composite, SWT.NONE);
                label.setText(Messages.LicenseMgmtPage_NoLicenseAvailable);
            }

            Composite emailComposite = new Composite(composite, SWT.BORDER);

            GridData emailGridData = new GridData();
            emailGridData.horizontalAlignment = SWT.LEFT;
            emailGridData.verticalAlignment = SWT.BOTTOM;
            emailGridData.grabExcessHorizontalSpace = true;
            emailGridData.grabExcessVerticalSpace = true;
            emailComposite.setLayoutData(emailGridData);

            Button sendEmailButton = new Button(emailComposite, SWT.CHECK);

            GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 0;
            gridLayout.horizontalSpacing = 0;
            emailComposite.setLayout(gridLayout);
            GridData gridData = new GridData();
            gridData.horizontalAlignment = SWT.LEFT;
            gridData.verticalAlignment = SWT.BOTTOM;
            gridData.grabExcessHorizontalSpace = true;
            gridData.grabExcessVerticalSpace = true;
            sendEmailButton.setLayoutData(gridData);
            sendEmailButton.setText(Messages.LicenseMgmtPage_Notification);
            sendEmailButton.setSelection(isSendEmail());
            sendEmailButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (e.getSource() instanceof Button) {
                        Button button = (Button) e.getSource();
                        setSendEmail(button.getSelection());
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    widgetSelected(e);
                }
            });
            sendEmailButton.setEnabled(!licenseList.isEmpty());
        } catch (LicenseManagementException e) {
            String msg = "Error getting vnl-License-Data";
            ExceptionUtil.log(e, msg);
            LOG.error(msg, e);
        }
    }

    @Override
    protected void initData() throws Exception {
        // no further setup
    }

    @Override
    public boolean isPageComplete() {
        return true;
    }

    /**
     * @param assignedLicenseIds
     *            the assignedLicenseIds to set
     */
    public void setAssignedLicenseIds(Set<String> assignedLicenseIds) {
        this.assignedLicenseIds = assignedLicenseIds;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Builds the string that labels a given licenseId in the client
     */
    public static String getLicenseLabelString(String licenseId) {
        final String licenseIdDelimiter = "###";
        StringTokenizer tokenizer = new StringTokenizer(licenseId, licenseIdDelimiter);
        if (tokenizer.countTokens() != 3) {
            return licenseId;
        }
        String contentId = tokenizer.nextToken();
        String validUntil = tokenizer.nextToken();
        String validUsers = tokenizer.nextToken();

        String assignedUsers = String.valueOf(ServiceFactory.lookupLicenseManagementService()
                .getLicenseIdAllocationCount(licenseId));

        return String.format(BASIC_LICENSE_FORMAT, contentId, validUntil, validUsers,
                assignedUsers);
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public void setAccount(Configuration account) {
        this.account = account;
    }

    private void createCheckbox(Composite composite, final LicenseEntry licenseEntry) {
        final Button checkbox = new Button(composite, SWT.CHECK);
        checkboxes.add(checkbox);
        boolean isLicenseAssigned = assignedLicenseIds.contains(licenseEntry.plainLicenseId);
        final Integer uncheckOffset = isLicenseAssigned ? -1 : 0;
        final Integer checkOffset = isLicenseAssigned ? 0 : 1;

        checkbox.setSelection(isLicenseAssigned);
        checkbox.setEnabled(licenseEntry.currentUser < licenseEntry.validUser || isLicenseAssigned);
        checkbox.setText(licenseEntry.toLicenseLabel(0));
        checkbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (checkbox.getSelection()) {
                    account.addLicensedContentId(licenseEntry.plainLicenseId);
                    assignedLicenseIds.add(licenseEntry.plainLicenseId);
                    checkbox.setText(licenseEntry.toLicenseLabel(checkOffset));
                } else {
                    account.removeLicensedContentId((licenseEntry.plainLicenseId));
                    assignedLicenseIds.remove(licenseEntry.plainLicenseId);
                    checkbox.setText(licenseEntry.toLicenseLabel(uncheckOffset));
                }
            }
        });
    }

    /**
     * create the data from the license entry.
     */
    private LicenseEntry createLicenseEntry(LicenseManagementEntry entry) {
        LicenseEntry dh = new LicenseEntry();
        dh.plainLicenseId = licenseService.decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
        dh.contentId = licenseService.decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
        dh.validUntil = licenseService.decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUNTIL);
        dh.validUser = licenseService.decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUSERS);

        dh.currentUser = licenseService.getLicenseIdAllocationCount(dh.plainLicenseId);
        dh.validMsg = dh.validUntil.isBefore(LocalDate.now())
                ? Messages.LicenseMgmtPage_License_Expired
                : "";
        return dh;
    }

}
