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
 * Manager ensures that there is no "nasty" code called from within a report template. neither using beanshell or javascript
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
    
    static {
        allowedPermissionsAndActionsMap.put(RuntimePermission.class.getCanonicalName(), Arrays.asList(new String[]{
                "createClassLoader",
                "getClassLoader",
                "accessDeclaredMembers",
                "accessClassInPackage.sun.util.resources",
                "accessClassInPackage.sun.util.resources.de",
//                "accessClassInPackage.sun.text.resources.zh",
                "accessClassInPackage.sun.misc",
                "accessClassInPackage.sun.awt.resources",
                "setContextClassLoader",
                "loadLibrary.awt",
                "loadLibrary./opt/java/jdk1.8.0_45/jre/lib/amd64/libawt_xawt.so",
//                "loadLibrary",
                "getenv.DISPLAY",
        "getProtectionDomain"
                }));
//        allowedPermissionsAndActionsMap.put(PropertyPermission.class.getCanonicalName(), Arrays.asList(new String[]{
////                "sun.util.logging.disableCallerCheck", 
////                "org.eclipse.emf.common.util.URI.encodePlatformResourceURIs",
////                "org.eclipse.emf.common.util.URI.archiveSchemes",
////                "org.eclipse.emf.ecore.plugin.EcorePlugin.doNotLoadResourcesPlugin",
////                "org.eclipse.emf.ecore.EPackage.Registry.INSTANCE",
////                "javax.xml.parsers.SAXParserFactory",
////                "jdk.xml.entityExpansionLimit",
////                "jdk.xml.maxOccurLimit",
////                "maxOccurLimit",
////                "jdk.xml.elementAttributeLimit",
////                "elementAttributeLimit",
////                "jdk.xml.totalEntitySizeLimit",
////                "jdk.xml.maxGeneralEntitySizeLimit",
////                "jdk.xml.maxParameterEntitySizeLimit",
////                "jdk.xml.maxElementDepth",
////                "javax.xml.accessExternalDTD",
////                "javax.xml.accessExternalSchema",
////                "*",
////                "user.dir",
////                "user.home",
////                "java.io.tmpdir",
//                "line.separator",
////                "debug",
////                "trace",
////                "localscoping",
////                "outfile",
//                "osgi.instance.area",
//                "osgi.configuration.area",
////                "webapplication.projectclasspath"
//                "org.apache.commons.logging.Log",
//                "org.apache.commons.logging.log",
//                "org.apache.commons.logging.Log.allowFlawedContext",
//                "org.apache.commons.logging.Log.allowFlawedDiscovery",
//                "org.apache.commons.logging.Log.allowFlawedHierarchy",
//                "org.apache.commons.logging.LogFactory"
//        }));
        allowedPermissionsAndActionsMap.put(ReflectPermission.class.getCanonicalName(), Arrays.asList(new String[]{"suppressAccessChecks"}));
        allowedPermissionsAndActionsMap.put(LoggingPermission.class.getCanonicalName(), Arrays.asList(new String[]{"control"}));
        allowedPermissionsAndActionsMap.put(NetPermission.class.getCanonicalName(), Arrays.asList(new String[]{"specifyStreamHandler"}));
        allowedPermissionsAndActionsMap.put("org.eclipse.equinox.log.LogPermission", Arrays.asList(new String[]{"*"}));
//        allowedPermissionsAndActionsMap.put(SocketPermission.class.getCanonicalName(), Arrays.asList(new String[]{"localhost"}));
//        allowedPermissionsAndActionsMap.put("", Arrays.asList(new String[]{""}));
//        allowedPermissionsAndActionsMap.put("org.apache.commons.logging.Log", Arrays.asList(new String[]{"read"}));
        
        
        
    }
    
    private ReportSecurityContext reportSecurityContext;
    
    public ReportSecurityManager(ReportSecurityContext reportSecurityContext) { 
        super();
        this.reportSecurityContext = reportSecurityContext;
    }
    
    
    @Override
    public void checkExec(String cmd) {
        throw new ReportSecurityException("Execute not allowed within verinice Report-Context");
    }
    

    
    /**
     * return
     */
    @Override
    public void checkPermission(Permission perm){
        if(!protectionEnabled){
            return;
        }
        if("org.osgi.framework.AdminPermission".equals(perm.getClass().getCanonicalName())){
            return;
        } else if("org.osgi.framework.ServicePermission".equals(perm.getClass().getCanonicalName())) {
            return;
        } else if (perm instanceof PropertyPermission ){
            return; // RunAndRenderTask.setReportRunnable(..) requires ("java.util.PropertyPermission" "*" "read,write")
        }else if(perm instanceof FilePermission){
            handleFilePermission(perm);
        } else if(allowedPermissionsAndActionsMap.containsKey(perm.getClass().getCanonicalName())){
            lookupPermissionMap(perm);
        } else { // default
            LOG.debug(perm.getClass().getCanonicalName() + " " + perm.getName());
            throw new ReportSecurityException("Permission:\t" + perm.getName() + " with action:\t" + perm.getActions() + " not allowed in verinice Report Context");
        }
    }


    private void lookupPermissionMap(Permission perm) throws ReportSecurityException{
        List<String> allowedActions = allowedPermissionsAndActionsMap.get(perm.getClass().getCanonicalName());
        for(String allowedAction : allowedActions){
            if(allowedAction.equals(perm.getName())){
                return;
            }
        }

        LOG.debug("Permission:\t" + perm.getName() + " with action:\t" + perm.getActions() + " not allowed in verinice Report Context");
        throw new ReportSecurityException("Permission:\t" + perm.getName() + " with action:\t" + perm.getActions() + " not allowed in verinice Report Context");
    }


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
            } else if(perm.getName().startsWith("/tmp/birt.log")){
                return;
            } else {
                LOG.debug(perm.getClass().getCanonicalName() + " " + perm.getName() + " " + filePermission.getActions());
                throw new ReportSecurityException("Permission:\t" + perm.getName() + " with action:\t" + perm.getActions() + " not allowed in verinice Report Context"); 
            }
        }
    }
    protected void setProtectionEnabled(boolean protectionEnabled){
        this.protectionEnabled = protectionEnabled;
    }
}
