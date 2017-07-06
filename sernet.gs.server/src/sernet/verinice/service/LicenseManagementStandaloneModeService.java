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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.threeten.bp.LocalDate;

import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.model.licensemanagement.NoLicenseAssignedException;
import sernet.verinice.model.licensemanagement.VNLMapper;

/**
 * 
 * This services re-defines the methods that are dealing with 
 * the user-management regarding the license-management. 
 * This is necessary because of only 1 existing "user" in standalone-
 * mode. This user does not need to get assignment of licenses,
 * he is allowed to use every license that exists within the workspace
 * 
 * 
 * All methods
 * not connected to user-specific details are defined in 
 * {@link LicenseManagementServerModeService} which this
 * class is inheriting from
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementStandaloneModeService 
    extends LicenseManagementServerModeService
    implements ILicenseManagementService {
    
    IEncryptionService cryptoService;
    
    /**
     * Checks if the {@link LicenseManagementEntry} is valid at the current date
     * and ignores the username, since in standalone mode username 
     * does not bother
     * 
     * @param encryptedLicenseId - encrypted licenseId (not contentId!) 
     * to validate time for 
     */
    @Override
    public boolean isUserAssignedLicenseStillValid(String user, 
            String encryptedLicenseId, boolean decrypt) 
                        throws LicenseManagementException {
        for (LicenseManagementEntry entry : getExistingLicenses()){
            if (entry.getLicenseID().equals(encryptedLicenseId)){
                long validUntil = decrypt(entry,
                        LicenseManagementEntry.COLUMN_VALIDUNTIL);
                long currentTime = System.currentTimeMillis();
                return validUntil > currentTime;
            }
        }
        return false;
    }

    /**
     * Checks if there are any free slots for assigning another user to allow
     * him the usage of the content referenced by (encrypted )licenseId
     * in standalone, the existance of an {@link LicenseManagementEntry}
     * (and a validUser count > 0) allows the (singlemode) user to use
     * the content, so we do not have to check for any free slots, since 
     * there is only 1 slot we can use. so just check for slotsize > 0
     * 
     * @param encryptedLicenseId - the encrypted licenseId 
     * (not contentId!) to validate
     * 
     */
    @Override
    public boolean hasLicenseIdAssignableSlots(String encryptedLicenseId) 
        throws LicenseManagementException{
        for (LicenseManagementEntry entry : getExistingLicenses()){
            if (entry.getLicenseID().equals(encryptedLicenseId)){
                int validUsers = decrypt(entry, 
                        LicenseManagementEntry.COLUMN_VALIDUSERS);
                return validUsers > 0;
            }
        }
        return true;
    }

    /**
     * Removes all user assignments to a given {@link LicenseManagementEntry} 
     * (referenced by licenseId) in server-mode. In standalone mode, 
     * the existance of a {@link LicenseManagementEntry} for a contentId
     * allows the user to use a license, no need for assignments here
     * 
     * So, the method should to nothing in this mode
     * 
     */
    @Override
    public void removeAllUsersForLicense(String licenseId) {
        // DO NOTHING
        
    }

    /**
     * Get location of vnl-repository, in tier2 it is 
     * <$workspace>/vnl 
     * (not configurable)
     */
    @Override
    public File getVNLRepository() throws LicenseManagementException{
        File location = null;
        try {
            String instanceAreaVNL = FilenameUtils.concat(
                    System.getProperty(IVeriniceConstants.OSGI_INSTANCE_AREA),
                    "vnl");
            location = FileUtils.toFile(new URL(instanceAreaVNL));
        } catch (MalformedURLException e) {
            throw new LicenseManagementException(
                    "Cannot create vnl-storage-folder", e);
        }
        return location;
    }
    
    /**
     * In standalone mode, read vnl-files from repository
     */
    @Override
    public synchronized Set<LicenseManagementEntry> readVNLFiles() 
            throws LicenseManagementException{
        if (existingLicenses != null){
            existingLicenses.clear();
        } else {
            existingLicenses = Collections.synchronizedSet(
                    new HashSet<LicenseManagementEntry>());
        }
        
        File location = getVNLRepository();

        
        Set<String> vnlFiles = new HashSet<>();
        if (!location.exists()){
            createVNLLocation(location);
        }
        if (location.isDirectory()){
            vnlFiles = readVNLFilesFromLocation(location);
        }
        
        existingLicenses.addAll(mapVNLFilesToObjects(vnlFiles));
        return existingLicenses;
    }

    /**
     * Maps a set of Strings (paths to vnl-files) to instances of
     * {@link LicenseManagementEntry}
     * 
     * @param vnlFiles
     * @throws LicenseManagementException
     */
    private Set<LicenseManagementEntry> mapVNLFilesToObjects(
            Set<String> vnlFiles) throws LicenseManagementException {
        Set<LicenseManagementEntry> mappedVNLFiles = new HashSet<>();
        try {
            for (String filename : vnlFiles){
                File file = new File(filename);
                byte[] fileContent = FileUtils.readFileToByteArray(file);
                byte[] decodedContent = getCryptoService().decodeBase64(fileContent);
                LicenseManagementEntry entry = VNLMapper.getInstance().
                        unmarshalXML(decodedContent);
                mappedVNLFiles.add(entry);
            }
        } catch (IOException e){
            String msg = "Error while reading licensefile"; 
            log.error(msg, e);
            throw new LicenseManagementException(msg, e);
        }
        return mappedVNLFiles;
    }

    /**
     * Returns a set of file-paths that are having the extension
     * ".vnl" and are contained in the given directory
     * 
     * @param location
     * @param vnlFiles
     */
    private Set<String> readVNLFilesFromLocation(File location) {
        Set<String> vnlFiles = new HashSet<>();
        List<String> filenames = Arrays.asList(location.list(
                new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".vnl");
            }
        }));
        for (String filename : filenames){
            vnlFiles.add(FilenameUtils.concat(location.getAbsolutePath(),
                    filename));
        }
        return vnlFiles;
    }

    /**
     * creates a file/directory at the given location
     * (should be a directoy, file makes no sense here)
     * 
     * @param location
     * @throws LicenseManagementException
     */
    private void createVNLLocation(File location) 
            throws LicenseManagementException {
        try {
            Files.createDirectory(Paths.get(location.toURI()));
        } catch (IOException e) {
            StringBuilder sb = new StringBuilder();
            sb.append("Error creating ")
                .append(location.getAbsolutePath())
                .append(" to store vnl-Files");
            log.error(sb.toString(), e);
            throw new LicenseManagementException(sb.toString(), e);
        }
    }
    
    
    /**
     * creates instance of {@link LicenseMessageInfos} that wraps 
     * information about a {@link LicenseManagementEntry}
     */
    @Override
    public LicenseMessageInfos getLicenseMessageInfos(String user,
            String encryptedContentId, String encryptedLicenseId,
            LicenseManagementEntry entry) throws LicenseManagementException {
        LicenseManagementEntry firstEntry = getFirstLicenseForUser(user, 
                encryptedContentId, encryptedLicenseId,entry);
        LicenseMessageInfos infos = null;
        if (firstEntry != null){
            String contentId = 
                    decrypt(firstEntry, LicenseManagementEntry.COLUMN_CONTENTID);
            String licenseId = 
                    decrypt(firstEntry, LicenseManagementEntry.COLUMN_LICENSEID);
            LocalDate validUntil = 
                    decrypt(firstEntry, LicenseManagementEntry.COLUMN_VALIDUNTIL);
            boolean invalidSoon = invalidInTheNextMonth(firstEntry);
            infos = new LicenseMessageInfos();
            infos.setContentId(contentId);
            infos.setInvalidSoon(invalidSoon);
            infos.setLicenseId(licenseId);
            infos.setNoLicenseAvailable(false);
            infos.setValidUntil(validUntil);
        } else {
            // no license found, set only noLicenseAvailable
            infos = new LicenseMessageInfos();
            infos.setNoLicenseAvailable(true);
        }
        return infos;
    }
    
    @Override
    public int getLicenseIdAllocationCount(String licenseId) {
        // standalone the default user is the only one
        // that is always assigend for every license
        return 1;
    }
    
    /**
     * returns a license for the (given) default user, if existant
     * considering the given contentId
     * 
     * @param user
     * @param encryptedContentId
     * @throws LicenseManagementException
     */
    private LicenseManagementEntry getFirstLicenseForUser(String user, 
            String encryptedContentId, String encryptedLicenseId,
            LicenseManagementEntry entry) throws LicenseManagementException {
        Set<LicenseManagementEntry> matchingEntries = new HashSet<>();
        for (LicenseManagementEntry existingEntry : 
            getExistingLicenses()){
            LicenseManagementEntry matchingEntry = getMatchingEntries(
                    encryptedContentId, encryptedLicenseId, entry, existingEntry);
            if (matchingEntry != null){
                matchingEntries.add(matchingEntry);
            }
        }
        return getValidLongestEntry(matchingEntries);
    }


    
    @Override
    public String decryptRestrictedProperty(String encryptedContentId, 
            String cypherText, String username) 
                    throws LicenseManagementException {
        LicenseManagementEntry entry = null;
        entry = getLicenseEntryToUseForDecryption(encryptedContentId);
        if (entry != null){
            // decrypt
            try {
                return getCryptoService().decryptLicenseRestrictedProperty(
                        getUserPasswordAsString(entry), cypherText);
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
     * get first instance of {@link LicenseManagementEntry} that will be found
     * matching a given contentId
     * 
     * returns null if no matching entry can be found
     * 
     * @param encryptedContentId
     * @param entry
     * @throws LicenseManagementException
     */
    private LicenseManagementEntry getLicenseEntryToUseForDecryption(
            String encryptedContentId) throws LicenseManagementException {
        for (LicenseManagementEntry existingEntry : getExistingLicenses()){
            try{
                String plainEntryContentId = decrypt(existingEntry, 
                        LicenseManagementEntry.COLUMN_CONTENTID);
                String plainContentId = getCryptoService().
                        decryptLicenseRestrictedProperty(
                                getUserPasswordAsString(existingEntry), encryptedContentId);
                if (plainContentId.equals(plainEntryContentId)){
                    return existingEntry;
                }
            } catch (EncryptionException e){
                // encryption failed, so try the next licenseEntry
                // no error handling needed (try&error(/next))
                continue;
            }
        }
        return null;
    }
    
    @Override
    public Set<LicenseManagementEntry> getLicenseEntriesForUserByContentId(
            String user, String contentId) throws LicenseManagementException{
        return getLicenseEntriesForContentId(contentId, true);
    }

    /**
     * @return the cryptoService
     */
    public IEncryptionService getCryptoService() {
        return cryptoService;
    }

    /**
     * @param cryptoService the cryptoService to set
     */
    public void setCryptoService(IEncryptionService cryptoService) {
        this.cryptoService = cryptoService;
    }

}
