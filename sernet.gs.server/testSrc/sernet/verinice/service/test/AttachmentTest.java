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
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import sernet.gs.ui.rcp.main.service.crudcommands.DeleteNote;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Addition;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.LoadAttachmentFile;
import sernet.verinice.service.commands.LoadAttachments;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SaveAttachment;
import sernet.verinice.service.commands.SaveNote;

/**
*
*/

public class AttachmentTest extends CommandServiceProvider {
    
    private static final Logger LOG = Logger.getLogger(AttachmentTest.class);
    
    @Resource(name="additionDAO")
    private IBaseDao<Addition, Integer> additionDao;
    
    @Test
    public void createAndDeleteAttachment() throws CommandException, IOException{
        // pre-setup 
        Organization org = createOrganization();
        checkOrganization(org);
        assertNotNull(org);
        
        // create the file to be attached
        Attachment a = createAndSave(org);
        
        // load attachment and file from db and check 
        loadAndCheck(org);
        
        // clean up by deleting attachment and org
        cleanUp(org, a);
    }

    private void loadAndCheck(Organization org) throws CommandException {
        Attachment attachmentFromDB = loadAttachmentFromDb(org);
        assertNotNull(attachmentFromDB);
        
        AttachmentFile fileFromDB = loadFileDataFromDB(attachmentFromDB);
        assertNotNull(fileFromDB);
    }

    private Attachment createAndSave(Organization org) throws IOException, FileNotFoundException, CommandException {
        File f = File.createTempFile("veriniceAttachment", "test");
        f.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        raf.setLength(1024*1024); // create 1mb of trash data
        assertNotNull(f);
        assertNotNull(raf);
        
        // create AttachmentObject
        Attachment a = createAttachment(org, f);
        
        // save attachment
        a = saveAttachment(a);
        assertNotNull(a);
        
        // create and save file to attachment
        attachFileData(f, a);
        return a;
    }

    private void cleanUp(Organization org, Attachment a) throws CommandException {
        int attachmentDbId = a.getDbId();
        DeleteNote deleteNote = new DeleteNote(a);
        deleteNote = commandService.executeCommand(deleteNote);
        Addition addition = null;
        try{
            addition = additionDao.findById(attachmentDbId);
        } catch (DataIntegrityViolationException e){
            LOG.debug("Element not found, but that was expected here");
        }
        assertNull(addition);
        checkOrganization(org);
        deleteElement(org);
    }

    private AttachmentFile loadFileDataFromDB(Attachment attachmentFromDB) throws CommandException {
        LoadAttachmentFile fileLoader = new LoadAttachmentFile(attachmentFromDB.getDbId());
        fileLoader = commandService.executeCommand(fileLoader);
        AttachmentFile fileFromDB = fileLoader.getAttachmentFile();
        return fileFromDB;
    }

    private Attachment loadAttachmentFromDb(Organization org) throws CommandException {
        LoadAttachments attachmentLoader = new LoadAttachments(org.getDbId());
        attachmentLoader = commandService.executeCommand(attachmentLoader);
        List<Attachment> listFromDB = attachmentLoader.getAttachmentList();
        assertNotNull(listFromDB);
        assertNotSame(0, listFromDB.size());
        Attachment attachmentFromDB = listFromDB.get(0);
        return attachmentFromDB;
    }

    private void attachFileData(File f, Attachment a) throws CommandException {
        AttachmentFile attachmentFile = new AttachmentFile();
        attachmentFile.setFileData(getFileContent(f));
        SaveAttachment saveFileCommand = new SaveAttachment(attachmentFile);
        attachmentFile.setDbId(a.getDbId());
        saveFileCommand = commandService.executeCommand(saveFileCommand);
        saveFileCommand.clear();
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
        a.setText("Automated Text by Unittest: " + AttachmentTest.class.getCanonicalName());
        return a;
    }
    
    private byte[] getFileContent(File f){
        FileInputStream fis=null;
        
        byte[] bFile = new byte[(int) f.length()];
 
        try {
            fis = new FileInputStream(f);
            fis.read(bFile);
            fis.close();
 
 
        }catch(Exception e){
            LOG.error("Error reading file content into byte[]",e );
        }
        assertNotNull(bFile);
        assertNotSame(0, bFile.length);
        return bFile;
    }
    
    /**
     * deletes given element from db and referencing validation elements
     * @param element
     */
    private void deleteElement(CnATreeElement element){
        assertNotNull(element);
        String uuid = element.getUuid();
        RemoveElement<CnATreeElement> deleteElement = new RemoveElement<CnATreeElement>(element);
        try {
            commandService.executeCommand(deleteElement);
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(element.getUuid());
            command = commandService.executeCommand(command);
            CnATreeElement e2 = command.getElement();
            assertNull("Organization was not deleted.", e2);
        } catch (CommandException e) {
            LOG.error("Error while deleting element", e);
        }
        LOG.debug("Element " + uuid + " deleted");
    }

}
