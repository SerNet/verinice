/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.AttachmentFileCreationFactory;
import sernet.verinice.service.commands.LoadAttachmentFile;
import sernet.verinice.service.commands.LoadAttachments;
import sernet.verinice.service.commands.SaveNote;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@ManagedBean(name = "attachment")
@SessionScoped
public class AttachmentBean {

    private static final Logger LOG = Logger.getLogger(AttachmentBean.class);

    private CnATreeElement element;
    private List<Attachment> attachments;
    private boolean autoUpload = true;
    private boolean useFlash = false;

    public void init() {
        attachments = loadAttachments();
    }

    public void handleFileUpload(FileUploadEvent event) throws CommandException, IOException {
        UploadedFile item = event.getFile();
        Attachment attachment = new Attachment();
        attachment.setCnATreeElement(getElement());
        attachment.setCnAElementTitel(getElement().getTitle());
        String filename = FilenameUtils.getName(item.getFileName());
        attachment.setTitel(filename);
        attachment.setDate(Calendar.getInstance().getTime());
        attachment.setFilePath(item.getFileName());
        attachment.setText(Messages.getString("AttachmentBean.0")); //$NON-NLS-1$
        attachment.setFileSize(String.valueOf(item.getContents().length));

        SaveNote command = new SaveNote(attachment);
        command = getCommandService().executeCommand(command);
        attachment = (Attachment) command.getAddition();

        AttachmentFileCreationFactory.createAttachmentFile(attachment, item.getContents());

        attachments.add(attachment);
        Collections.sort(attachments);
    }

    public List<Attachment> getAttachments() {
        if (attachments == null) {
            attachments = loadAttachments();
        }
        return attachments;
    }

    private List<Attachment> loadAttachments() {
        List<Attachment> result = Collections.emptyList();
        try {
            if (getElement() != null) {
                result = loadAttachmentsByCommand();
            }
        } catch (Exception e) {
            LOG.error("Error while loading attachment", e); //$NON-NLS-1$
        }
        return result;
    }

    private List<Attachment> loadAttachmentsByCommand() throws CommandException {
        LoadAttachments command = new LoadAttachments(getElement().getDbId());
        command = getCommandService().executeCommand(command);
        List<Attachment> result = command.getAttachmentList();
        if (result != null) {
            for (final Attachment attachment : result) {
                // set transient cna-element-titel
                attachment.setCnAElementTitel(getElement().getTitle());
            }
            Collections.sort(result);
        }
        return result;
    }

    public void download() throws CommandException, IOException {
        String id = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get("id"); //$NON-NLS-1$
        String name = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("name"); //$NON-NLS-1$
        LoadAttachmentFile command = new LoadAttachmentFile(Integer.valueOf(id));
        command = getCommandService().executeCommand(command);
        AttachmentFile attachmentFile = command.getAttachmentFile();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext()
                .getResponse();
        /*
         * Some JSF component library or some Filter might have set some headers
         * in the buffer beforehand. We want to get rid of them, else it may
         * collide.
         */
        response.reset();
        /*
         * Set it with the file size. This header is optional. It will work if
         * it's omitted, but the download progress will be unknown.
         */
        response.setContentLength(attachmentFile.getFileData().length);
        /*
         * The Save As popup magic is done here. You can give it any file name
         * you want, this only won't work in MSIE, it will use current request
         * URL as file name instead.
         */
        response.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        OutputStream output = response.getOutputStream();
        output.write(attachmentFile.getFileData());

        facesContext.responseComplete();
    }

    public boolean isAutoUpload() {
        return autoUpload;
    }

    public void setAutoUpload(boolean autoUpload) {
        this.autoUpload = autoUpload;
    }

    public boolean isUseFlash() {
        return useFlash;
    }

    public void setUseFlash(boolean useFlash) {
        this.useFlash = useFlash;
    }

    protected CnATreeElement getElement() {
        return element;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    private ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}