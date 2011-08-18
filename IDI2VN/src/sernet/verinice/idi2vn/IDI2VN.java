/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.idi2vn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.bind.JAXB;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import sernet.verinice.service.commands.ExportFactory;

import de.sernet.sync.data.SyncAttribute;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.sync.SyncRequest;


/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class IDI2VN {
    
    /**
     * 
     */
    private static final String OBJECT_TAG = "object";

    /**
     * 
     */
    private static final String CAT_DATA = "cat_data";

    /**
     * 
     */
    private static final String DATA = "data";

    /**
     * 
     */
    private static final String TYPE = "type";

    /**
     * 
     */
    private static final String TITLE = "title";

    /**
     * 
     */
    private static final String SYSID = "sysid";

    // one object
    class IDIObject {
        String sysid;
        
        String title;
        
        String type;

        // information about each object
        // contained inside data, category, cat_data are XML elements that differ between each object 
        HashMap<String, String> data = new HashMap<String, String>();
    }
    
    
    private static final Logger log = Logger.getLogger(IDI2VN.class);
    
    private  Document doc;
    
    private static IDI2VN instance;
    
    private List<IDIObject> objects;

    private String sourceId;

    private String outputFile;
    
    public static void main(String[] args) {
        
        instance = new IDI2VN();
        instance.run(args);
    }
    
    private IDI2VN() {
        objects = new ArrayList<IDIObject>();
    }
    
  
    
    private void run(String[] args) {
        
        if (args.length < 2) {
            System.out.println("USAGE: java IDI2VN <sourceId> <input.xml> <output.xml>\n");
            System.out.println("Creates an XML file that can be imported in verinice.");
            System.exit(1);
        }
        
        sourceId = args[0];
        File xmlFile = new File(args[1]);
        if (args.length < 3) {
            outputFile = "output.xml";
        } else {
            outputFile = args[2];
        }
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        DocumentBuilder parser = null;
        try {
            parser = factory.newDocumentBuilder();
            log.debug("Getting i-do-it objects from " + xmlFile);
            parser.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException exception) throws SAXException {
                    throw new RuntimeException(exception);
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    throw new RuntimeException(exception);
                }

                public void warning(SAXParseException exception) throws SAXException {
                    Logger.getLogger(this.getClass()).debug("Parser warning: " + exception.getLocalizedMessage());
                }
            });
            FileInputStream fis = new FileInputStream(xmlFile);
            
            // remove null characters:
            String wholeFile = new Scanner(fis).useDelimiter("\\A").next();
            wholeFile = replaceBadChars(wholeFile);
            ByteArrayInputStream bis = new ByteArrayInputStream(wholeFile.getBytes());
            
            doc = parser.parse(bis);
            
            // read all objects from the i-do-it "XML" file
            readObjects();
            
            log.debug("Creating sync data...");
            SyncData syncData = createSyncData();
            
            log.debug("Reading sync map...");
            SyncMapping syncMap = readSyncMap();
            
            log.debug("Combining data and map...");
            SyncRequest sr = new SyncRequest();
            sr.setSyncData(syncData);
            sr.setSyncMapping(syncMap);
            sr.setSourceId(sourceId);
            sr.setDelete(true);
            sr.setInsert(true);
            sr.setUpdate(true);
            
            log.debug("Writing output file " + outputFile);
            FileOutputStream fos = new FileOutputStream(outputFile);
            ExportFactory.marshal(sr, fos);
            fos.close();
            log.debug("DONE");
            

        } catch (IOException ie) {
            log.error("Die XML Datei konnte nicht geladen werden! Bitte Pfad und Erreichbarkeit  überprüfen.", ie);
        } catch (SAXException e) {
            log.error("Die XML Datei ist defekt!", e);
        } catch (ParserConfigurationException e) {
            log.error("Der XML Parser ist falsch konfiguriert!", e);
        }
    }

    /**
     * @return
     * @throws FileNotFoundException 
     */
    private SyncMapping readSyncMap() throws FileNotFoundException {
        FileInputStream fis = new FileInputStream("idoit2verinice.xml");
        SyncMapping sm = JAXB.unmarshal(fis, SyncMapping.class);
        return sm;
    }

    /**
     * 
     */
    private SyncData createSyncData() {
        HashMap<String, SyncObject> categories = new HashMap<String, SyncObject>();
        
        SyncData syncData = new SyncData();
        
        for (IDIObject object : this.objects) {
        	if(object.sysid!=null && object.type!=null) {
	            SyncObject syncObject = new SyncObject();
	            syncObject.setExtId(object.sysid);
	            syncObject.setExtObjectType(object.type);
	            
	            // we have to set the abbreviation to prevent verinice to setting the default value on first edit:
	            SyncAttribute abbrev = new SyncAttribute();
	            abbrev.setName("idoit-abbrev");
	            abbrev.getValue().add(" ");
	            syncObject.getSyncAttribute().add(abbrev);
	
	            // set title and combined description:
	            SyncAttribute title = new SyncAttribute();
	            title.setName("idoit-title");
	            title.getValue().add(object.title);
	            syncObject.getSyncAttribute().add(title);
	
	            SyncAttribute desc = new SyncAttribute();
	            desc.setName("idoit-description");
	            desc.getValue().add(combineDetails(object));
	            syncObject.getSyncAttribute().add(desc);
	
	            // set original object type as tag for easy filtering by type:
	            SyncAttribute tag = new SyncAttribute();
	            tag.setName("idoit-tag");
	            tag.getValue().add(object.type);
	            syncObject.getSyncAttribute().add(tag);
	            
	            syncData.getSyncObject().add(syncObject);
	        } else {
	        	log.warn("Idoit objekt hat keine sysid oder keinen typ und kann nicht umgewandelt werden, Titel: " + object.title);
	        }
	    }
        return syncData;
    }

    /**
     * @param object
     * @return
     */
    private String combineDetails(IDIObject object) {
        StringBuilder result = new StringBuilder();
        result.append("*** I-Do-It Details: ***\n");
        for (String key : object.data.keySet()) {
            result.append(key + " : " + object.data.get(key) + "\n");
        }
        return result.toString();
    }

    /**
     * @param wholeFile
     * @return
     * @throws IOException 
     */
    private String replaceBadChars(String wholeFile) throws IOException {
       return wholeFile.replaceAll("\u0000", " ");
    }

    /**
     * 
     */
    private  void readObjects() {
        NodeList objects = doc.getElementsByTagName(OBJECT_TAG);
        for (int i = 0; i < objects.getLength(); i++) {
            Element object = (Element) objects.item(i);
            readObject(object);
        }
    }

    /**
     * @param object
     */
    private  void readObject(Element object) {
    
        IDIObject idiObject = new IDIObject();
        
        NodeList nodes = object.getChildNodes();
        allChildNodes: for (int i = 0; i < nodes.getLength(); ++i) {
            if (!(nodes.item(i) instanceof Element)) {
                continue allChildNodes;
            }
            Element child = (Element) nodes.item(i);
            
            if (child.getTagName().equals(SYSID)) {   
                idiObject.sysid = child.getTextContent();
            }
            else if (child.getTagName().equals(TITLE)) {   
                idiObject.title = child.getTextContent();
            }
            else if (child.getTagName().equals(TYPE)) {   
                idiObject.type = child.getTextContent();
            }
            else if (child.getTagName().equals(DATA)) {   
                readData(child, idiObject);
            }
            
        }
        objects.add(idiObject);
        log.debug("Added object: " + idiObject.sysid + ", " + idiObject.title + ", " + idiObject.type);
    }

    /**
     * Put all found data in the object as key, value pairs.
     * 
     * @param child 
     * @param idiObject
     */
    private void readData(Element data, IDIObject idiObject) {
        NodeList catDataElements = data.getElementsByTagName(CAT_DATA);
        allCatData: for (int i = 0; i < catDataElements.getLength(); i++) {
            if (!(catDataElements.item(i) instanceof Element)) {
                continue allCatData;
            }
            
            NodeList properties = ((Element)catDataElements.item(i)).getChildNodes();
            allProps: for (int j = 0; j < properties.getLength(); j++) {
                if (!(properties.item(j) instanceof Element)) {
                    continue allProps;
                }
                
                Element entry = (Element) properties.item(j);
                String key = entry.getTagName(); 
                String value = entry.getTextContent().replace("\n", "");
                idiObject.data.put(key, value);
                log.debug("  Added data: " + entry.getTagName() + " : " + entry.getTextContent());
            }
            
        }
    }
        
        
}


