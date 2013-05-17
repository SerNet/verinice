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
package sernet.verinice.rcp;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.views.FileView;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.service.commands.LoadAttachmentFile;

/**
 * If file attachment is an image ImageCellProvider loads an scaled instance 
 * of this image an displays is in a table cell.
 * 
 * Once an image is loaded it is cached by EHCache.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class ImageCellProvider extends OwnerDrawLabelProvider {
    
    private static final Logger LOG = Logger.getLogger(ImageCellProvider.class);
    
    public static final Object EMPTY_CACHE_ELEMENT = new Object();
    
    private int thumbSize;
    private ICommandService commandService;
    
    protected ImageCellProvider(int thumbnailSize) {
        this.thumbSize = thumbnailSize;
    }
    
    @Override
    protected void paint(Event event, Object element){
       long start = System.currentTimeMillis();   
       if(thumbSize>0) {
           Image img = getImage(element);      
           if (LOG.isDebugEnabled()) {
               LOG.debug("get image: " + (System.currentTimeMillis() - start));
           }
           if(img!=null) {         
               int imgWidth = img.getBounds().width;
               int imgHeight = img.getBounds().height;
               Rectangle tableItemBounds = ((TableItem) event.item).getBounds(event.index);
               int cellWidth = tableItemBounds.width;
               int cellHeight = tableItemBounds.height;
               cellWidth /= 2;
               cellWidth -= imgWidth / 2;
               cellHeight /= 2;
               cellHeight -= imgHeight / 2;
               int x = (cellWidth > 0 ? tableItemBounds.x + cellWidth : tableItemBounds.x);
               int y = cellHeight > 0 ? tableItemBounds.y + cellHeight : tableItemBounds.y;
               event.gc.drawImage(img, 0, 0, imgWidth, imgHeight, x, y, imgWidth, imgHeight);         
            }
       }
        if (LOG.isDebugEnabled()) {
            LOG.debug("paint: " + (System.currentTimeMillis() - start));
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event, java.lang.Object)
     */
    @Override
    protected void measure(Event arg0, Object arg1) {}
    
    protected abstract Image getImage(Object element);
    
    protected byte[] loadFileData(Attachment attachment) {
        if(Arrays.asList(Attachment.getImageMimeTypes()).contains(attachment.getMimeType())) {
            try {
                LoadAttachmentFile command = new LoadAttachmentFile(attachment.getDbId(), thumbSize);
                command = getCommandService().executeCommand(command);
                AttachmentFile attachmentFile = command.getAttachmentFile();
                if(attachmentFile!=null) {                    
                  return attachmentFile.getFileData();
                } 
            } catch (Exception e) {
                LOG.error("Error while loading attachment", e); //$NON-NLS-1$
                ExceptionUtil.log(e, Messages.FileView_27);
            }
        }
        return null;
    }
    
    protected Image createImage(byte[] fileData) {
        if(fileData==null) {
            return null;
        }
        try {
            return new Image(FileView.getDisplay(),new ByteArrayInputStream(fileData));
        } catch (Exception e) {
            LOG.error("Error while creating SWT image", e);
            return null;
        }
    }

    public int getThumbSize() {
        return thumbSize;
    }

    public void setThumbSize(int thumbSize) {
        this.thumbSize = thumbSize;
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }
    
}