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
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.bind.JAXB;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import sernet.gs.service.VeriniceCharset;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.UsernameExistsException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.licensemanagement.LicenseManagementEntry;
import sernet.verinice.model.licensemanagement.LicenseManagementException;
import sernet.verinice.model.licensemanagement.LicenseMessageInfos;
import sernet.verinice.model.licensemanagement.propertyconverter.PropertyConverter;
import sernet.verinice.service.account.AccountSearchParameter;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.ExportFactory;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SaveConfiguration;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.commands.UpdateElementEntity;
import sernet.verinice.service.commands.crud.PrepareObjectWithAccountDataForDeletion;
import sernet.verinice.service.crypto.PasswordBasedEncryption;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class LicenseManagementTier3Test extends BeforeEachVNAImportHelper{
    
    private static final Logger LOG = Logger.getLogger(LicenseManagementTier3Test.class);
    
    private static final String VNA_FILENAME = "LicenseMgmt.vna";
    
    @Resource(name = "licenseManagementService")
    ILicenseManagementService licenseManagementService;
    
    @Resource(name = "encryptionService")
    IEncryptionService cryptoService;
    
    @Resource(name = "accountService")
    IAccountService accountService;
    
    private final static String TEST_USERNAME = "dd";
    
    private final static String CONTENT_ID = "verinice";
    private final static String LICENSE_ID = "verinice";
    private final static String cryptoSalt = "verinice";
    private final static String cryptoPassword = "verinice";
    private final static LocalDate VALID_UNTIL = LocalDate.now();
    private final static int VALID_USERS = 5;
    private final static String TEMP_FILE_NAME = "vnlTest";
    private final static String TEMP_FILE_NAME_EXT = ".vnl";
    private final static String VNL_DIR = "./vnl/";
    
    private Organization accountOrg = null;
      
    private boolean removeAccountOrg() throws CommandException{
        if(accountOrg != null){
            PrepareObjectWithAccountDataForDeletion removeAccount = new PrepareObjectWithAccountDataForDeletion(accountOrg);
            commandService.executeCommand(removeAccount);
            RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(accountOrg);
            commandService.executeCommand(removeCommand);
            return true;
        }
        return false;
    }
    
    @BeforeClass
    public static void setUpBeforeClass() throws MalformedURLException {
        // nothing to do
    }
    
    @Override
    public void tearDown() throws CommandException {
        super.tearDown();
        Assert.assertTrue(emptyVNLRepo());
        Assert.assertTrue(removeAccountOrg());
    }
    
    private boolean emptyVNLRepo() {
        boolean deleteSuccessful = true;
        Collection<File> vnlFiles = FileUtils.listFiles(new File(VNL_DIR), 
                new String[]{ILicenseManagementService
                        .VNL_FILE_EXTENSION}, false);
        for (File vnlFile : vnlFiles) {
            deleteSuccessful &= vnlFile.delete();
        }
        return deleteSuccessful;
    }
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        createTestOrganization();
    }
    
    private LicenseManagementEntry getSingleCryptedEntry(){

        
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(cryptoService.encrypt(
                CONTENT_ID, cryptoPassword.toCharArray(), cryptoSalt));
        entry.setLicenseID(cryptoService.encrypt(
                LICENSE_ID, cryptoPassword.toCharArray(), cryptoSalt));
        entry.setSalt(new String(Base64.encodeBase64(cryptoSalt.getBytes(VeriniceCharset.CHARSET_UTF_8))));
        entry.setUserPassword(new String(Base64.encodeBase64(cryptoPassword.getBytes(VeriniceCharset.CHARSET_UTF_8))));
        entry.setValidUntil(cryptoService.encrypt(String.valueOf(
                VALID_UNTIL.toString()), cryptoPassword.toCharArray(), cryptoSalt));
        entry.setValidUsers(cryptoService.encrypt(String.valueOf(
                VALID_USERS), cryptoPassword.toCharArray(), cryptoSalt));
        
        return entry;
    }
    
    @Test
    public void testGetLicenses() throws URISyntaxException, LicenseManagementException{
        int vnlFileCount = 0;
        try {
            for (File vnlFile : getTestVNLFiles()){
                licenseManagementService.addVNLToRepository(vnlFile);
                vnlFileCount++;
            }
            Set<LicenseManagementEntry> entries =
                    licenseManagementService.getExistingLicenses();
            Assert.assertTrue(entries.size() == vnlFileCount);
        } catch (LicenseManagementException e) {
            LOG.error("Something went wrong", e);
        }
    }
    
    private List<File> getTestVNLFiles() throws URISyntaxException, LicenseManagementException{
        List<File> vnlFiles = new ArrayList<>();
        File vnlDir = licenseManagementService.getVNLRepository();
        if(vnlDir.exists() && vnlDir.isDirectory()) {
            Collection<File> vnlCollection = FileUtils.listFiles(vnlDir, 
                    new String[]{ILicenseManagementService
                            .VNL_FILE_EXTENSION}, false);
            vnlFiles.addAll(vnlCollection);
        }
        return vnlFiles;
    }
    
    @Test
    public void deEncrypt(){
        LicenseManagementEntry entry = getSingleCryptedEntry();
        String decryptedEntryContentId = 
                licenseManagementService.decrypt(entry, 
                        LicenseManagementEntry.COLUMN_CONTENTID);
        Assert.assertEquals(decryptedEntryContentId, CONTENT_ID);
    }
    
    @Test
    public void licenseEntriesForUserByContentId(){
        try{
            String encryptedContentId = getDynamicEncryptedString(CONTENT_ID, 
                    cryptoPassword.toCharArray(), cryptoSalt);
                    
            int count = 0;
            for (LicenseManagementEntry entry : licenseManagementService.
                    getExistingLicenses()){
                String plainContentId = licenseManagementService.decrypt(
                        entry, LicenseManagementEntry.COLUMN_CONTENTID);
                if (CONTENT_ID.equals(plainContentId)){
                    String plainLicenseId = licenseManagementService.decrypt(
                            entry, LicenseManagementEntry.COLUMN_LICENSEID);
                    licenseManagementService.addLicenseIdAuthorisation(
                            getConfiguration(TEST_USERNAME), plainLicenseId);
                    count++;
                }
            }
                    
            Set<LicenseManagementEntry> entriesForUser =
            licenseManagementService.getLicenseEntriesForUserByContentId(
                    TEST_USERNAME, encryptedContentId);
            Assert.assertTrue(entriesForUser.size() == count);
        } catch (LicenseManagementException e){
            LOG.error("Something went wrong getting licenseEntries for user:\t"
                    + TEST_USERNAME + "and contentId:\t" + CONTENT_ID);
        } catch (CommandException e){
            LOG.error("Something went wrong with assigning a licenseId to "
                    + "user:\t" + TEST_USERNAME);
        }
    }
    
    @Test
    public void entryByLicenseId() throws IOException, LicenseManagementException{
        File repoFile = null;
        try {
            LicenseManagementEntry entry = getSingleCryptedEntry();  
            repoFile = addLicenseToRepository(entry);
            LicenseManagementEntry entryFromRepo = 
                    licenseManagementService.getLicenseEntryForLicenseId(
                            entry.getLicenseID(), false);
            Assert.assertTrue(entry.equals(entryFromRepo));
        } finally {
            FileUtils.forceDelete(repoFile);
        }
    }
    
    private File addLicenseToRepository(LicenseManagementEntry entry) throws LicenseManagementException, IOException {
        File vnlFile = null;
        try {
            vnlFile = writeLMEntryToTempFile(entry, TEMP_FILE_NAME,
                    TEMP_FILE_NAME_EXT);
            return licenseManagementService.addVNLToRepository(vnlFile);
        } finally {
            FileUtils.forceDelete(vnlFile);
        }
    }
    
    public void licensingInfos(){
        try {
            Set<LicenseMessageInfos> allInfos = 
                    licenseManagementService.getAllLicenseMessageInfos();
            
            Assert.assertTrue(allInfos.size() == licenseManagementService.
                    getExistingLicenses().size());
            LicenseManagementEntry expiredEntry = createAndAddExpiredEntryToRepository();
            LicenseManagementEntry validEntry = getSingleCryptedEntry();
            
            LocalDate validDate = LocalDate.now().plusDays(10);
            validEntry.setValidUntil(cryptoService.encrypt(String.valueOf(
                    validDate.toString()),
                    getUserPassword(validEntry),
                    decodeEntryProperty(validEntry.getSalt())));
            
            String licenseIdExpired = licenseManagementService.decrypt(expiredEntry, 
                    LicenseManagementEntry.COLUMN_LICENSEID);
            String plainContentIdExpired = licenseManagementService.decrypt(
                    expiredEntry, LicenseManagementEntry.COLUMN_CONTENTID);

            String licenseIdValid = licenseManagementService.decrypt(validEntry, 
                    LicenseManagementEntry.COLUMN_LICENSEID);
            String plainContentIdValid = licenseManagementService.decrypt(
                    validEntry, LicenseManagementEntry.COLUMN_CONTENTID);

            String encryptedContentIdExpired = getDynamicEncryptedString(
                    plainContentIdExpired,
                    getUserPassword(expiredEntry), 
                    decodeEntryProperty(expiredEntry.getSalt()));            
            String encryptedContentIdValid = getDynamicEncryptedString(
                    plainContentIdExpired,
                    getUserPassword(validEntry), 
                    decodeEntryProperty(validEntry.getSalt()));
            
            try {
                licenseManagementService.addLicenseIdAuthorisation(
                        getConfiguration(TEST_USERNAME), licenseIdExpired);
                licenseManagementService.addLicenseIdAuthorisation(
                        getConfiguration(TEST_USERNAME), licenseIdValid);
                
            } catch (CommandException e) {
                LOG.error("Error while assigning license to user", e);
            }
            
            LicenseMessageInfos infosExpired = 
            licenseManagementService.getLicenseMessageInfos(TEST_USERNAME, 
                    encryptedContentIdExpired, "", null);
            LicenseMessageInfos infosValid = 
            licenseManagementService.getLicenseMessageInfos(TEST_USERNAME, 
                    encryptedContentIdValid, "", null);
            Assert.assertTrue(infosExpired.isNoLicenseAvailable());
            Assert.assertTrue(infosValid.isInvalidSoon());
            Assert.assertTrue(infosValid.getAssignedUsers() == 1);
            Assert.assertTrue(plainContentIdValid.equals(infosValid.getContentId()));
            Assert.assertTrue(licenseManagementService.isLicenseInvalidSoon(
                    TEST_USERNAME, encryptedContentIdValid));
            
        } catch (LicenseManagementException e) {
            LOG.error("dealing with licenseMessageInfos went wrong", e);
        }
    }
    
    @Test
    public void isRepoExistant(){
        File vnlRepo;
        try {
            vnlRepo = licenseManagementService.getVNLRepository();
            Assert.assertTrue(vnlRepo.exists());
            Assert.assertTrue(vnlRepo.isDirectory());
        } catch (LicenseManagementException e) {
            LOG.error("Error while determing vnl-Repository", e);
        }
    }
    
    private String getDynamicEncryptedString(String plainValue, 
            char[] password, String salt) {
        byte[] plainTextBytes = plainValue.getBytes(VeriniceCharset.CHARSET_UTF_8);
        byte[] saltBytes = salt.getBytes();
        byte[] cypherTextBytes = PasswordBasedEncryption.encrypt(plainTextBytes,
                password, saltBytes, true);
        String encryptedValue = new String(org.apache.commons.codec.binary.Base64.encodeBase64(cypherTextBytes));
        return encryptedValue;
    }
    
    private LicenseManagementEntry createAndAddExpiredEntryToRepository(){
        LicenseManagementEntry entry = getSingleCryptedEntry();
        LocalDate expiredDate = LocalDate.now().minusDays(1L);
        entry.setValidUntil(cryptoService.encrypt(String.valueOf(
                expiredDate.toString()), cryptoPassword.toCharArray(), cryptoSalt));
        File repoFile = writeLMEntryToTempFile(entry, TEMP_FILE_NAME, TEMP_FILE_NAME_EXT);
        repoFile.deleteOnExit();
        return entry;
        
    }
    
    @Test
    public void testIfUserIsValidForLicenseId(){
        // check why this lasts so long (currently 192s )
        try {
            String encryptedContentId = cryptoService.encrypt(CONTENT_ID, cryptoPassword.toCharArray(), cryptoSalt);
            Set<LicenseManagementEntry> licenseEntries = 
                    licenseManagementService.getLicenseEntriesForContentId(
                            encryptedContentId, false);

            for (LicenseManagementEntry entry : licenseEntries){
                String licenseId = licenseManagementService.
                        decrypt(entry, LicenseManagementEntry.COLUMN_LICENSEID);
                licenseManagementService.addLicenseIdAuthorisation(
                        getConfiguration(TEST_USERNAME), licenseId);

                
                LocalDate validUntil = licenseManagementService.
                        decrypt(entry, LicenseManagementEntry.COLUMN_VALIDUNTIL);
                if (validUntil.isAfter(LocalDate.now())){
                    Assert.assertTrue(licenseManagementService.
                            isCurrentUserValidForLicense(TEST_USERNAME, entry.getLicenseID(), false));
                    Assert.assertTrue("user " + TEST_USERNAME 
                            + " is no longer authorised to use content from :\t" 
                            + entry.getContentIdentifier(), 
                            licenseManagementService.
                            isUserAssignedLicenseStillValid(TEST_USERNAME, entry.getLicenseID(), false));
                } else {
                    Assert.assertFalse(licenseManagementService.
                            isCurrentUserValidForLicense(TEST_USERNAME, entry.getLicenseID(), false));
                    Assert.assertFalse("user " + TEST_USERNAME 
                            + " is no longer authorised to use content from :\t" 
                            + entry.getContentIdentifier(), 
                            licenseManagementService.
                            isUserAssignedLicenseStillValid(TEST_USERNAME, entry.getLicenseID(), false));                    
                }
            }
        } catch (LicenseManagementException e){
            LOG.error("something went wrong with content-licensing", e);
        } catch (CommandException e){
            LOG.error("Error while adding a license to a user", e);
        }

    }
    
    @Test
    public void testResetLicenseAssignments(){
        try {
            for(String encryptedContentId : licenseManagementService.getAllContentIds(false)){
                for (String licenseId : licenseManagementService.
                        getLicenseIdsForContentId(encryptedContentId, false)){
                    licenseManagementService.
                    addLicenseIdAuthorisation(getConfiguration(TEST_USERNAME), licenseId);
                }
                Assert.assertTrue(licenseManagementService.
                        getContentIdAllocationCount(encryptedContentId) > 0);

                licenseManagementService.removeAllContentIdAssignments(encryptedContentId);

                Assert.assertTrue(licenseManagementService.
                        getContentIdAllocationCount(encryptedContentId) == 0);
            }
        } catch (LicenseManagementException e){

        } catch (CommandException e) {
            LOG.error("Error granting license to user", e);
        }
    }

    
    @Test
    public void getEntryFromRepository() throws IOException{
        File repoFile = null;
        try{ 
            repoFile = addLicenseToRepository(getSingleCryptedEntry());                   
            
            String encryptedContentId = cryptoService.encrypt(CONTENT_ID, 
                    cryptoPassword.toCharArray(), cryptoSalt);
            Set<LicenseManagementEntry> entries =
                    licenseManagementService.getLicenseEntriesForContentId(
                            encryptedContentId, false);         
            Assert.assertNotNull(entries);
            Assert.assertTrue(entries.size() > 0);
        String plainContentId = 
                licenseManagementService.decrypt(entries.iterator().next(), 
                        LicenseManagementEntry.COLUMN_CONTENTID);
        Assert.assertEquals(CONTENT_ID, plainContentId);
        
        } catch (LicenseManagementException e){
            LOG.error("Error while getting entry for licenseId:\t" + CONTENT_ID);
        } finally {
            FileUtils.forceDelete(repoFile);
        }
    }
    
    @Test
    public void testRemovingUsersFromLicenseId() throws IOException{
        LicenseManagementEntry firstEntry = getSingleCryptedEntry();
        LicenseManagementEntry secondEntry = new LicenseManagementEntry();
        
        String plainLicenseId2 = LICENSE_ID + "2";
        String encryptedLicenseId1 = cryptoService.encrypt(LICENSE_ID, cryptoPassword.toCharArray(), cryptoSalt);
        String encryptedLicenseId2 = cryptoService.encrypt(plainLicenseId2, cryptoPassword.toCharArray(), cryptoSalt);   
        
        firstEntry.setLicenseID(encryptedLicenseId1);
        
        // ensure both entries do have the same contentid since this is what we need for the test 
        secondEntry.setContentIdentifier(firstEntry.getContentIdentifier());
        secondEntry.setLicenseID(encryptedLicenseId2);
        secondEntry.setSalt(firstEntry.getSalt());
        secondEntry.setUserPassword(firstEntry.getUserPassword());
        secondEntry.setValidUntil(firstEntry.getValidUntil());
        secondEntry.setValidUsers(firstEntry.getValidUsers());
        
        File vnlFile1 = writeLMEntryToTempFile(firstEntry, TEMP_FILE_NAME + "ZWEI", 
                TEMP_FILE_NAME_EXT);
        File vnlFile2 = writeLMEntryToTempFile(secondEntry, TEMP_FILE_NAME +"DREI", 
                TEMP_FILE_NAME_EXT);
        File repoFile1 = null;
        File repoFile2 = null;

        
        try {
            repoFile1 = licenseManagementService.addVNLToRepository(vnlFile1);
            repoFile2 = licenseManagementService.addVNLToRepository(vnlFile2);
            licenseManagementService.addLicenseIdAuthorisation(getConfiguration(TEST_USERNAME), 
                    LICENSE_ID);
            licenseManagementService.addLicenseIdAuthorisation(getConfiguration(TEST_USERNAME), 
                    plainLicenseId2);

            Assert.assertTrue(licenseManagementService.getAllLicenseIds(false).size() >= 2);

            Assert.assertTrue("more users assigned than allowed by licenseEntry", 
                    licenseManagementService.hasLicenseIdAssignableSlots(
                            firstEntry.getLicenseID()));
            Assert.assertTrue("more users assigned than allowed by licenseEntry", 
                    licenseManagementService.hasLicenseIdAssignableSlots(
                            secondEntry.getLicenseID()));

            int contentIDCount = licenseManagementService.getContentIdAllocationCount(
                    secondEntry.getContentIdentifier());

            licenseManagementService.removeAllUsersForLicense(firstEntry.getLicenseID());
        
        int countFor1 = licenseManagementService.getLicenseIdAllocationCount(LICENSE_ID);
        int countFor2 = licenseManagementService.getLicenseIdAllocationCount(plainLicenseId2);
        
        Assert.assertEquals(countFor1, countFor2);
        Assert.assertTrue("removing user assignments for licenseId " 
                + firstEntry.getLicenseID() + " failed", countFor1 < contentIDCount);

        } catch (LicenseManagementException e){
            LOG.error("Something went wrong with removing licenses from users", e);
        } catch (CommandException e) {
            LOG.error("Error granting license to user", e);
        } finally { 
            FileUtils.forceDelete(vnlFile1);
            FileUtils.forceDelete(vnlFile2);
            FileUtils.forceDelete(repoFile1);
            FileUtils.forceDelete(repoFile2);
        }
    }
     
    @Test
    public void serviceTest() throws IOException{
        File repoFile = null;
        try{ 
            repoFile = addLicenseToRepository(getSingleCryptedEntry());                   
            Set<String> allIds = licenseManagementService.getAllContentIds(true);
            Assert.assertTrue(allIds.contains(CONTENT_ID));
        } catch (LicenseManagementException e){
            LOG.error("Error getting license Data contentId", e);
        } finally {
            if(repoFile != null) {
                FileUtils.forceDelete(repoFile);
            }
        }
    }
    
    private de.sernet.model.licensemanagement.LicenseManagementEntry 
        marshalEntryToXMLObject(LicenseManagementEntry entry){
        de.sernet.model.licensemanagement.LicenseManagementEntry xml = 
                new de.sernet.model.licensemanagement.LicenseManagementEntry();
        xml.setE1(entry.getContentIdentifier());
        xml.setE2(entry.getLicenseID());
        xml.setE3(entry.getSalt());
        xml.setE4(entry.getUserPassword());
        xml.setE5(entry.getValidUntil());
        xml.setE6(entry.getValidUsers());
        return xml;
    }
    
    private File writeLMEntryToTempFile(LicenseManagementEntry entry, String filename, 
            String extension){
        File file = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ExportFactory.marshal(marshalEntryToXMLObject(entry), stream);
        try {
            file = File.createTempFile(filename, extension);
            FileUtils.writeByteArrayToFile(file, Base64.encodeBase64(stream.toByteArray()));
            stream.close();
        } catch (Exception e){
            LOG.error("Error handling file",e );
        }
        return file;
    }
    
    @Test
    public void manualMarshallingTest() throws IOException{
        LicenseManagementEntry entry = new LicenseManagementEntry();
        entry.setContentIdentifier(CONTENT_ID);
        entry.setLicenseID("licenseIdBLA");
        entry.setSalt(new String(Base64.encodeBase64("saltBLA".getBytes(VeriniceCharset.CHARSET_UTF_8))));
        entry.setUserPassword(new String(Base64.encodeBase64("passwordBLA".getBytes(VeriniceCharset.CHARSET_UTF_8))));
        entry.setValidUntil("validUntilBLA");
        entry.setValidUsers("validUsersBLA");
        
        File vnlFile1 = writeLMEntryToTempFile(entry, 
                TEMP_FILE_NAME + "EINS", TEMP_FILE_NAME_EXT); 
        
        try {
            byte [] bytesFromHD = FileUtils.readFileToByteArray(vnlFile1);
            byte[] decodedBytes = Base64.decodeBase64(bytesFromHD);
            InputStream is = new ByteArrayInputStream(decodedBytes);
            de.sernet.model.licensemanagement.LicenseManagementEntry 
            objectFromHD = JAXB.unmarshal(is, de.sernet.model.licensemanagement.
                    LicenseManagementEntry.class);
            
            Assert.assertTrue(objectFromHD.getE1().equals(
                    entry.getContentIdentifier()));
            Assert.assertTrue(objectFromHD.getE2().equals(entry.getLicenseID()));
            Assert.assertTrue(objectFromHD.getE3().equals(entry.getSalt()));
            Assert.assertTrue(objectFromHD.getE4().equals(entry.getUserPassword()));
            Assert.assertTrue(objectFromHD.getE5().equals(entry.getValidUntil()));
            Assert.assertTrue(objectFromHD.getE6().equals(entry.getValidUsers()));
            
        } catch (IOException e) {
            LOG.error("Error in Filehandling", e);
        } finally {
            FileUtils.forceDelete(vnlFile1);
            
        }
    }
   
    
//    @Test
    public void testUserMgmtInService(){
        
        LicenseManagementEntry entry = getSingleCryptedEntry();
        
        String localLicenseId  = entry.getLicenseID();
        
        try {
            licenseManagementService.addLicenseIdAuthorisation(getConfiguration(TEST_USERNAME), localLicenseId);
            licenseManagementService.addLicenseIdAuthorisation(getConfiguration(TEST_USERNAME), localLicenseId);
        
        Set<String> idsFromDb = licenseManagementService.
                getAuthorisedContentIdsByUser(TEST_USERNAME);
        Assert.assertEquals(1, licenseManagementService.
                getContentIdAllocationCount(entry.getContentIdentifier()));
        
        Assert.assertTrue(idsFromDb.contains(localLicenseId));
        
        } catch (CommandException e){
            LOG.error("Error granting license to user", e);
        } catch (LicenseManagementException e){
            LOG.error("Error dealing with license-/vnl data", e);
        }
    }
    
    
//    @Test
    public void cryptoServiceTest(){
        testPlainCryptoFunctionality();
    }
    
    private Configuration getConfiguration(String username){
        IAccountSearchParameter parameter = new AccountSearchParameter();
        parameter.setLogin(username);
        return accountService.findAccounts(parameter).get(0);
    }
    
    
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
    
//    @Test
    public void testPropertyConverter(){
        PropertyConverter converter = new PropertyConverter();
        Object intObject = converter.convertToInteger("5");
        Assert.assertTrue(intObject instanceof Integer);
        Date d = new Date();
        Long l = converter.convertToLong(d);
        Assert.assertTrue(l instanceof Long);
        Object dateObject = converter.convertToDate(l);
        Assert.assertTrue(dateObject instanceof LocalDate);
        Object longObject = converter.convertToLong(intObject);
        Assert.assertTrue(longObject instanceof Long);
        longObject = converter.convertToLong(dateObject);
        Assert.assertTrue(longObject instanceof Long);
        Object stringObject = converter.convertToString(dateObject);
        Assert.assertTrue(stringObject instanceof String);
    }
    
    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME).getPath();
    }
    
    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
        return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }
    
    private void saveAccount(Configuration configuration) throws CommandException {
        try {
            SaveConfiguration<Configuration> command = new SaveConfiguration<Configuration>(configuration, false);
            command = commandService.executeCommand(command);
        } catch (UsernameExistsException e) {
            LOG.warn("Account already exists, skipping creation of Configuration", e);
        }
    }
    
    private Configuration createAccount(PersonIso person) throws CommandException {
        CreateConfiguration createConfiguration = new CreateConfiguration(person);
        createConfiguration = commandService.executeCommand(createConfiguration);
        Configuration configuration = createConfiguration.getConfiguration();
        return configuration;
    }
    
    private void createAccount(Group<CnATreeElement> personGroup, IAccountSearchParameter paramter) throws CommandException {
        PersonIso person = (PersonIso) createNewElement(personGroup, PersonIso.class);
        person.setName(paramter.getFirstName());
        if (paramter.getFamilyName() != null) {
            person.setSurname(paramter.getFamilyName());
        }
        saveElement(person);
        Configuration configuration = createAccount(person);
        configuration.setUser(paramter.getLogin());
        if (paramter.isAdmin() != null) {
            configuration.setAdminUser(paramter.isAdmin());
        }
        if (paramter.isScopeOnly() != null) {
            configuration.setScopeOnly(paramter.isScopeOnly());
        }
        saveAccount(configuration);
    }
    
    private void saveElement(CnATreeElement element) throws CommandException {
        UpdateElementEntity<CnATreeElement> updateCommand;
        updateCommand = new UpdateElementEntity<CnATreeElement>(element, ChangeLogEntry.STATION_ID);
        commandService.executeCommand(updateCommand);
    }
    
    private Organization createTestOrganization() throws CommandException {
        accountOrg = createOrganization();

        Group<CnATreeElement> personGroup = getGroupForClass(accountOrg, PersonIso.class);

        IAccountSearchParameter paramter = new AccountSearchParameter();

        paramter = new AccountSearchParameter();
        paramter.setLogin(TEST_USERNAME).setIsAdmin(false).setIsScopeOnly(false).setFamilyName("Duck").setFirstName("Dagobert");
        createAccount(personGroup, paramter);

        return accountOrg;
    }

    public IAccountService getAccountService() {
        return accountService;
    }

    public void setAccountService(IAccountService accountService) {
        this.accountService = accountService;
    }
    
    protected char[] getUserPassword(LicenseManagementEntry entry) {
        String encodedPassword = entry.getUserPassword();
        return decodeEntryProperty(encodedPassword).toCharArray();
    }
    
    protected String decodeEntryProperty(String encodedProperty) {
        byte[] encodedByteArray = encodedProperty.getBytes(VeriniceCharset.CHARSET_UTF_8);
        String decodedProperty = new String(cryptoService.decodeBase64(encodedByteArray), 
                VeriniceCharset.CHARSET_UTF_8);
        return decodedProperty;
        
    }

}
