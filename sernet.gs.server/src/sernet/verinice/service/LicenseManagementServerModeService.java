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
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.html.HTMLWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDirectoryCreator;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.model.licensemanagement.NoLicenseAssignedException;
import sernet.verinice.model.licensemanagement.VNLMapper;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.propertyconverter.PropertyConverter;
import sernet.verinice.rcp.account.AccountView;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementServerModeService 
    implements ILicenseManagementService {
    
    protected Logger log = Logger.getLogger(
            LicenseManagementServerModeService.class);

    // injected by spring
    private LicenseManagementEntryDao licenseManagementDao;
    private IBaseDao<Configuration, Serializable> configurationDao;
    private IEncryptionService cryptoService;
    private ICommandService commandService;
    private IDirectoryCreator lmDirectoryCreator;
    private IAuthService authService;
    
    protected Set<LicenseManagementEntry> existingLicenses = null;

    /**
     * checks if a given username is authorised for the usage of content
     * defined by a given (encrypted) licenseId 
     * 
     * @param username - login of user to check for
     * @param encryptedLicenseId - licenseId (not contentId!) 
     * that should be looked up 
     */
    @Override
    public boolean isCurrentUserValidForLicense(String username, 
            String encryptedLicenseId, boolean decrypt) 
                    throws LicenseManagementException {
        LicenseManagementEntry entryToUse = null;
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            if (entry.getLicenseID().equals(encryptedLicenseId)) {
                entryToUse = entry;
                break;
            }
        }
        String plainEntryLicenseId = decrypt(entryToUse, 
                LicenseManagementEntry.COLUMN_LICENSEID);
        boolean userHasLicense = getConfigurationByUsername(username).
                getAssignedLicenseIds().contains(plainEntryLicenseId);
        return userHasLicense &&
                isUserAssignedLicenseStillValid(username, 
                        encryptedLicenseId, decrypt);
    }

    /**
     * checks if a given encrypted licenseId for a given user is invalid by time
     * @param user - username (login) to check
     * @param encryptedLicenseId - licenseId (not contentId!) to check
     * @param decrypt - decrypt encryptedLicenseId before using it
     * 
     * @return status of validation
     */
    @Override
    public boolean isUserAssignedLicenseStillValid(String user, 
            String encryptedLicenseId, boolean decrypt) 
                    throws LicenseManagementException{

        LicenseManagementEntry entryToUse = null;
        entryToUse = findEntryForLicenseId(encryptedLicenseId, decrypt);
        if (entryToUse != null){
            Object o = decrypt(
                    entryToUse, LicenseManagementEntry.COLUMN_VALIDUNTIL);
            LocalDate ld = LocalDate.parse(o.toString());
            return ld.isAfter(LocalDate.now());
        }
        return false;
    }

    /**
     * finds the instance of {@link LicenseManagementEntry}
     * which matches to @param encryptedLicenseId,
     * search for entry possible in en- or decrypted mode (@param decrypt)
     * 
     * @param encryptedLicenseId
     * @param decrypt
     * @param entryToUse
     * @return
     * @throws LicenseManagementException
     */
    private LicenseManagementEntry findEntryForLicenseId(
            String encryptedLicenseId, boolean decrypt) 
                    throws LicenseManagementException {
        LicenseManagementEntry entryToUse = null;
        for (LicenseManagementEntry entry : getExistingLicenses()){
            if (decrypt){
                String plainLicenseId = getCryptoService().
                        decryptLicenseRestrictedProperty(entry.getUserPassword(),
                                encryptedLicenseId);
                String plainEntryLicenseId = 
                        decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
                if (plainLicenseId.equals(plainEntryLicenseId)){
                    entryToUse = entry;
                    break;
                }

            } else {
                if (entry.getLicenseID().equals(encryptedLicenseId)){
                    entryToUse = entry;
                    break;
                }
            }
        }
        return entryToUse;
    }

    /**
     * checks if the amount of authorised users for a given encrypted 
     * licenseId is below the amount allowed at basis of db entries (licenses)
     * 
     * has to be synchronized, because data to be read can be edited by 
     * several threads simultaneously
     * 
     * @param encryptedLicenseId - licenseId (not contentId) to check for
     * @return are there free slots to be assigned for a given licenseId
     */
    @Override
    public synchronized boolean hasLicenseIdAssignableSlots(String encryptedLicenseId) 
            throws LicenseManagementException {
        int validUsers = 0;
        int assignedUsers = 0;
        LicenseManagementEntry entry = null;
        for (LicenseManagementEntry existingEntry : getExistingLicenses()){
            if (encryptedLicenseId.equals(existingEntry.getLicenseID())){
                entry = existingEntry;
            }
        }
        if (entry != null){
            validUsers = decrypt(entry, 
                    LicenseManagementEntry.COLUMN_VALIDUSERS);
            String decryptedLicenseId = (String)decrypt(
                    entry, LicenseManagementEntry.COLUMN_LICENSEID);
            for (Configuration configuration : getAllConfigurations()) {
                Set<String> assignedIds = configuration.getAllLicenseIds();
                if (assignedIds.contains(decryptedLicenseId)) {
                    assignedUsers++;
                }
            }
        }
        log.debug(encryptedLicenseId + " currently has (" 
                + assignedUsers + "/" + validUsers + ") users");
        return assignedUsers < validUsers;

    }

    /**
     * removes all user assignments for a given licenseId
     * 
     * @param encryptedLicenseId - id of licenseEntry (not contentId!) 
     * that should be cleared 
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
     * {@link LicenseManagementEntry} available in the system
     * 
     * @param decrypted - decrypt licenseIds
     */
    @Override
    public Set<String> getAllLicenseIds(boolean decrypted) 
            throws LicenseManagementException {
        Set<String> allIds = new HashSet<>();
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            String cypherLicenseId = entry.getLicenseID();
            String plainLicenseId = decrypt(entry, 
                    LicenseManagementEntry.COLUMN_LICENSEID);
            allIds.add(decrypted ? plainLicenseId : cypherLicenseId);
        }
        return allIds;
    }
    
    
    /**
     * gets all instances of {@link LicenseManagementEntry} that are
     * representing the given contentId and are assigned to the 
     * given user
     * @param user - the user to check for 
     * @param contentId - the contentId to check for
     */
    @Override
    public Set<LicenseManagementEntry> getLicenseEntriesForUserByContentId(
            String user, String contentId) throws LicenseManagementException {
        Set<LicenseManagementEntry> userLicenses = new HashSet<>();
        
        Configuration configuration = getConfigurationByUsername(user);
        for (LicenseManagementEntry entry : 
            getLicenseEntriesForContentId(contentId, true)){
            String entryLicenseIdPlain = 
                    decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
            if (configuration.getAssignedLicenseIds().
                    contains(entryLicenseIdPlain)){
                userLicenses.add(entry);
            }
        }
        return userLicenses;
    }

    /**
     * @return all instances of {@link LicenseManagementEntry} referencing 
     * the encrypted contentId (not licenseId!) given by parameter.
     * 
     * @param encryptedContentId - the id of the content to search for
     * @param decrypt decrypt contentId before using it
     */
    @Override
    public Set<LicenseManagementEntry> getLicenseEntriesForContentId(
            String encryptedContentId, boolean decrypt) 
                    throws LicenseManagementException{
        Set<LicenseManagementEntry> uniqueEntryCollection = new HashSet<>();

        if (decrypt){
            for (LicenseManagementEntry entry : getExistingLicenses()){
                try {
                    String plainContentId = getCryptoService().
                            decryptLicenseRestrictedProperty(entry.getUserPassword(),
                                    encryptedContentId);
                    String plainEntryContentId = decrypt(entry, 
                            LicenseManagementEntry.COLUMN_CONTENTID);
                    if (plainContentId.equals(plainEntryContentId)){
                        uniqueEntryCollection.add(entry);
                    }
                } catch (EncryptionException e){
                    log.info("Could not decrypt correctly for value:\t" + encryptedContentId 
                            + "with entry (licenseId):\t" + entry.getLicenseID());
                    continue;
                }
            }
            return uniqueEntryCollection;
        }
        
        for (LicenseManagementEntry entry : getExistingLicenses()){
            if (entry.getContentIdentifier().equals(encryptedContentId)){
                uniqueEntryCollection.add(entry);
            }
        }
        return uniqueEntryCollection;
    }
    
    
    /**
     * gets a single {@link LicenseManagementEntry} for a 
     * given licenseId
     * 
     * @param licenseId - the licenseId that represents the 
     * {@link LicenseManagementEntry}
     * @param decrypt - decrypt the licenseId of entry before checking if
     * equal to given licenseId 
     * 
     */
    @Override
    public LicenseManagementEntry getLicenseEntryForLicenseId(
            String licenseId, boolean decrypt) throws LicenseManagementException{
        for (LicenseManagementEntry entry : getExistingLicenses()){
            if (!decrypt){ // don't use decryption
                if (entry.getLicenseID().equals(licenseId)){
                    return entry;
                }
            } else { // use decryption
                String plainEntryLicenseId = 
                        decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
                if (plainEntryLicenseId.equals(licenseId)){
                    return entry;
                }
            }

        }
        return null;
    }
    

    /**
     * @return all decrypted licenseIds for a given encrypted contentId
     * @param contentID - the contentId to search for
     * @param decrypted - decrypt values before comparing to parameter contentId
     */
    @Override
    public Set<String> getLicenseIdsForContentId(String contentId, 
            boolean decrypted) 
            throws LicenseManagementException{
        // unless contentId is crypted with pw and salt, this returns an empty set
        Set<String> uniqueIds = new HashSet<>();
        for (LicenseManagementEntry entry : getExistingLicenses()){
            String plainLicenseId = (String)decrypt(entry,
                    LicenseManagementEntry.COLUMN_LICENSEID);
            if (!decrypted){
                if (entry.getContentIdentifier().equals(contentId)){
                    uniqueIds.add(plainLicenseId);
                }
            } else {
                String plainContentIdEntry = 
                        decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
                String plainContentIdInput = "";
                try {
                    plainContentIdInput = getCryptoService().
                            decryptLicenseRestrictedProperty(entry.getUserPassword(),
                                    contentId);
                } catch (EncryptionException e){
                    // this is try & error, so fails are ok here
                    continue;
                }
                if (plainContentIdEntry.equals(plainContentIdInput)){
                    uniqueIds.add(plainLicenseId);
                }
            }
        }

        return uniqueIds;
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
    public void setLicenseManagementDao(
            LicenseManagementEntryDao licenseManagementDao) {
        this.licenseManagementDao = licenseManagementDao;
    }

    /**
     * @return all contentIds 
     */
    @Override
    public Set<String> getAllContentIds(boolean decrypted)
        throws LicenseManagementException{
        Set<String> allIds = new HashSet<>();
        for (LicenseManagementEntry entry : getExistingLicenses()){
            if (decrypted){
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

    /**
     * returns the amount of users that is allowed to use the 
     * given licenseId (has to be provided in plainText)
     * @param licenseId
     * @return
     */
    @Override
    public int getLicenseIdAllocationCount(String licenseId) {
        int count = 0;
        for (Configuration configuration : getAllConfigurations()){
            if (configuration.getAssignedLicenseIds().contains(licenseId)) {
                count++;
            }      
        }
        return count;
    }

    /**
     * returns how many users are currently assigned to use 
     * the license with the @param contentId
     * 
     * 
     */
    @Override
    public synchronized int getContentIdAllocationCount(String encryptedContentId) 
            throws LicenseManagementException{
        int count = 0;
        Set<String> licenseIds = getLicenseIdsForContentId(encryptedContentId,
                false);
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
     *  has to be synchronized, because adding a user to a license my only happen
     *  by one thread ad a time, to prevent two (or more threads) adding
     *  users at a time producing a assignment of e.g. 6/5
     *  
     *  @param user - username to authorise
     *  @param licenseId - licenseId (not contentId!) the user will 
     *  get authorised for
     *  
     * @throws CommandException 
     */
    @Override
    public synchronized void addLicenseIdAuthorisation(String user, String licenseId) 
                throws CommandException {
        Configuration configuration = getConfigurationByUsername(user);
        configuration.addLicensedContentId(licenseId);
        configurationDao.merge(configuration, true);
    }

    /**
     * removes all user assignments for a given licenseId (not contentId!)
     * 
     * @param licenseId - licenseId (not contentId!) that should be dereferenced
     * by all users
     * 
     */
    @Override
    public void removeAllLicenseIdAssignments(String licenseId) {
        for (Configuration configuration : getAllConfigurations()) {
            if (getAuthorisedContentIdsByUser(configuration.getUser()).
                    contains(licenseId)) {
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
     */
    @Override
    public void removeAllContentIdAssignments(String encryptedContentId) 
            throws LicenseManagementException{
        Set<String> licenseIds = getLicenseIdsForContentId(encryptedContentId,
                false);
        for (Configuration configuration : getAllConfigurations()) {
            for (String licenseId : licenseIds) {
                if (getAuthorisedContentIdsByUser(configuration.getUser())
                        .contains(licenseId)) {
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
        for (LicenseManagementEntry entry : getLicenseEntriesForContentId(
                encryptedContentId, false)) {
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
        LicenseManagementEntry entry = getLicenseEntryForLicenseId(licenseId, false);
        if (licenseIdEncrypted){
            decryptedLicenseId = decrypt(entry, 
                    LicenseManagementEntry.COLUMN_LICENSEID);
        } else {
            decryptedLicenseId = licenseId;
        }
        if (configuration != null){
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
    
    
    /**
     * decrypts a given field of {@link LicenseManagementEntry}
     * @param entry
     * @param propertyType
     * @return field in plaintext value
     */
    @Override
    public <T extends Object> T decrypt(LicenseManagementEntry entry,
            String propertyType){
        String plainText = "";
        switch (propertyType){
        case LicenseManagementEntry.COLUMN_CONTENTID:
            plainText = getCryptoService().decrypt(entry.getContentIdentifier(), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_LICENSEID:
            plainText = getCryptoService().decrypt(entry.getLicenseID(), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_VALIDUNTIL:
            plainText = getCryptoService().decrypt(entry.getValidUntil(), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_VALIDUSERS:
            plainText = getCryptoService().decrypt(entry.getValidUsers(), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return convertPropery(plainText, propertyType);
        default:
            plainText = getCryptoService().decrypt(entry.
                    getPropertyByType(propertyType), 
                    entry.getUserPassword().toCharArray(), entry.getSalt());
            return (T)entry.getPropertyByType(plainText);
        }
    }
    
    private <T extends Object> T convertPropery(Object property,
            String propertyType){
        T returnValue;
        PropertyConverter converter = new PropertyConverter();
        switch (propertyType){
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

    /**
     * read all vnl-Files from configured directory and map them
     * to instances of {@link LicenseManagementEntry}
     * @return Set of {@link LicenseManagementEntry}
     * @throws LicenseManagementException
     */
    @Override
    public Set<LicenseManagementEntry> readVNLFiles() 
            throws LicenseManagementException{
        if (existingLicenses != null){
            existingLicenses.clear();
        } else {
            existingLicenses = new HashSet<>();
        }
        
        Collection<File> vnlFiles = FileUtils.listFiles(getVNLRepository(), 
                new String[]{ILicenseManagementService.
                        VNL_FILE_EXTENSION}, false);
        
        try {
            for (File vnlFile : vnlFiles){
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
    
    /**
     * get configured location of vnl-repository
     */
    @Override
    public File getVNLRepository() throws LicenseManagementException{
        return new File(lmDirectoryCreator.create());
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
        if (existingLicenses == null || existingLicenses.size() == 0){
            readVNLFiles();
        }
        return existingLicenses;
    }

    /**
     * adds a file to the vnl-repository
     * 
     * not used yet, since there is no UI for adding vnl-Files
     * (has to be added on file-system-layer)
     **/
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
    
    

    /**
     * decrypts a license restricted property value with the usage of 
     * a given license (by contentId) and user
     * 
     * @param encryptedContentId
     * @param cypherText
     * @param username
     * @return
     * @throws LicenseManagementException
     */
    @Override
    public String decryptRestrictedProperty(String encryptedContentId, 
            String cypherText, String username) 
                    throws LicenseManagementException {
        LicenseManagementEntry entry = null;
        String licenseIdToUse = 
                getLicenseIdForDecryptionByUser(encryptedContentId, username);
        return getDecryptedPropertyValue(encryptedContentId, cypherText,
                username, entry, licenseIdToUse);
    }

    /**
     * decrypts a license restricted property value
     *
     * throws {@link NoLicenseAssignedException} if user has no 
     * permission to see the restricted content
     * 
     * throws {@link LicenseManagementException} if something went
     * wrong with the decryption of the property value
     * 
     * 
     * @param encryptedContentId
     * @param cypherText
     * @param username
     * @param entry
     * @param licenseIdToUse
     * @return
     * @throws LicenseManagementException
     * @throws NoLicenseAssignedException
     */
    private String getDecryptedPropertyValue(String encryptedContentId, 
            String cypherText, String username, LicenseManagementEntry entry, 
            String licenseIdToUse) 
                    throws LicenseManagementException {
        if (StringUtils.isNotEmpty(licenseIdToUse)){ // is user valid for content
            // get related licenceInformation
            for (LicenseManagementEntry existingEntry : getExistingLicenses()){ 
                if (licenseIdToUse.equals(this.decrypt(existingEntry, 
                        LicenseManagementEntry.COLUMN_LICENSEID))){
                    entry = existingEntry;
                }
            }
            // decrypt
            try {
                if (entry != null){
                    return getCryptoService().decryptLicenseRestrictedProperty(
                            entry.getUserPassword(), cypherText);
                } else {
                    throw new NoLicenseAssignedException("License " 
                            + encryptedContentId + " is not assigned to user: " 
                            + username);                }
            } catch (EncryptionException e) {
                throw new LicenseManagementException(
                        "Problem while decrypting license restricted property",
                        e);
            }
        } else {
            throw new NoLicenseAssignedException("License " 
                    + encryptedContentId + " is not assigned to user: " 
                    + username);
        }
    }

    /**
     * returns a licenseId for a given contentId and a given user,
     * if the user has the permission to see content of that contentId
     * 
     * 
     * @param encryptedContentId
     * @param username
     * @return licenseId
     * @throws LicenseManagementException
     */
    private String getLicenseIdForDecryptionByUser(String encryptedContentId, 
            String username) throws LicenseManagementException {
        Set<String> licenseIdsForContentId = getLicenseIdsForContentId(
                encryptedContentId, true);
        Configuration configuration = getConfigurationByUsername(username);
        if (configuration != null){
            for (String assignedLicenseId : configuration.
                    getAssignedLicenseIds()){
                if (licenseIdsForContentId.contains(assignedLicenseId)){
                    return assignedLicenseId;
                }
            }
        }
        return "";
    }
    
    /**
     * @return the directoryCreator
     */
    public IDirectoryCreator getLmDirectoryCreator() {
        return lmDirectoryCreator;
    }

    /**
     * @param directoryCreator the directoryCreator to set
     */
    public void setLmDirectoryCreator(IDirectoryCreator directoryCreator) {
        this.lmDirectoryCreator = directoryCreator;
    }

    /**
     * computes if the license a user has assigned for a given contentId
     * becomes invaled within the next 31 days (month). If more 
     * than one license for a pair of contentId/user is assigned,
     * the license that is valid the longest will be considered 
     * @param username
     * @param encryptedContentId
     * @return
     * @throws LicenseManagementException
     */
    @Override
    public boolean isLicenseInvalidSoon(String username, 
            String encryptedContentId) throws LicenseManagementException{
        LicenseManagementEntry longestValidEntry = null;
        for (LicenseManagementEntry entry : getLicenseEntriesForContentId(
                encryptedContentId, true)){
            if (isCurrentUserValidForLicense(username, entry.getLicenseID(),
                    false)){
                if (longestValidEntry != null){
                    Object currentEntry = decrypt(entry, 
                            LicenseManagementEntry.COLUMN_VALIDUNTIL);
                    Object longestLasting = decrypt(longestValidEntry, 
                            LicenseManagementEntry.COLUMN_VALIDUNTIL);
                    LocalDate currentDate = (LocalDate)currentEntry;
                    LocalDate longestDate = (LocalDate)longestLasting;
                    if (currentDate.isAfter(longestDate)){
                        longestValidEntry = entry;
                    }
                } else { // for the first loop iteration 
                    longestValidEntry = entry;
                }
            }
        }
        if (longestValidEntry != null){
            boolean isBefore = invalidInTheNextMonth(longestValidEntry);
            return isBefore; 
        }
        
        return false; // false is default, show message only if difference is < 
                      // ILicenseManagementService.WARNING_VALID_LESS_THAN_DAYS
    }

    /**
     * checks if the given license will be invalid within the next 
     * 31 days
     * @param entry
     * @return
     */
    protected boolean invalidInTheNextMonth(LicenseManagementEntry entry) {
        LocalDate validUntil = (LocalDate)decrypt(entry, 
                LicenseManagementEntry.COLUMN_VALIDUNTIL);
        LocalDate currentDate = LocalDate.now();
        LocalDate currentPlusOneMonth = currentDate.plusDays(31);
        boolean isBefore = validUntil.isBefore(currentPlusOneMonth);
        return isBefore;
    }

    /**
     * get the information wrapping objects ( {@link LicenseMessageInfos} )
     * for all of the existing licenses 
     */
    @Override
    public Set<LicenseMessageInfos> getAllLicenseMessageInfos() throws LicenseManagementException {
        Set<LicenseMessageInfos> infos = new HashSet<>();
        for (LicenseManagementEntry entry : getExistingLicenses()){
            infos.add(getSingleLicenseMessageInfos(entry));
        }
        return infos;
    }

    /**
     * creates instance of {@link LicenseMessageInfos} that wraps 
     * information about a {@link LicenseManagementEntry}
     * @param user
     * @param encryptedContentId
     * @return
     * @throws LicenseManagementException
     */
    @Override
    public LicenseMessageInfos getLicenseMessageInfos(String user,
            String encryptedContentId, String encryptedLicenseId, 
            LicenseManagementEntry entry) 
                    throws LicenseManagementException {
        LicenseManagementEntry firstEntry = getFirstLicenseForUser(user,
               encryptedContentId, encryptedLicenseId,entry);
        LicenseMessageInfos infos = null;
        infos = getSingleLicenseMessageInfos(firstEntry);
        return infos;
    }

    /**
     * create a {@link LicenseMessageInfos} for a given {@link LicenseManagementEntry}
     * @param entry
     * @return
     */
    private LicenseMessageInfos getSingleLicenseMessageInfos(LicenseManagementEntry entry) {
        LicenseMessageInfos infos;
        if (entry != null){
            String contentId = 
                    decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
            String licenseId = 
                    decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
            LocalDate validUntil = 
                    decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUNTIL);
            int validUsers = 
                    decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUSERS);
            boolean invalidSoon = invalidInTheNextMonth(entry);
            infos = new LicenseMessageInfos();
            infos.setContentId(contentId);
            infos.setInvalidSoon(invalidSoon);
            infos.setLicenseId(licenseId);
            infos.setNoLicenseAvailable(false);
            infos.setValidUntil(validUntil);
            infos.setValidUsers(validUsers);
            infos.setAssignedUsers(getLicenseIdAllocationCount(licenseId));
        } else {
            // no license found, set only noLicenseAvailable
            infos = new LicenseMessageInfos();
            infos.setNoLicenseAvailable(true);
        }
        return infos;
    }
    
    
    /**
     * iterates over all existing licences and returns the 
     * {@link LicenseManagementEntry} that matches the given contentId,
     * licenseId and is assigned to the given user
     *
     * note that licenseId can be empty ("" or null), usecase for
     * calling this from {@link HTMLWriter}, that does not know about
     * an entry. In the case of more than one assigned license
     * for the given contentId, the one that is valid the longest will
     * be taken
     * TODO
     * 
     * @param user
     * @param encryptedContentId
     * @param encryptedLicenseId
     * @return
     * @throws LicenseManagementException
     */
    private LicenseManagementEntry getFirstLicenseForUser(String user, 
            String encryptedContentId, String encryptedLicenseId,
            LicenseManagementEntry entry) throws LicenseManagementException {
        Set<LicenseManagementEntry> matchingEntries = new HashSet<>();
        for (LicenseManagementEntry existingEntry : 
            getLicenseEntriesForUserByContentId(user, encryptedContentId)){
            LicenseManagementEntry matchingEntry = 
                    getMatchingEntries(encryptedContentId, encryptedLicenseId,
                            entry, existingEntry);
            if (matchingEntry != null){
                matchingEntries.add(matchingEntry);
            }
        }
        
        return getValidLongestEntry(matchingEntries);
    }

    /**
     * finds the entry of a set of {@link LicenseManagementEntry} instances
     * that is valid the longest
     * @param entries
     * @return
     */
    protected LicenseManagementEntry getValidLongestEntry(
            Set<LicenseManagementEntry> entries){
        if (entries.size() == 1){
            return entries.stream().findFirst().get();
        } else {
            LicenseManagementEntry oldestEntry = null;
            for (LicenseManagementEntry possibleEntry : entries) {
                if (oldestEntry == null){
                    oldestEntry = possibleEntry;
                    continue;
                } else {
                    LocalDate current = decrypt(possibleEntry, 
                            LicenseManagementEntry.COLUMN_VALIDUNTIL);
                    LocalDate oldest = decrypt(oldestEntry, 
                            LicenseManagementEntry.COLUMN_VALIDUNTIL);
                    if (oldest.isBefore(current)) {
                        oldestEntry = possibleEntry;
                        continue;
                    }
                }
            }
            return oldestEntry;
        }
    }
    
    /**
     * 
     * checks if a given @param existingEntry (instance of 
     * {@link LicenseManagementEntry} matches to a given
     * encrypted ContentId and optionally a given
     * encrypted licenseId. Also possible to pass the license-/content-Id
     * containing {@link LicenseManagementEntry} (needed by use-case
     * of {@link AccountView} to create license-columns
     * 
     * @param encryptedContentId
     * @param encryptedLicenseId
     * @param entry
     * @param existingEntry
     */
    protected LicenseManagementEntry getMatchingEntries(String encryptedContentId,
            String encryptedLicenseId, LicenseManagementEntry entry, 
            LicenseManagementEntry existingEntry) {
        String plainContentId;
        String plainLicenseId = "";
        if (entry == null){
            plainContentId = getCryptoService().
                decryptLicenseRestrictedProperty(
                        existingEntry.getUserPassword(), encryptedContentId);
            if (StringUtils.isNotEmpty(encryptedLicenseId)){
                plainLicenseId = getCryptoService().
                        decryptLicenseRestrictedProperty(
                                existingEntry.getUserPassword(), encryptedLicenseId);
            }
        } else {
            plainContentId = decrypt(
                    entry, LicenseManagementEntry.COLUMN_CONTENTID);
            if (StringUtils.isNotEmpty(encryptedLicenseId)){
                plainLicenseId = decrypt(
                        entry, LicenseManagementEntry.COLUMN_LICENSEID);
            }
            
        }
        String entryPlainContentId = decrypt(existingEntry, 
                LicenseManagementEntry.COLUMN_CONTENTID);
        String entryPlainLicenseId = decrypt(existingEntry, 
                LicenseManagementEntry.COLUMN_LICENSEID);
        if (StringUtils.isEmpty(plainLicenseId) &&
                plainContentId.equals(entryPlainContentId)){
            return existingEntry;
        } else if (StringUtils.isNotEmpty(plainLicenseId) &&
                plainLicenseId.equals(entryPlainLicenseId) &&
                plainContentId.equals(entryPlainContentId)){
            return existingEntry;
        }
        return null;
    }

    /**
     * @return the authService
     */
    public IAuthService getAuthService() {
        return authService;
    }

    /**
     * @param authService the authService to set
     */
    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }


}
