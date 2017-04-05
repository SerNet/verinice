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
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public interface ILicenseManagementService {

    public static final String VNL_STORAGE_FOLDER = "vnl_storage_folder";
    
    public static final String VNL_FILE_EXTENSION = "vnl";
    
    public static final int WARNING_VALID_LESS_THAN_DAYS = 31;
    
    void addLicenseIdAuthorisation(
            Configuration account, String licenseId) throws CommandException;

    File addVNLToRepository(File vnlFile) throws LicenseManagementException;

    <T extends Object> T  decrypt(
            LicenseManagementEntry entry, String propertyType);

    String decryptRestrictedProperty(String licenseId, String cypherText,
            String username) throws LicenseManagementException;

    IEncryptionService getCryptoService();
    

    Set<String> getAllContentIds(boolean decrypted) 
            throws LicenseManagementException;

    Set<String> getAllLicenseIds(boolean decrypted) 
            throws LicenseManagementException;

    Set<String> getAuthorisedContentIdsByUser(String user);

    int getContentIdAllocationCount(String contentId) 
            throws LicenseManagementException;

    Set<LicenseManagementEntry> getExistingLicenses() 
            throws LicenseManagementException;

    Set<LicenseManagementEntry> getLicenseEntriesForContentId(
            String contentId, boolean decrypt) throws LicenseManagementException;

    LicenseManagementEntry getLicenseEntryForLicenseId(
            String encryptedLicenseId, boolean decrypt) throws LicenseManagementException;

    int getLicenseIdAllocationCount(String licenseId);

    Set<String> getLicenseIdsForContentId(String contentId, boolean decrypted) 
            throws LicenseManagementException;

    Set<LicenseManagementEntry> getLicenseEntriesForUserByContentId(
            String user, String contentId) throws LicenseManagementException;
    
    LicenseMessageInfos getLicenseMessageInfos(String user, String contentId,
            String licenseId,
            LicenseManagementEntry entry)
        throws LicenseManagementException;
    
    Set<LicenseMessageInfos> getAllLicenseMessageInfos()
            throws LicenseManagementException;

    File getVNLRepository() throws LicenseManagementException;

    boolean hasLicenseIdAssignableSlots(String licenseId) 
            throws LicenseManagementException;
    
    boolean isCurrentUserValidForLicense(String user, String licenseId, 
            boolean decrypt) throws LicenseManagementException;
    

    boolean isLicenseInvalidSoon(String username, String encryptedContentId)
            throws LicenseManagementException;

    boolean isUserAssignedLicenseStillValid(String user, String licenseId,
            boolean decrypt) throws LicenseManagementException;

    Set<LicenseManagementEntry> readVNLFiles() 
            throws LicenseManagementException;
    

    void removeAllContentIdAssignments(String contentId) 
            throws LicenseManagementException;

    void removeAllLicenseIdAssignments(String licenseId);
    
    void removeAllUsersForLicense(String licenseId);

    void removeContentIdUserAssignment(Configuration account, String contentId)
            throws LicenseManagementException;

    
    void removeLicenseIdUserAssignment(Configuration account, String licenseId, 
            boolean licenseIdEncrypted) throws LicenseManagementException;


    

    
    
    
    
    
    
    
}
