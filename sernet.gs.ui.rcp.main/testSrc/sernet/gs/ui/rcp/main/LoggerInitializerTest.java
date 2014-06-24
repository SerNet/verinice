/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class LoggerInitializerTest extends LoggerInitializer {

    private static final String CUSTOM_LOG4J_XML = "custom_log4j.xml";
    Logger LOG = Logger.getLogger(LoggerInitializerTest.class.getName());

    @Test
    public void getLogPathTest() {
        clearEnvironment();
        String logFileWithUuid = "/tmp/verinice-" + UUID.randomUUID();
        System.setProperty(LOGGING_PATH_KEY, logFileWithUuid);
        Assert.assertEquals(logFileWithUuid, new LoggerInitializer().getLogPath());
    }

    @Test
    public void getDefaultLogPath() {
        clearEnvironment();
        System.setProperty("osgi.instance.area",  System.getProperty("java.io.tmpdir"));
        LoggerInitializer.tryReadingCustomLog4jFile();
        Assert.assertEquals(System.getProperty("osgi.instance.area") + "/" + LOG_FOLDER, new LoggerInitializer().getLogPath());
    }

    @Test
    public void testCustomLog4jFile() throws IOException, SAXException, ParserConfigurationException {
        
        clearEnvironment();
        System.setProperty(LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(CUSTOM_LOG4J_XML).getPath());
        String path = extractLoggingPath(CUSTOM_LOG4J_XML);
        
        LoggerInitializer.tryReadingCustomLog4jFile();
        
        Assert.assertEquals(path.replaceFirst("\\$\\{java.io.tmpdir\\}", System.getProperty("java.io.tmpdir")), getLogPath());
    }
    
    @Test
    public void pathFromVeriniceIniOverride()
    {
        clearEnvironment();
        String uuidLogFile = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString() + ".log";
        System.setProperty(LOGGING_PATH_KEY,  uuidLogFile);
        System.setProperty(LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(CUSTOM_LOG4J_XML).getPath());
        
        LoggerInitializer.tryReadingCustomLog4jFile();
        LoggerInitializer.tryConfiguringLoggingPath();
        
        Assert.assertEquals(uuidLogFile, getLogPath()); 
    }


    @Test
    public void parseLog4jFile() throws ParserConfigurationException, SAXException, IOException {

        Document customLog4jConfig = loadLog4jFile(CUSTOM_LOG4J_XML);
        Assert.assertTrue("could not parse custom_log4j.xml", customLog4jConfig != null);
    }

    @After
    public void clearEnvironment() {
        System.clearProperty(LOGGING_PATH_KEY);
        System.clearProperty(LOG4J_CONFIGURATION_JVM_ENV_KEY);
    }

    private String extractLoggingPath(String customLog4jXml) throws ParserConfigurationException, SAXException, IOException {
        
        Document document = loadLog4jFile(customLog4jXml);
        NodeList nodes = document.getElementsByTagName("appender");

        String path = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (hasAttributeName("FILE", node)) {
                path = getLog4jPathFromFileParamValue(node);
            }
        }

        return path;
    }

    private Document loadLog4jFile(String name) throws ParserConfigurationException, SAXException, IOException {

        URL costumLog4jFile = getClass().getResource(name);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(costumLog4jFile.getPath());

    }

    private boolean hasAttributeName(String name, Node node) {
        NamedNodeMap nodeMap = node.getAttributes();

        if (nodeMap == null) {
            return false;
        }

        for (int j = 0; j < nodeMap.getLength(); j++) {
            if (name.equals(nodeMap.item(j).getNodeValue())) {
                return true;
            }
        }

        return false;
    }

    private String getLog4jPathFromFileParamValue(Node node) {

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {

            Node child = node.getChildNodes().item(i);

            if (hasAttributeName("File", child)) {
                return getNodeAttributeValueByName("value", child);
            }
        }

        return null;
    }

    private String getNodeAttributeValueByName(String name, Node node) {

        NamedNodeMap nodeMap = node.getAttributes();

        if (nodeMap != null) {
            for (int j = 0; j < nodeMap.getLength(); j++) {
                if (name.equals(nodeMap.item(j).getNodeName())) {
                    return nodeMap.item(j).getNodeValue();
                }
            }
        }

        return null;
    }

}
