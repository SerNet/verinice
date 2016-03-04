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
package sernet.verinice.report.service.impl.security;

import java.io.File;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.security.Permission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import org.apache.log4j.Logger;

import sernet.verinice.security.report.ReportSecurityContext;
import sernet.verinice.security.report.ReportSecurityException;

/**
 * Manager ensures that no unauthorized code gets called from within a report template. 
 * neither using beanshell or javascript
 * 
 * notice: 
 * - violations caused by misuse of javascript (rhino-engine) will get logged
 *  in the report-logfile (rhino-engine runs within the context of the birt-engine)
 *  
 * - violations nested in beanshell are logged in the client-logile (beanshell-interpreter is running in 
 * sernet.verinice.oda.driver.impl.Query (in the verinice-Client-Context)
 * 
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class ReportSecurityManager extends SecurityManager {
    
    private static final Logger LOG = Logger.getLogger(ReportSecurityManager.class);
    
    private static Map<String, List<String>> allowedPermissionsAndActionsMap = new HashMap<>();
    
    private boolean protectionEnabled = true;
    
    private final static String SUN_LIBRARY_PATH = System.getProperty("sun.boot.library.path");
    
    static {
        allowedPermissionsAndActionsMap.put(RuntimePermission.class.getCanonicalName(), Arrays.asList(new String[]{
                "createClassLoader",
                "getClassLoader",
                "accessDeclaredMembers",
                "accessClassInPackage.sun.util.resources",
                "accessClassInPackage.sun.util.resources.de",
                "accessClassInPackage.sun.misc",
                "accessClassInPackage.sun.awt.resources",
                "setContextClassLoader",
                "loadLibrary.awt",
                "loadLibrary.libawt_xawt.so",
                "getenv.DISPLAY",
                "getProtectionDomain"
        }));
        
        allowedPermissionsAndActionsMap.put(ReflectPermission.class.getCanonicalName(), Arrays.asList(new String[]{"suppressAccessChecks"}));
        allowedPermissionsAndActionsMap.put(LoggingPermission.class.getCanonicalName(), Arrays.asList(new String[]{"control"}));
        allowedPermissionsAndActionsMap.put(NetPermission.class.getCanonicalName(), Arrays.asList(new String[]{"specifyStreamHandler"}));
        allowedPermissionsAndActionsMap.put("org.eclipse.equinox.log.LogPermission", Arrays.asList(new String[]{"*"}));
        
    }
    
    private ReportSecurityContext reportSecurityContext;
    
    public ReportSecurityManager(ReportSecurityContext reportSecurityContext) { 
        super();
        this.reportSecurityContext = reportSecurityContext;
    }
    
    /**
     * preventes use of Runtime.getRuntime().exec("rm -rf");
     */
    @Override
    public void checkExec(String cmd) {
        throw new ReportSecurityException("Execution of code not allowed within verinice Report-Context in general");
    }
    

    
    /**
     * return
     */
    @Override
    public void checkPermission(Permission perm){
        if(!protectionEnabled){
            return;
        }
        // enable loading of libraries from the jre
        if(perm instanceof RuntimePermission && perm.getName().startsWith("loadLibrary."+SUN_LIBRARY_PATH)){
            return;
        // enable osgi-stuff
        }else if("org.osgi.framework.AdminPermission".equals(perm.getClass().getCanonicalName())){
            return;
        } else if("org.osgi.framework.ServicePermission".equals(perm.getClass().getCanonicalName())) {
            return;
        // enable reading, writing and deleting of all(!) properties
        } else if (perm instanceof PropertyPermission ){
            return; // RunAndRenderTask.setReportRunnable(..) requires ("java.util.PropertyPermission" "*" "read,write")
        // enable reading, writing, deleting of files on some custom defined places
        }else if(perm instanceof FilePermission){
            handleFilePermission(perm);
        // allow some more (static) actions on RuntimePermissions and 4 other permissions
        } else if(allowedPermissionsAndActionsMap.containsKey(perm.getClass().getCanonicalName())){
            lookupPermissionMap(perm);
        } else { // default | everything else is not on the whitelist, so throw exception!
            StringBuilder sb = new StringBuilder().append("Permission")
                    .append("<").append(perm.getClass().getCanonicalName()).append(">")
                    .append(":\t")
                    .append(perm.getName())
                    .append(" with action(s):\t")
                    .append(perm.getActions())
                    .append(" not allowed in verinice Report Context");
            throw new ReportSecurityException(sb.toString());
        }
    }

    /** checks if permission name in combination with permission action is whitelisted
     * 
     * @param perm
     */
    private void lookupPermissionMap(Permission perm) {
        List<String> allowedActions = allowedPermissionsAndActionsMap.get(perm.getClass().getCanonicalName());
        for(String allowedAction : allowedActions){
            if(allowedAction.equals(perm.getName())){
                return;
            }
        }
        StringBuilder sb = new StringBuilder().append("Permission:\t")
                .append(perm.getName())
                .append(" with action(s):\t")
                .append(perm.getActions())
                .append(" not allowed in verinice Report Context");
        LOG.debug(sb.toString());
        throw new ReportSecurityException(sb.toString());
    }

    /**
     *  allow writing && deleting files on some custom defined places
     * @param perm
     * @throws ReportSecurityException
     */
    private void handleFilePermission(Permission perm) throws ReportSecurityException{
        FilePermission filePermission = (FilePermission)perm;

        if(filePermission.getActions().contains("delete") || filePermission.getActions().contains("write")){
            if(perm.getName().startsWith(reportSecurityContext.getLogFileLocation())){
                return;
            } else if (reportSecurityContext.getReportOptions().getOutputFile().getAbsolutePath().equals(perm.getName())) {
                return;
            } else if(("file:" + filePermission.getName()).equals(System.getProperty("osgi.instance.area") + "log")){
                return;
            } else if(filePermission.getName().equals(System.getProperty("osgi.instance.area") + File.separator + ".metadata" + File.separator + ".log")){
                return;
            } else if(("file:" + filePermission.getName()).startsWith(System.getProperty("osgi.configuration.area"))){
                return;
            } else {
                StringBuilder sb = new StringBuilder().append("Permission")
                        .append("<").append(perm.getClass().getCanonicalName()).append(">")
                        .append(":\t").append(perm.getName())
                        .append(" with action(s):\t")
                        .append(perm.getActions())
                        .append(" not allowed in verinice Report Context");
                throw new ReportSecurityException(sb.toString()); 
            }
        }
    }
    
    /**
     * this disables(!) this manager and behaves like
     * System.setSecurityManager(null)
     * so call with caution!
     * 
     * its needed here because of .. 
     * @param protectionEnabled
     */
    protected void setProtectionEnabled(boolean protectionEnabled){
        this.protectionEnabled = protectionEnabled;
    }
}
