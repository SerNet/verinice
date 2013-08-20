/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.fei.rcp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.validation.IValidationService;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.SaveAttachment;
import sernet.verinice.service.commands.SaveNote;

/**
 * FileElementImportTraverser traverses the file system to import files
 * as verinice objects with attachments.
 * 
 * Every file which is found in a starting folder and it's subfolders
 * is imported as a verinice object with the file as an attachment.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class FileElementImportTraverser extends FileSystemTraverser {

    private static final Logger LOG = Logger.getLogger(FileElementImportTraverser.class);
    
    public static final String CURRENT_DIRECTORY = "CURRENT_DIRECTORY";
    public static final String LINK_TARGET = "LINK_TARGET";
    
    private int numberOfFiles = 0;
    
    private Map<String, CnATreeElement> groupMap;
    
    private ICommandService commandService;
    
    private IValidationService validationService;
    
    /**
     * @param startPath
     * @param target 
     */
    public FileElementImportTraverser(String startPath, Group<CnATreeElement> group) {
        super(startPath);
        groupMap = new Hashtable<String, CnATreeElement>();       
        groupMap.put(startPath.substring(0, startPath.lastIndexOf(File.separatorChar)), group);
        addDirectoryHandler(new DirectoryHandler());
        addDirectoryHandler(new PathFinder());
        addFileHandler(new FileHandler());
    }
       
    public void handleDirectory(File dir, TraverserContext context) {
        if(dir==null) {
            return;
        }
        try {
            CnATreeElement parentGroup = groupMap.get(dir.getParent());
            CreateElement<CnATreeElement> saveCommand = new CreateElement<CnATreeElement>(parentGroup, parentGroup.getTypeId(), dir.getName());
            saveCommand.setInheritAuditPermissions(true);
            saveCommand = getCommandService().executeCommand(saveCommand);    
            CnATreeElement newElement = saveCommand.getNewElement();
            groupMap.put(dir.getPath(), newElement);
            
            context.addProperty(CURRENT_DIRECTORY, newElement);
            
            CnAElementFactory.getModel(newElement).childAdded(parentGroup, newElement);
            CnAElementFactory.getModel(newElement).databaseChildAdded(newElement);
            
            if(Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)){
                validateElement(newElement);
            }
        } catch (Exception e) {
            LOG.error("Erro while handle directory: " + dir.getPath(), e);
        }
    }
    
    public void handleFile(File file, TraverserContext context) {
        try {
            if(PathFinder.PROPERTY_FILE_NAME.equals(file.getName())) {
                return;
            }
            Group<CnATreeElement> parent = (Group<CnATreeElement>) groupMap.get(file.getParent());
            CreateElement<CnATreeElement> saveCommand = new CreateElement<CnATreeElement>(parent, parent.getChildTypes()[0], file.getName());
            saveCommand.setInheritAuditPermissions(true);
            saveCommand = getCommandService().executeCommand(saveCommand);    
            CnATreeElement newElement = saveCommand.getNewElement();
            
            CnAElementFactory.getModel(newElement).childAdded(parent, newElement);
            CnAElementFactory.getModel(newElement).databaseChildAdded(newElement);
            
            if(Activator.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)){
                validateElement(newElement);
            }    
            createAttachment(newElement, file); 
            CnATreeElement linkTarget = (CnATreeElement) context.getProperty(LINK_TARGET);
            if(linkTarget!=null) {
                List<CnATreeElement> targetList = new ArrayList<CnATreeElement>();
                targetList.add(linkTarget);
                CnAElementHome.getInstance().createLinksAccordingToBusinessLogic(newElement, targetList);
            }
            
            numberOfFiles++;
        } catch (Exception e) {
            LOG.error("Erro while handle directory: " + file.getPath(), e);
        }
    }

    protected void createAttachment(CnATreeElement element, File file) throws CommandException, IOException {
        // create attachment (without file data)
        Attachment attachment = new Attachment();
        attachment.setCnATreeElementId(element.getDbId());
        attachment.setCnAElementTitel(element.getTitle());
        attachment.setTitel(file.getName());
        attachment.setDate(Calendar.getInstance().getTime());
        attachment.setFilePath(file.getPath());
        SaveNote command = new SaveNote(attachment);
        command = getCommandService().executeCommand(command);
        attachment = (Attachment) command.getAddition();
        
        // save file data
        AttachmentFile attachmentFile = new AttachmentFile();
        attachmentFile.readFileData(attachment.getFilePath());
        SaveAttachment saveFileCommand = new SaveAttachment(attachmentFile);
        attachmentFile.setDbId(attachment.getDbId());
        saveFileCommand = getCommandService().executeCommand(saveFileCommand);
        saveFileCommand.clear();
    }
    
    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    private void validateElement(CnATreeElement elmt){
        getValidationService().createValidationForSingleElement(elmt);
        CnAElementFactory.getModel(elmt).validationAdded(elmt.getScopeId());
    }
    
    private IValidationService getValidationService(){
        if(validationService == null){
            validationService = ServiceFactory.lookupValidationService();
        }
        return validationService;
    }
    
    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }
    
    class DirectoryHandler implements IDirectoryHandler { 
        /* (non-Javadoc)
         * @see sernet.verinice.fei.rcp.IFileHandler#handle(java.io.File)
         */
        @Override
        public void enter(File dir, TraverserContext context) {
            handleDirectory(dir, context);          
        }

        /* (non-Javadoc)
         * @see sernet.verinice.fei.rcp.IDirectoryHandler#leave(java.io.File, sernet.verinice.fei.rcp.TraverserContext)
         */
        @Override
        public void leave(File file, TraverserContext context) { 
        }      
    }
    
    class FileHandler implements IFileHandler {    
        /* (non-Javadoc)
         * @see sernet.verinice.fei.rcp.IFileHandler#handle(java.io.File)
         */
        @Override
        public void handle(File file, TraverserContext context) {
            handleFile(file, context);
        }       
    }
}
