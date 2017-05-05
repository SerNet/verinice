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
package sernet.verinice.model.licensemanagement;

import java.io.Serializable;
import org.threeten.bp.LocalDate;


/**
 * This class wraps the information needed for showing information
 * (additionally) in the objectbrowser if the license to use
 * is invalid soon, or if the user has no license assigend
 * to decrypt the restricted content
 * 
 * Attention: data is stored decrypted here
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseMessageInfos implements Serializable {

    private static final long serialVersionUID = 201702281015L;
    
    private LocalDate validUntil;
    private String licenseId;
    private String contentId;
    private String accountViewColumnHeader;
    private String accountWizardLabel;
    private boolean isInvalidSoon;
    private boolean isNoLicenseAvailable;
    private int validUsers;
    private int assignedUsers;
    
    /**
     * @param validUntil
     * @param licenseId
     * @param contentId
     * @param isInvalidSoon
     * @param isNoLicenseAvailable
     */
    public LicenseMessageInfos(LocalDate validUntil, 
            String licenseId, 
            String contentId, 
            boolean isInvalidSoon, 
            boolean isNoLicenseAvailable,
            int validUsers,
            int assigendUsers) {
        super();
        this.validUntil = validUntil;
        this.licenseId = licenseId;
        this.contentId = contentId;
        this.isInvalidSoon = isInvalidSoon;
        this.isNoLicenseAvailable = isNoLicenseAvailable;
        this.validUsers = validUsers;
        this.assignedUsers = assigendUsers;
    }
    
    public LicenseMessageInfos(){
        // default constructor
    }
    
    /**
     * @return the validUntil
     */
    public LocalDate getValidUntil() {
        return validUntil;
    }
    /**
     * @param validUntil the validUntil to set
     */
    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }
    /**
     * @return the licenseId
     */
    public String getLicenseId() {
        return licenseId;
    }
    /**
     * @param licenseId the licenseId to set
     */
    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }
    /**
     * @return the contentId
     */
    public String getContentId() {
        return contentId;
    }
    /**
     * @param contentId the contentId to set
     */
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    /**
     * @return the isInvalidSoon
     */
    public boolean isInvalidSoon() {
        return isInvalidSoon;
    }
    /**
     * @param isInvalidSoon the isInvalidSoon to set
     */
    public void setInvalidSoon(boolean isInvalidSoon) {
        this.isInvalidSoon = isInvalidSoon;
    }
    /**
     * @return the isNoLicenseAvailable
     */
    public boolean isNoLicenseAvailable() {
        return isNoLicenseAvailable;
    }
    /**
     * @param isNoLicenseAvailable the isNoLicenseAvailable to set
     */
    public void setNoLicenseAvailable(boolean isNoLicenseAvailable) {
        this.isNoLicenseAvailable = isNoLicenseAvailable;
    }

    /**
     * @return the validUsers
     */
    public int getValidUsers() {
        return validUsers;
    }

    /**
     * @param validUsers the validUsers to set
     */
    public void setValidUsers(int validUsers) {
        this.validUsers = validUsers;
    }

    /**
     * @return the assignedUsers
     */
    public int getAssignedUsers() {
        return assignedUsers;
    }

    /**
     * @param assignedUsers the assignedUsers to set
     */
    public void setAssignedUsers(int assignedUsers) {
        this.assignedUsers = assignedUsers;
    }

    /**
     * @return the accountViewColumnHeader
     */
    public String getAccountViewColumnHeader() {
        return accountViewColumnHeader;
    }

    /**
     * @param accountViewColumnHeader the accountViewColumnHeader to set
     */
    public void setAccountViewColumnHeader(String accountViewColumnHeader) {
        this.accountViewColumnHeader = accountViewColumnHeader;
    }

    /**
     * @return the accountWizardLabel
     */
    public String getAccountWizardLabel() {
        return accountWizardLabel;
    }

    /**
     * @param accountWizardLabel the accountWizardLabel to set
     */
    public void setAccountWizardLabel(String accountWizardLabel) {
        this.accountWizardLabel = accountWizardLabel;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "LicenseMessageInfos [validUntil=" + validUntil + ", licenseId="
                + licenseId + ", contentId=" + contentId + ", "
                + "accountViewColumnHeader=" + accountViewColumnHeader + ","
                + " accountWizardLabel=" + accountWizardLabel + ","
                + " isInvalidSoon=" + isInvalidSoon + ", isNoLicenseAvailable="
                + isNoLicenseAvailable + ", validUsers=" + validUsers 
                + ", assignedUsers=" + assignedUsers + "]";
    }
    
    
}
