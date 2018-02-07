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
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.text.html.HTMLWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.threeten.bp.LocalDate;

import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDirectoryCreator;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.model.licensemanagement.NoLicenseAssignedException;
import sernet.verinice.model.licensemanagement.VNLMapper;
import sernet.verinice.model.licensemanagement.propertyconverter.PropertyConverter;

/**
 * 
 * This service provides the base functionalities needed for dealing with
 * licenses to read (and manage permissions to read) license-restricted content.
 * Consider that all the methods dealing with user-specific details, they are
 * redefined in the class {@link LicenseManagementStandaloneModeService} for
 * providing functionality in standalone-mode
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementServerModeService implements ILicenseManagementService {

    protected Logger log = Logger.getLogger(LicenseManagementServerModeService.class);

    // injected by spring
    private IBaseDao<Configuration, Serializable> configurationDao;
    private IEncryptionService cryptoService;
    private ICommandService commandService;
    private IDirectoryCreator lmDirectoryCreator;
    private IAuthService authService;

    protected Set<LicenseManagementEntry> existingLicenses = null;

    public void init() {
        try {
            watchVNLDirectory();
        } catch (LicenseManagementException e) {
            log.error("Something went wrong, watching the File-Events in the vnl-Directory", e);
        }
    }

    @Override
    public boolean isCurrentUserValidForLicense(String username, String encryptedLicenseId,
            boolean decrypt) throws LicenseManagementException {
        LicenseManagementEntry entryToUse = null;
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            if (entry.getLicenseID().equals(encryptedLicenseId)) {
                entryToUse = entry;
                break;
            }
        }
        String plainEntryLicenseId = decrypt(entryToUse, LicenseManagementEntry.COLUMN_LICENSEID);
        Configuration configuration = getConfigurationByUsername(username);
        boolean userHasLicense = configuration != null
                && configuration.getAssignedLicenseIds().contains(plainEntryLicenseId);
        return userHasLicense
                && isUserAssignedLicenseStillValid(username, encryptedLicenseId, decrypt);
    }

    @Override
    public boolean isUserAssignedLicenseStillValid(String user, String encryptedLicenseId,
            boolean decrypt) throws LicenseManagementException {

        LicenseManagementEntry entryToUse = findEntryForLicenseId(encryptedLicenseId, decrypt);

        if (entryToUse != null) {
            Object o = decrypt(entryToUse, LicenseManagementEntry.COLUMN_VALIDUNTIL);
            LocalDate ld = LocalDate.parse(o.toString());
            return ld.isAfter(LocalDate.now());
        }
        return false;
    }

    /**
     * Finds the instance of {@link LicenseManagementEntry} which matches
     * to @param encryptedLicenseId
     * 
     * Search for entry is possible in en- or decrypted mode (@param decrypt)
     * 
     * @param encryptedLicenseId
     * @param decrypt
     * @param entryToUse
     * @return
     * @throws LicenseManagementException
     */
    private LicenseManagementEntry findEntryForLicenseId(String encryptedLicenseId, boolean decrypt)
            throws LicenseManagementException {
        LicenseManagementEntry entryToUse = null;
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            if (decrypt) {
                String plainLicenseId = getCryptoService().decryptLicenseRestrictedProperty(
                        getUserPasswordAsString(entry), encryptedLicenseId);
                String plainEntryLicenseId = decrypt(entry,
                        LicenseManagementEntry.COLUMN_LICENSEID);
                if (plainLicenseId.equals(plainEntryLicenseId)) {
                    entryToUse = entry;
                }

            } else {
                if (entry.getLicenseID().equals(encryptedLicenseId)) {
                    entryToUse = entry;
                }
            }

            if (entryToUse != null) {
                break;
            }
        }
        return entryToUse;
    }

    @Override
    public synchronized boolean hasLicenseIdAssignableSlots(String encryptedLicenseId)
            throws LicenseManagementException {
        int validUsers = 0;
        int assignedUsers = 0;
        LicenseManagementEntry entry = null;
        for (LicenseManagementEntry existingEntry : getExistingLicenses()) {
            if (encryptedLicenseId.equals(existingEntry.getLicenseID())) {
                entry = existingEntry;
            }
        }
        String decryptedLicenseId = null;
        if (entry != null) {
            validUsers = decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUSERS);
            decryptedLicenseId = decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
            for (Configuration configuration : getAllConfigurations()) {
                Set<String> assignedIds = configuration.getAllLicenseIds();
                if (assignedIds.contains(decryptedLicenseId)) {
                    assignedUsers++;
                }
            }
        }
        if (log.isDebugEnabled()) {
            String debugId = (decryptedLicenseId != null) ? decryptedLicenseId : encryptedLicenseId;
            log.debug(debugId + " currently has (" + assignedUsers + "/" + validUsers + ") users");
        }
        return assignedUsers < validUsers;

    }

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
     *         {@link LicenseManagementEntry} available in the system
     * 
     * @param decrypted
     *            - decrypt licenseIds
     */
    @Override
    public Set<String> getAllLicenseIds(boolean decrypted) throws LicenseManagementException {
        Set<String> allIds = new HashSet<>();
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            String cypherLicenseId = entry.getLicenseID();
            String plainLicenseId = decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
            allIds.add(decrypted ? plainLicenseId : cypherLicenseId);
        }
        return allIds;
    }

    @Override
    public Set<LicenseManagementEntry> getLicenseEntriesForUserByContentId(String user,
            String contentId) throws LicenseManagementException {
        Set<LicenseManagementEntry> userLicenses = new HashSet<>();

        Configuration configuration = getConfigurationByUsername(user);
        for (LicenseManagementEntry entry : getLicenseEntriesForContentId(contentId, true)) {
            String entryLicenseIdPlain = decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
            if (configuration != null
                    && configuration.getAssignedLicenseIds().contains(entryLicenseIdPlain)) {
                userLicenses.add(entry);
            }
        }
        return userLicenses;
    }

    @Override
    public Set<LicenseManagementEntry> getLicenseEntriesForContentId(String encryptedContentId,
            boolean decrypt) throws LicenseManagementException {
        Set<LicenseManagementEntry> uniqueEntryCollection = new HashSet<>();

        if (decrypt) {
            return getLicenseEntriesViaDecryptedContentIds(encryptedContentId,
                    uniqueEntryCollection);
        }

        for (LicenseManagementEntry entry : getExistingLicenses()) {
            if (entry.getContentIdentifier().equals(encryptedContentId)) {
                uniqueEntryCollection.add(entry);
            }
        }
        return uniqueEntryCollection;
    }

    /**
     * 
     * Iterates over all {@link LicenseManagementEntry} and decrypts their
     * contentId. Tries to decrypt the given @param encryptedContentId with the
     * password of the {@link LicenseManagementEntry} and after that compares
     * both contentIds. If they are equal, the entry will be added to a set that
     * gets returned after the iteration has finished
     * 
     * @param encryptedContentId
     * @param uniqueEntryCollection
     * @return
     * @throws LicenseManagementException
     */
    private Set<LicenseManagementEntry> getLicenseEntriesViaDecryptedContentIds(
            String encryptedContentId, Set<LicenseManagementEntry> uniqueEntryCollection)
            throws LicenseManagementException {
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            try {
                String plainContentId = getCryptoService().decryptLicenseRestrictedProperty(
                        getUserPasswordAsString(entry), encryptedContentId);
                String plainEntryContentId = decrypt(entry,
                        LicenseManagementEntry.COLUMN_CONTENTID);
                if (plainContentId.equals(plainEntryContentId)) {
                    uniqueEntryCollection.add(entry);
                }
            } catch (EncryptionException e) {
                log.info("Could not decrypt correctly for value:\t" + encryptedContentId
                        + "with entry (licenseId):\t" + entry.getLicenseID(), e);
                continue;
            }
        }
        return uniqueEntryCollection;
    }

    @Override
    public LicenseManagementEntry getLicenseEntryForLicenseId(String licenseId, boolean decrypt)
            throws LicenseManagementException {
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            if (!decrypt) { // don't use decryption
                if (entry.getLicenseID().equals(licenseId)) {
                    return entry;
                }
            } else { // use decryption
                String plainEntryLicenseId = decrypt(entry,
                        LicenseManagementEntry.COLUMN_LICENSEID);
                if (plainEntryLicenseId.equals(licenseId)) {
                    return entry;
                }
            }

        }
        return null;
    }

    @Override
    public Set<String> getLicenseIdsForContentId(String contentId, boolean decrypted)
            throws LicenseManagementException {
        // unless contentId is crypted with pw and salt, this returns an empty
        // set
        Set<String> uniqueIds = new HashSet<>();
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            String plainLicenseId = decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
            if (!decrypted) {
                if (entry.getContentIdentifier().equals(contentId)) {
                    uniqueIds.add(plainLicenseId);
                }
            } else {
                String plainContentIdEntry = decrypt(entry,
                        LicenseManagementEntry.COLUMN_CONTENTID);
                String plainContentIdInput = "";
                try {
                    plainContentIdInput = getCryptoService().decryptLicenseRestrictedProperty(
                            getUserPasswordAsString(entry), contentId);
                } catch (EncryptionException e) {
                    // this is try & error, so fails are ok here
                    continue;
                }
                if (plainContentIdEntry.equals(plainContentIdInput)) {
                    uniqueIds.add(plainLicenseId);
                }
            }
        }

        return uniqueIds;
    }

    @Override
    public Set<String> getAllContentIds(boolean decrypted) throws LicenseManagementException {
        Set<String> allIds = new HashSet<>();
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            if (decrypted) {
                Object decryptedValue = decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
                String decryptedString = String.valueOf(decryptedValue);
                allIds.add(decryptedString);
            } else {
                allIds.add(entry.getContentIdentifier());
            }

        }
        return allIds;
    }

    @Override
    public int getLicenseIdAllocationCount(String licenseId) {
        int count = 0;
        for (Configuration configuration : getAllConfigurations()) {
            if (configuration.getAssignedLicenseIds().contains(licenseId)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public synchronized int getContentIdAllocationCount(String encryptedContentId)
            throws LicenseManagementException {
        int count = 0;
        Set<String> licenseIds = getLicenseIdsForContentId(encryptedContentId, false);
        for (String licenseId : licenseIds) {
            count += getLicenseIdAllocationCount(licenseId);
        }
        return count;
    }

    @Override
    public synchronized void addLicenseIdAuthorisation(Configuration configuration,
            String licenseId) throws CommandException {
        configuration.addLicensedContentId(licenseId);
        configurationDao.merge(configuration, true);
        if (log.isDebugEnabled()) {
            Configuration reloaded = configurationDao.findById(configuration.getDbId());
            log.debug("config in db has now licenses:\t"
                    + reloaded.getAssignedLicenseIds().toString());
        }
    }

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

    @Override
    public void removeAllContentIdAssignments(String encryptedContentId)
            throws LicenseManagementException {
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

    @Override
    public void removeContentIdUserAssignment(Configuration configuration,
            String encryptedContentId) throws LicenseManagementException {
        for (LicenseManagementEntry entry : getLicenseEntriesForContentId(encryptedContentId,
                false)) {
            removeLicenseIdUserAssignment(configuration, entry.getLicenseID(), true);
        }
    }

    @Override
    public void removeLicenseIdUserAssignment(Configuration configuration, String licenseId,
            boolean licenseIdEncrypted) throws LicenseManagementException {
        String decryptedLicenseId;

        LicenseManagementEntry entry = getLicenseEntryForLicenseId(licenseId, false);
        if (licenseIdEncrypted) {
            decryptedLicenseId = decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
        } else {
            decryptedLicenseId = licenseId;
        }
        if (configuration != null) {
            configuration.removeLicensedContentId(decryptedLicenseId);
            configurationDao.saveOrUpdate(configuration);
        }
    }

    /**
     * Load all instances of {@link Configuration} via hql including their
     * properties via join to avoid {@link LazyInitializationException} when
     * iterating them
     * 
     * @return a set of all instances of {@link Configuration}
     */
    private Set<Configuration> getAllConfigurations() {
        Set<Configuration> configurations = new HashSet<>();
        String hql = "from Configuration conf inner join fetch conf.entity as entity "
                + "inner join fetch entity.typedPropertyLists as propertyList "
                + "inner join fetch propertyList.properties as props ";

        Object[] params = new Object[] {};
        List<?> hqlResult = getConfigurationDao().findByQuery(hql, params);
        for (Object o : hqlResult) {
            if (o instanceof Configuration) {
                configurations.add((Configuration) o);
            }
        }
        return configurations;
    }

    /**
     * Load a {@link Configuration} referenced by a username
     * 
     * @param username
     *            - username that identifies a {@link Configuration}
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

    @Override
    public Set<String> getAuthorisedContentIdsByUser(String username) {
        Set<String> contentIds = new HashSet<>();
        Configuration configuration = getConfigurationByUsername(username);
        if (configuration != null) {
            contentIds.addAll(configuration.getAssignedLicenseIds());
        }
        return contentIds;
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
     * @param cryptoService
     *            the cryptoService to set
     */
    public void setCryptoService(IEncryptionService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Override
    public IEncryptionService getCryptoService() {
        return cryptoService;
    }

    @Override
    public <T extends Object> T decrypt(LicenseManagementEntry entry, String propertyType) {
        String plainText = "";
        switch (propertyType) {
        case LicenseManagementEntry.COLUMN_CONTENTID:
            plainText = getCryptoService().decrypt(entry.getContentIdentifier(),
                    getUserPassword(entry), decodeEntryProperty(entry.getSalt()));
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_LICENSEID:
            plainText = getCryptoService().decrypt(entry.getLicenseID(), getUserPassword(entry),
                    decodeEntryProperty(entry.getSalt()));
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_VALIDUNTIL:
            plainText = getCryptoService().decrypt(entry.getValidUntil(), getUserPassword(entry),
                    decodeEntryProperty(entry.getSalt()));
            return convertPropery(plainText, propertyType);
        case LicenseManagementEntry.COLUMN_VALIDUSERS:
            plainText = getCryptoService().decrypt(entry.getValidUsers(), getUserPassword(entry),
                    decodeEntryProperty(entry.getSalt()));
            return convertPropery(plainText, propertyType);
        default:
            plainText = getCryptoService().decrypt(entry.getPropertyByType(propertyType),
                    getUserPassword(entry), decodeEntryProperty(entry.getSalt()));
            return (T) entry.getPropertyByType(plainText);
        }
    }

    protected char[] getUserPassword(LicenseManagementEntry entry) {
        String encodedPassword = entry.getUserPassword();
        return decodeEntryProperty(encodedPassword).toCharArray();
    }

    protected String getUserPasswordAsString(LicenseManagementEntry entry) {
        return String.valueOf(getUserPassword(entry));
    }

    protected String decodeEntryProperty(String encodedProperty) {
        byte[] encodedByteArray = encodedProperty.getBytes(VeriniceCharset.CHARSET_UTF_8);
        String decodedProperty = new String(getCryptoService().decodeBase64(encodedByteArray),
                VeriniceCharset.CHARSET_UTF_8);
        return decodedProperty;

    }

    private <T extends Object> T convertPropery(Object property, String propertyType) {
        T returnValue;
        PropertyConverter converter = new PropertyConverter();
        switch (propertyType) {
        case LicenseManagementEntry.COLUMN_CONTENTID:
            returnValue = (T) converter.convertToString(property);
            break;
        case LicenseManagementEntry.COLUMN_LICENSEID:
            returnValue = (T) converter.convertToString(property);
            break;
        case LicenseManagementEntry.COLUMN_VALIDUNTIL:
            returnValue = (T) converter.convertToDate(property);
            break;
        case LicenseManagementEntry.COLUMN_VALIDUSERS:
            returnValue = (T) converter.convertToInteger(property);
            break;
        default:
            // if none of the defined rules apply, just return a string
            returnValue = (T) String.valueOf(property);
        }
        return returnValue;
    }

    @Override
    public synchronized Set<LicenseManagementEntry> readVNLFiles()
            throws LicenseManagementException {
        if (existingLicenses != null) {
            existingLicenses.clear();
        } else {
            existingLicenses = Collections.synchronizedSet(new HashSet<LicenseManagementEntry>());
        }

        try (DirectoryStream<Path> vlnFilesStream = Files.newDirectoryStream(
                getVNLRepository().toPath(),
                "*.".concat(ILicenseManagementService.VNL_FILE_EXTENSION))) {
            for (Path vnlFile : vlnFilesStream) {
                addLicenseFromPath(vnlFile);
            }
        } catch (IOException e) {
            String msg = "Error while reading license files from " + getVNLRepository();
            log.error(msg, e);
            throw new LicenseManagementException(msg);
        }
        return existingLicenses;
    }

    private void addLicenseFromPath(Path vnlFile) {
        try {
            byte[] fileContent = Files.readAllBytes(vnlFile);
            byte[] decodedContent = getCryptoService().decodeBase64(fileContent);
            LicenseManagementEntry entry = VNLMapper.getInstance().unmarshalXML(decodedContent);
            existingLicenses.add(entry);
        } catch (Exception e) {
            log.error("Error processing VNL file " + vnlFile, e);
        }
    }

    /**
     * Get configured location of vnl-repository
     */
    @Override
    public File getVNLRepository() throws LicenseManagementException {
        return new File(lmDirectoryCreator.create());
    }

    /**
     * @return the commandService
     */
    public ICommandService getCommandService() {
        return commandService;
    }

    /**
     * @param commandService
     *            the commandService to set
     */
    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * @return the existingLicenses
     */
    public Set<LicenseManagementEntry> getExistingLicenses() throws LicenseManagementException {
        if (existingLicenses == null || existingLicenses.isEmpty()) {
            readVNLFiles();
        }
        return existingLicenses;
    }

    @Override
    public File addVNLToRepository(File vnlFile) throws LicenseManagementException {
        File newVnlInRepo = new File(
                FilenameUtils.concat(getVNLRepository().getAbsolutePath(), vnlFile.getName()));
        try {
            FileUtils.copyFile(vnlFile, newVnlInRepo);
            readVNLFiles(); // refresh the list
            return newVnlInRepo;
        } catch (IOException e) {
            String msg = "Error adding vnl to repository";
            log.error(msg, e);
            throw new LicenseManagementException(msg, e);
        }
    }

    @Override
    public String decryptRestrictedProperty(String encryptedContentId, String cypherText,
            String username) throws LicenseManagementException {
        LicenseManagementEntry entry = null;
        String licenseIdToUse = getLicenseIdForDecryptionByUser(encryptedContentId, username);
        return getDecryptedPropertyValue(encryptedContentId, cypherText, username, entry,
                licenseIdToUse);
    }

    /**
     * Decrypts a license restricted property value
     *
     * Throws {@link NoLicenseAssignedException} if user has no permission to
     * see the restricted content
     * 
     * Throws {@link LicenseManagementException} if something went wrong with
     * the decryption of the property value
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
    private String getDecryptedPropertyValue(String encryptedContentId, String cypherText,
            String username, LicenseManagementEntry entry, String licenseIdToUse)
            throws LicenseManagementException {
        if (StringUtils.isNotEmpty(licenseIdToUse)) { // is user valid for
                                                      // content
            // get related licenceInformation
            for (LicenseManagementEntry existingEntry : getExistingLicenses()) {
                if (licenseIdToUse.equals(
                        this.decrypt(existingEntry, LicenseManagementEntry.COLUMN_LICENSEID))) {
                    entry = existingEntry;
                }
            }
            // decrypt
            try {
                if (entry != null) {
                    return getCryptoService().decryptLicenseRestrictedProperty(
                            getUserPasswordAsString(entry), cypherText);
                } else {
                    throw new NoLicenseAssignedException("License " + encryptedContentId
                            + " is not assigned to user: " + username);
                }
            } catch (EncryptionException e) {
                throw new LicenseManagementException(
                        "Problem while decrypting license restricted property", e);
            }
        } else {
            throw new NoLicenseAssignedException(
                    "License " + encryptedContentId + " is not assigned to user: " + username);
        }
    }

    /**
     * Returns a licenseId for a given contentId and a given user, if the user
     * has the permission to see content of that contentId
     * 
     * 
     * @param encryptedContentId
     * @param username
     * @return licenseId
     * @throws LicenseManagementException
     */
    private String getLicenseIdForDecryptionByUser(String encryptedContentId, String username)
            throws LicenseManagementException {
        Set<String> licenseIdsForContentId = getLicenseIdsForContentId(encryptedContentId, true);
        Configuration configuration = getConfigurationByUsername(username);
        if (configuration != null) {
            for (String assignedLicenseId : configuration.getAssignedLicenseIds()) {
                if (licenseIdsForContentId.contains(assignedLicenseId)) {
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
     * @param directoryCreator
     *            the directoryCreator to set
     */
    public void setLmDirectoryCreator(IDirectoryCreator directoryCreator) {
        this.lmDirectoryCreator = directoryCreator;
    }

    @Override
    public boolean isLicenseInvalidSoon(String username, String encryptedContentId)
            throws LicenseManagementException {
        LicenseManagementEntry longestValidEntry = null;
        for (LicenseManagementEntry entry : getLicenseEntriesForContentId(encryptedContentId,
                true)) {
            if (isCurrentUserValidForLicense(username, entry.getLicenseID(), false)) {
                if (longestValidEntry != null) {
                    Object currentEntry = decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUNTIL);
                    Object longestLasting = decrypt(longestValidEntry,
                            LicenseManagementEntry.COLUMN_VALIDUNTIL);
                    LocalDate currentDate = (LocalDate) currentEntry;
                    LocalDate longestDate = (LocalDate) longestLasting;
                    if (currentDate.isAfter(longestDate)) {
                        longestValidEntry = entry;
                    }
                } else { // for the first loop iteration
                    longestValidEntry = entry;
                }
            }
        }
        if (longestValidEntry != null) {
            return invalidInTheNextMonth(longestValidEntry);
        }

        return false; // false is default, show message only if difference is <
                      // ILicenseManagementService.WARNING_VALID_LESS_THAN_DAYS
    }

    /**
     * Checks if the given license will be invalid within the next 31 days
     * 
     * @param entry
     * @return
     */
    protected boolean invalidInTheNextMonth(LicenseManagementEntry entry) {
        LocalDate validUntil = decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUNTIL);
        LocalDate currentDate = LocalDate.now();
        LocalDate currentPlusOneMonth = currentDate.plusDays(31);
        return validUntil.isBefore(currentPlusOneMonth);
    }

    @Override
    public Set<LicenseMessageInfos> getAllLicenseMessageInfos() throws LicenseManagementException {
        Set<LicenseMessageInfos> infos = new HashSet<>();
        for (LicenseManagementEntry entry : getExistingLicenses()) {
            infos.add(getSingleLicenseMessageInfos(entry));
        }
        return infos;
    }

    @Override
    public LicenseMessageInfos getLicenseMessageInfos(String user, String encryptedContentId,
            String encryptedLicenseId, LicenseManagementEntry entry)
            throws LicenseManagementException {
        LicenseManagementEntry firstEntry = getFirstLicenseForUser(user, encryptedContentId,
                encryptedLicenseId, entry);
        return getSingleLicenseMessageInfos(firstEntry);
    }

    /**
     * Create a {@link LicenseMessageInfos} for a given
     * {@link LicenseManagementEntry}
     * 
     * @param entry
     * @return
     */
    private LicenseMessageInfos getSingleLicenseMessageInfos(LicenseManagementEntry entry) {
        LicenseMessageInfos infos;
        if (entry != null) {
            String contentId = decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
            String licenseId = decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
            LocalDate validUntil = decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUNTIL);
            int validUsers = decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUSERS);
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
     * Iterates over all existing licences and returns the
     * {@link LicenseManagementEntry} that matches the given contentId,
     * licenseId and is assigned to the given user
     *
     * Note that licenseId can be empty ("" or null), usecase for calling this
     * from {@link HTMLWriter}, that does not know about an entry. In the case
     * of more than one assigned license for the given contentId, the one that
     * is valid the longest will be taken
     * 
     * @param user
     * @param encryptedContentId
     * @param encryptedLicenseId
     * @return
     * @throws LicenseManagementException
     */
    private LicenseManagementEntry getFirstLicenseForUser(String user, String encryptedContentId,
            String encryptedLicenseId, LicenseManagementEntry entry)
            throws LicenseManagementException {
        Set<LicenseManagementEntry> matchingEntries = new HashSet<>();
        for (LicenseManagementEntry existingEntry : getLicenseEntriesForUserByContentId(user,
                encryptedContentId)) {
            LicenseManagementEntry matchingEntry = getMatchingEntries(encryptedContentId,
                    encryptedLicenseId, entry, existingEntry);
            if (matchingEntry != null) {
                matchingEntries.add(matchingEntry);
            }
        }

        return getValidLongestEntry(matchingEntries);
    }

    /**
     * Finds the entry of a set of {@link LicenseManagementEntry} instances that
     * is valid the longest.
     * 
     * @param entries
     * @return
     */
    protected LicenseManagementEntry getValidLongestEntry(Set<LicenseManagementEntry> entries) {
        if (entries.size() == 1) {
            return entries.iterator().next();
        } else {
            LicenseManagementEntry oldestEntry = null;
            for (LicenseManagementEntry possibleEntry : entries) {
                if (oldestEntry == null) {
                    oldestEntry = possibleEntry;
                } else {
                    LocalDate current = decrypt(possibleEntry,
                            LicenseManagementEntry.COLUMN_VALIDUNTIL);
                    LocalDate oldest = decrypt(oldestEntry,
                            LicenseManagementEntry.COLUMN_VALIDUNTIL);
                    if (oldest.isBefore(current)) {
                        oldestEntry = possibleEntry;
                    }
                }
            }
            return oldestEntry;
        }
    }

    /**
     * 
     * Checks if a given @param existingEntry (instance of
     * {@link LicenseManagementEntry} matches to a given encrypted ContentId and
     * optionally a given encrypted licenseId. Also possible to pass the
     * license-/content-Id containing {@link LicenseManagementEntry} (needed by
     * use-case of {@link AccountView} to create license-columns)
     * 
     * @param encryptedContentId
     * @param encryptedLicenseId
     * @param entry
     * @param existingEntry
     */
    protected LicenseManagementEntry getMatchingEntries(String encryptedContentId,
            String encryptedLicenseId, LicenseManagementEntry entry,
            LicenseManagementEntry existingEntry) {
        String plainContentId = "";
        String plainLicenseId = "";
        if (entry == null) {
            try {
                plainContentId = getCryptoService().decryptLicenseRestrictedProperty(
                        getUserPasswordAsString(existingEntry), encryptedContentId);
                if (StringUtils.isNotEmpty(encryptedLicenseId)) {
                    plainLicenseId = getCryptoService().decryptLicenseRestrictedProperty(
                            existingEntry.getUserPassword(), encryptedLicenseId);
                }
            } catch (EncryptionException e) {
                // appears only in tier2 and means, that candidate to encrypt
                // was the wrong one, so we try the next. No error handling
                // needed here
                return null;
            }
        } else {
            plainContentId = decrypt(entry, LicenseManagementEntry.COLUMN_CONTENTID);
            if (StringUtils.isNotEmpty(encryptedLicenseId)) {
                plainLicenseId = decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
            }

        }
        String entryPlainContentId = decrypt(existingEntry,
                LicenseManagementEntry.COLUMN_CONTENTID);
        String entryPlainLicenseId = decrypt(existingEntry,
                LicenseManagementEntry.COLUMN_LICENSEID);
        if (plainContentId.equals(entryPlainContentId)) {
            boolean plainLicenseIdIsEmpty = StringUtils.isEmpty(plainLicenseId);
            if (plainLicenseIdIsEmpty || plainLicenseId.equals(entryPlainLicenseId)) {
                return existingEntry;
            }
        }
        return null;
    }

    /**
     * watch the vnl-repository directory for events that create or delete files
     * with the extension ".vnl". In both cases (creation/deletion of file) the
     * map with the systemwide available licenses will be resetted and refilled
     * by rereading the vnl-files from the configured repository. This ensures,
     * that the objects represented by the files in the repository are always in
     * sync with the state of the directory that is the repository.
     * 
     * @throws LicenseManagementException
     */
    private void watchVNLDirectory() throws LicenseManagementException {
        File vnlDir = getVNLRepository();
        if (vnlDir.isDirectory()) {
            Path vnlDirPath = vnlDir.toPath();
            Kind<?>[] eventKinds = new Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE };
            final WatchService watchService;
            try {
                watchService = FileSystems.getDefault().newWatchService();
                vnlDirPath.register(watchService, eventKinds);
            } catch (IOException e) {
                throw new LicenseManagementException(
                        "Error while initialising file-Watch-Service for vnl-Directory", e);
            }
            Thread vnlDirectoryWatcher = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        handleWatchKeyEvents(watchService);
                    } catch (LicenseManagementException e) {
                        log.error("Something went wrong handling file-Events", e);
                    }
                }
            }, "vnl-directory-watcher");
            vnlDirectoryWatcher.setDaemon(true);
            vnlDirectoryWatcher.start();
        }
    }

    /**
     * listening for watchKey-events. As soon as an event appears,
     * 
     * watchService.take()
     * 
     * delievers it. Up to that moment the loop stops in that line. To ensure
     * all events are covered during complete runtime, this peace of code has to
     * run inside of an endless loop, in an own non-blocking, thread.
     * 
     * 
     * @param watchService
     * @throws InterruptedException
     * @throws LicenseManagementException
     */
    private void handleWatchKeyEvents(final WatchService watchService)
            throws LicenseManagementException {
        while (true) {
            try {
                final WatchKey watchKey = watchService.take();
                log.debug("WatchKey taken:\t" + watchKey.toString());
                handleWatchKeyEvent(watchKey);
                // reset the key
                boolean valid = watchKey.reset();
                if (!valid) {
                    log.error("Overflow-Event appeared. Cancelling "
                            + "watchService-loop for vnl-Directory");
                    break;
                }
            } catch (InterruptedException e) {
                log.error("Thread Interrupted");
                Thread.currentThread().interrupt();
                throw new LicenseManagementException(
                        "Watchservice for vnl-Directory was terminated unexpectedly", e);
            }
        }
    }

    /**
     * handles a single watchKey-Event and consideres its
     * {@link StandardWatchEventKinds} -type to determine the process to deal
     * with this event. Since ENTRY_MODIFY needs not to be considered, it has
     * been left out. OVERFLOW can always happen and has to be dealt with. It
     * appears e.g. in case of a sudden shutdown / interruption of the
     * watchService or its running thread.
     * 
     * @param watchKey
     * @throws LicenseManagementException
     */
    private void handleWatchKeyEvent(final WatchKey watchKey) throws LicenseManagementException {
        for (WatchEvent<?> event : watchKey.pollEvents()) {
            log.debug("Event triggered of Kind:\t" + event.kind().name());
            if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
                // handle deleted vnl file
                final Path changed = (Path) event.context();
                if (changed.toString().endsWith(VNL_FILE_EXTENSION)) {
                    readVNLFiles();
                    log.warn("VNL-File:\t" + changed.toString() + " has been deleted");
                }

            } else if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                // handle added vnl file
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();

                if (filename.toString().endsWith(VNL_FILE_EXTENSION)) {
                    readVNLFiles();
                    log.info("VNL-File:\t" + filename.toString() + " has been added");
                }
            } else if (StandardWatchEventKinds.OVERFLOW.equals(event.kind())) {
                // handle overflow event
                continue;
            }

        }
    }

    /**
     * @return the authService
     */
    public IAuthService getAuthService() {
        return authService;
    }

    /**
     * @param authService
     *            the authService to set
     */
    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

}
