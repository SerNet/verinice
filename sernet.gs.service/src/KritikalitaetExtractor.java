import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/

/**
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class KritikalitaetExtractor {

    private static final String SYNCATTRIBUTE_OPEN = "<syncAttribute>";
    private static final String SYNCATTRIBUTE_CLOSE = "</syncAttribute>";
    private static final String NAME_OPEN = "<name>";
    private static final String VALUE_OPEN = "<value>";
    private static final String VALUE_CLOSE = "</value>\n";
    private static final String CHILDREN_OPEN = "<children>";
    private static final String CHILDREN_CLOSE = "</children>";
    private static final String EXTID_OPEN = "<extId>";
    private static final String EXTOBJECTTYPE_OPEN = "<extObjectType>";
    private static final String SYNCOBJECT_OPEN = "<syncObject>";
    private static final String SYNCOBJECT_CLOSE = "</syncObject>";
    private static final String SYNCDATA_OPEN = "<syncData>";

    private static final String NEWLINE = "\n";

    /**
     * @param args
     * 
     */
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println("Starting Kritikalitaetsparser for vna-Files");
        System.out.println("===========================================");
        
        File xmlFile = null;

        if (args == null || args.length != 1) {
            System.out.println("wrong number of arguments, terminating");
            return;
        } else {
            xmlFile = new File(args[0]);
            if (!xmlFile.exists() || !xmlFile.isFile()) {
                System.out.println(args[0] + " is not a valid file, terminating");
                return;
            }
            System.out.println("given file:\t" + args[0]);


            // doItUsingXML(xmlFile);
            // String unformattedXML = doItLineBased(xmlFile);
            String unformattedXML = filterEverythingButCritcality(xmlFile);
            Document formattedXML = parseXmlFile(unformattedXML);
            saveChangedXML(formattedXML);
        }
        System.out.println("Duration:\t" + String.valueOf((System.currentTimeMillis() - startTime) / 1000) + " seconds");
    }

    private static String filterEverythingButCritcality(File xmlFile) {
        StringBuilder output = new StringBuilder();
        StringBuilder tmpBuilder = new StringBuilder();
        BufferedReader in = null;

        boolean syncAttributeOpen = false;
        boolean kritikalitaetFound = false;
        boolean extIdOpen = false;
        boolean syncObjectOpen = false;

        try {
            in = new BufferedReader(new FileReader(xmlFile));
            String line = null;
            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(EXTID_OPEN)) {
                    line.hashCode();
                }

                // handle suffix
                if (SYNCDATA_OPEN.equals(line)) {
                    tmpBuilder.append(line).append(NEWLINE);
                    output.append(tmpBuilder.toString());
                    tmpBuilder.setLength(0);
                    continue;
                }

                // handle syncAttribute
                if (SYNCATTRIBUTE_OPEN.equals(line)) {
                    tmpBuilder.append(SYNCOBJECT_OPEN).append(NEWLINE);
                    syncObjectOpen = true;
                    tmpBuilder.append(line).append(NEWLINE);
                    syncAttributeOpen = true;
                    continue;
                }

                if (line.startsWith(EXTID_OPEN)) {
                    tmpBuilder.append(line).append(NEWLINE);
                    extIdOpen = true;
                    continue;
                }

                if (line.startsWith(EXTOBJECTTYPE_OPEN) && extIdOpen && line.contains("netzkomponente")) {
                    boolean addedContent = false;
                    if (tmpBuilder.toString().endsWith(SYNCATTRIBUTE_CLOSE + NEWLINE) || output.toString().endsWith(SYNCATTRIBUTE_CLOSE + NEWLINE)) {
                        tmpBuilder.append(line).append(NEWLINE);
                        addedContent = true;
                    }

                    if (syncObjectOpen && !(tmpBuilder.toString().endsWith(SYNCOBJECT_CLOSE + "\n") || output.toString().endsWith(SYNCOBJECT_CLOSE + "\n"))) {
                        tmpBuilder.append(SYNCOBJECT_CLOSE).append(NEWLINE);
                        syncObjectOpen = false;
                    }
                    if(addedContent){
                        output.append(tmpBuilder.toString());
                        addedContent = false;
                    } 
                    tmpBuilder.setLength(0);
                    continue;
                }

                // handle kritikalitaetprop
                if (line.contains("netzkomponente_kritikalitaet")) {
                    kritikalitaetFound = true;
                }

                if (SYNCATTRIBUTE_CLOSE.equals(line)) {
                    if (kritikalitaetFound) {
                        tmpBuilder.append(line).append(NEWLINE);
                        output.append(tmpBuilder.toString());
                        kritikalitaetFound = false;
                    }
                    syncAttributeOpen = false;
                    tmpBuilder.setLength(0);
                    continue;
                }

                if (line.equals(CHILDREN_OPEN) || line.equals(CHILDREN_CLOSE)) {
                    continue;
                }


                tmpBuilder.append(line).append(NEWLINE);
            }
            output.append(tmpBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            FileUtils.writeStringToFile(new File("/tmp/kritikalitaet_plain.txt"), output.toString(), "UTF-8");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return output.toString();

    }


    private static String doItLineBased(File xmlFile) {

        
        
        StringBuilder suffix = new StringBuilder();

        suffix.append("<syncMapping>\n").append("<mapObjectType extId=\"netzkomponente\" intId=\"netzkomponente\">\n ").append("<mapAttributeType extId=\"netzkomponente_protokolle\" intId=\"netzkomponente_protokolle\"/>\n").append("<mapAttributeType extId=\"netzkomponente_kuerzel\" intId=\"netzkomponente_kuerzel\"/>\n").append("<mapAttributeType extId=\"netzkomponente_tag\" intId=\"netzkomponente_tag\"/>\n")
                .append("<mapAttributeType extId=\"netzkomponente_kapazitaet\" intId=\"netzkomponente_kapazitaet\"/>\n").append("<mapAttributeType extId=\"netzkomponente_dokument\" intId=\"netzkomponente_dokument\"/>\n").append("<mapAttributeType extId=\"netzkomponente_name\" intId=\"netzkomponente_name\"/>\n").append("<mapAttributeType extId=\"netzkomponente_extnetz\" intId=\"netzkomponente_extnetz\"/>\n")
                .append("<mapAttributeType extId=\"netzkomponente_kabel\" intId=\"netzkomponente_kabel\"/>\n").append("<mapAttributeType extId=\"netzkomponente_kritikalitaet\" intId=\"netzkomponente_kritikalitaet\"/>\n").append("<mapAttributeType extId=\"netzkomponente_erlaeuterung\" intId=\"netzkomponente_erlaeuterung\"/>\n").append("<mapAttributeType extId=\"nkkomponente_ergaenzendeanalyse\" intId=\"nkkomponente_ergaenzendeanalyse\"/>\n")
                .append("<mapAttributeType extId=\"nkkomponente_risikoanalyse\" intId=\"nkkomponente_risikoanalyse\"/>\n").append("<mapAttributeType extId=\"nkkomponente_risikoanalyse_begruendung\" intId=\"nkkomponente_risikoanalyse_begruendung\"/>\n").append("<mapAttributeType extId=\"nkkomponente_ergaenzendeanalyse_entscheidung_durch\" intId=\"nkkomponente_ergaenzendeanalyse_entscheidung_durch\"/>\n")
                .append("<mapAttributeType extId=\"nkkomponente_ergaenzendeanalyse_entscheidung_am\" intId=\"nkkomponente_ergaenzendeanalyse_entscheidung_am\"/>\n").append("<mapAttributeType extId=\"nkkomponente_ergaenzendeanalyse_entscheidung_bis\" intId=\"nkkomponente_ergaenzendeanalyse_entscheidung_bis\"/>\n").append("</mapObjectType>").append("</syncMapping>").append("</syncRequest>");

        StringBuilder output = new StringBuilder();
        StringBuilder tmpBuffer = new StringBuilder();
        boolean syncAttributeOpen = false;
        boolean valueOpen = false;
        boolean extIdOpen = false;
        boolean childrenOpen = false;
        boolean syncObjectOpen = false;
        BufferedReader in = null;

        // no children items at all
        /**
         * <syncObject> <syncAttribute> <name>netzkomponente_kritikalitaet
         * </name> <value>netzkomponente_kritikalitaet_1</value>
         * <value>netzkomponente_kritikalitaet_5</value> </syncAttribute>
         * <extId>E3EDF79D-1F6E-454A-8FF9-2D269DC6F81D</extId>
         * <extObjectType>netzkomponente</extObjectType>
         * <syncAttribute> <name>netzkomponente_kritikalitaet</name>
         * <value>netzkomponente_kritikalitaet_1</value> </syncAttribute>
         * <extId>366E1E30-A0F3-46E3-9989-94D8EC23767F</extId>
         * <extObjectType>netzkomponente</extObjectType> </syncObject>
         * <syncObject>
         */

        try {
            in = new BufferedReader(new FileReader(xmlFile));
            String line = null;
            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(SYNCDATA_OPEN)) {
                    tmpBuffer.append(line).append(NEWLINE);
                    output.append(tmpBuffer.toString());
                    tmpBuffer.setLength(0);
                    continue;
                }

                if (SYNCOBJECT_CLOSE.equals(line)) {
                    line.hashCode();
                }

                if (line.startsWith(SYNCOBJECT_OPEN) && !syncObjectOpen) {
                    tmpBuffer.append(line).append(NEWLINE);
                    syncObjectOpen = true;
                    continue;
                }

                if (line.startsWith(SYNCOBJECT_CLOSE) && syncObjectOpen) {
                        tmpBuffer.append(line).append(NEWLINE);
                        output.append(tmpBuffer.toString());
                         tmpBuffer.setLength(0);
                        // output.append(tmpBuffer.toString());
                        syncObjectOpen = false;
                    continue;
                } else if (line.startsWith(SYNCOBJECT_CLOSE) && !syncObjectOpen) {
                    throw new RuntimeException("SyncObject Closed without beeing opened!");
                }

                if (line.startsWith(CHILDREN_OPEN) || line.startsWith(CHILDREN_CLOSE)) {
                    // tmpBuffer.append(line).append(NEWLINE);
                    // childrenOpen = true;
                    continue;
                }
                // if (line.startsWith(CHILDREN_CLOSE) && childrenOpen) {
                // if (tmpBuffer.length() > (CHILDREN_OPEN.length() +
                // NEWLINE.length()) || tmpBuffer.length() == 0) {
                // tmpBuffer.append(line).append(NEWLINE);
                // output.append(tmpBuffer.toString());
                // }
                // tmpBuffer.setLength(0);
                //
                // childrenOpen = false;
                // continue;
                // } else if (line.startsWith(CHILDREN_CLOSE) && !childrenOpen)
                // {
                // continue;
                // }

                if (line.startsWith(EXTID_OPEN)) {
                    extIdOpen = true;
                    tmpBuffer.append(line).append(NEWLINE);
                    continue;
                }

                if (line.startsWith(EXTOBJECTTYPE_OPEN) && extIdOpen) {
                    if (line.contains("netzkomponente")) {
                        tmpBuffer.append(line).append(NEWLINE);
                        // output.append(tmpBuffer.toString());
                    }
                    // tmpBuffer.setLength(0);
                    extIdOpen = false;
                    continue;
                }

                if (line.startsWith(SYNCATTRIBUTE_OPEN)) {
                    // if (tmpBuffer.length() > 0) {
                    // output.append(tmpBuffer.toString());
                    // tmpBuffer.setLength(0);
                    // }
                    syncAttributeOpen = true;
                    tmpBuffer.append(line).append(NEWLINE);
                    continue;
                }

                if (line.startsWith(SYNCATTRIBUTE_CLOSE) && syncAttributeOpen) {
                    syncAttributeOpen = false;
                    valueOpen = false;
                    tmpBuffer.append(line).append(NEWLINE);
                    continue;
                }

                if (!line.startsWith(SYNCATTRIBUTE_OPEN) && !syncAttributeOpen) {
                    tmpBuffer.append(line).append(NEWLINE);
                    continue;
                }

                if (line.startsWith(NAME_OPEN) && line.contains("netzkomponente_kritikalitaet") && syncAttributeOpen) {
                    tmpBuffer.append(line).append(NEWLINE);
                    valueOpen = true;
                    continue;
                } else if (line.startsWith(NAME_OPEN) && !line.contains("netzkomponente_kritikalitaet")) {
                    continue;
                }
                if (line.startsWith(VALUE_OPEN) && valueOpen) {
                    tmpBuffer.append(line).append(NEWLINE);
                    continue;
                }

                // tmpBuffer.append(line).append(NEWLINE);


            }
            // FileUtils.writeStringToFile(new File(OUTPUT_FILE),
            // output.toString(), "UTF-8");
            // System.out.println("File written to:\t " + OUTPUT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
        }
        String outputString = output.toString().trim();
        // System.out.println(output.toString());
        if (!outputString.endsWith("</syncData>")) {
            output.append("</syncData>").append(NEWLINE);
        }
        output.append(suffix.toString());
        String fixedOutput = removeDuplicates(output.toString());
        fixedOutput = removeDoubleSyncAttributes(fixedOutput);
        fixedOutput = encapsulateObjects(fixedOutput);
        try {
            FileUtils.writeStringToFile(new File("/tmp/kritikalitaet_plain.txt"), fixedOutput, "UTF-8");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return fixedOutput;

    }

    private static String encapsulateObjects(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input, "\n");
        StringBuilder output = new StringBuilder();
        // add </syncObject><syncObject> after every extObjectType
        boolean syncObjectOpen = false;
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            line = line.trim();
            //
            // if (SYNCOBJECT_OPEN.equals(line)) {
            // syncObjectOpen = true;
            // } else if (SYNCOBJECT_CLOSE.equals(line)) {
            // syncObjectOpen = false;
            // }

            if (line.startsWith(EXTID_OPEN)) {
                output.append(line).append(NEWLINE);
                output.append(SYNCOBJECT_CLOSE).append(NEWLINE).append(SYNCOBJECT_OPEN).append(NEWLINE);
            } else {
                output.append(line).append(NEWLINE);
            }
        }

        return output.toString();

    }

    private static String removeDoubleSyncAttributes(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input, "\n");

        StringBuilder output = new StringBuilder();

        StringBuilder tmpBuilder = new StringBuilder();

        boolean syncAttrOpen = false;

        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            line = line.trim();
            if (line.equals(SYNCATTRIBUTE_OPEN) && !syncAttrOpen) {
                syncAttrOpen = true;
                tmpBuilder.append(line).append(NEWLINE);
                continue;
            } else if (line.equals(SYNCATTRIBUTE_CLOSE) && syncAttrOpen) {
                if (tmpBuilder.length() == SYNCATTRIBUTE_OPEN.length() + NEWLINE.length()) {
                    tmpBuilder.setLength(0);
                    syncAttrOpen = false;
                    continue;
                } else {
                    tmpBuilder.append(line).append(NEWLINE);
                    output.append(tmpBuilder.toString());
                    tmpBuilder.setLength(0);
                    syncAttrOpen = false;
                    continue;
                }
            } else if (line.startsWith(EXTID_OPEN)) {
                if (output.toString().endsWith(VALUE_CLOSE)) {
                    output.append(line).append(NEWLINE);
                }
                continue;
            }

            else {
                output.append(line).append(NEWLINE);
            }
        }

        return output.toString();
    }

    private static String removeDuplicates(String input) {
        StringBuilder output = new StringBuilder();
        StringBuilder tmpBuilder = new StringBuilder();

        StringTokenizer tokenizer = new StringTokenizer(input, "\n");

        boolean syncAttributeOpen = false;
        boolean extIdopen = false;

        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            line = line.trim();

            if (line.startsWith(SYNCDATA_OPEN)) {
                tmpBuilder.append(line).append(NEWLINE);
                output.append(tmpBuilder.toString());
                tmpBuilder.setLength(0);
                continue;
            }

            if (line.startsWith(EXTID_OPEN) && !extIdopen) {
                tmpBuilder.append(line).append(NEWLINE);
                extIdopen = true;
                continue;
            } else if (line.startsWith(EXTID_OPEN) && extIdopen) {
                extIdopen = false;
                tmpBuilder.setLength(0);
                continue;
            }

            if (line.startsWith(EXTOBJECTTYPE_OPEN) && extIdopen) {
                tmpBuilder.append(line).append(NEWLINE);
                output.append(tmpBuilder.toString());
                tmpBuilder.setLength(0);
                extIdopen = false;
                continue;
            } else if (line.startsWith(EXTOBJECTTYPE_OPEN) && !extIdopen) {

                continue;
            }

            if (line.startsWith(SYNCATTRIBUTE_OPEN) && !syncAttributeOpen) {
                tmpBuilder.append(line).append(NEWLINE);
                syncAttributeOpen = true;
                continue;
            } else if (line.startsWith(SYNCATTRIBUTE_OPEN) && syncAttributeOpen) {
                throw new RuntimeException("double open syncAttribute");
            }

            if (line.startsWith(SYNCATTRIBUTE_CLOSE) && syncAttributeOpen) {
                if (tmpBuilder.length() > (SYNCATTRIBUTE_OPEN.length() + NEWLINE.length())) {
                    tmpBuilder.append(line).append(NEWLINE);
                    output.append(tmpBuilder.toString());
                    tmpBuilder.setLength(0);
                    syncAttributeOpen = false;
                    continue;
                } else if (tmpBuilder.length() == (SYNCATTRIBUTE_OPEN.length() + NEWLINE.length())) {
                    tmpBuilder.setLength(0);
                    syncAttributeOpen = false;
                    continue;
                } else {
                    syncAttributeOpen = false;
                    continue;
                }

            } else if (line.startsWith(SYNCATTRIBUTE_CLOSE) && !syncAttributeOpen) {
                throw new RuntimeException("syncattribute closed without beeing open");
            }

            // everything else
            tmpBuilder.append(line).append(NEWLINE);

        }

        output.append(tmpBuilder.toString());


        return output.toString();

    }

    private static void doItUsingXML(File xmlFile) {
        Document doc = parseXMLFile(xmlFile);
        doc.getDocumentElement().normalize();
        readAllEntities(doc);
        saveChangedXML(doc);
    }

    private static Document parseXMLFile(File xmlFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = null;

        // uncomment this to enable validating of the schema:
        try {

            parser = factory.newDocumentBuilder();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            parser.setErrorHandler(new ErrorHandler() {
                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw new RuntimeException(exception);
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw new RuntimeException(exception);
                }

                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    Logger.getLogger(this.getClass()).debug("Parser warning: " + exception.getLocalizedMessage());
                }
            });
            return parser.parse(xmlFile);

        } catch (IOException ie) {
            ie.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }
        
    private static Document readAllEntities(Document doc) {
        // switch(iteration){
        // case 1:
        // return firstIteration(doc);
        // case 2:
        // return secondIteration(doc);
        // default:

        doc = firstIteration(doc);
        doc = secondIteration(doc);
        return doc;
    }


    private static Document secondIteration(Document doc) {
        NodeList entities = doc.getElementsByTagName("syncAttribute");
        for (int i = 0; i < entities.getLength(); i++) {
            Node syncAttributeNode = entities.item(i);
            for (int c = 0; c < syncAttributeNode.getChildNodes().getLength(); c++) {
                Node grandChildren = syncAttributeNode.getChildNodes().item(c);
                if (grandChildren.getNodeType() == Node.ELEMENT_NODE && !(grandChildren.getTextContent().contains("netzkomponente_kritikalitaet"))) {
                    Node parentNode = syncAttributeNode.getParentNode();
                    if (parentNode != null) {
                        parentNode.removeChild(syncAttributeNode);
                    }
                }
            }
        }
        doc.getDocumentElement().normalize();

        return doc;
    }

    private static Document firstIteration(Document doc) {
        NodeList entities = doc.getElementsByTagName("children");
        for (int i = 0; i < entities.getLength(); i++) {

            Node childrenNode = entities.item(i);
            for (int c = 0; c < childrenNode.getChildNodes().getLength(); c++) {
                Node grandChild = childrenNode.getChildNodes().item(c);
                String textContent = grandChild.getTextContent();
                if (!"netzkategorie".equals(textContent) && !"netzkomponente".equals(textContent)) {
                    Node parentNode = childrenNode.getParentNode();
                    if (parentNode != null) {
                        parentNode.removeChild(childrenNode);
                    }
                }
                
            }


            // readChildElements(entityObj, null);
        }
        doc.getDocumentElement().normalize();

        return doc;
    }

    private static void saveChangedXML(Document doc) {
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("/tmp/changedDVZfile.xml"));

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);

            transformer.transform(source, result);


            System.out.println("File saved!");
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setNamespaceAware(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}



