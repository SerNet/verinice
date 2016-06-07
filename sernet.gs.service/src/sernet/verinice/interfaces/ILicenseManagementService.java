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

import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public interface ILicenseManagementService {
    
  int getValidUsersForContentId(String contentId);
  
  Date getMaxValidUntil(String contentId);
  
  String getLicenseId(int dbId);
  
  Object getCryptoService();
  
  String getCurrentUser();
  
  boolean isCurrentUserValidForLicense(String user, String licenseId);
  
  boolean isCurrentUserAuthorizedForLicenseUsage(String user, String licenseid);
  
  boolean isUserAssignedLicenseStillValid(String user, String licenseId);
  
  boolean checkAssignedUsersForLicenseId(String licenseId);
  
  void removeAllUsersForLicense(String licenseId);
  
  // returns false, if user cannot be authorized / granted to license
  boolean grantUserToLicense(Configuration user, String licenseId);
  
  Set<String> getAllLicenseIds();
  
  Set<String> getAllContentIds();
  
  Set<LicenseManagementEntry> getLicenseEntriesForLicenseId(String licenseId);
  
  Map<String, String> getPublicInformationForLicenseIdEntry(LicenseManagementEntry licenseEntry);


}
