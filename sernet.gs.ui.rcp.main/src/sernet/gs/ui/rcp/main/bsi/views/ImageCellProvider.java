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
package sernet.gs.ui.rcp.main.bsi.views;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.internal.intro.impl.util.Log;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.service.commands.LoadAttachmentFile;

class ImageCellProvider extends OwnerDrawLabelProvider {
    
    private static final Logger LOG = Logger.getLogger(ImageCellProvider.class);
    
    private static final Object EMPTY_CACHE_ELEMENT = new Object();
    
    int thumbSize;
    ICommandService commandService;     
    ImageLoader loader = new ImageLoader();

    private CacheManager manager = null;
    private String cacheId = null;
    private Cache cache = null;
    
    /**
     * @param fileView
     */
    ImageCellProvider(int thumbnailSize) {
        this.thumbSize = thumbnailSize;
    }
    
    @Override
    protected void paint(Event event, Object element){
       long start = System.currentTimeMillis();       
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("paint: " + (System.currentTimeMillis() - start));
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.OwnerDrawLabelProvider#measure(org.eclipse.swt.widgets.Event, java.lang.Object)
     */
    @Override
    protected void measure(Event arg0, Object arg1) {}
    
    /**
     * @param attachment
     */
    private Image getImage(Object element) {
        if(! (element instanceof Attachment)) {
            return null;
        }
        Attachment attachment = (Attachment) element;
        
        Image thumb = null;
        if(Arrays.asList(Attachment.IMAGE_MIME_TYPES).contains(attachment.getMimeType())) {
            Element cacheElement = getCache().get(attachment.getDbId());
            if(cacheElement!=null) {         
                // != is correct here
                if(cacheElement.getObjectValue()!=EMPTY_CACHE_ELEMENT) {
                    ImageData imageData = (ImageData) cacheElement.getObjectValue();
                    thumb = new Image(FileView.getDisplay(),imageData);
                }
            } else {
                byte[] fileData = loadFileData(attachment);
                thumb = createImage(fileData);
                if(thumb!=null) {
                    getCache().put(new Element(attachment.getDbId(), thumb.getImageData()));
                } else {
                    getCache().put(new Element(attachment.getDbId(), EMPTY_CACHE_ELEMENT));
                }
            }
        }
        return thumb;
    }
    

    private byte[] loadFileData(Attachment attachment) {
        if(Arrays.asList(Attachment.IMAGE_MIME_TYPES).contains(attachment.getMimeType())) {
            try {
                LoadAttachmentFile command = new LoadAttachmentFile(attachment.getDbId(), thumbSize);
                command = getCommandService().executeCommand(command);
                AttachmentFile attachmentFile = command.getAttachmentFile();
                if(attachmentFile!=null) {                    
                  return attachmentFile.getFileData();
                } 
            } catch (Exception e) {
                FileView.LOG.error("Error while loading attachment", e); //$NON-NLS-1$
                ExceptionUtil.log(e, Messages.FileView_27);
            }
        }
        return null;
    }
    
    private Image createImage(byte[] fileData) {
        //return new Image(FileView.getDisplay(),loader.load(new ByteArrayInputStream(fileData))[0]);
        try {
            return new Image(FileView.getDisplay(),new ByteArrayInputStream(fileData));
        } catch (Exception e) {
            LOG.error("Error while creating SWT image", e);
            return null;
        }
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
    
    protected void finalize() throws Throwable {
        shutdownCache();
    }
    
    public void shutdownCache() {
        CacheManager.getInstance().shutdown();
        manager=null;
    }
    
    private Cache getCache() {  
        if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || cache==null || !Status.STATUS_ALIVE.equals(cache.getStatus())) {
            cache = createCache();
        } else {
            cache = getManager().getCache(cacheId);
        }
        return cache;
    }
    
    private Cache createCache() {
        cacheId = UUID.randomUUID().toString();
        cache = new Cache(cacheId, 1000, false, false, 3600, 3600);
        getManager().addCache(cache);
        return cache;
    }
    
    
    
    public CacheManager getManager() {
        if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus())) {
            manager = CacheManager.create();
        }
        return manager;
    }
}