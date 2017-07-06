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

import static org.apache.commons.io.FilenameUtils.concat;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static sernet.gs.ui.rcp.main.logging.LogDirectoryProvider.DEFAULT_VERINICE_LOG;
import static sernet.gs.ui.rcp.main.logging.LogDirectoryProvider.LOGGING_PATH_KEY;
import static sernet.gs.ui.rcp.main.logging.LogDirectoryProvider.LOG_FOLDER;
import static sernet.gs.ui.rcp.main.logging.LogDirectoryProvider.WORKSPACE_PROPERTY_KEY;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sernet.gs.ui.rcp.main.logging.LogDirectoryProvider;
import sernet.gs.ui.rcp.main.logging.LoggerInitializer;
import sernet.gs.ui.rcp.main.logging.WindowsLogDirectory;
import sernet.verinice.interfaces.IVeriniceConstants;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class LoggerInitializerTest {

    private static final String WITHOUT_FILE_PATH_LOG4J_XML = "without_file_path_log4j.xml";

    private static final String CUSTOM_LOG4J_XML = "custom_log4j.xml";

    Logger LOG = Logger.getLogger(LoggerInitializerTest.class.getName());

    @Before
    public void setUp() {
        clearEnvironment();
        System.setProperty(IVeriniceConstants.OSGI_INSTANCE_AREA, 
                System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR));
    }

    @Test
    public void getLogDirectoryTest() {
        String directory = System.getProperty(
                IVeriniceConstants.JAVA_IO_TMPDIR) + "/";
        String logFileWithUuid = directory + UUID.randomUUID().toString();
        System.setProperty(LOGGING_PATH_KEY, logFileWithUuid);

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals(directory, loggerInit.getLogDirectory());
    }

    @Test
    public void getLogInvalidDirectoryTest() {
        String directory = "";
        String logFileWithUuid = directory + UUID.randomUUID().toString();
        System.setProperty(LOGGING_PATH_KEY, logFileWithUuid);

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals(directory, loggerInit.getLogDirectory());
    }

    @Test
    public void getDefaultLogDirectory() {

        System.setProperty(IVeriniceConstants.OSGI_INSTANCE_AREA, 
                System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR));
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(WITHOUT_FILE_PATH_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals(
                System.getProperty(IVeriniceConstants.OSGI_INSTANCE_AREA) + 
                "/" + LOG_FOLDER, 
                loggerInit.getLogDirectory());
    }

    @Test
    public void getDefaultLogFilePath() {

        System.setProperty(IVeriniceConstants.OSGI_INSTANCE_AREA,
                System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR));
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(WITHOUT_FILE_PATH_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals(
                System.getProperty(IVeriniceConstants.OSGI_INSTANCE_AREA) + 
                "/" + LOG_FOLDER + DEFAULT_VERINICE_LOG, 
                getPathFromRootLogger());
    }

    @Test
    public void testCustomLog4jFile() throws SAXException, ParserConfigurationException, IOException {

        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(CUSTOM_LOG4J_XML).getPath());
        String path = extractLoggingPath(CUSTOM_LOG4J_XML);

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals(path.replaceFirst("\\$\\{java.io.tmpdir\\}",
                System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR)),
                getPathFromRootLogger());
    }

    @Test
    public void testGetDirectoryWithCustomLog4jFile() throws SAXException, ParserConfigurationException, IOException {

        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(CUSTOM_LOG4J_XML).getPath());
        String path = extractLoggingPath(CUSTOM_LOG4J_XML);

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        String expected = path.replaceFirst("\\$\\{java.io.tmpdir\\}",
                System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR));
        Assert.assertEquals(expected, getPathFromRootLogger());
    }

    @Test
    public void filePathFromVeriniceIniOverride() {

        String uuidLogFile = System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR) + 
                "/" + UUID.randomUUID().toString() + ".log";
        System.setProperty(LOGGING_PATH_KEY, uuidLogFile);
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(CUSTOM_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals(uuidLogFile, getPathFromRootLogger());
    }

    @Test
    public void testGetDirectoryWithVeriniceIniOverride() {

        String uuidLogFile = System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR) + 
                "/" + UUID.randomUUID().toString() + ".log";
        System.setProperty(LOGGING_PATH_KEY, uuidLogFile);
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY,
                getClass().getResource(CUSTOM_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals(System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR) + 
                "/", loggerInit.getLogDirectory());
    }

    @Test
    public void parseLog4jFile() throws ParserConfigurationException, SAXException, IOException {

        Document customLog4jConfig = loadLog4jFile(CUSTOM_LOG4J_XML);
        Assert.assertTrue("could not parse custom_log4j.xml", customLog4jConfig != null);
    }

    @Test
    public void removeInvalidPrefixes() {

        System.setProperty(LOGGING_PATH_KEY, "file:/tmp/" + DEFAULT_VERINICE_LOG);
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(CUSTOM_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals("/tmp/", loggerInit.getLogDirectory());

    }

    @Test
    public void removeInvalidWindowsPrefix() {

        System.setProperty(LOGGING_PATH_KEY, "file:\\C:\\tmp\\" + DEFAULT_VERINICE_LOG);
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(CUSTOM_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();
        loggerInit.setLogDirectoryProvider(new WindowsLogDirectory("file:\\C:\\tmp\\" + DEFAULT_VERINICE_LOG));

        Assert.assertEquals("C:/tmp/", loggerInit.getLogDirectory());
    }

    @Test
    public void writeToForbiddenPath() {

        System.setProperty(LOGGING_PATH_KEY, FilenameUtils.concat(File.separator + "root" + File.separator, "verinice-client.log"));
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY, getClass().getResource(WITHOUT_FILE_PATH_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();

        Assert.assertEquals(FilenameUtils.concat(System.getProperty(
                IVeriniceConstants.USER_HOME), "verinice" + File.separator),
                loggerInit.getLogDirectory());

    }

    @Test
    public void writeToNonExistingPath() {

        String uuid = concat(concat(System.getProperty(
                IVeriniceConstants.JAVA_IO_TMPDIR), UUID.randomUUID().toString()),
                "verinice-client.log");
        System.setProperty(LOGGING_PATH_KEY, uuid);
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY,
                getClass().getResource(WITHOUT_FILE_PATH_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();
        Assert.assertEquals(getFullPath(uuid), loggerInit.getLogDirectory());
    }

    @Test
    public void writeToDirectory() {

        System.setProperty(LOGGING_PATH_KEY, System.getProperty(
                IVeriniceConstants.JAVA_IO_TMPDIR));
        System.setProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY,
                getClass().getResource(WITHOUT_FILE_PATH_LOG4J_XML).getPath());

        LoggerInitializer loggerInit = LoggerInitializer.setupLogFilePath();
        Assert.assertEquals(FilenameUtils.concat(System.getProperty(
                IVeriniceConstants.USER_HOME), "verinice" + File.separator),
                loggerInit.getLogDirectory());
    }

    @After
    public void clearEnvironment() {
        System.clearProperty(LOGGING_PATH_KEY);
        System.clearProperty(LogDirectoryProvider.LOG4J_CONFIGURATION_JVM_ENV_KEY);
        System.clearProperty(WORKSPACE_PROPERTY_KEY);
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();
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

    public String getPathFromRootLogger() {
        Logger log = Logger.getRootLogger();
        Enumeration<Appender> appenders = log.getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender appender = appenders.nextElement();
            if (appender instanceof FileAppender) {

                FileAppender fileAppender = (FileAppender) appender;
                return fileAppender.getFile();
            }
        }

        return null;
    }

}
