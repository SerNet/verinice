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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDirectoryCreator;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.NoLicenseAssignedException;
import sernet.verinice.model.licensemanagement.VNLMapper;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.propertyconverter.PropertyConverter;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementServerModeService implements ILicenseManagementService {
    
    protected Logger log = Logger.getLogger(LicenseManagementServerModeService.class);

    // injected by spring
    private LicenseManagementEntryDao licenseManagementDao;
    private IBaseDao<Configuration, Serializable> configurationDao;
    private IEncryptionService cryptoService;
    private ICommandService commandService;
    private IDirectoryCreator directoryCreator;
    
    protected Set<LicenseManagementEntry> existingLicenses = null;

    /**
     * @param encryptedContentId - (encrypted) id of content to inspect
     * @return amount of authorised users for a given contentId 
     */
    @Override
    public int getValidUsersForContentId(String encryptedContentId) throws LicenseManagementException {
        int sum = 0;
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
     * @param encryptedLicenseId - id of content to inspect
     * @return the maximal date a content is valid to  
     */
    @Override
    public Date getMaxValidUntil(String encryptedLicenseId) throws LicenseManagementException {
        Date longestValidDate = new Date(System.currentTimeMillis());
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            if(encryptedLicenseId.equals(entry.getLicenseID())){
                Date current = decrypt(
                        entry,
                        LicenseManagementEntry.COLUMN_VALIDUNTIL);
                if (current.after(longestValidDate)) {
                    longestValidDate = current;
                }
            }
        }
        return longestValidDate;
    }

    /**
     * checks if a given username is authorised for the usage of content
     * defined by a given (encrypted) licenseId 
     * 
     * @param username - login of user to check for
     * @param encryptedLicenseId - licenseId (not contentId!) that should be looked up 
     */
    @Override
    public boolean isCurrentUserValidForLicense(String username, 
            String encryptedLicenseId) throws LicenseManagementException{
        return isUserAssignedLicenseStillValid(username, encryptedLicenseId) &&
                hasLicenseIdAssignableSlots(encryptedLicenseId);
    }

    /**
     * checks if a given encrypted licenseId for a given user is invalid by time
     * @param user - username (login) to check
     * @parm licenseId - licenseId (not contentId!) to check
     * 
     * @return status of validation
     */
    @Override
    public boolean isUserAssignedLicenseStillValid(String user, 
            String encryptedLicenseId) throws LicenseManagementException{
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
    public boolean hasLicenseIdAssignableSlots(String encryptedLicenseId) throws LicenseManagementException {
        int validUsers = 0;
        int assignedUsers = 0;
        LicenseManagementEntry entry = null;
        for(LicenseManagementEntry existingEntry : getExistingLicenses()){
            if(encryptedLicenseId.equals(existingEntry.getLicenseID())){
                entry = existingEntry;
            }
        }
        if(entry != null){
            validUsers = decrypt(entry, 
                    LicenseManagementEntry.COLUMN_VALIDUSERS);
            String decryptedLicenseId = (String)decrypt(
                    entry, LicenseManagementEntry.COLUMN_LICENSEID);
            assignedUsers = 0;
            for (Configuration configuration : getAllConfigurations()) {
                Set<String> assignedIds = configuration.getAllLicenseIds();
                if (assignedIds.contains(decryptedLicenseId)) {
                    assignedUsers++;
                }
            }
        }
        log.debug(encryptedLicenseId + " currently has (" + assignedUsers + "/" + validUsers + ") users");
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
     * @return decrypted licenceIds (not contentIds!) of all 
     * {@link LicenseManagementEntry} available the db
     */
    @Override
    public Set<String> getAllLicenseIds(boolean decrypted) throws LicenseManagementException {
        Set<String> allIds = new HashSet<>();
        for(LicenseManagementEntry entry : getExistingLicenses()){
            String cypherLicenseId = entry.getLicenseID();
            String plainLicenseId = decrypt(entry, 
                    LicenseManagementEntry.COLUMN_LICENSEID);
            allIds.add(decrypted ? plainLicenseId : cypherLicenseId);
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
    public Set<LicenseManagementEntry> getLicenseEntriesForContentId(
            String encryptedContentId) throws LicenseManagementException{
        
        Set<LicenseManagementEntry> uniqueEntryCollection = new HashSet<>();
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if(entry.getContentIdentifier().equals(encryptedContentId)){
                uniqueEntryCollection.add(entry);
            }
        }
        return uniqueEntryCollection;
    }
    
    @Override
    public LicenseManagementEntry getLicenseEntryForLicenseId(
            String encryptedLicenseId) throws LicenseManagementException{
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if(entry.getLicenseID().equals(encryptedLicenseId)){
                return entry;
            }
        }
        return null;
    }
    

    /**
     * @return all decrypted licenseIds for a given encrypted contentId
     * @param contentID - the contentId to search for
     */
    @Override
    public Set<String> getLicenseIdsForContentId(String contentId, boolean decrypted) 
            throws LicenseManagementException{
        // unless contentId is crypted with pw and salt, this returns an empty set
        Set<String> uniqueIds = new HashSet<>();
        for(LicenseManagementEntry entry : getExistingLicenses()){
            String plainLicenseId = (String)decrypt(entry,
                    LicenseManagementEntry.COLUMN_LICENSEID);
            if(!decrypted){
                if(entry.getContentIdentifier().equals(contentId)){
                    uniqueIds.add(plainLicenseId);
                }
            } else {
                String plainContentIdEntry = 
                        decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
                String plainContentIdInput = cryptoService.decryptLicenseRestrictedProperty(entry.getUserPassword(), contentId);
                if(plainContentIdEntry.equals(plainContentIdInput)){
                    uniqueIds.add(plainLicenseId);
                }
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
    public Set<String> getAllContentIds(boolean decrypted)
        throws LicenseManagementException{
        Set<String> allIds = new HashSet<>();
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if(decrypted){
                Object decryptedValue = 
                        decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
                String decryptedString = String.valueOf(decryptedValue);
                allIds.add(decryptedString);
            } else {
                allIds.add(entry.getContentIdentifier());
            }
            
        }
        return allIds;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.licensemanagement.ILicenseManagementService#getLicenseIdAllocationCount(java.lang.String)
     */
    @Override
    public int getLicenseIdAllocationCount(String licenseId) {
        int count = 0;
        for(Configuration configuration : getAllConfigurations()){
            if (configuration.getAssignedLicenseIds().contains(licenseId)) {
                count++;
            }      
        }
        return count;
    }

    /**
     * returns how many users are currently assigned to use 
     * the license with the @param contentId
     */
    @Override
    public int getContentIdAllocationCount(String encryptedContentId) 
            throws LicenseManagementException{
        int count = 0;
        Set<String> licenseIds = getLicenseIdsForContentId(encryptedContentId, false);
        for (String licenseId : licenseIds){
            count += getLicenseIdAllocationCount(licenseId);
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
     * @throws CommandException 
     */
    @Override
    public void addLicenseIdAuthorisation(String user, String licenseId) throws CommandException {
        Configuration configuration = getConfigurationByUsername(user);
        configuration.addLicensedContentId(licenseId);
        configurationDao.merge(configuration, true);
        "".hashCode();
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
    public void removeAllContentIdAssignments(String encryptedContentId) 
            throws LicenseManagementException{
        Set<String> licenseIds = getLicenseIdsForContentId(encryptedContentId, false);
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
     * remove all user assignments (licenseIds) for a given encrypted contentId
     * 
     * 
     * @param username - user that should be forbidden to use content
     * @param encryptedContentId - content that should be dereferenced from username
     * 
     */
    @Override
    public void removeContentIdUserAssignment(String username, 
            String encryptedContentId) throws LicenseManagementException {
        for (LicenseManagementEntry entry : getLicenseEntriesForContentId(encryptedContentId)) {
            removeLicenseIdUserAssignment(username, entry.getLicenseID(), true);
        }
    }

    
    /**
     * remove a single licenseId assignment from a configuration (user)
     **/
    @Override
    public void removeLicenseIdUserAssignment(String user, String licenseId, 
            boolean licenseIdEncrypted) throws LicenseManagementException {
        String decryptedLicenseId;
        
        Configuration configuration = getConfigurationByUsername(user);
        LicenseManagementEntry entry = getLicenseEntryForLicenseId(licenseId);
        if(licenseIdEncrypted){
            decryptedLicenseId = decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
        } else {
            decryptedLicenseId = licenseId;
        }
        if(configuration != null){
            configuration.removeLicensedContentId(decryptedLicenseId);
            configurationDao.saveOrUpdate(configuration);
        }
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
        return getConfigurationByUsername(username).getAssignedLicenseIds();
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
    public void setConfigurationDao(IBaseDao<Configuration,
            Serializable> configurationDao) {
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
    public <T extends Object> T decrypt(LicenseManagementEntry entry,
            String propertyType){
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
    
    private <T extends Object> T convertPropery(Object property,
            String propertyType){
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
     * @see sernet.verinice.interfaces.licensemanagement.
     * ILicenseManagementService#getAllLicenseEntries()
     */
    @Override
    public Set<LicenseManagementEntry> readVNLFiles() 
            throws LicenseManagementException{
        if(existingLicenses != null){
            existingLicenses.clear();
        } else {
            existingLicenses = new HashSet<>();
        }
        
        Collection<File> vnlFiles = FileUtils.listFiles(getVNLRepository(), 
                new String[]{ILicenseManagementService.
                        VNL_FILE_EXTENSION}, false);
        
        try{
            for(File vnlFile : vnlFiles){
                byte[] fileContent = FileUtils.readFileToByteArray(vnlFile);
                LicenseManagementEntry entry = VNLMapper.getInstance().
                        unmarshalXML(fileContent);
                existingLicenses.add(entry);
            }
        } catch (IOException e){
            String msg = "Error while reading licensefile"; 
            log.error(msg, e);
            throw new LicenseManagementException(msg);
        } 
        return existingLicenses;
    }
    
    @Override
    public File getVNLRepository() throws LicenseManagementException{
        return new File(directoryCreator.create());
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
    public Set<LicenseManagementEntry> getExistingLicenses() 
            throws LicenseManagementException{
        if(existingLicenses == null || existingLicenses.size() == 0){
            readVNLFiles();
        }
        return existingLicenses;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.licensemanagement.
     * ILicenseManagementService#addVNLToRepository()
     */
    @Override
    public boolean addVNLToRepository(File vnlFile) 
            throws LicenseManagementException{
        File newVnlInRepo =  new File(FilenameUtils.concat(getVNLRepository().
                getAbsolutePath(), vnlFile.getName()));
        try {
            FileUtils.copyFile(vnlFile, newVnlInRepo);
            return true;
        } catch (IOException e) {
            String msg = "Error adding vnl to repository";
            log.error(msg, e);
            throw new LicenseManagementException(msg,e );
        }
    }
    
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.licensemanagement.
     * ILicenseManagementService#decryptRestrictedProperty(java.lang.String, 
     * java.lang.String)
     */
    @Override
    public String decryptRestrictedProperty(String encryptedContentId, 
            String cypherText, String username) 
                    throws LicenseManagementException {
        LicenseManagementEntry entry = null;
        Set<String> licenseIdsForContentId = getLicenseIdsForContentId(encryptedContentId, true);
        boolean userValidForContent = false;
        Configuration configuration = getConfigurationByUsername(username);
        String licenseIdToUse = "";
        if(configuration != null){
            for(String assignedLicenseId : configuration.getAssignedLicenseIds()){
                if(licenseIdsForContentId.contains(assignedLicenseId)){
                    userValidForContent = true;
                    licenseIdToUse = assignedLicenseId;
                    break;
                }
            }
        }
        if(userValidForContent){
            // get related licenceInformation
            for(LicenseManagementEntry existingEntry : getExistingLicenses()){ 
                if(licenseIdToUse.equals(this.decrypt(existingEntry, 
                        LicenseManagementEntry.COLUMN_LICENSEID))){
                    entry = existingEntry;
                }
            }
            // decrypt
            try {
                if(entry != null){
                    return getCryptoService().decryptLicenseRestrictedProperty(
                            entry.getUserPassword(), cypherText);
                } else {
                    throw new NoLicenseAssignedException("License " 
                            + encryptedContentId + " is not assigned to user: " 
                            + username);                }
            } catch (EncryptionException e) {
                throw new LicenseManagementException(
                        "Problem while decrypting license restricted property", e);
            }
        } else {
            throw new NoLicenseAssignedException("License " 
                    + encryptedContentId + " is not assigned to user: " 
                    + username);
        }
    }
    
    /**
     * @return the directoryCreator
     */
    public IDirectoryCreator getDirectoryCreator() {
        return directoryCreator;
    }

    /**
     * @param directoryCreator the directoryCreator to set
     */
    public void setDirectoryCreator(IDirectoryCreator directoryCreator) {
        this.directoryCreator = directoryCreator;
    }

}
