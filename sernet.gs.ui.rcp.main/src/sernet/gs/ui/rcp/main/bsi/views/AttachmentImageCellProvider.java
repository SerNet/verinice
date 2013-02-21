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

import java.util.Arrays;
import java.util.UUID;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.rcp.ImageCellProvider;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AttachmentImageCellProvider extends ImageCellProvider {
    
    private CacheManager manager = null;
    private String cacheId = null;
    private Cache cache = null;
    
    AttachmentImageCellProvider(int size) {
        super(size);
    }

    protected Image getImage(Object element) {
        if(! (element instanceof Attachment)) {
            return null;
        }
        Attachment attachment = (Attachment) element;
        
        Image thumb = null;
        if(Arrays.asList(Attachment.getImageMimeTypes()).contains(attachment.getMimeType())) {
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
    
    protected void finalize() throws Throwable {
        shutdownCache();
        super.finalize();
    }
    
    public void shutdownCache() {
        CacheManager.getInstance().shutdown();
        manager=null;
    }
    
    protected Cache getCache() {  
        if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || cache==null || !Status.STATUS_ALIVE.equals(cache.getStatus())) {
            cache = createCache();
        } else {
            cache = getManager().getCache(cacheId);
        }
        return cache;
    }
    
    public void clearCache() {
        getCache().removeAll();
    }
    
    private Cache createCache() {
        final int overFlowToDisk = 1000;
        final long timeToLive = 3600;
        final long timeToIdle = 3600;
        cacheId = UUID.randomUUID().toString();
        cache = new Cache(cacheId, overFlowToDisk, false, false, timeToLive, timeToIdle);
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
