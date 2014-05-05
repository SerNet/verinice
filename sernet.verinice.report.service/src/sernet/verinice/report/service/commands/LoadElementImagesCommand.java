/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 *     Sebastian Hagedorn <sh@sernet.de>
 ******************************************************************************/
package sernet.verinice.report.service.commands;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.ImageIO;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementById;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.LoadAttachmentFile;

/**
 * Loads the images that have been stored along with a finding.
 * 
 * TODO: The images are supposedly attached to a measure, the
 * {@link CnATreeElement} to which the measure belongs or a completely different
 * entity. The {@link #id} property is supposed to be used for this.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * @author Sebastian Hagedorn <sh@sernet.de>
 * 
 */
@SuppressWarnings("serial")
public class LoadElementImagesCommand extends GenericCommand {

    private int id;

    private int imageNr;

    private transient Logger log;

    private byte[] result;

    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;

    private static final int MAX_IMAGE_HEIGHT = 151;
    private static final int MAX_IMAGE_WIDTH = 310;
    
    public LoadElementImagesCommand(){
    	// BIRT JavaScript Constructor for use with class.newInstance()
    }

    public LoadElementImagesCommand(int id) {
        this.id = id;
        log = Logger.getLogger(LoadElementImagesCommand.class);
    }

    public LoadElementImagesCommand(int id, int imageNr) {
        this(id);
        this.imageNr = imageNr;
    }

    public byte[] getResult() {
        return (result != null) ? result.clone() : null;
    }

    private boolean isImage(Attachment attachment) {
        boolean isImage = Boolean.FALSE;
        for (String imageMimeType : Attachment.getImageMimeTypes()) {
            if (attachment.getMimeType().equals(imageMimeType)) {
                isImage = Boolean.TRUE;
                break;
            }
        }
        return isImage;
    }

    /**
     * 
     * @param topic
     *            The SamtTopic which is the worst finding and contains the
     *            attachments / images
     * @param n
     *            just two images will be shown in the report, so the only possible
     *            values are 1 and 2
     * @return
     */
    private Attachment getNthImage(SamtTopic topic, int n) {
        for (int i = 0; i < topic.getFiles().size(); i++) {
            Iterator<Attachment> iter = topic.getFiles().iterator();
            int count = 0;
            while (iter.hasNext()) {
                Attachment a = iter.next();
                if (isImage(a)) {
                    count++;
                    if (count == n) {
                        return a;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void execute() {
        SamtTopic topic = null;
        try {
            LoadWorstFindingsCommand command = new LoadWorstFindingsCommand(id);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            Object[][] wfResult = command.getResult();
            if (wfResult != null && wfResult.length > 0 && wfResult[0].length > 0) {
                int topicId = (Integer) wfResult[0][0];
                LoadCnAElementById command2 = new LoadCnAElementById(SamtTopic.TYPE_ID, topicId);
                command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
                topic = (SamtTopic) command2.getFound();
                if (!topic.isChildrenLoaded()) {
                    topic = (SamtTopic) loadChildren(topic);
                }
            }
            if (topic != null && topic.getFiles() != null && topic.getFiles().size() > 0) {
                Attachment attachment = getNthImage(topic, imageNr);
                if (attachment != null) {
                    LoadAttachmentFile command1 = new LoadAttachmentFile(attachment.getDbId());
                    command1 = getCommandService().executeCommand(command1);
                    AttachmentFile attachmentFile = command1.getAttachmentFile();
                    if (attachmentFile != null) {
                        result = processImageData(attachmentFile);
                    }
                }
            }
            if(result == null){
            	setDummyImage();
            }
        } catch (CommandException e) {
            if (log == null) {
                log = Logger.getLogger(this.getClass());
            }
            log.error("Error while executing command", e);

        } catch (IOException e) {
            log.error("Error reading image from byte data", e);
        }
    }

    private byte[] processImageData(AttachmentFile attachmentFile) throws IOException {
        final int degrees90 = 90;
        final int defaultByteArraySize = 1000;
        InputStream in = new ByteArrayInputStream(attachmentFile.getFileData());
        BufferedImage bImage = ImageIO.read(in);
        ByteArrayOutputStream baos = null;
        
        BufferedImage rotatedImage = null; 
        if(bImage.getHeight() > bImage.getWidth()){ 
            rotatedImage = rotateImage(bImage, degrees90);
        }
        BufferedImage imageToWorkWith = null;
        if(rotatedImage != null){
            imageToWorkWith = rotatedImage;
        } else {
            imageToWorkWith = bImage;
        }
        if (imageToWorkWith.getWidth() > MAX_IMAGE_WIDTH || imageToWorkWith.getHeight() > MAX_IMAGE_HEIGHT) {
            Image image = imageToWorkWith.getScaledInstance(MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT, BufferedImage.SCALE_DEFAULT);

            baos = new ByteArrayOutputStream(defaultByteArraySize);
            if (image instanceof BufferedImage) {
                imageToWorkWith = (BufferedImage) image;
            } else {
                imageToWorkWith = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
                imageToWorkWith.getGraphics().drawImage(image, 0, 0, null);
            }
        }
        if(baos !=  null){
            ImageIO.write(imageToWorkWith, "jpeg", baos);
            return baos.toByteArray();
        } else {
        	return attachmentFile.getFileData();
        }
    }
    
    private void setDummyImage(){
        final int defaultByteArraySize = 4096;
    	URL url = getClass().getResource("onewhitepixel.jpg");
    	InputStream is = null;
    	ByteArrayOutputStream bais = new ByteArrayOutputStream();
    	try{
    		is = url.openStream();
    		byte[] byteChunk = new byte[defaultByteArraySize];
    		int i = 0;
    		while((i = is.read(byteChunk)) > 0){
    			bais.write(byteChunk, 0, i);
    		}
    		result = bais.toByteArray();
    	} catch(IOException e){
    		log.error("I-/O-Exception", e);
    	} finally {
    		if(is != null){
    			try {
					is.close();
				} catch (IOException e) {
					log.error("I-/O-Exception", e);
				}
    		}
    	}
    }
    
    private BufferedImage rotateImage(BufferedImage image, int radiant){
        AffineTransform transform = new AffineTransform();
        transform.rotate(Math.toRadians(radiant), (double)image.getWidth() / 2.0, (double)image.getHeight() / 2.0);
        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(image, null);
    }

    private Cache getCache() {
        if (manager == null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || cache == null || !Status.STATUS_ALIVE.equals(cache.getStatus())) {
            cache = createCache();
        } else {
            cache = manager.getCache(cacheId);
        }
        return cache;
    }

    private Cache createCache() {
        final int maxElementsInMemory = 20000;
        final int timeToLiveSeconds = 600;
        final int timeToIdleSeconds = 500;
        cacheId = UUID.randomUUID().toString();
        manager = CacheManager.create();
        cache = new Cache(cacheId, maxElementsInMemory, false, false, timeToLiveSeconds, timeToIdleSeconds);
        manager.addCache(cache);
        return cache;
    }

    private CnATreeElement loadChildren(CnATreeElement el) {
        if (el.isChildrenLoaded()) {
            return el;
        } else if (getCache().get(el.getDbId()) != null) {
            return (CnATreeElement) getCache().get(el.getDbId()).getValue();
        }

        LoadChildrenForExpansion command;
        command = new LoadChildrenForExpansion(el);
        try {
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            CnATreeElement newElement = command.getElementWithChildren();
            newElement.setChildrenLoaded(true);
            getCache().put(new Element(el.getDbId(), newElement));
            return newElement;
        } catch (CommandException e) {
            log.error("error while loading children of CnaTreeElment", e);
        }
        return null;
    }

	public void setId(int id) {
		this.id = id;
	}

	public void setImageNr(int imageNr) {
		this.imageNr = imageNr;
	}

}
