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
import java.util.Arrays;
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
    
    private final static String TEST_USERNAME = "dd";
    
    private final static List<String> ALL_USER_NAMES = Arrays.asList(new String[]{"bb", "cc", "dd", "ee"});
    
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
    
    private int getRandomNotNullInt(int intervall){
        int random = 0;
        while(random == 0){
            random = RandomUtils.nextInt(intervall);
        }
        return random;
        
    }
    
    private LicenseManagementEntry getSingleRandomInstance(){
        String licenseId = RandomStringUtils.randomAlphabetic(getRandomNotNullInt(32));
        String userPassword = RandomStringUtils.randomAlphanumeric(getRandomNotNullInt(16));
        String salt = RandomStringUtils.randomAlphanumeric(getRandomNotNullInt(64));
        Date validUntil = getRandomDate(2017, 2030);
        int validUsers = getRandomNotNullInt(20);
        
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
    public void testIfUserIsValidForLicenseId(){
        // check why this lasts so long (currently 192s )
        Set<LicenseManagementEntry> licenseEntries = licenseManagementService.getLicenseEntriesForContentId(CONTENTID_TESTDATA.get(0));
        
        for(LicenseManagementEntry entry : licenseEntries){
            String licenseId = entry.getLicenseID();
            licenseManagementService.grantUserToLicense(TEST_USERNAME, licenseId);
            
            Assert.assertTrue(licenseManagementService.isCurrentUserValidForLicense(TEST_USERNAME, licenseId));
            
            Assert.assertTrue("user " + TEST_USERNAME 
                    + " is no longer authorised to use content from :\t" 
                    + entry.getContentIdentifier(), 
                    licenseManagementService.
                    isUserAssignedLicenseStillValid(TEST_USERNAME, licenseId));
        }
        
    }
    
    @Test
    public void testResetLicenseAssignments(){
        
        createTestData(20);
        
        String contentId = CONTENTID_TESTDATA.get(RandomUtils.nextInt(CONTENTID_TESTDATA.size() - 1));
        
        for (String licenseId : licenseManagementService.getLicenseIdsForContentId(contentId)){
            licenseManagementService.grantUserToLicense(ALL_USER_NAMES.get(RandomUtils.nextInt(ALL_USER_NAMES.size() - 1)), licenseId);
        }
        
        Assert.assertTrue(licenseManagementService.getContentIdAllocationCount(contentId) > 0);
        
        licenseManagementService.removeAllContentIdAssignments(contentId);
        
        Assert.assertTrue(licenseManagementService.getContentIdAllocationCount(contentId) == 0);
    }
    
    @Test
    public void testResetUserAssignmentsByContentId(){
        
        for (String licenseId : licenseManagementService.getLicenseIdsForContentId(CONTENTID)){
            licenseManagementService.grantUserToLicense(TEST_USERNAME, licenseId);
        }
        
        licenseManagementService.removeContentIdUserAssignment(TEST_USERNAME, CONTENTID);
        
        Assert.assertFalse(licenseManagementService.getAuthorisedContentIdsByUser(TEST_USERNAME).contains(CONTENTID));
    }
    
    @Test
    public void testRemovingUsersFromLicenseId(){
        
        
        LicenseManagementEntry firstEntry = getSingleRandomInstance();
        LicenseManagementEntry secondEntry = new LicenseManagementEntry();
        
        firstEntry.setValidUsers(String.valueOf(5));
        firstEntry.setLicenseID("blubb");
        
        // ensure both entries do have the same contentid since this is what we need for the test 
        secondEntry.setContentIdentifier(firstEntry.getContentIdentifier());
        secondEntry.setLicenseID("bla");
        secondEntry.setSalt(firstEntry.getSalt());
        secondEntry.setUserPassword(firstEntry.getUserPassword());
        secondEntry.setValidUntil(firstEntry.getValidUntil());
        secondEntry.setValidUsers(firstEntry.getValidUsers());
        
        elementDao.merge(firstEntry);
        elementDao.merge(secondEntry);
        
        for (String username : ALL_USER_NAMES){
            licenseManagementService.addLicenseIdAuthorisation(username, firstEntry.getLicenseID());
            licenseManagementService.addLicenseIdAuthorisation(username, secondEntry.getLicenseID());
        }
        
        Assert.assertTrue(licenseManagementService.getAllLicenseIds().size() >= 2);
        
        Assert.assertTrue("more users assigned than allowed by licenseEntry", licenseManagementService.checkAssignedUsersForLicenseId(firstEntry.getLicenseID()));
        Assert.assertTrue("more users assigned than allowed by licenseEntry", licenseManagementService.checkAssignedUsersForLicenseId(secondEntry.getLicenseID()));
        
        int countFor2 = licenseManagementService.getContentIdAllocationCount(firstEntry.getContentIdentifier());
        
        licenseManagementService.removeAllUsersForLicense(firstEntry.getLicenseID());
        
        int countFor1 = licenseManagementService.getContentIdAllocationCount(firstEntry.getContentIdentifier());
        Assert.assertTrue("removing user assignments for licenseId " + firstEntry.getLicenseID() + " failed", countFor1 < countFor2);
        
    }
    
    @Test
    public void testGetValidUsersForContentId(){
        for(String contentId : CONTENTID_TESTDATA){
            Assert.assertTrue("assigned users for " + contentId + " is 0", licenseManagementService.getValidUsersForContentId(contentId) > 0);
        }
    }
    
    @Test
    public void testGetLicenseIdByDbId(){
        for(LicenseManagementEntry entry : elementDao.findAll()){
            String licenseId = licenseManagementService.getLicenseId(entry.getDbId());
            Assert.assertTrue("entry has no licenseId", licenseId != null && !licenseId.equals(""));
        }
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
            file = File.createTempFile("veriniceTest", ".vnl");
            file.deleteOnExit();
            
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
   
    @Test
    public void removeAllTest(){
        CONTENTID_TESTDATA.add("podolski");
        CONTENTID_TESTDATA.add("blafasel");
        CONTENTID_TESTDATA.add("blafasel23");
        
        for (String contentId : CONTENTID_TESTDATA){
            for (String licenseId : licenseManagementService.getLicenseIdsForContentId(contentId)){
                licenseManagementService.removeAllLicenseIdAssignments(licenseId);
            }
        }
        
        for(String contentId : CONTENTID_TESTDATA){
            Assert.assertTrue (0 == licenseManagementService.getContentIdAllocationCount(contentId));
        }
    }
    
    @Test
    public void testUserMgmtInService(){
        
        LicenseManagementEntry entry = getSingleRandomInstance();
        entry = elementDao.merge(entry);
        
        String localLicenseId  = entry.getLicenseID();
        
        licenseManagementService.addLicenseIdAuthorisation(TEST_USERNAME, localLicenseId);
        licenseManagementService.addLicenseIdAuthorisation(TEST_USERNAME, localLicenseId);
        
        Assert.assertTrue(licenseManagementService.isCurrentUserAuthorizedForLicenseUsage(TEST_USERNAME, localLicenseId));
        
        Set<String> idsFromDb = licenseManagementService.getAuthorisedContentIdsByUser(TEST_USERNAME);
        Assert.assertEquals(1, licenseManagementService.getContentIdAllocationCount(entry.getContentIdentifier()));
        
        
        Assert.assertTrue(idsFromDb.contains(localLicenseId));
        
        elementDao.delete(entry);
    }
    
    @Test
    public void vnlMapperTest(){
        LicenseManagementEntry entry = getSingleRandomInstance();
        // dbid is not considered by marshaller and unmarshalling sets 0 
        // as default for non-mapped property, so set it here also
        entry.setDbId(0);
        byte[] vnlData = VNLMapper.getInstance().marshalLicenseManagementEntry(entry);
        
      
        try {
            File file = File.createTempFile("veriniceTest", ".vnl");
            file.deleteOnExit();
            FileUtils.writeByteArrayToFile(file, vnlData);
            File file2 = new File(file.getAbsolutePath());
            file2.deleteOnExit();
            byte[] bytesFromDisk = FileUtils.readFileToByteArray(file2);
            LicenseManagementEntry entryFromDisk = VNLMapper.getInstance().unmarshalXML(bytesFromDisk);
            Assert.assertEquals(entry, entryFromDisk);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        LicenseManagementEntry serializedEntry = VNLMapper.getInstance().unmarshalXML(vnlData);
        
        Assert.assertEquals(entry, serializedEntry);
        
        
        
        
        Assert.assertTrue(entry.equals(serializedEntry));
        
    }

}
