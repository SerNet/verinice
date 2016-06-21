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
package sernet.verinice.service;

import java.util.List;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.interfaces.ILicenseManagementService;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementStandaloneModeService extends LicenseManagementServerModeService
    implements ILicenseManagementService {
    
    LicenseManagementEntryDao licenseManagementDao;

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getValidUsers(java.lang.String)
     */
    @Override
    public int getValidUsersForContentId(String contentId) {
        // in standalone mode, there is always only 1 user
        return 1;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#isCurrentUserValidForLicense(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isCurrentUserValidForLicense(String user, String licenseId) {
        // user does not bother in tier2, so check only valid interval for license
        return isUserAssignedLicenseStillValid(user, licenseId) &&
                checkAssignedUsersForLicenseId(licenseId);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#isUserAssignedLicenseStillValid(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isUserAssignedLicenseStillValid(String user, String licenseId) {
        String hql = "select validUntil from LicenseManagementEntry " 
                + "where licenseID = ?";
        Object[] params = new Object[]{licenseId};
        List validUntilList = licenseManagementDao.findByQuery(hql, params);
        if(validUntilList != null && validUntilList.size() == 1){
            long validUntil = Long.parseLong((String)validUntilList.get(0));
            long currentTime = System.currentTimeMillis();
            return validUntil > currentTime;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#checkAssignedUsersForLicenseId(java.lang.String)
     */
    @Override
    public boolean checkAssignedUsersForLicenseId(String licenseId) {
        String hql = "select validUsers from LicenseManagementEntry " 
                + "where licenseID = ?";
        Object[] params = new Object[]{licenseId};
        List validUserList = licenseManagementDao.findByQuery(hql, params);
        if(validUserList != null && validUserList.size() == 1){
            return Integer.valueOf((String)validUserList.get(0)) > 0;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#removeAllUsersForLicense(java.lang.String)
     */
    @Override
    public void removeAllUsersForLicense(String licenseId) {
        // DO NOTHING
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#grantUserToLicense(java.lang.String, java.lang.String)
     */
    @Override
    public void grantUserToLicense(String username, String contentId) {
        // should not be used in tier2, so always return true
        // since the tier2-user is always allowed to use a license, if its existant
    }

}
