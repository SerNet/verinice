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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.threeten.bp.LocalDate;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.service.commands.SaveConfiguration;

/**
 * Page for {@link AccountWizard} that enables to configure
 * license-management-properties (assigned license-Ids and 
 * automatic e-mail notification) for a single instance of 
 * {@link Configuration}
 * 
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseMgmtPage extends BaseWizardPage {

    ILicenseManagementService licenseService;
    
    private static final Logger LOG = Logger.getLogger(LicenseMgmtPage.class);    
    public static final String PAGE_NAME = "account-wizard-license-mgmt-page"; //$NON-NLS-1$

    private Set<String> assignedLicenseIds;
    private Set<Button> checkboxes;
    private Map<String, LicenseManagementEntry> licenseEntryMap;
    private Map<String, String> licenseIdToLabelMap;
    private String user;
    private boolean sendEmail;
    private Configuration account;
    
    
    public LicenseMgmtPage(Configuration account) {
        super(PAGE_NAME);
        this.licenseService = ServiceFactory.lookupLicenseManagementService();
        this.checkboxes = new HashSet<>();
        this.licenseEntryMap = new HashMap<>();
        this.licenseIdToLabelMap = new HashMap<>();
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
        
        setTitle(Messages.LicenseMgmtPage_Title);
        setMessage(Messages.LicenseMgmtPage_Text);

        try {
            List<LicenseManagementEntry> licenseList = 
                    getSortedExistingLicenses();
            
            if(licenseList.size() > 0){
                for (int index = 0; index < licenseList.size(); index++){
                    createLicenseIdCheckbox(composite, licenseList.get(index), 
                            index);
                }
            } else {
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
            
            Button sendEmailButton = new Button(emailComposite, SWT.CHECK );
            
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
                    if (e.getSource() instanceof Button){
                        Button button = (Button)e.getSource();
                        setSendEmail(button.getSelection());
                    }
                }
                
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    widgetSelected(e);
                }
            });
            sendEmailButton.setEnabled(licenseList.size() > 0);
        } catch (LicenseManagementException e){
            String msg = "Error getting vnl-License-Data";
            ExceptionUtil.log(e, msg);
            LOG.error(msg, e);

        }

    }

    /**
     * sorts all in the system existing licenses by their label as 
     * shown in the wizardpage and returns them as a list 
     * @return
     * @throws LicenseManagementException
     */
    private List<LicenseManagementEntry> getSortedExistingLicenses() 
            throws LicenseManagementException {
        List<LicenseManagementEntry> licenseList = new ArrayList<>(); 
        licenseList.addAll(licenseService.getExistingLicenses());
        Collections.sort(licenseList, new Comparator<LicenseManagementEntry>() {
            NumericStringComparator ncs = new NumericStringComparator();

            @Override
            public int compare(LicenseManagementEntry entry1, 
                    LicenseManagementEntry entry2) {
                String licenseId1 = licenseService.decrypt(entry1, 
                        LicenseManagementEntry.COLUMN_LICENSEID);
                String licenseId2 = licenseService.decrypt(entry2, 
                        LicenseManagementEntry.COLUMN_LICENSEID);
                String label1 = getLicenseLabel(licenseId1, false);
                String label2 = getLicenseLabel(licenseId2, false);
                return ncs.compare(label1, label2);
            }
        });
        return licenseList;
    }

    /**
     * creates a checkbox for a given instance of 
     * {@link de.sernet.model.licensemanagement.LicenseManagementEntry}
     * , adds an increasing index number as a prefix and optionally
     * a expired label
     * 
     * @param composite
     * @param entry
     * @param index
     */
    private void createLicenseIdCheckbox(Composite composite, 
            LicenseManagementEntry entry, int index) {
        Button checkbox = new Button(composite, SWT.CHECK);

        String plainLicenseId = licenseService.decrypt(entry, 
                LicenseManagementEntry.COLUMN_LICENSEID);

        String checkboxText = getCheckboxText(entry, index, true);
        checkbox.setText(checkboxText);
        licenseEntryMap.put(plainLicenseId, entry);
        checkbox.addSelectionListener(getCheckboxSelectionListener());
        checkboxes.add(checkbox);
        
        validateCheckboxStatus();
    }

    private String getCheckboxText(LicenseManagementEntry entry,
            int index, boolean update) {
        
        String plainLicenseId = licenseService.decrypt(entry, 
                LicenseManagementEntry.COLUMN_LICENSEID);
        LocalDate validUntil = licenseService.decrypt(entry,
                LicenseManagementEntry.COLUMN_VALIDUNTIL);
        
        StringBuilder sb = new StringBuilder();
        if(update){
            index += 1;
        }
        sb.append(index).append(". ");
        sb.append(getLicenseLabel(plainLicenseId, update));
        if (validUntil.isBefore(LocalDate.now())){
            sb.append(Messages.LicenseMgmtPage_License_Expired);
        }
        return sb.toString();
    }

    private SelectionListener getCheckboxSelectionListener(){
        return new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (event.getSource() instanceof Button){
                    final Button checkbox = (Button)event.getSource();
                    Display.getDefault().asyncExec(new Runnable() {
                        
                        @Override
                        public void run() {
                            triggerLicenseCheckbox(checkbox);
                        }
                    });
                }
            }


            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
        };
    }
    
    /**
     * @param checkbox
     */
    private void triggerLicenseCheckbox(Button checkbox) {
        String checkboxText = checkbox.getText();
        String licenseIdLabel = getLicenseLabelFromCheckboxText(checkboxText);
        String licenseId = getLicenseIdForLabel(licenseIdLabel);
        try{
            if (checkbox.getSelection()){
                getAccount(false).addLicensedContentId(licenseId);
                assignedLicenseIds.add(licenseId);
            } else {
                getAccount(false).removeLicensedContentId(licenseId);
                assignedLicenseIds.remove(licenseId);
            }
            SaveConfiguration<Configuration> saveConfig = new SaveConfiguration<Configuration>(getAccount(false), false);
            ServiceFactory.lookupCommandService().executeCommand(saveConfig);
            refreshCheckboxLabel(checkbox);
        } catch (CommandException e){
            LOG.error("Something went wrong with adding license-assignments", e);
        }
        validateCheckboxStatus();
    }
    
    protected void refreshCheckboxLabel(Button checkbox){
        if (checkbox != null){
            String licenseIdLabel = getLicenseIdForLabel(
                    getLicenseLabelFromCheckboxText(checkbox.getText()));
            LicenseManagementEntry entry = licenseEntryMap.
                    get(licenseIdLabel);
            int index = Integer.parseInt(checkbox.getText().substring(0, 1));
            String updatedText = getCheckboxText(entry, index, false);
            checkbox.setText(updatedText);
        }
    }
    
   
    /**
     * gets the licenseId that belongs to a given label of the wizardpage
     * 
     * @param label
     * @return
     */
    private String getLicenseIdForLabel(String label){
        for (Entry<String, String> entry : licenseIdToLabelMap.entrySet()){
            if (label.equals(entry.getValue())){
                return entry.getKey();
            }
        }
        return label;
    }
    
    /**
     * removes index count and optionally added "expired"-hint from
     * checkbox-label
     * @param checkboxText
     * @return
     */
    private String getLicenseLabelFromCheckboxText(String checkboxText){
        if (checkboxText.endsWith(Messages.LicenseMgmtPage_License_Expired)){
        checkboxText = checkboxText.substring(
                checkboxText.indexOf(". ")+2, 
                checkboxText.lastIndexOf(
                        Messages.LicenseMgmtPage_License_Expired));
        } else {
            checkboxText = checkboxText.substring(
                    checkboxText.indexOf(". ")+2);
        }
        return checkboxText;
    }
    
    /**
     * ensures that checkboxes are only selectable if the license
     * they are representing still has free slots to assign
     * if no slots available and checkbox unchecked, checkbox gets disabled
     * if no slots available and checkbox checked, checkbox gets enabled
     * if license is expired and checkbox unchecked, checkbox gets disabled
     * if license if expired and checkbox checked, checkbox gets enabled and 
     *  disabled when unchecked
     */
    private void validateCheckboxStatus(){
        try {
            for (Button checkbox : checkboxes){
                String checkboxText = checkbox.getText();

                String licenseIdLabel = getLicenseIdForLabel(
                        getLicenseLabelFromCheckboxText(checkboxText));
                
                LicenseManagementEntry entry = licenseEntryMap.
                        get(licenseIdLabel);
                boolean hasFreeSlots = 
                        licenseService.
                        hasLicenseIdAssignableSlots(entry.getLicenseID());
                if (hasFreeSlots){
                    checkbox.setEnabled(true);    
                } else {
                    if (!checkbox.getSelection()){
                        checkbox.setEnabled(false);
                    }
                }
                if (checkbox.getText().endsWith(Messages.
                        LicenseMgmtPage_License_Expired)){
                    checkbox.setEnabled(false);
                }
                if (checkbox.getSelection()){
                    checkbox.setEnabled(true);
                }
            } 
        } catch (LicenseManagementException e){
            String msg = "Error validating license-Data";
            ExceptionUtil.log(e, msg);
            LOG.error(msg, e);
        }
    }

    /**
     * initializes the checkboxes of the wizardpage with the data 
     * given by the wizard
     */
    @Override
    protected void initData() throws Exception {
        for (Button checkbox : checkboxes){
            String label = getLicenseIdForLabel(
                    getLicenseLabelFromCheckboxText(checkbox.getText()));
            
            if (assignedLicenseIds.contains(
                    label)){
                checkbox.setSelection(true);
                checkbox.setEnabled(true);
            } 
        }
    }
    
    @Override
    public boolean isPageComplete() {
        return true;
    }

    /**
     * @return the assignedLicenseIds
     */
    public Set<String> getAssignedLicenseIds() {
        assignedLicenseIds = new HashSet<>();
        for (Button checkbox : checkboxes){
            if (checkbox.getSelection()){
                String checkboxText = checkbox.getText();
                String licenseLabel = getLicenseLabelFromCheckboxText(
                        checkboxText);
                String licenseId = getLicenseIdForLabel(licenseLabel);
                assignedLicenseIds.add(licenseId);
            }
        }
        return assignedLicenseIds;
    }

    /**
     * @param assignedLicenseIds the assignedLicenseIds to set
     */
    public void setAssignedLicenseIds(Set<String> assignedLicenseIds) {
        this.assignedLicenseIds = assignedLicenseIds;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }
    
    private String getLicenseLabel(String licenseId, boolean update){
        if (update && licenseIdToLabelMap.containsKey(licenseId)){
            return licenseIdToLabelMap.get(licenseId);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getLicenseLabelString(licenseId));
        String label = sb.toString();
        licenseIdToLabelMap.put(licenseId, label);
        return label;
    }

    /**
     * builds the string that labels a given licenseId in the client
     * @param licenseId
     * @return
     */
    public static String getLicenseLabelString(String licenseId){
        StringBuilder sb = new StringBuilder();
        final String singleSpace = " ";
        final String licenseIdDelimiter = "###";
        final String openBracket = "(";
        final String closingBracket = ")";
        final String dash = "-";
        final String slash = "/";
        StringTokenizer tokenizer = new StringTokenizer(licenseId,
                licenseIdDelimiter);
        if (tokenizer.countTokens() != 3){
            return licenseId;
        };
        String contentId = tokenizer.nextToken();
        String validUntil = tokenizer.nextToken();
        String validUsers = tokenizer.nextToken();

        String assignedUsers = String.valueOf(ServiceFactory.
                lookupLicenseManagementService().
                getLicenseIdAllocationCount(licenseId));

        sb.append(contentId).append(singleSpace);
        sb.append(dash).append(singleSpace).
        append(validUntil).append(singleSpace).append(dash);
        sb.append(singleSpace).append(openBracket).append(assignedUsers);
        sb.append(slash).append(validUsers).append(closingBracket);
        return sb.toString();
    }

    /**
     * @return the sendEmail
     */
    public boolean isSendEmail() {
        return sendEmail;
    }

    /**
     * @param sendEmail the sendEmail to set
     */
    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    /**
     * @return the account
     */
    public Configuration getAccount(boolean reload) {
        if(reload){
            account = 
                    ServiceFactory.lookupAccountService().
                    getAccountById(account.getDbId());
        }
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(Configuration account) {
        this.account = account;
    }

}
