/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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

/**
 * Class maps content of a single vnl-file, which contains 
 * license-information for one content-catalogue and is purchased
 * via the verinice.SHOP
 * 
 * Since ALL (but hibernate primary key "id", "salt" and "userPassword") attributes of this
 * class are stored encrypted in the database, the datatype of all
 * attributes is String 
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementEntry implements Serializable {

    private static final long serialVersionUID = 2016060910229L;
    
    public static final String COLUMN_CONTENTID = "contentId";
    public static final String COLUMN_LICENSEID = "licenseID";
    public static final String COLUMN_VALIDUSERS = "validUsers";
    public static final String COLUMN_VALIDUNTIL = "validUntil";
    
    // primary key for usage with hibernate
    private int dbId;

    // password users entered on purchase
    private String userPassword;
    
    // salt used to encrypt properties
    private String salt;
    
    // specifies the referencing content (unique for each content)
    private String contentIdentifier;
    
    // specifies this single entry of licenseInformation, must be unique (id)
    // secondary key
    private String licenseID;

    
    // amount of users that are allowed to be authorised by this entry
    private String validUsers;
    
    // timestamp until the license is valid
    private String validUntil;
    
    public LicenseManagementEntry(){
        // hibernate constructor
    }

    /**
     * @return the id
     */
    public int getDbId() {
        return dbId;
    }

    /**
     * @param id the id to set
     */
    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    /**
     * @return the userPassword
     */
    public String getUserPassword() {
        return userPassword;
    }

    /**
     * @param userPassword the userPassword to set
     */
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    /**
     * @return the salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * @param salt the salt to set
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * @return the contentIdentifier
     */
    public String getContentIdentifier() {
        return contentIdentifier;
    }

    /**
     * @param contentIdentifier the contentIdentifier to set
     */
    public void setContentIdentifier(String contentIdentifier) {
        this.contentIdentifier = contentIdentifier;
    }

    /**
     * @return the licenseID
     */
    public String getLicenseID() {
        return licenseID;
    }

    /**
     * @param licenseID the licenseID to set
     */
    public void setLicenseID(String licenseID) {
        this.licenseID = licenseID;
    }

    /**
     * @return the validUsers
     */
    public String getValidUsers() {
        return validUsers;
    }

    /**
     * @param validUsers the validUsers to set
     */
    public void setValidUsers(String validUsers) {
        this.validUsers = validUsers;
    }

    /**
     * @return the validUntil
     */
    public String getValidUntil() {
        return validUntil;
    }

    /**
     * @param validUntil the validUntil to set
     */
    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }
    
    public String getPropertyByType(String propertyType){
        switch (propertyType){
            case COLUMN_CONTENTID: 
                return getContentIdentifier();
            case COLUMN_LICENSEID: 
                return getLicenseID();
            case COLUMN_VALIDUNTIL: 
                return getValidUntil();
            case COLUMN_VALIDUSERS: 
                return getValidUsers();
            default: 
                return "";
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contentIdentifier == null) ? 0 : contentIdentifier.hashCode());
        result = prime * result + dbId;
        result = prime * result + ((licenseID == null) ? 0 : licenseID.hashCode());
        result = prime * result + ((validUntil == null) ? 0 : validUntil.hashCode());
        result = prime * result + ((validUsers == null) ? 0 : validUsers.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LicenseManagementEntry other = (LicenseManagementEntry) obj;
        if (contentIdentifier == null) {
            if (other.contentIdentifier != null)
                return false;
        } else if (!contentIdentifier.equals(other.contentIdentifier))
            return false;
        if (dbId != other.dbId)
            return false;
        if (licenseID == null) {
            if (other.licenseID != null)
                return false;
        } else if (!licenseID.equals(other.licenseID))
            return false;
        if (validUntil == null) {
            if (other.validUntil != null)
                return false;
        } else if (!validUntil.equals(other.validUntil))
            return false;
        if (validUsers == null) {
            if (other.validUsers != null)
                return false;
        } else if (!validUsers.equals(other.validUsers))
            return false;
        return true;
    }
    
    

}
