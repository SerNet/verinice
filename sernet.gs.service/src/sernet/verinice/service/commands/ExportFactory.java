/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
package sernet.verinice.service.commands;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;

import sernet.gs.service.VeriniceCharset;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.sync.VeriniceArchive;
import de.sernet.sync.data.SyncAttribute;
import de.sernet.sync.data.SyncLink;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public final class ExportFactory {
    
    private static final Logger LOG = Logger.getLogger(ExportFactory.class);
    
    private static final int BUFFER = 2048;
    
    private ExportFactory(){}
    
    /**
     * Creates a SyncLink instance out of a {@link CnALink} instance.
     * SyncLink is a JAXB Xml class generated out of verinice import and export
     * XML schema files:
     * sernet/verinice/service/sync
     *  sync.xsd
     *  data.xsd
     *  mapping.xsd
     * 
     * @param syncLink
     * @param link
     */
    public static void transform(CnALink link, List<SyncLink> syncLinkXmlList) {
        SyncLink syncLink = new SyncLink();
        syncLink.setDependant(ExportFactory.createExtId(link.getDependant()));
        syncLink.setDependency(ExportFactory.createExtId(link.getDependency()));
        syncLink.setRelationId(link.getRelationId());
        if(link.getComment()!=null && !link.getComment().isEmpty()) {
            syncLink.setComment(link.getComment());
        }
        syncLinkXmlList.add(syncLink);     
    }
    
  
    
    /**
     * Creates new SyncAttribute instances out of the properties of 
     * an entity. The newly created instances are added to syncAttributeList.
     * 
     * SyncLSyncAttributeink is a JAXB Xml class generated out of verinice import and export
     * XML schema files:
     * sernet/verinice/service/sync
     *  sync.xsd
     *  data.xsd
     *  mapping.xsd
     * 
     * @param entity
     * @param syncAttributeList
     * @param typeId
     * @param huiTypeFactory
     */
    public static void transform(Entity entity, List<SyncAttribute> syncAttributeList, String typeId, HUITypeFactory huiTypeFactory) {
        Map<String, PropertyList> properties = entity.getTypedPropertyLists();
        for (String propertyTypeId : properties.keySet()) {             
            PropertyType propertyType = huiTypeFactory.getPropertyType(typeId, propertyTypeId);
            if(propertyType==null) {
                LOG.warn("Property type not found in SNCA.xml: " + propertyTypeId + ", typeId: " + typeId);
            }
            if( propertyType==null || propertyType.isReportable()) {
                SyncAttribute syncAttribute = new SyncAttribute();
                // Add <syncAttribute> to this <syncObject>:
                syncAttribute.setName(propertyTypeId);
                
                int noOfValues = entity.exportProperties(propertyTypeId, syncAttribute.getValue());
                // Only if any value for the attribute could be found the whole
                // attribute instance is being added to the SyncObject's attribute
                // list.
                if (noOfValues > 0) {
                    syncAttributeList.add(syncAttribute);
                }
            }
        }
    }
    
    /**
     * Serializes jaxbObject to an output stream.
     * 
     * @param jaxbObject
     * @param os
     */
    public static void marshal( Object jaxbObject, OutputStream os ) {
        try {       
            JAXBContext context = JAXBContext.newInstance( jaxbObject.getClass() );
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
            m.setProperty(Marshaller.JAXB_ENCODING,VeriniceCharset.CHARSET_UTF_8.name());
            m.marshal(jaxbObject, os);
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        } 
    }
    
    public static void createZipEntry(ZipOutputStream zipOut, String entryName, byte[] data) throws IOException {
        zipOut.putNextEntry(new ZipEntry(entryName));
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        while (true) {
            int nRead = in.read(data, 0, data.length);
            if (nRead <= 0){
              break;
            }
            zipOut.write(data, 0, nRead);
        }
        in.close();
    }
    
    public static void createZipEntry(ZipOutputStream zipOut, String entryName, InputStream in) throws IOException {
        zipOut.putNextEntry(new ZipEntry(entryName));     
        BufferedInputStream origin = new BufferedInputStream(in, BUFFER);
        byte[] dataBlock = new byte[BUFFER];
        int count;
        while((count = origin.read(dataBlock, 0, BUFFER)) != -1) {
            zipOut.write(dataBlock, 0, count);
        }
        origin.close();
        in.close();
    }
    
    /**
     * @param attachment
     */
    public static String createZipFileName(Attachment attachment) {
        StringBuilder sb = new StringBuilder();
        sb.append(VeriniceArchive.FILES).append("/");
        sb.append(attachment.getDbId()).append("-");
        // avoid problems with non-ASCII file names 
        String fileName = attachment.getFileName();
        fileName = ExportFactory.replaceNonAsciiChars(fileName);
        sb.append(fileName);
        return sb.toString().replaceAll(" ", "_");
    }
    
    /**
     * Creates an ext-id for a tree-element
     * 
     * @param element a tree-element
     * @return ext-id for the tree-element
     */
    public static String createExtId(CnATreeElement element) {
        String extId = element.getExtId();
        if(extId==null || extId.isEmpty()) {
            extId = element.getId();
        }
        return extId;
    }
    
    /**
     * Creates an ext-id for an attachment
     * 
     * @param attachment an attachment of a tree-element
     * @return ext-id for the attachment
     */
    public static String createExtId(Attachment attachment) {
        String extId = attachment.getExtId();
        if(extId==null || extId.isEmpty()) {
            extId = attachment.getEntity().getId();
        }
        return extId;
    }

    /**
     * Replaces German non-ASCII chars in fileName,
     * which creates problems in Zip-Archives.
     * See: http://stackoverflow.com/questions/106367/add-non-ascii-file-names-to-zip-in-java
     * 
     * All other non-ASCII are still a known problem.
     * You can use commons-compress as solution...
     * 
     * @param fileName
     * @return fileName without German non-ASCII chars
     */
    private static String replaceNonAsciiChars(String fileName) {
        String result = fileName;
        result = result.replaceAll("ä", "ae");
        result = result.replaceAll("ü", "ue");
        result = result.replaceAll("ö", "oe");
        result = result.replaceAll("Ä", "Ae");
        result = result.replaceAll("Ü", "Ue");
        result = result.replaceAll("Ö", "Oe");
        result = result.replaceAll("ß", "ss");
        return result;
    }



    
}
