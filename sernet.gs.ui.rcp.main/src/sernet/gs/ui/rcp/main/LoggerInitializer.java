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

import java.util.Enumeration;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Provides additional logging configuration.
 * 
 * Sets the logging path to the verinice workspace if no path is configured for
 * a {@link FileAppender}. Verinice can be also forced to use a specific log
 * path with setting the "-Dlogging.file=<path>" in verinice.ini. This will
 * override the defined file path of all FileAppender in the root logger.
 * 
 * It is also possible to provide your own log4j file with the parameter
 * "-Dlog4j.configuration" in the verinice.ini file.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class LoggerInitializer {

    private static final String LOG4J_CONFIGURATION_JVM_ENV_KEY = "log4j.configuration";
    private static final String LOGGING_PATH_KEY = "logging.file";
    private static final String DEFAULT_VERINICE_LOG = "log/verinice.log";
    private static final String WORKSPACE_PROPERTY_KEY = "osgi.instance.area";

    /**
     * Checks if the -Dlog4j.configuration system property is set and if so it
     * reconfigures the verinice client logger.
     * 
     */
    static void tryReadingCustomLog4jFile() {

        if (existsCustomLog4jConfigurationFile()) {
            configureWithCustomLog4jFile();
        }
    }

    /**
     * Checks all {#link {@link FileAppender}, if a log path is already defined.
     * If a log path is set by the system property "logging.file" in verinic.ini
     * this path is applied to all {@link FileAppender} and overrides always the
     * origin file path defined in a log4j file.
     */
    static void tryConfiguringLoggingPath() {
        String p = getLoggingPath();
        p = replaceInvalidSuffix(p);
        configureAllFileAppender(p);
    }

    private static boolean existsCustomLog4jConfigurationFile() {
        return System.getProperty(LOG4J_CONFIGURATION_JVM_ENV_KEY) != null;
    }

    private static void configureWithCustomLog4jFile() {

        Logger.getRootLogger().getLoggerRepository().resetConfiguration();
        String config = System.getProperty(LOG4J_CONFIGURATION_JVM_ENV_KEY);
        String extension = FilenameUtils.getExtension(config);

        if (extension.equals("xml")) {
            DOMConfigurator.configure(config);
        }

        else if ("properties".equals(extension)) {
            PropertyConfigurator.configure(config);
        }
    }

    private static void configureAllFileAppender(String p) {

        Logger log = Logger.getRootLogger();
        Enumeration<Appender> appenders = log.getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender appender = appenders.nextElement();
            if (appender instanceof FileAppender) {

                FileAppender fileAppender = (FileAppender) appender;
                if (!isFilePathConfigured(fileAppender) || isConfiguredInVeriniceIniFile()) {
                    fileAppender.setFile(p);

                    // without this call, the changes does have no effect
                    fileAppender.activateOptions();
                }
            }
        }
    }

    private static boolean isConfiguredInVeriniceIniFile() {
        return System.getProperty(LOGGING_PATH_KEY) != null;
    }

    private static boolean isFilePathConfigured(FileAppender fileAppender) {
        return fileAppender.getFile() != null;
    }

    private static String getLoggingPath() {

        String p = readFromVeriniceIniFile();

        if (p == null) {
            p = System.getProperty(WORKSPACE_PROPERTY_KEY);
            return p + DEFAULT_VERINICE_LOG;
        }

        return p;
    }

    private static String readFromVeriniceIniFile() {
        return System.getProperty(LOGGING_PATH_KEY);
    }

    private static String replaceInvalidSuffix(String path) {
        if (path.startsWith("file:/")) {
            path = path.replaceFirst("file:", "");
        }

        return path;
    }

}
