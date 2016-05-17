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
package sernet.verinice.service.commands;

import java.io.IOException;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class AttachmentFileCreationFactory {
    
    private AttachmentFileCreationFactory(){}
    
    /**
     * creates an entity of {@link AttachmentFile} as referenced by @param hostAttachment
     * to reference the data, that should be stored in the new {@link AttachmentFile}
     * @param dbIdOfDataSource defines the content of the new entity 
     **/
    public static void createAttachmentFile(Attachment hostAttachment, byte[] fileData) throws IOException, CommandException{
        AttachmentFile newFileAttachment = new AttachmentFile();
        if(fileData != null && fileData.length > 0){
            newFileAttachment.setFileData(fileData);
            newFileAttachment.setDbId(hostAttachment.getDbId());
        }
        SaveAttachment saveFileCommand = new SaveAttachment(newFileAttachment);
        saveFileCommand =  ((ICommandService)VeriniceContext.get(VeriniceContext.COMMAND_SERVICE)).executeCommand(saveFileCommand);
        saveFileCommand.clear();
    }

}
