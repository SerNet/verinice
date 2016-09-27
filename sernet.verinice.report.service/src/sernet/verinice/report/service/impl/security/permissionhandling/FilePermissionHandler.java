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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.report.service.impl.security.permissionhandling;

import java.io.File;
import java.io.IOException;
import java.security.Permission;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;

import sernet.verinice.security.report.ReportSecurityContext;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class FilePermissionHandler extends AbstractPermissionHandler {
    
    private static final Logger LOG = Logger.getLogger(FilePermissionHandler.class);
    
    private static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
    private static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";
    private static final String SUFFIX_LOG_DIR = "log";
    private static final String SUFFIX_LOG_FILE = ".log";
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
    private static final String USER_HOME = "user.home";
    
    public FilePermissionHandler(ReportSecurityContext securityContext) {
        this.reportSecurityContext = securityContext;
    }

    @Override
    public void handlePermission(Permission permission) {

        if (permission.getActions().contains("delete") || permission.getActions().contains("write")){
            if((PREFIX_FILE + permission.getName()).startsWith(System.getProperty
                    (OSGI_INSTANCE_AREA) + ".metadata" + File.separator 
                    + ".plugins" + File.separator + "org.eclipse.core.runtime"
                    + File.separator + ".settings")){
                throwSecurityException(permission);
            }
            if (permission.getName().startsWith(reportSecurityContext.getLogFileLocation())){
                return;
            } else if (reportSecurityContext.getReportOptions().getOutputFile().getAbsolutePath().equals(permission.getName())) {
                return;
            } else if((PREFIX_FILE + permission.getName()).equals(System.getProperty(OSGI_INSTANCE_AREA) + SUFFIX_LOG_DIR)){ 
                return;
            } else if ((PREFIX_FILE + permission.getName()).equals(getCanonicalFile(System.getProperty(OSGI_INSTANCE_AREA)) + SUFFIX_LOG_DIR)){ 
                return;
            } else if((PREFIX_FILE + permission.getName()).startsWith(System.getProperty(OSGI_INSTANCE_AREA) + SUFFIX_LOG_DIR)){ 
                return;
            } else if ((PREFIX_FILE + permission.getName()).startsWith(getCanonicalFile(System.getProperty(OSGI_INSTANCE_AREA)) + SUFFIX_LOG_DIR)){
                return;
            } else if (permission.getName().equals(getCanonicalFile(System.getProperty(OSGI_INSTANCE_AREA)) + File.separator + ".metadata" + File.separator + SUFFIX_LOG_FILE)){
                return;
            } else if (permission.getName().equals(System.getProperty(OSGI_INSTANCE_AREA) + File.separator + ".metadata" + File.separator + SUFFIX_LOG_FILE)){ 
                return;                
            } else if ((PREFIX_FILE + permission.getName()).startsWith(getCanonicalFile(System.getProperty(OSGI_CONFIGURATION_AREA)))){
                return;
            } else if ((PREFIX_FILE + permission.getName()).startsWith(System.getProperty(OSGI_CONFIGURATION_AREA))){ 
                return;
            } else if ((permission.getName()).startsWith(getCanonicalFile(System.getProperty(JAVA_IO_TMPDIR)))){ 
                return;
            } else if ((permission.getName()).startsWith(System.getProperty(JAVA_IO_TMPDIR))){ 
                return;
            } else if ((permission.getName()).startsWith(getCanonicalFile(System.getProperty(USER_HOME)) + File.separator + ".java" + File.separator + "fonts")) {
                return;
            } else if ((permission.getName()).startsWith(System.getProperty(USER_HOME) + File.separator + ".java" + File.separator + "fonts")) { 
                return;
            } else if (permission.getName().startsWith(FilenameUtils.getFullPath(getCanonicalFile(reportSecurityContext.getReportOptions().getOutputFile().getAbsolutePath())))){
                return; // needed by win32, cause path there looks like c:\$path\.\reportOutput.pdf
            } else if (permission.getName().startsWith(FilenameUtils.getFullPath(reportSecurityContext.getReportOptions().getOutputFile().getAbsolutePath()))){
                return; // needed by win32, cause path there looks like c:\$path\.\reportOutput.pdf
            } else if (SystemUtils.IS_OS_MAC_OSX && permission.getName().startsWith("/private/var/folders/")){
                return; // handling osx tmp folder
            } else {
                throwSecurityException(permission);

            }
        }
    }
    
    /**
     * on old windows versions some System.getProperty-Calls return file
     * paths in the 8.3 format.
     * CanonicalPath gets the long version of it 
     * this prevents trouble in comparing different path versions
     * of the "same" directory
     */
    private String getCanonicalFile(String fileName){
        try {
            File f = new File(fileName);
            String canonicalPath = f.getCanonicalPath();
            LOG.debug("Canonical filename for:\t" + fileName + " is:\t" + canonicalPath);
            return canonicalPath;
        } catch (IOException e) {
            LOG.debug("Can not determine canonical path of:\t" + fileName, e);
        }
        return fileName;
    }

}
