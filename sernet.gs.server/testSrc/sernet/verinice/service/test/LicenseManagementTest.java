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
package sernet.verinice.service.test;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementTest extends ContextConfiguration{
    
    @Resource(name="licenseManagementDao")
    protected LicenseManagementEntryDao elementDao;
    
    @Test
    public void daoTest(){
        String contentIdentifier = "content123";
        String licenseId = "uniqueLicenseId";
        String userPassword = "secretPassword";
        String salt = "saltyMcSaltFace";
        Date validUntil = new Date();
        int validUsers = 5;
        Assert.assertNull(elementDao.findByLicenseId(licenseId));
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(contentIdentifier);
        entry.setLicenseID(licenseId);
        entry.setSalt(salt);
        entry.setUserPassword(userPassword);
        entry.setValidUntil(validUntil.toString());
        entry.setValidUsers(String.valueOf(validUsers));
        
        elementDao.merge(entry);
        
        elementDao.flush();
        
        LicenseManagementEntry persistedObject = elementDao.findByLicenseId(licenseId);
        Assert.assertEquals(entry.getContentIdentifier(), persistedObject.getContentIdentifier());
        Assert.assertEquals(entry.getLicenseID(), persistedObject.getLicenseID());
        Assert.assertEquals(entry.getSalt(), persistedObject.getSalt());
        Assert.assertEquals(entry.getUserPassword(), persistedObject.getUserPassword());
        Assert.assertEquals(entry.getValidUntil(), persistedObject.getValidUntil());
        Assert.assertEquals(entry.getValidUsers(), persistedObject.getValidUsers());
        
        elementDao.delete(persistedObject);
        elementDao.flush();
        
        Assert.assertNull(elementDao.findByLicenseId(licenseId));
        
    }

}
