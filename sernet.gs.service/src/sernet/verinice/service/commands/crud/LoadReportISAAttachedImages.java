/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

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
public class LoadReportISAAttachedImages extends GenericCommand implements ICachedCommand{
    
    private static final Logger LOG = Logger.getLogger(LoadReportISAAttachedImages.class);
    
    private Integer rootElmt;
    
    private List<byte[]> results;
    
    private boolean oddNumbers = false; // return only images with odd number
    
    private boolean resultInjectedFromCache = false;

    // max picture size in pixels (max is a 350x350 rectangle)
    private static final int maxImageHeightAndWidth = 350;
    
    private static final String[] IMAGEMIMETYPES = new String[]{
        "jpg",
        "png"
    };
    
    public static final String[] COLUMNS = new String[] { 
        "imageData"
    };
    
    public LoadReportISAAttachedImages(){
        // do nothing
    }
    
    public LoadReportISAAttachedImages(Integer root){
        this(root, false);
    }
    
    public LoadReportISAAttachedImages(Integer root, boolean odd){
        this.rootElmt = root;
        this.oddNumbers = odd;
    }
    

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            results = new ArrayList<byte[]>(0);
            for(int dbid : getChildren(rootElmt, SamtTopic.TYPE_ID)){
                SamtTopic topic = (SamtTopic)getDaoFactory().getDAO(SamtTopic.TYPE_ID).findById(dbid);
                if(LOG.isDebugEnabled()){
                    LOG.debug("Inspecting Evidences of SamtTopic:\t" + topic.getTitle());
                }

                
                Iterator<Entry<CnATreeElement, CnALink>> iterator = CnALink.getLinkedElements(topic,Evidence.TYPE_ID).entrySet().iterator();
                while(iterator.hasNext()){
                    Entry<CnATreeElement, CnALink> entry = iterator.next();
                    if(LOG.isDebugEnabled()){
                        LOG.debug("\tInspecting Attachments of Evidence:\t" + entry.getKey().getTitle());
                    }
                    try {
                        LoadAttachmentsUserFiltered command = new LoadAttachmentsUserFiltered(entry.getKey().getDbId());

                        command = getCommandService().executeCommand(command);
                        for(int i = 0; i < command.getResult().size(); i++){
                            // report uses two tables next to each other showing odd/even numbered images only
                            // done to restriction showing always two images in a row
                            if((i % 2 == 0 && !oddNumbers) || (i % 2 == 1 && oddNumbers)){ 
                                Attachment attachment = (Attachment)command.getResult().get(i);
                                if(LOG.isDebugEnabled()){
                                    LOG.debug("\t\tChecking MIME-Type of Attachment:\t" + attachment.getFileName());
                                }
                                if(isSupportedMIMEType(attachment.getMimeType())){
                                    if(LOG.isDebugEnabled()){
                                        LOG.debug("\t\t\tMime-Type is suiteable");
                                    }
                                    LoadAttachmentFile fileLoader = new LoadAttachmentFile(attachment.getDbId());
                                    fileLoader = getCommandService().executeCommand(fileLoader);
                                    results.add(scaleImageIfNeeded(fileLoader.getAttachmentFile().getFileData(), attachment.getMimeType()));
                                }
                            }
                        }
                    } catch (CommandException e) {
                        LOG.error("Error while loading attachments", e);
                    }
                }
            }
        }
    }
    
    public List<byte[]> getResult(){
        return results;
    }
    
    public List<List<String>> getDummyResult(){
        List<List<String>> result = new ArrayList<List<String>>();
        for(int i = 0; i < results.size(); i++){
            ArrayList<String> nList = new ArrayList<String>(0);
            nList.add(String.valueOf(i));
            result.add(nList);
        }
        return result;
    }
    
    public void setRootElmt(int root){
        this.rootElmt = Integer.valueOf(root);
    }
    
    public byte[] getResult(int resultNr){
        if(results.size() >= resultNr + 1){
            return results.get(resultNr);
        }
        else return new byte[0];
    }
    
    private boolean isSupportedMIMEType(String mimetype){
        for(String s : IMAGEMIMETYPES){
            if(s.equalsIgnoreCase(mimetype)){
               return true; 
            }
        }
        return false;
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
        this.results = (ArrayList<byte[]>)result;
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
    
    private List<Integer> getChildren(int parent, String type) {
        List<Integer> results = new ArrayList<Integer>(0);
        String hql = "select dbId from CnATreeElement where objectType = ? AND parentId = ?";
        Object[] scopeIDparams = new Object[]{type, parent};
        List<Object> hqlResult   = getDaoFactory().getDAO(CnATreeElement.class).findByQuery(hql, scopeIDparams);
        if (hqlResult != null && hqlResult.size() > 0) {
            for(int i = 0; i < hqlResult.size(); i++){
                results.add((Integer)hqlResult.get(i));
            }
        }
        return results;
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
            LOG.error("Error while scaling image", e);
        }
        return imageData;
    }

}
