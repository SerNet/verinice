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
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.validation.IValidationService;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.service.commands.AttachmentFileCreationFactory;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.LoadFileSizeLimit;
import sernet.verinice.service.commands.SaveNote;

/**
 * FileElementImportTraverser traverses the file system to import files as
 * verinice objects with attachments.
 * 
 * Every file which is found in a starting folder and it's subfolders is
 * imported as a verinice object with the file as an attachment.
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

    private Integer fileSizeMax;

    public FileElementImportTraverser(String startPath, Group<CnATreeElement> group) {
        super(startPath);
        groupMap = new Hashtable<String, CnATreeElement>();
        groupMap.put(startPath.substring(0, startPath.lastIndexOf(File.separatorChar)), group);
        addDirectoryHandler(new DirectoryHandler());
        addDirectoryHandler(new PathFinder());
        addFileHandler(new FileHandler());
    }

    public void handleDirectory(File dir, TraverserContext context) {
        if (dir == null) {
            return;
        }
        try {
            CnATreeElement parentGroup = groupMap.get(dir.getParent());
            CnATreeElement element = getElement(parentGroup, dir.getName());
            if (element == null) {
                CreateElement<CnATreeElement> saveCommand = new CreateElement<CnATreeElement>(
                        parentGroup, parentGroup.getTypeId(), dir.getName());
                saveCommand.setInheritAuditPermissions(true);
                saveCommand = getCommandService().executeCommand(saveCommand);
                element = saveCommand.getNewElement();
                CnAElementFactory.getModel(element).childAdded(parentGroup, element);
                CnAElementFactory.getModel(element).databaseChildAdded(element);
            }
            groupMap.put(dir.getPath(), element);

            context.addProperty(CURRENT_DIRECTORY, element);

            if (Activator.getDefault().getPluginPreferences()
                    .getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)) {
                validateElement(element);
            }
        } catch (FileExceptionNoStop e) {
            // do not handle this exception here but all others
            throw e;
        } catch (Exception e) {
            LOG.error("Error while handle directory: " + dir.getPath(), e);
        }
    }

    public void handleFile(File file, TraverserContext context) {
        try {
            if (PathFinder.PROPERTY_FILE_NAME.equals(file.getName())) {
                return;
            }

            checkFileSize(file, context);

            Group<CnATreeElement> parent = (Group<CnATreeElement>) groupMap.get(file.getParent());
            CnATreeElement element = getElement(parent, file.getName());
            if (element != null) {
                return;
            }
            CreateElement<CnATreeElement> saveCommand = new CreateElement<CnATreeElement>(parent,
                    parent.getChildTypes()[0], file.getName());
            saveCommand.setInheritAuditPermissions(true);
            saveCommand = getCommandService().executeCommand(saveCommand);
            element = saveCommand.getNewElement();

            CnAElementFactory.getModel(element).childAdded(parent, element);
            CnAElementFactory.getModel(element).databaseChildAdded(element);

            if (Activator.getDefault().getPluginPreferences()
                    .getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)) {
                validateElement(element);
            }
            createAttachment(element, file);
            CnATreeElement linkTarget = (CnATreeElement) context.getProperty(LINK_TARGET);
            if (linkTarget != null) {
                List<CnATreeElement> targetList = new ArrayList<CnATreeElement>();
                targetList.add(linkTarget);
                CnAElementHome.getInstance().createLinksAccordingToBusinessLogic(element,
                        targetList);
            }

            numberOfFiles++;
        } catch (FileExceptionNoStop e) {
            // do not handle this exception here but all others
            throw e;
        } catch (Exception e) {
            LOG.error("Erro while handle file: " + file.getPath(), e);
        }
    }

    protected void checkFileSize(File file, TraverserContext context) {
        long size = file.length();
        if (FileSystemTraverser.convertByteToMB(size) > getMaxFileSizeInMB()) {
            String readableSize = FileSystemTraverser.formatByteToMB(size);
            throw new FileExceptionNoStop(file.getPath(), NLS.bind(
                    Messages.FileElementImportTraverser_0, readableSize, getMaxFileSizeInMB()));
        }
    }

    private CnATreeElement getElement(CnATreeElement parent, String name) {
        if (parent == null) {
            return null;
        }
        parent = Retriever.checkRetrieveChildren(parent);
        Set<CnATreeElement> children = parent.getChildren();
        for (CnATreeElement child : children) {
            child = Retriever.checkRetrieveElementAndChildren(child);
            if (child.getTitle().equals(name)) {
                return child;
            }
        }
        return null;
    }

    protected void createAttachment(CnATreeElement element, File file)
            throws CommandException, IOException {
        // create attachment (without file data)
        Attachment attachment = new Attachment();
        attachment.setCnATreeElement(element);
        attachment.setCnAElementTitel(element.getTitle());
        attachment.setTitel(file.getName());
        attachment.setDate(Calendar.getInstance().getTime());
        attachment.setFilePath(file.getPath());
        attachment.setFileSize(String.valueOf(file.length()));
        SaveNote command = new SaveNote(attachment);
        command = getCommandService().executeCommand(command);
        attachment = (Attachment) command.getAddition();

        // // save file data
        AttachmentFileCreationFactory.createAttachmentFile(attachment,
                FileUtils.readFileToByteArray(new File(attachment.getFilePath())));
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    private void validateElement(CnATreeElement elmt) {
        getValidationService().createValidationForSingleElement(elmt);
        CnAElementFactory.getModel(elmt).validationAdded(elmt.getScopeId());
    }

    private IValidationService getValidationService() {
        if (validationService == null) {
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

    private int getMaxFileSizeInMB() {
        if (fileSizeMax == null) {
            fileSizeMax = loadFileSizeMax();
        }
        return fileSizeMax;
    }

    private Integer loadFileSizeMax() {
        int result = LoadFileSizeLimit.FILE_SIZE_MAX_DEFAULT;
        LoadFileSizeLimit loadFileSizeLimit = new LoadFileSizeLimit();
        try {
            loadFileSizeLimit = getCommandService().executeCommand(loadFileSizeLimit);
        } catch (CommandException e) {
            LOG.error("Error while saving note", e); //$NON-NLS-1$
        }
        result = loadFileSizeLimit.getFileSizeMax();
        return result;
    }

    class DirectoryHandler implements IDirectoryHandler {
        @Override
        public void enter(File dir, TraverserContext context) {
            handleDirectory(dir, context);
        }

        @Override
        public void leave(File file, TraverserContext context) {
            // empty
        }
    }

    class FileHandler implements IFileHandler {
        @Override
        public void handle(File file, TraverserContext context) {
            handleFile(file, context);
        }
    }
}
