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
package sernet.verinice.service.commands.crud;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import sernet.gs.service.Retriever;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.LoadAttachmentFile;
import sernet.verinice.service.commands.LoadAttachmentsUserFiltered;

/**
 *
 */
public class LoadReportISANetworkImages extends GenericCommand implements ICachedCommand {
    

    private int rootElmt;
    
    private boolean resultInjectedFromCache = false;
    
    private static transient Logger LOG = Logger.getLogger(LoadReportISANetworkImages.class);
    
    private boolean oddNumbers = false;
    
    private List<List<Object>> results;
    
    // max picture size in pixels (max is a 350x350 rectangle)
    private static final int maxImageHeightAndWidth = 350;
    
    private static final String[] IMAGEMIMETYPES = new String[]{
        "jpg",
        "png"
    };
    
    public static final String[] COLUMNS = new String[] { 
        "imageData",
        "imageDescription"
    };
    
    public LoadReportISANetworkImages(){
        // do nothing
    }

    public LoadReportISANetworkImages(int root, boolean oddImages){
        this.rootElmt = root;
        this.oddNumbers = oddImages;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            results = new ArrayList<List<Object>>(0);
            for(SamtTopic topic : getSamtChildren(rootElmt)){
                List<Object[]> topicPictureList = getNetworkPictures(getLinkedEvidences(topic));
                for(Object[] picture : topicPictureList){
                    results.add(Arrays.asList(picture));
                }
            }
        }
    }
    
    public List<List<Object>> getResults(){
        return results;
    }

    private List<SamtTopic> getSamtChildren(int dbid){
        String hql = "from CnATreeElement where " +
                   " objectType = ?" + 
                   " AND parentId = ?";
        Object[] params = new Object[]{SamtTopic.TYPE_ID, dbid};
        List<SamtTopic> hqlResult   = getDaoFactory().getDAO(SamtTopic.TYPE_ID).findByQuery(hql, params);
        return hqlResult;
    }
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        cacheID.append(String.valueOf(oddNumbers));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.results = (List<List<Object>>)result;
        resultInjectedFromCache = true;
        if(LOG.isDebugEnabled()){
            LOG.debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.results;
    }
    
    public Logger getLog(){
        if(LOG != null){
            LOG = Logger.getLogger(LoadReportISANetworkImages.class);
        }
        return LOG;
    }
    
    private List<Object[]> getNetworkPictures(List<Evidence> evidences){
        List<Object[]> pictures = new ArrayList<Object[]>(0);
        for(Evidence evidence : evidences){

            try {
                LoadAttachmentsUserFiltered attachmentLoader = new LoadAttachmentsUserFiltered(evidence.getDbId());
                attachmentLoader = getCommandService().executeCommand(attachmentLoader);
                for(int i = 0; i < attachmentLoader.getResult().size(); i++){
                    // report uses two tables next to each other showing odd/even numbered images only
                    // done to restriction showing always two images in a row
                    if((i % 2 == 0 && !oddNumbers) || (i % 2 == 1 && oddNumbers)){ 
                        Attachment attachment = (Attachment)attachmentLoader.getResult().get(i);
                        if(LOG.isDebugEnabled()){
                            LOG.debug("\t\tChecking MIME-Type of Attachment:\t" + attachment.getFileName());
                        }
                        if(isSupportedMIMEType(attachment.getMimeType())){
                            if(LOG.isDebugEnabled()){
                                LOG.debug("\t\t\tMime-Type is suiteable");
                            }
                            LoadAttachmentFile fileLoader = new LoadAttachmentFile(attachment.getDbId());
                            fileLoader = getCommandService().executeCommand(fileLoader);
                            Object[] picture = new Object[2];
                            picture[0] = (scaleImageIfNeeded(fileLoader.getAttachmentFile().getFileData(), attachment.getMimeType()));
                            picture[1] = attachment.getText();
                            pictures.add(picture);
                        }
                    }
                }
            } catch (CommandException e) {
                LOG.error("Error while loading attachments", e);
            }
        }
        return pictures;
    }
    
    private boolean isSupportedMIMEType(String mimetype){
        for(String s : IMAGEMIMETYPES){
            if(s.equalsIgnoreCase(mimetype)){
               return true; 
            }
        }
        return false;
    }
    
    private List<Evidence> getLinkedEvidences(SamtTopic topic){
        List<Evidence> evidences = new ArrayList<Evidence>(0);
        topic = (SamtTopic)Retriever.checkRetrieveLinks(topic, true);
        Set<Entry<CnATreeElement, CnALink>> entryset = CnALink.getLinkedElements(topic, Evidence.TYPE_ID).entrySet();
        Iterator<Entry<CnATreeElement, CnALink>> iter = entryset.iterator();
        while(iter.hasNext()){
            evidences.add((Evidence)iter.next().getKey());
        }
        return evidences;
    }
    
    private byte[] scaleImageIfNeeded(byte[] imageData, String mimetype){
        ByteArrayInputStream in = new ByteArrayInputStream(imageData);
        try{
            BufferedImage img = ImageIO.read(in);
            if(img.getWidth() > maxImageHeightAndWidth || img.getHeight() > maxImageHeightAndWidth){
                // compute scalefactor (percentage)
                int biggerOne = img.getWidth();
                if(img.getHeight() > biggerOne){
                    biggerOne = img.getHeight();
                }
                float onePercent = biggerOne / 100;
                int percentage = (int)(maxImageHeightAndWidth / onePercent);
                // resize image
                int newHeight = (img.getHeight() / 100) * percentage;
                int newWidth = (img.getWidth() / 100) * percentage;
                Image scaledImage = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                BufferedImage imageBuff = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                imageBuff.getGraphics().drawImage(scaledImage, 0, 0, new Color(0,0,0),null);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ImageIO.write(imageBuff, mimetype, buffer);
                return buffer.toByteArray();
            }
        } catch(IOException e){
            getLog().error("Error while scaling image", e);
        }
        return imageData;
    }

}
