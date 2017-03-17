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

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
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
    
    
    public LicenseMgmtPage() {
        super(PAGE_NAME);
        this.licenseService = ServiceFactory.lookupLicenseManagementService();
        this.checkboxes = new HashSet<>();
        this.licenseEntryMap = new HashMap<>();
        this.licenseIdToLabelMap = new HashMap<>();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.account.BaseWizardPage#initGui(org.eclipse.swt.widgets.Composite)
     */
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

        try{
            List<LicenseManagementEntry> licenseList = 
                    getSortedExistingLicenses();
            
            for(int index = 0; index < licenseList.size(); index++){
                createLicenseIdCheckbox(composite, licenseList.get(index), 
                        index);
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
                    if(e.getSource() instanceof Button){
                        Button button = (Button)e.getSource();
                        setSendEmail(button.getSelection());
                    }
                }
                
                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    widgetSelected(e);
                }
            });
        } catch (LicenseManagementException e){
            String msg = "Error getting vnl-License-Data";
            ExceptionUtil.log(e, msg);
            LOG.error(msg, e);

        }

    }

    /**
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
                String label1 = getLicenseLabel(licenseId1);
                String label2 = getLicenseLabel(licenseId2);
                return ncs.compare(label1, label2);
            }
        });
        return licenseList;
    }

    /**
     * @param composite
     * @param entry
     */
    private void createLicenseIdCheckbox(Composite composite, 
            LicenseManagementEntry entry, int index) {
        Button checkbox = new Button(composite, SWT.CHECK);

        String plainLicenseId = licenseService.decrypt(entry, 
                LicenseManagementEntry.COLUMN_LICENSEID);
        LocalDate validUntil = licenseService.decrypt(entry,
                LicenseManagementEntry.COLUMN_VALIDUNTIL);

        StringBuilder sb = new StringBuilder();
        sb.append(index+1).append(". ");
        sb.append(getLicenseLabel(plainLicenseId));
        if(validUntil.isBefore(LocalDate.now())){
            sb.append(Messages.LicenseMgmtPage_License_Expired);
        }
        checkbox.setText(sb.toString());
        licenseEntryMap.put(plainLicenseId, entry);
        checkbox.addSelectionListener(getCheckboxSelectionListener());
        checkboxes.add(checkbox);
        
        validateCheckboxStatus();
    }

    private SelectionListener getCheckboxSelectionListener(){
        return new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (event.getSource() instanceof Button){
                    Button checkbox = (Button)event.getSource();
                    String checkboxText = checkbox.getText();
                    String licenseIdLabel = getLicenseLabelFromCheckboxText(checkboxText);
                    String licenseId = getLicenseIdForLabel(licenseIdLabel);
                    if (checkbox.getSelection()){
                        assignedLicenseIds.add(licenseId);
                    } else {
                        assignedLicenseIds.remove(licenseId);
                    }
                }
                validateCheckboxStatus();

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
        };
    }
    
    private String getLicenseIdForLabel(String label){
        for(Entry<String, String> entry : licenseIdToLabelMap.entrySet()){
            if(label.equals(entry.getValue())){
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
    
    private void validateCheckboxStatus(){
        try{
            for(Button checkbox : checkboxes){
                String checkboxText = checkbox.getText();

                String licenseIdLabel = getLicenseIdForLabel(
                        getLicenseLabelFromCheckboxText(checkboxText));
                
                LicenseManagementEntry entry = licenseEntryMap.
                        get(licenseIdLabel);
                boolean hasFreeSlots = 
                        licenseService.
                        hasLicenseIdAssignableSlots(entry.getLicenseID());
                if(hasFreeSlots){
                    checkbox.setEnabled(true);    
                } else {
                    if(!checkbox.getSelection()){
                        checkbox.setEnabled(false);
                    }
                }
                if(checkbox.getText().endsWith(Messages.LicenseMgmtPage_License_Expired)){
                    checkbox.setEnabled(false);
                }
                if(checkbox.getSelection()){
                    checkbox.setEnabled(true);
                }
            } 
        } catch (LicenseManagementException e){
            String msg = "Error validating license-Data";
            ExceptionUtil.log(e, msg);
            LOG.error(msg, e);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.account.BaseWizardPage#initData()
     */
    @Override
    protected void initData() throws Exception {
        for(Button checkbox : checkboxes){
            String label = getLicenseIdForLabel(
                    getLicenseLabelFromCheckboxText(checkbox.getText()));
            
            if(assignedLicenseIds.contains(
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
        for(Button checkbox : checkboxes){
            if(checkbox.getSelection()){
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
    
    private String getLicenseLabel(String licenseId){
        if(licenseIdToLabelMap.containsKey(licenseId)){
            return licenseIdToLabelMap.get(licenseId);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getLicenseLabelString(licenseId));
        String label = sb.toString();
        licenseIdToLabelMap.put(licenseId, label);
        return label;
    }
    
    public static String getLicenseLabelString(String licenseId){
        final String singleSpace = " ";
        final String licenseIdDelimiter = "###";
        final String openBracket = "(";
        final String closingBracket = ")";
        final String dash = "-";
        final String slash = "/";
        StringTokenizer tokenizer = new StringTokenizer(licenseId,
                licenseIdDelimiter);
        StringBuilder sb = new StringBuilder();
        if(tokenizer.countTokens() != 3){
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
        LOG.debug("LicenseLabel for id:\t" + licenseId + "\t=" + sb.toString());
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

}
