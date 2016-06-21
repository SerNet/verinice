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
package sernet.verinice.interfaces;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public interface ILicenseManagementService {
    
  int getValidUsersForContentId(String contentId);
  
  Date getMaxValidUntil(String contentId);
  
  String getLicenseId(int dbId);
  
  Object getCryptoService();
  
  boolean isCurrentUserValidForLicense(String user, String licenseId);
  
  boolean isUserAssignedLicenseStillValid(String user, String licenseId);
  
  boolean checkAssignedUsersForLicenseId(String licenseId);
  
  void removeAllUsersForLicense(String licenseId);
  
  void grantUserToLicense(String user, String licenseId);
  
  Set<String> getAllLicenseIds();
  
  Set<String> getAllContentIds();
  
  Set<LicenseManagementEntry> getLicenseEntriesForContentId(String contentId);
  
  Map<String, String> getPublicInformationForLicenseIdEntry(LicenseManagementEntry licenseEntry);
  
  int getContentIdAllocationCount(String contentId);
  
  void addLicenseIdAuthorisation(String user, String contentId);
  
  void removeAllLicenseIdAssignments(String licenseId);
  
  void removeAllContentIdAssignments(String contentId);
  
  void removeContentIdUserAssignment(String user, String contentId);
  
  Set<String> getAuthorisedContentIdsByUser(String user);
  
  Set<String> getLicenseIdsForContentId(String contentId);


}
