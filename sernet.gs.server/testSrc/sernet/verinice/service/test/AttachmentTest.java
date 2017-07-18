/*******************************************************************************
 * Copyright (c) 2013 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 *     Daniel Murygin <dm[at]sernet[dot]de> - Test of multiple attachments, MD5 hash sum check
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import sernet.gs.service.FileUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Addition;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.AttachmentFileCreationFactory;
import sernet.verinice.service.commands.LoadAttachmentFile;
import sernet.verinice.service.commands.LoadAttachments;
import sernet.verinice.service.commands.SaveNote;
import sernet.verinice.service.commands.crud.DeleteNote;

/**
* Test class creates <code>numberOfFiles</code> files with a ramdom size
* between 0-10 MB. Each of these files is attached to one organization.
* Test stores the MD5 hash sum of the files in a map before saving it in DB.
* Every attachment is loaded after that and the MD5 hash sum is compared 
* to the values in the map.
*/
public class AttachmentTest extends CommandServiceProvider {
    
    private static final Logger LOG = Logger.getLogger(AttachmentTest.class);
    
    private static final int numberOfFiles = 20;
    private static final int maxFileSizeInMb = 3;
    
    @Resource(name="additionDAO")
    private IBaseDao<Addition, Integer> additionDao;
    
    Map<Integer, String> dbIdHashSumMap = new HashMap<Integer, String>();
    
    @Test
    public void createLoadAndDeleteAttachment() throws Exception{
        // pre-setup 
        Organization org = createOrganization(AttachmentTest.class.getSimpleName());
        checkOrganization(org);
        assertNotNull(org);
        
        for (int i = 0; i < numberOfFiles; i++) {
            createAttachment(org);
        }
        assertSame("Size of dbIdHashSumMap is not: " + numberOfFiles, numberOfFiles, dbIdHashSumMap.size());
        
        // load attachment and file from db and check 
        loadAndCheck(org);
        
        // clean up by deleting attachment and org
        cleanUp(org);
    }
    
    private Attachment createAttachment(Organization org) throws Exception {
        File f = File.createTempFile("veriniceAttachment", "test");
        f.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        long length = Math.round(Math.random() * (1024*1024.0*maxFileSizeInMb));
        raf.setLength(length); // create 1mb of trash data
        assertNotNull(f);
        assertNotNull(raf);
        
        // create AttachmentObject
        Attachment a = createAttachment(org, f);
        
        // save attachment
        a = saveAttachment(a);
        assertNotNull(a);
        
        // create and save file to attachment
        attachFileData(f, a);
        
        String hashSum = FileUtil.getMD5Checksum(f.getAbsolutePath());
        dbIdHashSumMap.put(a.getDbId(), hashSum);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("File created, length: " + length + ", path: " + f.getAbsolutePath() + ", hash sum: " + hashSum);
        }
        
        return a;
    }

    private void loadAndCheck(Organization org) throws Exception {
        List<Attachment> attachments = loadAttachmentFromDb(org);
        assertNotNull("Attachment list is null", attachments);
        assertSame("Number of attachments is not: " + numberOfFiles, numberOfFiles, attachments.size());
        
        for (Attachment attachment : attachments) {
            checkAttachment(attachment);
        }
    }

    private void checkAttachment(Attachment attachment) throws CommandException, IOException, Exception {
        Integer dbId = attachment.getDbId();
        assertNotNull("Attachment from db not found in hash map, db-id: " + dbId, dbIdHashSumMap.get(dbId));
        AttachmentFile fileFromDB = loadFileDataFromDB(attachment);
        assertNotNull("File data not found in DB, db-id: " + dbId, fileFromDB);
        File tempFile = File.createTempFile("veriniceAttachment_" + dbId, "test");
        fileFromDB.writeFileData(tempFile.getAbsolutePath());
        String checkSum = FileUtil.getMD5Checksum(tempFile.getAbsolutePath());
        String checkSumExpected = dbIdHashSumMap.get(dbId);
        assertEquals("MD5 checksum is not: " + checkSumExpected + ", db-id: " + dbId,checkSumExpected , checkSum);
        FileUtils.deleteQuietly(tempFile);
    }

   

    private void cleanUp(Organization org) throws CommandException {
        List<Attachment> attachments = loadAttachmentFromDb(org);
        for (Attachment attachment : attachments) {
            int attachmentDbId = attachment.getDbId();
            DeleteNote deleteNote = new DeleteNote(attachment);
            deleteNote = commandService.executeCommand(deleteNote);
            Addition addition = null;
            try{
                addition = additionDao.findById(attachmentDbId);
            } catch (DataIntegrityViolationException e){
                LOG.debug("Element not found, but that was expected here");
            }
            assertNull("Addition was not deleted.", addition);
        }      
        checkOrganization(org);
        removeOrganization(org);
    }

    private AttachmentFile loadFileDataFromDB(Attachment attachmentFromDB) throws CommandException {
        LoadAttachmentFile fileLoader = new LoadAttachmentFile(attachmentFromDB.getDbId());
        fileLoader = commandService.executeCommand(fileLoader);
        AttachmentFile fileFromDB = fileLoader.getAttachmentFile();
        return fileFromDB;
    }

    private List<Attachment> loadAttachmentFromDb(Organization org) throws CommandException {
        LoadAttachments attachmentLoader = new LoadAttachments(org.getDbId());
        attachmentLoader = commandService.executeCommand(attachmentLoader);
        return attachmentLoader.getAttachmentList();
    }

    private void attachFileData(File f, Attachment a) throws CommandException, IOException {
        AttachmentFileCreationFactory.createAttachmentFile(a, FileUtils.readFileToByteArray(f));
    }

    private Attachment saveAttachment(Attachment a) throws CommandException {
        SaveNote command = new SaveNote(a);     
        command = commandService.executeCommand(command);
        a = (Attachment) command.getAddition();
        return a;
    }

    private Attachment createAttachment(Organization org, File f) throws IOException {
        Attachment a = new Attachment();
        a.setCnATreeElementId(org.getDbId());
        a.setCnAElementTitel(org.getTitle());
        a.setTitel(f.getName());
        a.setDate(Calendar.getInstance().getTime());
        a.setFilePath(f.getCanonicalPath());
        a.setFileSize(String.valueOf(getFileData(f).length));
        a.setText("Automated Text by Unittest: " + AttachmentTest.class.getCanonicalName());
        return a;
    }
    
    private byte[] getFileData(File f){ 
        byte[] bFile = FileUtil.getFileData(f);   
        assertNotNull(bFile);
        assertNotSame(0, bFile.length);
        return bFile;
    }
    


}
