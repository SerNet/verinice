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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.bind.JAXB;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.interfaces.ILicenseManagementService;
import sernet.verinice.model.licensemanagement.VNLMapper;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;
import sernet.verinice.service.commands.ExportFactory;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementTest extends ContextConfiguration{
    
    @Resource(name="licenseManagementDao")
    protected LicenseManagementEntryDao elementDao;
    
    @Resource(name="licenseManagementService")
    ILicenseManagementService licenseManagementService;
    
    private final static String CONTENTID = "licenseTestContent";
    
    private final static int AMOUNT_OF_TESTDATA = 100;
    
    private final static List<String> CONTENTID_TESTDATA = new ArrayList<>();
    
    static {
        CONTENTID_TESTDATA.add("ContentID1");
        CONTENTID_TESTDATA.add("ContentID2");
        CONTENTID_TESTDATA.add("ContentID3");
        CONTENTID_TESTDATA.add("ContentID4");
        CONTENTID_TESTDATA.add("ContentID5");
    }

    @Before
    public void init(){
        createTestData(AMOUNT_OF_TESTDATA);
    }
    
    @After
    public void cleanUp(){
        for(int j = 0; j < CONTENTID_TESTDATA.size(); j++){
            for(LicenseManagementEntry entry : elementDao.findByContentIdentifier(CONTENTID_TESTDATA.get(j))){
                elementDao.delete(entry);
            }
        }
        elementDao.flush();
        elementDao.clear();
    }
    
    private void createTestData(final int amount){
        for(int i = 0; i < amount; i++){
            LicenseManagementEntry entry = getSingleRandomInstance();
            elementDao.merge(entry);
        }
        elementDao.flush();
    }
    
    private LicenseManagementEntry getSingleRandomInstance(){
        String licenseId = RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(32));
        String userPassword = RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(16));
        String salt = RandomStringUtils.randomAlphanumeric(RandomUtils.nextInt(64));
        Date validUntil = getRandomDate(2017, 2030);
        int validUsers = RandomUtils.nextInt(20);
        
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(CONTENTID_TESTDATA.get(RandomUtils.nextInt(CONTENTID_TESTDATA.size())));
        entry.setLicenseID(licenseId);
        entry.setSalt(salt);
        entry.setUserPassword(userPassword);
        entry.setValidUntil(String.valueOf(validUntil.getTime()));
        entry.setValidUsers(String.valueOf(validUsers));
        
        return entry;
    }
    
    private Date getRandomDate(int startYear, int endYear){
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.set(startYear, 01, 01, 0, 0, 0);
        long offset = calendar.getTimeInMillis();
        calendar.clear();
        calendar.set(endYear, 01, 01, 0, 0, 0);
        long end = calendar.getTimeInMillis();
        long diff = end - offset + 1;
        long randomDate = offset + (long)(Math.random() * diff);
        calendar.clear();
        calendar.setTimeInMillis(randomDate);
        return calendar.getTime();
    }
    
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
        entry.setValidUntil(String.valueOf(validUntil.getTime()));
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
        
        for(int j = 0; j < CONTENTID_TESTDATA.size(); j++){
            Date maximalValid = licenseManagementService.getMaxValidUntil(CONTENTID_TESTDATA.get(j));
            int sumOfValidUsers = licenseManagementService.getValidUsersForContentId(CONTENTID_TESTDATA.get(j));
            System.out.println(CONTENTID_TESTDATA.get(j) + " is valid until:\t" + maximalValid.toString() + " and for " + String.valueOf(sumOfValidUsers) +" users" );
        }
        
        
    }
    
    @Test
    public void serviceTest(){
        Set<String> allIds = licenseManagementService.getAllContentIds();
        for(int j = 0; j < CONTENTID_TESTDATA.size(); j++){
            Assert.assertTrue(allIds.contains(CONTENTID_TESTDATA.get(j)));
        }
        
    }
    
    @Test
    public void manualMarshallingTest(){
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(CONTENTID);
        entry.setLicenseID("licenseIdBLA");
        entry.setSalt("saltBLA");
        entry.setUserPassword("passwordBLA");
        entry.setValidUntil("validUntilBLA");
        entry.setValidUsers("validUsersBLA");
        
        de.sernet.model.licensemanagement.LicenseManagementEntry xml = new de.sernet.model.licensemanagement.LicenseManagementEntry();
        xml.setContentIdentifier(entry.getContentIdentifier());
        xml.setLicenseID(entry.getLicenseID());
        xml.setSalt(entry.getSalt());
        xml.setUserPassword(entry.getUserPassword());
        xml.setValidUntil(entry.getValidUntil());
        xml.setValidUsers(entry.getValidUsers());
        
        OutputStream os = new ByteArrayOutputStream();
        ExportFactory.marshal(xml, os);
        File file = null;
        try {
            file = File.createTempFile("veriniceTest", "vnl");
//            file.deleteOnExit();
            
            FileUtils.writeByteArrayToFile(file, ((ByteArrayOutputStream)os).toByteArray());
            os.close();
            byte [] bytesFromHD = FileUtils.readFileToByteArray(file);
            InputStream is = new ByteArrayInputStream(bytesFromHD);
            de.sernet.model.licensemanagement.LicenseManagementEntry objectFromHD = JAXB.unmarshal(is, de.sernet.model.licensemanagement.LicenseManagementEntry.class);
            
            Assert.assertTrue(objectFromHD.getContentIdentifier().equals(entry.getContentIdentifier()));
            Assert.assertTrue(objectFromHD.getLicenseID().equals(entry.getLicenseID()));
            Assert.assertTrue(objectFromHD.getSalt().equals(entry.getSalt()));
            Assert.assertTrue(objectFromHD.getUserPassword().equals(entry.getUserPassword()));
            Assert.assertTrue(objectFromHD.getValidUntil().equals(entry.getValidUntil()));
            Assert.assertTrue(objectFromHD.getValidUsers().equals(entry.getValidUsers()));
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
//    @Test
    public void vnlMapperTest(){
        LicenseManagementEntry entry = getSingleRandomInstance();
        // dbid is not considered by marshaller and unmarshalling sets 0 
        // as default for non-mapped property, so set it here also
        entry.setDbId(0);
        byte[] vnlData = VNLMapper.getInstance().marshalLicenseManagementEntry(entry);
        LicenseManagementEntry serializedEntry = VNLMapper.getInstance().unmarshalXML(vnlData);
        
        try {
            vnlData = FileUtils.readFileToByteArray(new File("/tmp/veriniceTest4150224110772808346vnl"));
            LicenseManagementEntry brokenEntry = VNLMapper.getInstance().unmarshalXML(vnlData);
            brokenEntry.hashCode();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        Assert.assertTrue(entry.equals(serializedEntry));
        
    }

}
