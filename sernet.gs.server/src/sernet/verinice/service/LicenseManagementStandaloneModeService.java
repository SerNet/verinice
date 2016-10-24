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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.VNLMapper;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementStandaloneModeService extends LicenseManagementServerModeService
    implements ILicenseManagementService {
    
    LicenseManagementEntryDao licenseManagementDao;

    /**
     * if any entries in db referencing the given encrypted contentId, return 1 since 
     * in standalone mode there is no second user, otherwise return 0
     * 
     * @param encryptedContentId - contentId (not licenseId!) to check for
     */
    @Override
    public int getValidUsersForContentId(String encryptedContentId) {
//        String hql = "from LicenseManagementEntry " + "where contentIdentifier = ?";
//        Object[] params = new Object[] { encryptedContentId };
//        List idList = licenseManagementDao.findByQuery(hql, params);
        int sum = 0;
//        for (Object o : idList) {
//            if (o instanceof LicenseManagementEntry) {
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if(entry.getContentIdentifier().equals(encryptedContentId)){
                    int validUsers = decrypt(entry,
                            LicenseManagementEntry.COLUMN_VALIDUSERS);
                    sum += validUsers;
            }
        }
        if(sum>0){
            return 1;
        }
        return sum;
    }

    /**
     * checks if the {@link LicenseManagementEntry} is valid at the current date
     * and ignores the username, since in standalone mode username does not bother
     * 
     * @param encryptedLicenseId - encrypted licenseId (not contentId!) 
     * to validate time for 
     */
    @Override
    public boolean isUserAssignedLicenseStillValid(String user, String encryptedLicenseId) {
//        String hql = "from LicenseManagementEntry " 
//                + "where licenseID = ?";
//        Object[] params = new Object[]{encryptedLicenseId};
//        List validUntilList = licenseManagementDao.findByQuery(hql, params);
//        if(validUntilList != null && validUntilList.size() == 1){
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if(entry.getLicenseID().equals(encryptedLicenseId)){
                long validUntil = decrypt(entry,
                        LicenseManagementEntry.COLUMN_VALIDUNTIL);
                long currentTime = System.currentTimeMillis();
                return validUntil > currentTime;
            }
        }
        return false;
    }

    /**
     * checks if there are any free slots for assigning another user to allow
     * him the usage of the content referenced by (encrypted )licenseId
     * in standalone, the existance of an {@link LicenseManagementEntry}
     * (and a validUser count > 0) allows the (singlemode) user to use
     * the content, so we do not have to check for any free slots, since 
     * there is only 1 slot we can use. so just check for slotsize > 0
     * 
     * @param encryptedLicenseId - the encrypted licenseId (not contentId!) to validate
     * 
     */
    @Override
    public boolean checkAssignedUsersForLicenseId(String encryptedLicenseId) {
//        String hql = "from LicenseManagementEntry " 
//                + "where licenseID = ?";
//        Object[] params = new Object[]{encryptedLicenseId};
//        List validUserList = licenseManagementDao.findByQuery(hql, params);
//        if(validUserList != null && validUserList.size() == 1){
        for(LicenseManagementEntry entry : getExistingLicenses()){
            if (entry.getLicenseID().equals(encryptedLicenseId)){
                int validUsers = decrypt(entry, 
                        LicenseManagementEntry.COLUMN_VALIDUSERS);
                return validUsers > 0;
            }
        }
        return true;
    }

    /**
     * removes all user assignments to a given {@link LicenseManagementEntry} 
     * (referenced by licenseId) in server-mode. In standalone mode, 
     * the existance of a {@link LicenseManagementEntry} for a contentId
     * allows the user to use a license, no need for assignments here
     * 
     * so the method should to nothing in this mode
     * 
     */
    @Override
    public void removeAllUsersForLicense(String licenseId) {
        // DO NOTHING
        
    }

    /**
     * assigns a user to a {@link LicenseManagementEntry} (referenced
     * by licenseId) to allow him to use the content referenced in that
     * entry. 
     * 
     * In standalone mode, 
     * the existance of a {@link LicenseManagementEntry} for a contentId
     * allows the user to use a license, no need for assignments here
     * 
     * so the method should to nothing in this mode
     * 
     */    
    @Override
    public void grantUserToLicense(String username, String licenseId) {
        // should not be used in tier2, so always return true
        // since the tier2-user is always allowed to use a license, if its existant
    }
    
    @Override
    public File getVNLRepository(){
        File location = null;
        try {
            String instanceAreaVNL = FilenameUtils.concat(
                    System.getProperty(IVeriniceConstants.OSGI_INSTANCE_AREA),
                    "vnl");
            location = FileUtils.toFile(new URL(instanceAreaVNL));
        } catch (MalformedURLException e) {
            throw new LicenseManagementException("Cannot create vnl-storage-folder", e);
        }
        return location;
    }
    
    /**
     * in standalone mode, read vnl-files from workspace
     */
    @Override
    public Set<LicenseManagementEntry> readVNLFiles(){
        if(existingLicenses != null){
            existingLicenses.clear();
        } else {
            existingLicenses = new HashSet<>();
        }
        
        File location = getVNLRepository();

        
        Set<String> vnlFiles = new HashSet<>();
        if(!location.exists()){
            try {
                FileUtils.touch(location);
            } catch (IOException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Error creating ")
                    .append(location.getAbsolutePath())
                    .append(" to store vnl-Files");
                log.error(sb.toString(), e);
                throw new LicenseManagementException(sb.toString(), e);
            }
        }
        if(location.isDirectory()){
            List<String> filenames = Arrays.asList(location.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".vnl");
                }
            }));
            for(String filename : filenames){
                vnlFiles.add(FilenameUtils.concat(location.getAbsolutePath(), filename));
            }
        }
        
        try{
            for(String filename : vnlFiles){
                File file = new File(filename);
                byte[] fileContent = FileUtils.readFileToByteArray(file);
                LicenseManagementEntry entry = VNLMapper.getInstance().unmarshalXML(fileContent);
                existingLicenses.add(entry);
            }
        } catch (IOException e){
            String msg = "Error while reading licensefile"; 
            log.error(msg, e);
            throw new LicenseManagementException(msg, e);
        }
        return existingLicenses;
    }

}
