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
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementServerModeService implements ILicenseManagementService {

    // injected by spring
    LicenseManagementEntryDao licenseManagementDao;
    IBaseDao<Configuration, Serializable> configurationDao;

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ILicenseManagementService#getValidUsers(java.
     * lang.String)
     */
    @Override
    public int getValidUsersForContentId(String contentId) {
        String hql = "select validUsers from LicenseManagementEntry " + "where contentIdentifier = ?";
        Object[] params = new Object[] { contentId };
        List idList = licenseManagementDao.findByQuery(hql, params);
        int sum = 0;
        for (Object o : idList) {
            if (o instanceof String) {
                int validUsers = Integer.parseInt(((String) o));
                sum += validUsers;
            }
        }
        return sum;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ILicenseManagementService#getValidUntil(java.
     * lang.String)
     */
    @Override
    public Date getMaxValidUntil(String contentId) {
        long longestValidDate = 0L;
        String hql = "select validUntil from LicenseManagementEntry " + "where contentIdentifier = ?";
        Object[] params = new Object[] { contentId };
        List dateList = licenseManagementDao.findByQuery(hql, params);
        for (Object o : dateList) {
            if (o instanceof String) {
                long current = Long.parseLong((String) o);
                if (current > longestValidDate) {
                    longestValidDate = current;
                }
            }
        }
        return new Date(longestValidDate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ILicenseManagementService#getLicenseId(int)
     */
    @Override
    public String getLicenseId(int dbId) {
        String hql = "select licenseID from LicenseManagementEntry " + "where dbId = ?";
        Object[] params = new Object[] { dbId };
        List idList = licenseManagementDao.findByQuery(hql, params);
        return (String) idList.get(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ILicenseManagementService#getCryptoService()
     */
    @Override
    public Object getCryptoService() {
        // TODO Auto-generated method stub
        // implement on VN-1538
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ILicenseManagementService#getCurrentUser()
     */
    @Override
    public String getCurrentUser() {
        // TODO Auto-generated method stub
        // shouldnt be necessary, use configurationservice before calling
        // methods of this one
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * isCurrentUserValidForLicense(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isCurrentUserValidForLicense(String username, String licenseId) {
        return getAuthorisedContentIdsByUser(username).contains(licenseId); 
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * isCurrentUserAuthorizedForLicenseUsage(java.lang.String,
     * java.lang.String)
     */
    @Override
    public boolean isCurrentUserAuthorizedForLicenseUsage(String user, String licenseid) {
        return getAuthorisedContentIdsByUser(user).contains(licenseid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * isUserAssignedLicenseStillValid(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isUserAssignedLicenseStillValid(String user, String licenseId) {
        Date validUntil = null;
        String hql = "select validUntil from LicenseManagementEntry " + "where licenseID = ?";
        Object[] params = new Object[] { licenseId };
        List hqlResult = licenseManagementDao.findByQuery(hql, params);
        if (hqlResult.size() != 1) {
            return false;
        } else {
            return Long.parseLong((String) hqlResult.get(0)) > System.currentTimeMillis();
        }
    }

    /**
     * checks if the amount of authorised users for a given licenseId is below
     * the amount allowed at basis of db entries (licenses)
     * 
     * @param licenseId
     * @return
     */
    @Override
    public boolean checkAssignedUsersForLicenseId(String licenseId) {
        int validUsers = 0;
        int assignedUsers = 0;
        String hql = "select validUsers from LicenseManagementEntry " + "where licenseID = ?";
        Object[] params = new Object[] { licenseId };
        List hqlResult = licenseManagementDao.findByQuery(hql, params);
        if (hqlResult.size() != 1) {
            return false;
        } else {
            validUsers = Integer.parseInt((String) hqlResult.get(0));
            assignedUsers = 0;
            for (Configuration configuration : getAllConfigurations()) {
                if (getAuthorisedContentIdsByUser(configuration.getUser()).contains(licenseId)) {
                    assignedUsers++;
                }
            }
        }

        return assignedUsers < validUsers;

    }

    /**
     * removes all user assignments for a given  @param licenseId 
     */
    @Override
    public void removeAllUsersForLicense(String licenseId) {
        for (Configuration configuration : getAllConfigurations()) {
            if (getAuthorisedContentIdsByUser(configuration.getUser()).contains(licenseId)) {
                configuration.removeLicensedContentId(licenseId);
            }
            configurationDao.saveOrUpdate(configuration);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ILicenseManagementService#grantUserToLicense(
     * java.lang.String, java.lang.String)
     */
    @Override
    public void grantUserToLicense(String user, String licenseId) {
        // TODO: after implementation of VN-1538 add decrypt here
        addLicenseIdAuthorisation(user, licenseId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ILicenseManagementService#getAllLicenseIds()
     */
    @Override
    public Set<String> getAllLicenseIds() {
        Set<String> allIds = new HashSet<String>();
        String hql = "select licenseID from LicenseManagementEntry";
        List allEntries = licenseManagementDao.findByQuery(hql, new Object[] {});
        allIds.addAll(allEntries);
        return allIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * getLicenseEntriesForLicenseId(java.lang.String)
     */
    @Override
    public Set<LicenseManagementEntry> getLicenseEntriesForContentId(String contentId) {
        String hql = "from LicenseManagementEntry entry where " + "entry.contentIdentifier = :contentId";
        String[] names = new String[] { "contentId" };
        Object[] params = new Object[] { contentId };
        Set<LicenseManagementEntry> uniqueEntryCollection = new HashSet<>();
        uniqueEntryCollection.addAll(licenseManagementDao.findByQuery(hql, names, params));
        return uniqueEntryCollection;
    }

    @Override
    public Set<String> getLicenseIdsForContentId(String contentId) {
        String hql = "select licenseID from LicenseManagementEntry entry where " + "entry.contentIdentifier = :contentId";
        String[] names = new String[] { "contentId" };
        Object[] params = new Object[] { contentId };
        Set<String> uniqueIds = new HashSet<>();
        List hqlResult = licenseManagementDao.findByQuery(hql, names, params);
        for (Object o : hqlResult) {
            if (o instanceof String) {
                uniqueIds.add((String) o);
            }
        }

        return uniqueIds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * getPublicInformationForLicenseIdEntry(sernet.verinice.model.
     * licensemanagement.LicenseManagementEntry)
     */
    @Override
    public Map<String, String> getPublicInformationForLicenseIdEntry(LicenseManagementEntry licenseEntry) {
     // TODO needs to be unittested
        Map<String, String> map = new HashMap<String, String>();
        // TODO: use decryption here, when VN-1538 is done
        // like
        // map.put(LicenseManagementEntry.COLUMN_CONTENTID,
        // getCryptoService().decrypt(licenseEntry.getContentIdentifier(),
        // licenseEntry.getUserPassword);
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
     * @param licenseManagementDao
     *            the licenseManagementDao to set
     */
    public void setLicenseManagementDao(LicenseManagementEntryDao licenseManagementDao) {
        this.licenseManagementDao = licenseManagementDao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ILicenseManagementService#getAllContentIds()
     */
    @Override
    public Set<String> getAllContentIds() {
        Set<String> allIds = new HashSet<String>();
        String hql = "select contentIdentifier from LicenseManagementEntry";
        List allEntries = licenseManagementDao.findByQuery(hql, new Object[] {});
        allIds.addAll(allEntries);
        return allIds;
    }

    /**
     * returns how many users are currently assigned to use 
     * the license with the @param contentId
     */
    @Override
    public int getContentIdAllocationCount(String contentId) {
        int count = 0;
        Set<String> licenseIds = getLicenseIdsForContentId(contentId);
        Set<Configuration> configurations = getAllConfigurations();
        for (Configuration configuration : configurations) {
            for (String licenseId : licenseIds){
                if (configuration.getLicensedContentIds().contains(licenseId)) {
                    count++;
                }
            }
        }
        return count;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * addLicenseIdAuthorisation(java.lang.String, java.lang.String)
     */
    @Override
    public void addLicenseIdAuthorisation(String user, String licenseId) {
        Configuration configuration = getConfigurationByUsername(user);
        configuration.addLicensedContentId(licenseId);
        configurationDao.merge(configuration);
        configurationDao.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * removeAllLicenseIdAssignments(java.lang.String)
     */
    @Override
    public void removeAllLicenseIdAssignments(String licenseId) {
        for (Configuration configuration : getAllConfigurations()) {
            if (getAuthorisedContentIdsByUser(configuration.getUser()).contains(licenseId)) {
                configuration.removeLicensedContentId(licenseId);
            }
            configurationDao.merge(configuration);
            configurationDao.flush();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * removeAllContentIdAssignments(java.lang.String)
     */
    @Override
    public void removeAllContentIdAssignments(String contentId) {
        Set<String> licenseIds = getLicenseIdsForContentId(contentId);
        for (Configuration configuration : getAllConfigurations()) {
            for (String licenseId : licenseIds) {
                if (getAuthorisedContentIdsByUser(configuration.getUser()).contains(licenseId)) {
                    configuration.removeLicensedContentId(licenseId);
                }
            }
            configurationDao.merge(configuration);
            configurationDao.flush();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * removeContentIdUserAssignment(java.lang.String, java.lang.String)
     */
    @Override
    public void removeContentIdUserAssignment(String username, String contentId) {
        Configuration configuration = getConfigurationByUsername(username);
        for (LicenseManagementEntry entry : getLicenseEntriesForContentId(contentId)) {
            configuration.removeLicensedContentId(entry.getLicenseID());
        }
        configurationDao.saveOrUpdate(configuration);
    }

    private Set<Configuration> getAllConfigurations() {
        Set<Configuration> configurations = new HashSet<>();
        String hql = "from Configuration conf " + "inner join fetch conf.entity as entity " + "inner join fetch entity.typedPropertyLists as propertyList " + "inner join fetch propertyList.properties as props ";

        Object[] params = new Object[] {};
        List hqlResult = getConfigurationDao().findByQuery(hql, params);
        for (Object o : hqlResult) {
            if (o instanceof Configuration) {
                configurations.add((Configuration) o);
            }
        }
        return configurations;
    }

    private Configuration getConfigurationByUsername(String username) {
        for (Configuration c : getAllConfigurations()) {
            if (username.equals(c.getUser())) {
                return c;
            }
        }
        return null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ILicenseManagementService#
     * getAuthorisedContentIdsByUser(java.lang.String)
     */
    @Override
    public Set<String> getAuthorisedContentIdsByUser(String username) {
        return getConfigurationByUsername(username).getLicensedContentIds();
    }

    /**
     * @return the configurationDao
     */
    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    /**
     * @param configurationDao
     *            the configurationDao to set
     */
    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }

}
