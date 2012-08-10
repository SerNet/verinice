/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.AttachmentFile;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Loads files/attachmets data for a {@link CnATreeElement}
 * File meta-data data will not be loaded by this command. 
 * Use command LoadAttachments to load file meta-data from database.
 * 
 * @see LoadAttachment
 * @see AttachmentFile
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class LoadAttachmentFile extends GenericCommand {

	private transient Logger log = Logger.getLogger(LoadAttachmentFile.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadAttachmentFile.class);
		}
		return log;
	}

	private Integer dbId;
	
	private AttachmentFile attachmentFile;
	
	// for images only, null means no scaling
	private Integer scaleSize;

	public LoadAttachmentFile(Integer dbId) {
		super();
		this.dbId = dbId;
	}
	
	public LoadAttachmentFile(Integer dbId, Integer scaleSize) {
        this(dbId);
        this.scaleSize = scaleSize;
    }

	public void execute() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("executing, id is: " + getDbId() + "...");
		}
		if(getDbId()!=null) {
			IBaseDao<AttachmentFile, Serializable> dao = getDaoFactory().getDAO(AttachmentFile.class);		
			setAttachmentFile(dao.retrieve(getDbId(),null));		
			if(scaleSize!=null) {
			    // clear dao otherwise scaled image is saved
			    dao.clear();
			    scaleImage();
			}
		}
		
	}

    private void scaleImage() {
        if(getAttachmentFile()==null || getAttachmentFile().getFileData()==null) {
            return;
        }
        try {          
            long start = 0;
            long sizeBefore = 0;
            if (getLog().isDebugEnabled()) {
                start = System.currentTimeMillis();
                sizeBefore = getAttachmentFile().getFileData().length;
            }
            
            BufferedImage image = readImageFromByteArray();
            if(image!=null) {
                BufferedImage thumbnailImage = createEmptyThumbnailImage(image);                 
                drawThumbnail(image, thumbnailImage);
                byte[] thumbByteArray = getByteArray(thumbnailImage);
                getAttachmentFile().setFileData(thumbByteArray);          
                if (getLog().isDebugEnabled()) {
                    long time = System.currentTimeMillis() - start;
                    long size = thumbByteArray.length;
                    getLog().debug("Before: " + sizeBefore + "b, after: " + size + "b, " + time + "ms");
                }
            } else {
                getAttachmentFile().setFileData(null);
                getLog().info("Can not scale image. Maybe it has an unknown type. Db-Id is: " + getDbId());
            }
        } catch(Exception e) {
            getLog().error("Error while scaling image", e);
        }
    }

    /**
     * @param thumbnailImage
     * @return
     * @throws IOException
     */
    private byte[] getByteArray(BufferedImage thumbnailImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write( thumbnailImage, "png", baos );
        baos.flush();
        byte[] thumbByteArray = baos.toByteArray();
        baos.close();
        return thumbByteArray;
    }

    /**
     * @param image
     * @param resizedImage
     */
    private void drawThumbnail(BufferedImage image, BufferedImage resizedImage) {
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.drawImage(image, 0, 0, resizedImage.getWidth(), resizedImage.getHeight(), null);
        g.dispose();
    }

    /**
     * @return
     * @throws IOException
     */
    private BufferedImage readImageFromByteArray() throws IOException {
        InputStream in = new ByteArrayInputStream(getAttachmentFile().getFileData());
        BufferedImage image = ImageIO.read(in);
        return image;
    }
    
    private BufferedImage createEmptyThumbnailImage(BufferedImage sourceImage) {
        final int imgWidth = sourceImage.getWidth();
        final int imgHeight = sourceImage.getHeight();
        final int thumbWidth = (imgWidth >= imgHeight) ? scaleSize : (int) Math.round(scaleSize * ((imgWidth*1.0) / (imgHeight*1.0)));
        final int thumbHeight = (imgWidth >= imgHeight) ? (int) Math.round(scaleSize * ((imgHeight*1.0) / (imgWidth*1.0))) : scaleSize;         
        return new BufferedImage(thumbWidth, thumbHeight,  BufferedImage.TYPE_INT_RGB);
    }

    public void setDbId(Integer cnAElementId) {
		this.dbId = cnAElementId;
	}

	public Integer getDbId() {
		return dbId;
	}
	
	public AttachmentFile getAttachmentFile() {
		return attachmentFile;
	}

	public void setAttachmentFile(AttachmentFile attachmentFile) {
		this.attachmentFile = attachmentFile;
	}

}
