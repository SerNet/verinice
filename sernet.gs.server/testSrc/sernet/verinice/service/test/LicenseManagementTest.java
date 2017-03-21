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
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.bind.JAXB;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sernet.verinice.hibernate.LicenseManagementEntryDao;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.VNLMapper;
import sernet.verinice.model.licensemanagement.hibernate.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.propertyconverter.PropertyConverter;
import sernet.verinice.service.commands.ExportFactory;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementTest extends ContextConfiguration{
    
    private static final Logger LOG = Logger.getLogger(LicenseManagementTest.class);
    
    @Resource(name = "licenseManagementDao")
    protected LicenseManagementEntryDao elementDao;
    
    @Resource(name = "licenseManagementService")
    ILicenseManagementService licenseManagementService;
    
    @Resource(name = "encryptionService")
    IEncryptionService cryptoService;
    
    private final static String CONTENTID = "licenseTestContent";
    
    private final static int AMOUNT_OF_TESTDATA = 100;
    
    private final static List<String> CONTENTID_TESTDATA = new ArrayList<>();
    
    private final static String TEST_USERNAME = "dd";
    
    private final static List<String> ALL_USER_NAMES = 
            Arrays.asList(new String[]{"bb", "cc", "dd", "ee"});
    
    private final static String CRYPTO_CONTENT_ID = "ISO27k1-Risk-Catalogue-2016";
    private final static String CRYPTO_LICENSE_ID = "sernetIso27k1-License";
    private final static String cryptoSalt = new String(
            new byte[]{0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48});
    private final static String cryptoPassword = "superSecretPassword";
    private final static Date CRYPTO_VALID_UNTIL = new Date();
    private final static int CRYPTO_VALID_USERS = 5;
    
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
    
//    @After
    public void cleanUp(){
        // regarding to crypto usage, this wont work anymore
        for (int j = 0; j < CONTENTID_TESTDATA.size(); j++){
            for (LicenseManagementEntry entry : elementDao.findByContentIdentifier(
                    CONTENTID_TESTDATA.get(j))){
                elementDao.delete(entry);
            }
        }
        elementDao.flush();
        elementDao.clear();
    }
    
    private void createTestData(final int amount){
        for (int i = 0; i < amount; i++){
            LicenseManagementEntry entry = getSingleCryptedEntry();
            elementDao.merge(entry);
        }
        elementDao.flush();
    }
    
    
    private LicenseManagementEntry getSingleCryptedEntry(){

        
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(cryptoService.encrypt(
                CRYPTO_CONTENT_ID, cryptoPassword.toCharArray(), cryptoSalt));
        entry.setLicenseID(cryptoService.encrypt(
                CRYPTO_LICENSE_ID, cryptoPassword.toCharArray(), cryptoSalt));
        entry.setSalt(cryptoSalt);
        entry.setUserPassword(cryptoPassword);
        entry.setValidUntil(cryptoService.encrypt(String.valueOf(
                CRYPTO_VALID_UNTIL.getTime()), cryptoPassword.toCharArray(), cryptoSalt));
        entry.setValidUsers(cryptoService.encrypt(String.valueOf(
                CRYPTO_VALID_USERS), cryptoPassword.toCharArray(), cryptoSalt));
        
        return entry;
    }
    
    
    @Test
    public void testIfUserIsValidForLicenseId(){
        // check why this lasts so long (currently 192s )
        try {
            Set<LicenseManagementEntry> licenseEntries = 
                    licenseManagementService.getLicenseEntriesForContentId(
                            CONTENTID_TESTDATA.get(0), false);

            for (LicenseManagementEntry entry : licenseEntries){
                String licenseId = licenseManagementService.
                        decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
                licenseManagementService.addLicenseIdAuthorisation(
                        TEST_USERNAME, licenseId);
                Assert.assertTrue(licenseManagementService.
                        isCurrentUserValidForLicense(TEST_USERNAME, licenseId, false));

                Assert.assertTrue("user " + TEST_USERNAME 
                        + " is no longer authorised to use content from :\t" 
                        + entry.getContentIdentifier(), 
                        licenseManagementService.
                        isUserAssignedLicenseStillValid(TEST_USERNAME, licenseId, false));
            }
        } catch (LicenseManagementException e){
            LOG.error("something went wrong with content-licensing", e);
        } catch (CommandException e){
            LOG.error("Error while adding a license to a user", e);
        }

    }
    
    @Test
    public void testResetLicenseAssignments(){
        
        createTestData(20);
        
        String contentId = CONTENTID_TESTDATA.get(RandomUtils.
                nextInt(CONTENTID_TESTDATA.size() - 1));
        
        try {
            for (String licenseId : licenseManagementService.
                    getLicenseIdsForContentId(contentId, false)){
                licenseManagementService.
                addLicenseIdAuthorisation(ALL_USER_NAMES.get(RandomUtils.
                        nextInt(ALL_USER_NAMES.size() - 1)), licenseId);
            }

            Assert.assertTrue(licenseManagementService.
                    getContentIdAllocationCount(contentId) > 0);

            licenseManagementService.removeAllContentIdAssignments(contentId);

            Assert.assertTrue(licenseManagementService.
                    getContentIdAllocationCount(contentId) == 0);
        } catch (LicenseManagementException e){

        } catch (CommandException e) {
            LOG.error("Error granting license to user", e);
        }
    }
    
    @Test
    public void testResetUserAssignmentsByContentId(){
        
        try {
            for (String licenseId : licenseManagementService.
                    getLicenseIdsForContentId(CONTENTID, false)){
                try {
                    licenseManagementService.addLicenseIdAuthorisation(TEST_USERNAME, licenseId);
                } catch (CommandException e) {
                    LOG.error("Error granting license to user", e);
                }
            }

            licenseManagementService.removeContentIdUserAssignment(TEST_USERNAME, CONTENTID);

            Assert.assertFalse(licenseManagementService.
                    getAuthorisedContentIdsByUser(TEST_USERNAME).contains(CONTENTID));
        } catch (LicenseManagementException e){
            LOG.error("Something went wrong with removing licenses", e);
        }
    }
    
    @Test
    public void testRemovingUsersFromLicenseId(){
        
        
        LicenseManagementEntry firstEntry = getSingleCryptedEntry();
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
        
        try {
        for (String username : ALL_USER_NAMES){
                licenseManagementService.addLicenseIdAuthorisation(username, 
                        firstEntry.getLicenseID());
                licenseManagementService.addLicenseIdAuthorisation(username, 
                        secondEntry.getLicenseID());
        }
        
        Assert.assertTrue(licenseManagementService.getAllLicenseIds(false).size() >= 2);
        
        Assert.assertTrue("more users assigned than allowed by licenseEntry", 
                licenseManagementService.hasLicenseIdAssignableSlots(
                        firstEntry.getLicenseID()));
        Assert.assertTrue("more users assigned than allowed by licenseEntry", 
                licenseManagementService.hasLicenseIdAssignableSlots(
                        secondEntry.getLicenseID()));
        
        int countFor2 = licenseManagementService.getContentIdAllocationCount(
                firstEntry.getContentIdentifier());
        
        licenseManagementService.removeAllUsersForLicense(firstEntry.getLicenseID());
        
        int countFor1 = licenseManagementService.getContentIdAllocationCount(
                firstEntry.getContentIdentifier());
        Assert.assertTrue("removing user assignments for licenseId " 
                + firstEntry.getLicenseID() + " failed", countFor1 < countFor2);

        } catch (LicenseManagementException e){
            LOG.error("Something went wrong with removing licenses from users", e);
        } catch (CommandException e) {
            LOG.error("Error granting license to user", e);
        }
    }
    
    @Test
    public void daoTest(){
        String plainContentIdentifier = "content123";
        String plainLicenseId = "uniqueLicenseId";
        String userPassword = "secretPassword";
        String salt = cryptoSalt;
        Date plainValidUntil = new Date();
        int plainValidUsers = 5;
        Assert.assertNull(elementDao.findByLicenseId(plainLicenseId));
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(cryptoService.encrypt(plainContentIdentifier,
                userPassword.toCharArray(), salt));
        entry.setLicenseID( plainLicenseId);
        entry.setSalt(salt);
        entry.setUserPassword(userPassword);
        entry.setValidUntil(String.valueOf(plainValidUntil.getTime()));
        entry.setValidUsers(String.valueOf(plainValidUsers));
        
        elementDao.merge(entry);
        
        elementDao.flush();
        
        LicenseManagementEntry persistedObject = elementDao.
                findByLicenseId(plainLicenseId);
        Assert.assertEquals(entry.getContentIdentifier(), 
                persistedObject.getContentIdentifier());
        Assert.assertEquals(entry.getLicenseID(), persistedObject.getLicenseID());
        Assert.assertEquals(entry.getSalt(), persistedObject.getSalt());
        Assert.assertEquals(entry.getUserPassword(), persistedObject.getUserPassword());
        Assert.assertEquals(entry.getValidUntil(), persistedObject.getValidUntil());
        Assert.assertEquals(entry.getValidUsers(), persistedObject.getValidUsers());
        
        elementDao.delete(persistedObject);
        elementDao.flush();
        
        Assert.assertNull(elementDao.findByLicenseId(plainLicenseId));

        
    }
    
    @Test
    public void serviceTest(){
        try {
            Set<String> allIds = licenseManagementService.getAllContentIds(true);
            for (int j = 0; j < CONTENTID_TESTDATA.size(); j++){
                Assert.assertTrue(allIds.contains(CONTENTID_TESTDATA.get(j)));
            }
        }   catch (LicenseManagementException e){
            LOG.error("Error getting license Data contentId", e);
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
        
        de.sernet.model.licensemanagement.LicenseManagementEntry xml = 
                new de.sernet.model.licensemanagement.LicenseManagementEntry();
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
            de.sernet.model.licensemanagement.LicenseManagementEntry 
            objectFromHD = JAXB.unmarshal(is, de.sernet.model.licensemanagement.
                    LicenseManagementEntry.class);
            
            Assert.assertTrue(objectFromHD.getContentIdentifier().equals(
                    entry.getContentIdentifier()));
            Assert.assertTrue(objectFromHD.getLicenseID().equals(entry.getLicenseID()));
            Assert.assertTrue(objectFromHD.getSalt().equals(entry.getSalt()));
            Assert.assertTrue(objectFromHD.getUserPassword().equals(entry.getUserPassword()));
            Assert.assertTrue(objectFromHD.getValidUntil().equals(entry.getValidUntil()));
            Assert.assertTrue(objectFromHD.getValidUsers().equals(entry.getValidUsers()));
            
        } catch (IOException e) {

            
        }
    }
   
    @Test
    public void removeAllTest(){
        CONTENTID_TESTDATA.add("podolski");
        CONTENTID_TESTDATA.add("blafasel");
        CONTENTID_TESTDATA.add("blafasel23");
        
        try {
            for (String contentId : CONTENTID_TESTDATA){
                for (String licenseId : licenseManagementService.
                        getLicenseIdsForContentId(contentId, false)){
                    licenseManagementService.removeAllLicenseIdAssignments(licenseId);
                }
            }

            for (String contentId : CONTENTID_TESTDATA){
                Assert.assertTrue (0 == licenseManagementService.
                        getContentIdAllocationCount(contentId));
            }
        } catch (LicenseManagementException e){
            LOG.error("Error dealing with license/vnl-Data", e);
        }
    }
    
    @Test
    public void testUserMgmtInService(){
        
        LicenseManagementEntry entry = getSingleCryptedEntry();
        entry = elementDao.merge(entry);
        
        String localLicenseId  = entry.getLicenseID();
        
        try {
            licenseManagementService.addLicenseIdAuthorisation(TEST_USERNAME, localLicenseId);
            licenseManagementService.addLicenseIdAuthorisation(TEST_USERNAME, localLicenseId);
        
        Set<String> idsFromDb = licenseManagementService.
                getAuthorisedContentIdsByUser(TEST_USERNAME);
        Assert.assertEquals(1, licenseManagementService.
                getContentIdAllocationCount(entry.getContentIdentifier()));
        
        Assert.assertTrue(idsFromDb.contains(localLicenseId));
        
        elementDao.delete(entry);
        } catch (CommandException e){
            LOG.error("Error granting license to user", e);
        } catch (LicenseManagementException e){
            LOG.error("Error dealing with license-/vnl data", e);
        }
    }
    
    @Test
    public void vnlMapperTest(){
        LicenseManagementEntry entry = getSingleCryptedEntry();
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
            LicenseManagementEntry entryFromDisk = VNLMapper.getInstance().
                    unmarshalXML(bytesFromDisk);
            Assert.assertEquals(entry, entryFromDisk);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        LicenseManagementEntry serializedEntry = VNLMapper.getInstance().unmarshalXML(vnlData);
        
        Assert.assertEquals(entry, serializedEntry);
        
        Assert.assertEquals(CRYPTO_CONTENT_ID, cryptoService.decrypt(
                serializedEntry.getContentIdentifier(),
                serializedEntry.getUserPassword().toCharArray(),
                serializedEntry.getSalt()));
        
        Assert.assertEquals(CRYPTO_LICENSE_ID, cryptoService.decrypt(
                serializedEntry.getLicenseID(),
                serializedEntry.getUserPassword().toCharArray(),
                serializedEntry.getSalt()));

        Assert.assertEquals(CRYPTO_VALID_UNTIL, new Date(Long.parseLong(cryptoService.decrypt(
                serializedEntry.getValidUntil(),
                serializedEntry.getUserPassword().toCharArray(),
                serializedEntry.getSalt()))));
        
        Assert.assertEquals(CRYPTO_VALID_USERS, Integer.parseInt(cryptoService.decrypt(
                serializedEntry.getValidUsers(),
                serializedEntry.getUserPassword().toCharArray(),
                serializedEntry.getSalt())));

        Assert.assertTrue(entry.equals(serializedEntry));
        
    }
    
    @Test
    public void cryptoServiceTest(){
        testPlainCryptoFunctionality();
        LicenseManagementEntry cryptedEntry = getSingleCryptedEntry();
    }
    

    
    

    /**
     * 
     */
    private void testPlainCryptoFunctionality() {
        final String plainText = "Sometimes you are the dog, sometimes you are the tree";
        int passwordAndSaltLength = 8;
        while (passwordAndSaltLength == 0){
            passwordAndSaltLength = RandomUtils.nextInt(8);
        }
        String salt = RandomStringUtils.randomAlphanumeric(passwordAndSaltLength);
        String password = RandomStringUtils.randomAlphanumeric(passwordAndSaltLength);
        String cypherText = cryptoService.encrypt(plainText, password.toCharArray(), salt);
        
        String decryptedText = cryptoService.decrypt(cypherText, password.toCharArray(), salt);
        
        Assert.assertEquals(plainText, decryptedText);
    }
    
    @Test
    public void testPropertyConverter(){
        PropertyConverter converter = new PropertyConverter();
        Object intObject = converter.convertToInteger("5");
        Assert.assertTrue(intObject instanceof Integer);
        Date d = new Date();
        Object dateObject = converter.convertToDate(converter.convertToLong(d));
        Assert.assertTrue(dateObject instanceof Date);
        Object longObject = converter.convertToLong(intObject);
        Assert.assertTrue(longObject instanceof Long);
        longObject = converter.convertToLong(dateObject);
        Assert.assertTrue(longObject instanceof Long);
        Object stringObject = converter.convertToString(dateObject);
        Assert.assertTrue(stringObject instanceof String);
    }

}
