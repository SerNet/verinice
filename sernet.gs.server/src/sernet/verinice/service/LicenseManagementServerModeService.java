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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.VNLMapper;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.propertyconverter.PropertyConverter;
import sernet.verinice.service.commands.LoadVNLFiles;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementServerModeService implements ILicenseManagementService {
    
    private Logger log = Logger.getLogger(LicenseManagementServerModeService.class);

    // injected by spring
    private LicenseManagementEntryDao licenseManagementDao;
    private IBaseDao<Configuration, Serializable> configurationDao;
    private IEncryptionService cryptoService;
    private ICommandService commandService;
    
    private Set<LicenseManagementEntry> existingLicenses = null;

    /**
     * @param encryptedContentId - (encrypted) id of content to inspect
     * @return amount of authorised users for a given contentId 
     */
    @Override
    public int getValidUsersForContentId(String encryptedContentId) {
//        String hql = "from LicenseManagementEntry " 
//                    + "where contentIdentifier = ?";
//        // if somethings wrong with the hql-parameter contentId (e.g.
//        // it does not exist in db) the result list will be empty
//        Object[] params = new Object[] { encryptedContentId };
//        List<LicenseManagementEntry> entryList = licenseManagementDao.
//                findByQuery(hql, params);
        int sum = 0;
//        for (LicenseManagementEntry entry : entryList) {
        for (LicenseManagementEntry entry : getExistingLicenses()) {
                int validUsers = decrypt(
                        entry, LicenseManagementEntry.COLUMN_VALIDUSERS);
                sum += validUsers;
        }
        return sum;

    }

    /**
     * 
     * iterates over all {@link LicenseManagementEntry} for a given (encrypted)
     * contentId to return to maximum validUntil-Date
     * 
     * @param encryptedContentId - id of content to inspect
     * @return the maximal date a content is valid to  
     */
    @Override
    public Date getMaxValidUntil(String encryptedContentId) {
        Date longestValidDate = new Date(System.currentTimeMillis());
//        String hql = "from LicenseManagementEntry " 
//                + "where contentIdentifier = ?";
//        Object[] params = new Object[] { encryptedContentId };
//        List<LicenseManagementEntry> entryList 
//            = licenseManagementDao.findByQuery(hql, params);
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            Date current = decrypt(
                    entry,
                    LicenseManagementEntry.COLUMN_VALIDUNTIL);
            if (current.after(longestValidDate)) {
                longestValidDate = current;
            }
        }
        return longestValidDate;
    }

    /**
     * 
     * @param dbId - dbId of a {@link LicenseManagementEntry}
     * @return returns a licenseId for a {@link LicenseManagementEntry} to 
     * a given dbId
     */
    @Deprecated //with storing the licenseinformation in files instead of 
                //db, this is deprecated
    @Override
    public String getLicenseId(int dbId) {
        String hql = "from LicenseManagementEntry " + "where dbId = ?";
        Object[] params = new Object[] { dbId };
        List<LicenseManagementEntry> entryList 
            = licenseManagementDao.findByQuery(hql, params);
        if(entryList.size() == 1){
            LicenseManagementEntry entry = entryList.get(0);
            return decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
        }
        return "";
    }


    /**
     * checks if a given username is authorised for the usage of content
     * defined by a given (encrypted) licenseId 
     * 
     * @param username - login of user to check for
     * @param encryptedLicenseId - licenseId (not contentId!) that should be looked up 
     */
    @Override
    public boolean isCurrentUserValidForLicense(String username, String encryptedLicenseId) {
        return isUserAssignedLicenseStillValid(username, encryptedLicenseId) &&
                checkAssignedUsersForLicenseId(encryptedLicenseId);
    }

    /**
     * checks if a given encrypted licenseId for a given user is invalid by time
     * @param user - username (login) to check
     * @parm licenseId - licenseId (not contentId!) to check
     * 
     * @return status of validation
     */
    @Override
    public boolean isUserAssignedLicenseStillValid(String user, String encryptedLicenseId) {
//        String hql = "from LicenseManagementEntry " + "where licenseID = ?";
//        Object[] params = new Object[] { encryptedLicenseId };
//        List<LicenseManagementEntry> entryList = licenseManagementDao.findByQuery(hql, params);
//        if (entryList.size() != 1) {
//            return false;
//        } else {
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if(entry.getLicenseID().equals(encryptedLicenseId)){
                return ((Date)decrypt(
                        entry, LicenseManagementEntry.COLUMN_VALIDUNTIL))
                        .getTime() > System.currentTimeMillis();
            }
        }
        return false;
    }

    /**
     * checks if the amount of authorised users for a given encrypted 
     * licenseId is below the amount allowed at basis of db entries (licenses)
     * 
     * @param encryptedLicenseId - licenseId (not contentId) to check for
     * @return are there free slots to be assigned for a given licenseId
     */
    @Override
    public boolean checkAssignedUsersForLicenseId(String encryptedLicenseId) {
        int validUsers = 0;
        int assignedUsers = 0;
//        String hql = "from LicenseManagementEntry " + "where licenseID = ?";
//        Object[] params = new Object[] { encryptedLicenseId };
//        List<LicenseManagementEntry> entryList = licenseManagementDao.
//                findByQuery(hql, params);
//        if (entryList.size() != 1) {
//            return false;
//        } else {
        for(LicenseManagementEntry entry : getExistingLicenses()){
            validUsers = decrypt(entry, 
                    LicenseManagementEntry.COLUMN_VALIDUSERS);
            assignedUsers = 0;
            for (Configuration configuration : getAllConfigurations()) {
                if (getAuthorisedContentIdsByUser(configuration.
                        getUser()).contains(encryptedLicenseId)) {
                    assignedUsers++;
                }
            }
        }

        return assignedUsers < validUsers;

    }

    /**
     * removes all user assignments for a given licenseId
     * 
     * @param encryptedLicenseId - id of licenseEntry (not contentId!) that should be cleared 
     */
    @Override
    public void removeAllUsersForLicense(String encryptedLicenseId) {
        for (Configuration configuration : getAllConfigurations()) {
            if (getAuthorisedContentIdsByUser(configuration.getUser())
                    .contains(encryptedLicenseId)) {
                configuration.removeLicensedContentId(encryptedLicenseId);
            }
            configurationDao.saveOrUpdate(configuration);
        }

    }

    /**
     * add given (decrypted) licenseId to userproperties,
     * which allows the user to use the contentId of t
     * he {@link LicenseManagementEntry} referenced by the licenseId
     * 
     * @param user - username (login) of user to authorise for license usage
     * @param decryptedLicenseId - id (not contentId!) of {@link LicenseManagementEntry} 
     */
    @Override
    public void grantUserToLicense(String user, String decryptedLicenseId) {
        addLicenseIdAuthorisation(user, decryptedLicenseId);
    }

    /**
     * @return decrypted licenceIds (not contentIds!) of all 
     * {@link LicenseManagementEntry} available the db
     */
    @Override
    public Set<String> getAllLicenseIds() {
        Set<String> allIds = new HashSet<String>();
//        String hql = "from LicenseManagementEntry";
//        List allEntries = licenseManagementDao.findByQuery(hql, new Object[] {});
//        for(Object o : allEntries){
//            LicenseManagementEntry entry = (LicenseManagementEntry)o;
        for(LicenseManagementEntry entry : getExistingLicenses()){
            String licenseId = decrypt(entry, 
                    LicenseManagementEntry.COLUMN_LICENSEID);
            allIds.add(licenseId);
        }
        return allIds;
    }

    /**
     * @return all instances of {@link LicenseManagementEntry} referencing 
     * the encrypted contentId (not licenseId!) given by parameter.
     * 
     * @param encryptedContentId - the id of the content to search for
     */
    @Override
    public Set<LicenseManagementEntry> getLicenseEntriesForContentId(String encryptedContentId) {
        // unless contentId is crypted with pw and salt, this returns an empty set
//        String hql = "from LicenseManagementEntry entry where "
//                + "entry.contentIdentifier = :contentId";
//        String[] names = new String[] { "contentId" };
//        Object[] params = new Object[] { encryptedContentId };
        
        Set<LicenseManagementEntry> uniqueEntryCollection = new HashSet<>();
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if(entry.getContentIdentifier().equals(encryptedContentId)){
                uniqueEntryCollection.add(entry);
            }
//        uniqueEntryCollection.addAll(licenseManagementDao
//                .findByQuery(hql, names, params));
        }
        return uniqueEntryCollection;
    }

    /**
     * @return all decrypted licenseIds for a given encrypted contentId
     * @param contentID - the contentId to search for
     */
    @Override
    public Set<String> getLicenseIdsForContentId(String encryptedContentId) {
        // unless contentId is crypted with pw and salt, this returns an empty set
//        String hql = "from LicenseManagementEntry entry where"
//                + " entry.contentIdentifier = :contentId";
//        String[] names = new String[] { "contentId" };
//        Object[] params = new Object[] { encryptedContentId };
        Set<String> uniqueIds = new HashSet<>();
//        List hqlResult = licenseManagementDao.findByQuery(hql, names, params);
//        for (Object o : hqlResult) {
//            if (o instanceof LicenseManagementEntry) {
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if(entry.getContentIdentifier().equals(encryptedContentId)){
                    uniqueIds.add((String)decrypt(entry,
                            LicenseManagementEntry.COLUMN_LICENSEID));
            }
        }

        return uniqueIds;
    }

    /**
     * returns (decrypted) values of a given {@link LicenseManagementEntry} that
     * should be displayed in the licenseManagement-UI-Element 
     */
    @Override
    public Map<String, String> getPublicInformationForLicenseIdEntry(LicenseManagementEntry licenseEntry) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(LicenseManagementEntry.COLUMN_CONTENTID, String.valueOf(decrypt(licenseEntry, LicenseManagementEntry.COLUMN_CONTENTID)));
        map.put(LicenseManagementEntry.COLUMN_LICENSEID, String.valueOf(decrypt(licenseEntry, LicenseManagementEntry.COLUMN_LICENSEID)));
        map.put(LicenseManagementEntry.COLUMN_VALIDUNTIL, String.valueOf(decrypt(licenseEntry, LicenseManagementEntry.COLUMN_VALIDUNTIL)));
        map.put(LicenseManagementEntry.COLUMN_VALIDUSERS, String.valueOf(decrypt(licenseEntry, LicenseManagementEntry.COLUMN_VALIDUSERS)));
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

    /**
     * @return all contentIds 
     */
    @Override
    public Set<String> getAllContentIds() {
        Set<String> allIds = new HashSet<String>();
//        String hql = "from LicenseManagementEntry";
//        List<LicenseManagementEntry> allEntries = licenseManagementDao.findByQuery(hql, new Object[] {});
        for(LicenseManagementEntry entry : getExistingLicenses()){
            allIds.add(String.valueOf(decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID)));
            
        }
        return allIds;
    }

    /**
     * returns how many users are currently assigned to use 
     * the license with the @param contentId
     */
    @Override
    public int getContentIdAllocationCount(String encryptedContentId) {
        int count = 0;
        Set<String> licenseIds = getLicenseIdsForContentId(encryptedContentId);
        Set<Configuration> configurations = getAllConfigurations();
        for (Configuration configuration : configurations) {
            for (String licenseId : licenseIds){
                // TODO: decrypt licenseId here, if the property of 
                // configuration is stored in plainText
                if (configuration.getLicensedContentIds().contains(licenseId)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     *  adds licenseId (not contentId!) to an instance of {@link Configuration}
     *  that is referenced by a given username. User will get authorised 
     *  for usage of that licenseId by this.
     *  
     *  Attention: this method does not(!) validate if the license
     *  has any free slots for another user
     *  
     *  @param user - username to authorise
     *  @param licenseId - licenseId (not contentId!) the will 
     *  get authorised for
     *  
     *  TODO: decide if licenseId is stored de- or encrypted at
     *  instanceof {@link Configuration}
     */
    @Override
    public void addLicenseIdAuthorisation(String user, String licenseId) {
        Configuration configuration = getConfigurationByUsername(user);
        configuration.addLicensedContentId(licenseId);
        configurationDao.merge(configuration);
        configurationDao.flush();
    }

    /**
     * removes all user assignments for a given licenseId (not contentId!)
     * 
     * @param licenseId - licenseId (not contentId!) that should be dereferenced
     * by all users
     * 
     * TODO: adjust this to decision of de-/encrypted storage of licenseId
     * in {@link Configuration}
     * 
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

    /**
     * remove all assignments for a given encrypted contentId (not licenseId!)
     * 
     * 
     * @param encryptedContentId - contentId (not licenseId) that should be 
     * dereferenced by all users
     * 
     * TODO: adjust to the decision of de-/encrypted storage of licenseId in
     * {@link Configuration}
     */
    @Override
    public void removeAllContentIdAssignments(String encryptedContentId) {
        Set<String> licenseIds = getLicenseIdsForContentId(encryptedContentId);
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

    /**
     * remove a single user assignment from a given encrypted contentId
     * 
     * 
     * @param username - user that should be forbidden to use content
     * @param encryptedContentId - content that should be dereferenced from username
     * 
     * TODO: adjust to the decision of de-/encrypted storage of licenseId in
     * {@link Configuration}
     * 
     */
    @Override
    public void removeContentIdUserAssignment(String username, String encryptedContentId) {
        Configuration configuration = getConfigurationByUsername(username);
        for (LicenseManagementEntry entry : getLicenseEntriesForContentId(encryptedContentId)) {
            configuration.removeLicensedContentId(entry.getLicenseID());
        }
        configurationDao.saveOrUpdate(configuration);
    }

    /**
     * load all instances of {@link Configuration} via hql
     * including their properties via join to avoid 
     * {@link LazyInitializationException} when iterating them 
     * 
     * @return a set of all instances of {@link Configuration}
     */
    private Set<Configuration> getAllConfigurations() {
        Set<Configuration> configurations = new HashSet<>();
        String hql = "from Configuration conf " + 
                "inner join fetch conf.entity as entity " + 
                "inner join fetch entity.typedPropertyLists as propertyList " + 
                "inner join fetch propertyList.properties as props ";

        Object[] params = new Object[] {};
        List hqlResult = getConfigurationDao().findByQuery(hql, params);
        for (Object o : hqlResult) {
            if (o instanceof Configuration) {
                configurations.add((Configuration) o);
            }
        }
        return configurations;
    }

    /**
     * load a Configuration referenced by a username
     * 
     * @param username - username that identifies a {@link Configuration}
     * @return a {@link Configuration} that is identified by username
     */
    private Configuration getConfigurationByUsername(String username) {
        for (Configuration c : getAllConfigurations()) {
            if (username.equals(c.getUser())) {
                return c;
            }
        }
        return null;

    }

    
    /**
     * get all contentIds (not licenseIds!) that a given user is
     * allowed to use
     * 
     * @param username - username to check ids for
     * @return all ids the user is allowed to see content for
     * 
     * TODO: adjust to the decision of de-/encrypted storage of licenseId in
     * {@link Configuration}
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

    /**
     * @param cryptoService the cryptoService to set
     */
    public void setCryptoService(IEncryptionService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Override
    public IEncryptionService getCryptoService() {
        return cryptoService;
    }
    
    @Override
    public <T extends Object> T decrypt(LicenseManagementEntry entry, String propertyType){
        T returnValue = null;
        String plainText = "";
        switch(propertyType){
        case LicenseManagementEntry.COLUMN_CONTENTID:
            plainText = cryptoService.decrypt(entry.getContentIdentifier(), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_LICENSEID:
            plainText = cryptoService.decrypt(entry.getLicenseID(), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_VALIDUNTIL:
            plainText = cryptoService.decrypt(entry.getValidUntil(), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_VALIDUSERS:
            plainText = cryptoService.decrypt(entry.getValidUsers(), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return convertPropery(plainText, propertyType);
        default:
            plainText = cryptoService.decrypt(entry.getPropertyByType(propertyType), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return (T)entry.getPropertyByType(plainText);
        }
    }
    
    private <T extends Object> T convertPropery(Object property, String propertyType){
        T returnValue;
        PropertyConverter converter = new PropertyConverter();
        switch(propertyType){
        case LicenseManagementEntry.COLUMN_CONTENTID:
            returnValue = (T)converter.convertToString(property);
            break;
        case LicenseManagementEntry.COLUMN_LICENSEID:
            returnValue = (T)converter.convertToString(property);
            break;
        case LicenseManagementEntry.COLUMN_VALIDUNTIL:
            returnValue = (T)converter.convertToDate(property);
            break;
        case LicenseManagementEntry.COLUMN_VALIDUSERS:
            returnValue = (T)converter.convertToInteger(property);              
            break;
        default:
            // if none of the defined rules apply, just return a string
            returnValue = (T)String.valueOf(property);
        }
        return returnValue;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.licensemanagement.ILicenseManagementService#getAllLicenseEntries()
     */
    @Override
    public Set<LicenseManagementEntry> readVNLFiles(){
        if(existingLicenses != null){
            existingLicenses.clear();
        } else {
            existingLicenses = new HashSet<>();
        }
        LoadVNLFiles vnlLoader = new LoadVNLFiles();
        try{
            for(String filename : getCommandService().executeCommand(vnlLoader).getVNLFiles()){
                File file = new File(filename);
                byte[] fileContent = FileUtils.readFileToByteArray(file);
                LicenseManagementEntry entry = VNLMapper.getInstance().unmarshalXML(fileContent);
                existingLicenses.add(entry);
            }
        } catch (IOException e){
            log.error("Error while reading licensefile", e);
        } catch (CommandException e){
            log.error("Error while loading vnl-Files", e);
        }
        return existingLicenses;
    }

    /**
     * @return the commandService
     */
    public ICommandService getCommandService() {
        return commandService;
    }

    /**
     * @param commandService the commandService to set
     */
    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * @return the existingLicenses
     */
    public Set<LicenseManagementEntry> getExistingLicenses() {
        if(existingLicenses == null){
            readVNLFiles();
        }
        return existingLicenses;
    }

    

}
