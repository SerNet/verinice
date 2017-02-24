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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
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
        setTitle(Messages.LicenseMgmtPage_Title);
        setMessage(Messages.LicenseMgmtPage_Text);

        try{
            for(LicenseManagementEntry entry : licenseService.getExistingLicenses()){
                createLicenseIdCheckbox(composite, entry);
            }
        } catch (LicenseManagementException e){
            String msg = "Error getting vnl-License-Data";
            ExceptionUtil.log(e, msg);
            LOG.error(msg, e);

        }

    }

    /**
     * @param composite
     * @param entry
     */
    private void createLicenseIdCheckbox(Composite composite, LicenseManagementEntry entry) {
        Button checkbox = new Button(composite, SWT.CHECK);
        String plainLicenseId = (String)licenseService.decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
        checkbox.setText(getLicenseLabel(plainLicenseId));
        licenseEntryMap.put(plainLicenseId, entry);
        checkbox.addSelectionListener(getCheckboxSelectionListener());
        checkboxes.add(checkbox);
        validateCheckboxStatus();
    }
    
    private SelectionListener getCheckboxSelectionListener(){
        return new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                try{
                if (event.getSource() instanceof Button){
                    Button checkbox = (Button)event.getSource();
                    if (checkbox.getSelection()){
                                licenseService.addLicenseIdAuthorisation(user, getLicenseIdForLabel(checkbox.getText()));
                    } else {
                        licenseService.removeLicenseIdUserAssignment(user, getLicenseIdForLabel(checkbox.getText()), false);
                    }
                }
                validateCheckboxStatus();
                } catch (CommandException e){
                    String msg = Messages.LicenseMgmtPage_Error_licenseNotAssigneable;
                    ExceptionUtil.log(e, msg);
                    LOG.error(msg, e);
                    
                } catch (LicenseManagementException e){
                    String msg = "Error adding or removing license data to user";
                    ExceptionUtil.log(e, msg);
                    LOG.error(msg, e);                    
                }
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
    
    private void validateCheckboxStatus(){
        try{
        for(Button checkbox : checkboxes){
            boolean slotsAvailable = licenseService.hasLicenseIdAssignableSlots(
                    licenseEntryMap.get(getLicenseIdForLabel(checkbox.getText())).getLicenseID());
            if(slotsAvailable){
                checkbox.setEnabled(true);    
            } else {
                if(!checkbox.getSelection()){
                    checkbox.setEnabled(false);
                }
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
            if(assignedLicenseIds.contains(getLicenseIdForLabel(checkbox.getText()))){
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
                assignedLicenseIds.add(getLicenseIdForLabel(checkbox.getText()));
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
        final String singleSpace = " ";
        final String licenseIdDelimiter = "###";
        final String openBracket = "(";
        final String closingBracket = ")";
        final String dash = "-";
        final String slash = "/";
        StringTokenizer tokenizer = new StringTokenizer(licenseId, licenseIdDelimiter);
        StringBuilder sb = new StringBuilder();
        if(tokenizer.countTokens() != 3){
            return licenseId;
        };
        String contentId = tokenizer.nextToken();
        String validUntil = tokenizer.nextToken();
        String validUsers = tokenizer.nextToken();
        
        String assignedUsers = String.valueOf(licenseService.
                getLicenseIdAllocationCount(licenseId));
        
        sb.append(contentId).append(singleSpace);
        sb.append(dash).append(singleSpace).
            append(validUntil).append(singleSpace).append(dash);
        sb.append(singleSpace).append(openBracket).append(assignedUsers);
        sb.append(slash).append(validUsers).append(closingBracket);
        licenseIdToLabelMap.put(licenseId, sb.toString());
        return sb.toString();
    }

}
