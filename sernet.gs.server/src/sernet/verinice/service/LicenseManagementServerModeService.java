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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.interfaces.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementServerModeService 
    implements ILicenseManagementService {
    

    
    LicenseManagementEntryDao licenseManagementDao;

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getValidUsers(java.lang.String)
     */
    @Override
    public int getValidUsersForContentId(String contentId) {
        String hql = "select validUsers from LicenseManagementEntry "
                + "where contentIdentifier = ?";
        Object[] params = new Object[]{contentId};
        List idList = licenseManagementDao.findByQuery(hql, params);
        int sum = 0;
        for(Object o : idList){
            if(o instanceof String){
                int validUsers = Integer.parseInt(((String)o));
                sum += validUsers;
            }
        }
        return sum;
        
                
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getValidUntil(java.lang.String)
     */
    @Override
    public Date getMaxValidUntil(String contentId) {
        long longestValidDate = 0L;
        String hql = "select validUntil from LicenseManagementEntry "
                + "where contentIdentifier = ?";
        Object[] params = new Object[]{contentId};
        List dateList = licenseManagementDao.findByQuery(hql, params);
        for(Object o : dateList){
            if(o instanceof String){
                long current = Long.parseLong((String)o);
                if(current > longestValidDate){
                    longestValidDate = current;
                }
            }
        }
        return new Date(longestValidDate);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getLicenseId(int)
     */
    @Override
    public String getLicenseId(int dbId) {
        String hql = "select licenseId from LicenseManagementEntry "
                + "where dbId = ?";
        Object[] params = new Object[]{dbId};
        List idList = licenseManagementDao.findByQuery(hql, params);
        return (String)idList.get(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getCryptoService()
     */
    @Override
    public Object getCryptoService() {
        // TODO Auto-generated method stub
        // implement on VN-1538
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getCurrentUser()
     */
    @Override
    public String getCurrentUser() {
        // TODO Auto-generated method stub
        // shouldnt be necessary, use configurationservice before calling methods of this one
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#isCurrentUserValidForLicense(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isCurrentUserValidForLicense(String user, String licenseId) {
        // TODO Auto-generated method stub
        // insert configurationservice via spring and ask for configuration for user
        // 
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#isCurrentUserAuthorizedForLicenseUsage(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isCurrentUserAuthorizedForLicenseUsage(String user, String licenseid) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#isUserAssignedLicenseStillValid(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isUserAssignedLicenseStillValid(String user, String licenseId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#checkAssignedUsersForLicenseId(java.lang.String)
     */
    @Override
    public boolean checkAssignedUsersForLicenseId(String licenseId) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#removeAllUsersForLicense(java.lang.String)
     */
    @Override
    public void removeAllUsersForLicense(String licenseId) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#grantUserToLicense(java.lang.String, java.lang.String)
     */
    @Override
    public boolean grantUserToLicense(Configuration configuration, String licenseId) {
        // TODO: after implementation of VN-1538 add decrypt here
        configuration.addLicensedContentId(licenseId);
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getAllLicenseIds()
     */
    @Override
    public Set<String> getAllLicenseIds() {
        Set<String> allIds = new HashSet<String>();
        String hql = "select licenseID from LicenseManagementEntry";
                List allEntries = licenseManagementDao.
                        findByQuery(hql, new Object[]{});
                allIds.addAll(allEntries);
        return allIds;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getLicenseEntriesForLicenseId(java.lang.String)
     */
    @Override
    public Set<LicenseManagementEntry> getLicenseEntriesForLicenseId(String licenseId) {
        String hql = "from LicenseManagementEntry entry where "
                + "entry.licenseID = :licenseId";
        String[] names = new String[]{"licenseId"};
        Object[] params = new Object[]{licenseId};
        Set<LicenseManagementEntry> uniqueEntryCollection = new HashSet<>();
        uniqueEntryCollection.addAll(
                licenseManagementDao.findByQuery(hql, names, params));
        return uniqueEntryCollection;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getPublicInformationForLicenseIdEntry(sernet.verinice.model.licensemanagement.LicenseManagementEntry)
     */
    @Override
    public Map<String, String> getPublicInformationForLicenseIdEntry(LicenseManagementEntry licenseEntry) {
        Map<String, String> map = new HashMap<String, String>();
        // TODO: use decryption here, when VN-1538 is done 
        // like 
        // map.put(LicenseManagementEntry.COLUMN_CONTENTID, 
        // getCryptoService().decrypt(licenseEntry.getContentIdentifier(), licenseEntry.getUserPassword);
        map.put(LicenseManagementEntry.COLUMN_CONTENTID, licenseEntry.getContentIdentifier());
        map.put(LicenseManagementEntry.COLUMN_LICENSEID, licenseEntry.getLicenseID());
        map.put(LicenseManagementEntry.COLUMN_VALIDUNTIL, licenseEntry.getValidUntil());
        map.put(LicenseManagementEntry.COLUMN_VALIDUSERS, licenseEntry.getValidUsers());
        return map;
    }

    /**
     * @return the licenseManagementDao
     */
    public LicenseManagementEntryDao getLicenseManagementDao() {
        return licenseManagementDao;
    }

    /**
     * @param licenseManagementDao the licenseManagementDao to set
     */
    public void setLicenseManagementDao(LicenseManagementEntryDao licenseManagementDao) {
        this.licenseManagementDao = licenseManagementDao;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getAllContentIds()
     */
    @Override
    public Set<String> getAllContentIds() {
        Set<String> allIds = new HashSet<String>();
        String hql = "select contentIdentifier from LicenseManagementEntry";
                List allEntries = licenseManagementDao.
                        findByQuery(hql, new Object[]{});
                allIds.addAll(allEntries);
        return allIds;
    }
    
}
