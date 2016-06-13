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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.hibernate.TreeElementDao;
import sernet.verinice.interfaces.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementServerModeService 
    implements ILicenseManagementService {
    
    // injected by spring
    LicenseManagementEntryDao licenseManagementDao;
    TreeElementDao<Configuration, Serializable> configurationDao;

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
    public boolean isCurrentUserValidForLicense(String username, String licenseId) {
        Configuration configuration = getConfigurationByUsername(username);
        return configuration.getLicensedContentIds().contains(licenseId);
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
        Date validUntil = null;
        String hql = "select validUntil from LicenseManagementEntry " +
                "where licenseId = ?";
        Object[] params = new Object[]{licenseId};
        List hqlResult = licenseManagementDao.findByQuery(hql, params);
        if(hqlResult.size() != 1){
            return false;
        } else {
            return  Long.parseLong((String)hqlResult.get(0) ) > System.currentTimeMillis(); 
        }
    }

    /**
     * checks if the amount of authorised users fpr a given licenseId is 
     * below the amount allowed at basis of db entries (licenses)
     * @param licenseId
     * @return
     */
    @Override
    public boolean checkAssignedUsersForLicenseId(String licenseId) {
        int validUsers = 0;
        int assignedUsers = 0;
        String hql = "select validUsers from LicenseManagementEntry " +
                        "where licenseId = ?";
        Object[] params = new Object[]{licenseId};
        List hqlResult = licenseManagementDao.findByQuery(hql, params);
        if(hqlResult.size() != 1){
            return false;
        } else {
            validUsers = Integer.parseInt((String)hqlResult.get(0));
            assignedUsers = 0;
            for(Configuration configuration : configurationDao.findAll()){
                if(configuration.getLicensedContentIds().contains(licenseId)){
                    assignedUsers++;
                }
            }
        }
        
        return assignedUsers < validUsers; 
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#removeAllUsersForLicense(java.lang.String)
     */
    @Override
    public void removeAllUsersForLicense(String licenseId) {
        for(Configuration configuration : configurationDao.findAll()){
            if(configuration.getLicensedContentIds().contains(licenseId)){
                configuration.removeLicensedContentId(licenseId);
            }
        configurationDao.saveOrUpdate(configuration);
        }

    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#grantUserToLicense(java.lang.String, java.lang.String)
     */
    @Override
    public void grantUserToLicense(String user, String licenseId) {
        // TODO: after implementation of VN-1538 add decrypt here
        addLicenseIdAuthorisation(user, licenseId);
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
    public Set<LicenseManagementEntry> getLicenseEntriesForContentId(String contentId) {
        String hql = "from LicenseManagementEntry entry where "
                + "entry.contentID = :contentId";
        String[] names = new String[]{"contentId"};
        Object[] params = new Object[]{contentId};
        Set<LicenseManagementEntry> uniqueEntryCollection = new HashSet<>();
        uniqueEntryCollection.addAll(
                licenseManagementDao.findByQuery(hql, names, params));
        return uniqueEntryCollection;
    }
    
    private Set<String> getLicenseIdsForContentId(String contentId){
        String hql = "select licenseId from LicenseManagementEntry entry where "
                + "entry.contentID = :contentId";
        String[] names = new String[]{"contentId"};
        Object[] params = new Object[]{contentId};
        Set<String> uniqueIds = new HashSet<>();
        List hqlResult = licenseManagementDao.findByQuery(hql, names, params);
        for(Object o : hqlResult){
            if(o instanceof String){
                uniqueIds.add((String)o);
            }
        }
                
        return uniqueIds;        
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
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IConfigurationService#getLicensedContentIdAllocationCount(java.lang.String)
     */
    @Override
    public int getContentIdAllocationCount(String licensedContentId) {
        int count = 0;
        for (Configuration configuration : configurationDao.findAll()){
            if(configuration.getLicensedContentIds().contains(licensedContentId)){
                count++;
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#addLicenseIdAuthorisation(java.lang.String, java.lang.String)
     */
    @Override
    public void addLicenseIdAuthorisation(String user, String licenseId) {
        Configuration configuration = getConfigurationByUsername(user);
        configuration.addLicensedContentId(licenseId);
        configurationDao.saveOrUpdate(configuration);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#removeAllLicenseIdAssignments(java.lang.String)
     */
    @Override
    public void removeAllLicenseIdAssignments(String licenseId) {
        for(Configuration configuration : configurationDao.findAll()){
            if(configuration.getLicensedContentIds().contains(licenseId)){
                configuration.removeLicensedContentId(licenseId);
            }
            configurationDao.saveOrUpdate(configuration);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#removeAllContentIdAssignments(java.lang.String)
     */
    @Override
    public void removeAllContentIdAssignments(String contentId) {
        Set<String> licenseIds = getLicenseIdsForContentId(contentId);
        for(Configuration configuration : configurationDao.findAll()){
            for(String licenseId : licenseIds){
                if(configuration.getLicensedContentIds().contains(licenseId)){
                    configuration.removeLicensedContentId(licenseId);
                }
            }
            configurationDao.saveOrUpdate(configuration);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#removeContentIdUserAssignment(java.lang.String, java.lang.String)
     */
    @Override
    public void removeContentIdUserAssignment(String username, String contentId) {
        Configuration configuration = getConfigurationByUsername(username);
        for(LicenseManagementEntry entry : getLicenseEntriesForContentId(contentId)){
            configuration.removeLicensedContentId(entry.getLicenseID());
        }
        configurationDao.saveOrUpdate(configuration);
    }
    
    private Configuration getConfigurationByUsername(String username){
        String hql = "from Configuration conf " + 
                "inner join conf.entity as entity " + 
                "inner join entity.typedPropertyLists as propertyList " +
                "inner join propertyList.properties as props " +
                "and props.propertyType = :userNameType" +
                "and props.propertyValue = :userNameValue";
        
        String escaped = username.replace("\\", "\\\\");
        String[] paramNames = new String[]{"userNameType", "userNameValue"};
        Object[] params = new Object[]{Configuration.PROP_USERNAME, escaped};
        List<Configuration> hqlResult = 
                getConfigurationDao().findByQuery(hql, paramNames, params);
        if(hqlResult.size() != 1){
            return null;
        } else {
            return hqlResult.get(0);
        }
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ILicenseManagementService#getAuthorisedContentIdsByUser(java.lang.String)
     */
    @Override
    public Set<String> getAuthorisedContentIdsByUser(String username) {
        // select all ids that are authorised for the user
        String hql = "select roleprops.propertyValue from Configuration as conf " + //$NON-NLS-1$
                "inner join conf.entity as entity " + //$NON-NLS-1$
                "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                "inner join propertyList.properties as props " + //$NON-NLS-1$
                "inner join conf.entity as entity2 " + //$NON-NLS-1$
                "inner join entity2.typedPropertyLists as propertyList2 " + //$NON-NLS-1$
                "inner join propertyList2.properties as roleprops " + //$NON-NLS-1$
                "where props.propertyType = ? " + //$NON-NLS-1$
                "and props.propertyValue like ? " + //$NON-NLS-1$
                "and roleprops.propertyType = ?"; //$NON-NLS-1$
        String escaped = username.replace("\\", "\\\\");
        Object[] params = new Object[]{Configuration.PROP_USERNAME,escaped,Configuration.PROP_LICENSED_CONTENT_IDS};        
        List hqlResult = getConfigurationDao().findByQuery(hql,params);
        for(Object o : hqlResult){
            o.hashCode();
        }
        
        
        return null;
    }

    /**
     * @return the configurationDao
     */
    public TreeElementDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    /**
     * @param configurationDao the configurationDao to set
     */
    public void setConfigurationDao(TreeElementDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }


    
}
