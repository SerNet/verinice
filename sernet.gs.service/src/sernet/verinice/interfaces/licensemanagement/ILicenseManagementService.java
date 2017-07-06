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
package sernet.verinice.interfaces.licensemanagement;

import java.io.File;
import java.util.Set;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;

/**
 * 
 * The services implementing this interface are realising the 
 * license-management of verinice. The difference between 
 * standalone- and server-mode is the user-management.
 * In server-mode, users have get assigned to licenses
 * to be able to use them. In standalone-mode only one
 * user exists, so every license existant in the workspace-folder
 * could be used by the default-user 
 *  
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public interface ILicenseManagementService {
    
    public static final String VNL_STORAGE_FOLDER = "vnl_storage_folder";
    
    /** 
     * Extension for license files AND name of folder to store license-files
     * in. $workspace in tier2, /WEB-INF/vnl in tier3 (by default)
     **/
    public static final String VNL_FILE_EXTENSION = "vnl";
    
    /**
     * If a license is valid less then the here encoded amount of,
     * display a warning in the ObjectBrowser
     */
    public static final int WARNING_VALID_LESS_THAN_DAYS = 31;
    
    
    /**
     * method used by spring-configuration to initialize the service
     */
    void init();
    
    /**
     *  Adds licenseId (not contentId!) to an instance of {@link Configuration}
     *  that is referenced by a given username. User will get authorised 
     *  for usage of that licenseId by this.
     *  
     *  Attention: this method does not(!) validate if the license
     *  has any free slots for another user
     *  
     *  Has to be synchronized, because adding a user to a license my only happen
     *  by one thread ad a time, to prevent two (or more threads) adding
     *  users at a time producing a assignment of e.g. 6/5
     *  
     *  Only used in server-mode
     *  
     *  @param configuration - username to authorise
     *  @param licenseId - licenseId (not contentId!) the user will 
     *  get authorised for
     *  
     * @throws CommandException 
     */
    void addLicenseIdAuthorisation(
            Configuration account, String licenseId) throws CommandException;

    /**
     * Adds a file to the vnl-repository
     * 
     * Not used yet, since there is no UI for adding vnl-Files
     * (has to be added on file-system-layer)
     **/
    File addVNLToRepository(File vnlFile) throws LicenseManagementException;

    /**
     * Decrypts a given field (constant of {@link LicenseManagementEntry})
     *  of {@link LicenseManagementEntry}
     * 
     * @param entry
     * @param propertyType
     * @return field in plaintext value
     */
    <T extends Object> T  decrypt(
            LicenseManagementEntry entry, String propertyType);

    /**
     * Decrypts a license restricted property value with the usage of 
     * a given license (by contentId) and user
     * 
     * @param encryptedContentId
     * @param cypherText
     * @param username
     * @return
     * @throws LicenseManagementException
     */
    String decryptRestrictedProperty(String licenseId, String cypherText,
            String username) throws LicenseManagementException;

    /**
     * Getter for {@link IEncryptionService}, used by spring
     * 
     * @return implementation of {@link IEncryptionService}
     */
    IEncryptionService getCryptoService();
    
    /**
     * Get contentIds of all {@link LicenseManagementEntry} 
     * existing in the system
     * 
     * @param decrypted - should the contentIds be returned decrypted? 
     * 
     * @return a Set<String> containing all available contentIds
     */
    Set<String> getAllContentIds(boolean decrypted) 
            throws LicenseManagementException;

    
    /**
     * 
     * Get licenseIds of all {@link LicenseManagementEntry} 
     * existing in the system
     * 
     * @param decrypted - should the licenseIds be returned decrypted? 
     * 
     * @return a Set<String> containing all available licenseIds
     */
    Set<String> getAllLicenseIds(boolean decrypted) 
            throws LicenseManagementException;

    /**
     * Get all contentIds (not licenseIds!) that a given user is
     * allowed to use
     * 
     * Only used in server-mode 
     * 
     * @param username - username to check ids for
     * @return all ids the user is allowed to see content for
     * 
     */
    Set<String> getAuthorisedContentIdsByUser(String user);

    /**
     * Returns how many users are currently assigned to use 
     * the license with the @param contentId
     * 
     * @return the amount of users assigned for one contentId
     * 
     */
    int getContentIdAllocationCount(String contentId) 
            throws LicenseManagementException;

    /**
     * Get all the licenses available on the system, 
     * vnl-files represented via {@link LicenseManagementEntry} 
     * 
     * @return the existingLicenses
     */
    Set<LicenseManagementEntry> getExistingLicenses() 
            throws LicenseManagementException;

    /**
     * @return All instances of {@link LicenseManagementEntry} referencing 
     * the encrypted contentId (not licenseId!) given by parameter.
     * 
     * @param encryptedContentId - the id of the content to search for
     * @param decrypt decrypt contentId before using it
     */
    Set<LicenseManagementEntry> getLicenseEntriesForContentId(
            String contentId, boolean decrypt) throws LicenseManagementException;

    /**
     * Gets a single {@link LicenseManagementEntry} for a 
     * given licenseId
     * 
     * @param licenseId - the licenseId that represents the 
     * {@link LicenseManagementEntry}
     * @param decrypt - decrypt the licenseId of entry before checking if
     * equal to given licenseId 
     * 
     */
    LicenseManagementEntry getLicenseEntryForLicenseId(
            String encryptedLicenseId, boolean decrypt) throws LicenseManagementException;

    /**
     * Returns the amount of users that is allowed to use the 
     * given licenseId (has to be provided in plainText)
     * 
     * @param licenseId
     * @return
     */
    int getLicenseIdAllocationCount(String licenseId);

    /**
     * @return All decrypted licenseIds for a given encrypted contentId
     * @param contentID - the contentId to search for
     * @param decrypted - decrypt values before comparing to parameter contentId
     */
    Set<String> getLicenseIdsForContentId(String contentId, boolean decrypted) 
            throws LicenseManagementException;

    /**
     * Gets all instances of {@link LicenseManagementEntry} that are
     * representing the given contentId and are assigned to the 
     * given user
     * 
     * @param user - the user to check for 
     * @param contentId - the contentId to check for
     */
    Set<LicenseManagementEntry> getLicenseEntriesForUserByContentId(
            String user, String contentId) throws LicenseManagementException;
    
    /**
     * Creates instance of {@link LicenseMessageInfos} that wraps 
     * information about a {@link LicenseManagementEntry}
     * 
     * @param user
     * @param encryptedContentId
     * @return
     * @throws LicenseManagementException
     */
    LicenseMessageInfos getLicenseMessageInfos(String user, String contentId,
            String licenseId,
            LicenseManagementEntry entry)
        throws LicenseManagementException;
    
    /**
     * Get the information wrapping objects ( {@link LicenseMessageInfos} )
     * for all of the existing licenses 
     */
    Set<LicenseMessageInfos> getAllLicenseMessageInfos()
            throws LicenseManagementException;

    /**
     * Get the configured location of the vnl-repository
     */
    File getVNLRepository() throws LicenseManagementException;

    /**
     * Checks if the amount of authorised users for a given encrypted 
     * licenseId is below the amount allowed at basis of db entries (licenses)
     * 
     * Has to be synchronized, because data to be read can be edited by 
     * several threads simultaneously
     * 
     * @param encryptedLicenseId - licenseId (not contentId) to check for
     * @return are there free slots to be assigned for a given licenseId
     */
    boolean hasLicenseIdAssignableSlots(String licenseId) 
            throws LicenseManagementException;
    
    /**
     * Checks if a given username is authorised for the usage of content
     * defined by a given (encrypted) licenseId 
     * 
     * @param username - login of user to check for
     * @param encryptedLicenseId - licenseId (not contentId!) 
     * that should be looked up 
     */
    boolean isCurrentUserValidForLicense(String user, String licenseId, 
            boolean decrypt) throws LicenseManagementException;
    

    /**
     * Computes if the license a user has assigned for a given contentId
     * becomes invaled within the next 31 days (month). If more 
     * than one license for a pair of contentId/user is assigned,
     * the license that is valid the longest will be considered 
     * 
     * @param username
     * @param encryptedContentId
     * @return
     * @throws LicenseManagementException
     */
    boolean isLicenseInvalidSoon(String username, String encryptedContentId)
            throws LicenseManagementException;

    /**
     * Checks if a given encrypted licenseId for a given user is invalid by time
     * 
     * @param user - username (login) to check
     * @param encryptedLicenseId - licenseId (not contentId!) to check
     * @param decrypt - decrypt encryptedLicenseId before using it
     * 
     * @return status of validation
     */
    boolean isUserAssignedLicenseStillValid(String user, String licenseId,
            boolean decrypt) throws LicenseManagementException;

    /**
     * Read all vnl-Files from configured directory and map them
     * to instances of {@link LicenseManagementEntry}
     * 
     * @return Set of {@link LicenseManagementEntry}
     * @throws LicenseManagementException
     */
    Set<LicenseManagementEntry> readVNLFiles() 
            throws LicenseManagementException;
    
    /**
     * Remove all assignments for a given encrypted contentId (not licenseId!)
     * 
     * 
     * @param encryptedContentId - contentId (not licenseId) that should be 
     * dereferenced by all users
     * 
     */
    void removeAllContentIdAssignments(String contentId) 
            throws LicenseManagementException;

    /**
     * Removes all user assignments for a given licenseId (not contentId!)
     * 
     * @param licenseId - licenseId (not contentId!) that should be dereferenced
     * by all users
     * 
     */
    void removeAllLicenseIdAssignments(String licenseId);
    
    /**
     * Removes all user assignments for a given licenseId
     * 
     * @param encryptedLicenseId - id of licenseEntry (not contentId!) 
     * that should be cleared 
     */
    void removeAllUsersForLicense(String licenseId);

    /**
     * Remove all user assignments (licenseIds) for a given encrypted contentId
     * 
     * 
     * @param username - user that should be forbidden to use content
     * @param encryptedContentId - content that should be dereferenced
     *  from username
     * 
     */
    void removeContentIdUserAssignment(Configuration account, String contentId)
            throws LicenseManagementException;


    /**
     * Remove a single licenseId assignment from a configuration (user)
     **/
    void removeLicenseIdUserAssignment(Configuration account, String licenseId, 
            boolean licenseIdEncrypted) throws LicenseManagementException;


    
    
}
