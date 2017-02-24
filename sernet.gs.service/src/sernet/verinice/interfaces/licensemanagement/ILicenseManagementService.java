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
import java.util.Date;
import java.util.Map;
import java.util.Set;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public interface ILicenseManagementService {

    public static final String VNL_STORAGE_FOLDER = "vnl_storage_folder";
    
    public static final String VNL_FILE_EXTENSION = "vnl";
    
    int getValidUsersForContentId(String contentId) 
            throws LicenseManagementException;

    Date getMaxValidUntil(String licenseId) throws LicenseManagementException;

    IEncryptionService getCryptoService();

    boolean isCurrentUserValidForLicense(String user, String licenseId) 
            throws LicenseManagementException;

    boolean isUserAssignedLicenseStillValid(String user, String licenseId) 
            throws LicenseManagementException;

    boolean hasLicenseIdAssignableSlots(String licenseId) 
            throws LicenseManagementException;

    void removeAllUsersForLicense(String licenseId);

    Set<String> getAllLicenseIds(boolean decrypted) 
            throws LicenseManagementException;

    Set<String> getAllContentIds(boolean decrypted) 
            throws LicenseManagementException;

    Set<LicenseManagementEntry> getLicenseEntriesForContentId(
            String contentId) throws LicenseManagementException;

    Map<String, String> getPublicInformationForLicenseIdEntry(
            LicenseManagementEntry licenseEntry);

    int getContentIdAllocationCount(String contentId) 
            throws LicenseManagementException;
    
    int getLicenseIdAllocationCount(String licenseId);

    void addLicenseIdAuthorisation(
            String user, String contentId) throws CommandException;

    void removeAllLicenseIdAssignments(String licenseId);

    void removeAllContentIdAssignments(String contentId) 
        throws LicenseManagementException;

    void removeContentIdUserAssignment(String user, String contentId)
        throws LicenseManagementException;
    
    void removeLicenseIdUserAssignment(String user, String licenseId, 
            boolean licenseIdEncrypted) throws LicenseManagementException;

    Set<String> getAuthorisedContentIdsByUser(String user);

    Set<String> getLicenseIdsForContentId(String contentId, boolean decrypted) 
            throws LicenseManagementException;
    
    LicenseManagementEntry getLicenseEntryForLicenseId(
            String encryptedLicenseId) throws LicenseManagementException;

    <T extends Object> T  decrypt(
            LicenseManagementEntry entry, String propertyType);
    
    Set<LicenseManagementEntry> readVNLFiles() 
            throws LicenseManagementException;
    
    Set<LicenseManagementEntry> getExistingLicenses() 
            throws LicenseManagementException;
    
    File getVNLRepository() throws LicenseManagementException;
    
    boolean addVNLToRepository(File vnlFile) throws LicenseManagementException;
    
    String decryptRestrictedProperty(String licenseId, String cypherText,
            String username) throws LicenseManagementException;
    
}
