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
import java.security.Permission;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;

import sernet.verinice.security.report.ReportSecurityContext;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class FilePermissionHandler extends AbstractPermissionHandler {
    
    private ReportSecurityContext reportSecurityContext;

    public FilePermissionHandler(ReportSecurityContext securityContext) {
        this.reportSecurityContext = securityContext;
    }

    @Override
    public void handlePermission(Permission permission) {

        if (permission.getActions().contains("delete") || permission.getActions().contains("write")){
            if((PREFIX_FILE + permission.getName()).startsWith(System.getProperty
                    ("osgi.instance.area") + ".metadata" + File.separator 
                    + ".plugins" + File.separator + "org.eclipse.core.runtime"
                    + File.separator + ".settings")){
                throwSecurityException(permission);
            }
            if (permission.getName().startsWith(reportSecurityContext.getLogFileLocation())){
                return;
            } else if (reportSecurityContext.getReportOptions().getOutputFile().getAbsolutePath().equals(permission.getName())) {// this wont work on windows, needs to be debuged
                return;
            } else if ((PREFIX_FILE + permission.getName()).equals(System.getProperty("osgi.instance.area") + "log")){
                return;
            } else if ((PREFIX_FILE + permission.getName()).startsWith(System.getProperty("osgi.instance.area") + "log")){
                return;
            } else if (permission.getName().equals(System.getProperty("osgi.instance.area") + File.separator + ".metadata" + File.separator + ".log")){
                return;
            } else if ((PREFIX_FILE + permission.getName()).startsWith(System.getProperty("osgi.configuration.area"))){
                return;
            } else if ((permission.getName()).startsWith(System.getProperty("java.io.tmpdir"))){
                return;
            } else if ((permission.getName()).startsWith(System.getProperty("user.home") + File.separator + ".java" + File.separator + "fonts")) {
                return;
            } else if (permission.getName().startsWith(FilenameUtils.getFullPath(reportSecurityContext.getReportOptions().getOutputFile().getAbsolutePath()))){
                return; // needed by win32, cause path there looks like c:\$path\.\reportOutput.pdf
            } else if (SystemUtils.IS_OS_MAC_OSX && permission.getName().startsWith("/private/var/folders/")){
                return; // handling osx tmp folder
            } else {
                throwSecurityException(permission);

            }
        }
    }

}
