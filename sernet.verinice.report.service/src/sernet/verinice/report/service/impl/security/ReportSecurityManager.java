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
import java.util.Map.Entry;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;

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
    
    private static Map<String, List<String>> allowedPermissionsAndActionsMap;
    private static Map<String, List<String>> authorizedRuntimeActions;
    
    private boolean protectionEnabled = true;
    
    private final static String VERINICE_RUN_QUERY_METHOD = "org.eclipse.birt.report.engine.api.impl.RunAndRenderTask.run";
    
    static {
        allowedPermissionsAndActionsMap = new HashMap<>();
        allowedPermissionsAndActionsMap.put(RuntimePermission.class.getCanonicalName(), Arrays.asList(new String[]{
        }));
        
        allowedPermissionsAndActionsMap.put(LoggingPermission.class.getCanonicalName(), Arrays.asList(new String[]{"control"}));
        allowedPermissionsAndActionsMap.put(NetPermission.class.getCanonicalName(), Arrays.asList(new String[]{"specifyStreamHandler"}));
        allowedPermissionsAndActionsMap.put("org.eclipse.equinox.log.LogPermission", Arrays.asList(new String[]{"*"}));
        
    }
    
    static{
        authorizedRuntimeActions = new HashMap<String, List<String>>();
        authorizedRuntimeActions.put("org.eclipse.osgi.framework.eventmgr.EventManager.dispatchEvent", Arrays.asList(new String[]{
                "getClassLoader",
                "readFileDescriptor",
                "suppressAccessChecks",
                "accessDeclaredMembers"
        }));
        authorizedRuntimeActions.put("org.eclipse.osgi.framework.util.SecureAction.start", Arrays.asList(new String[]{
                "suppressAccessChecks"
        }));
        authorizedRuntimeActions.put("org.apache.commons.logging.LogFactory.directGetContextClassLoader", Arrays.asList(new String[]{
                "getClassLoader"
        }));
        authorizedRuntimeActions.put("org.eclipse.equinox.internal.util.impl.tpt.threadpool.Executor.run", Arrays.asList(new String[]{
                "getClassLoader"
        }));
        authorizedRuntimeActions.put("sernet.gs.ui.rcp.main.service.TransactionLogWatcher.checkLog", Arrays.asList(new String[]{
                "accessDeclaredMembers"
        }));
        authorizedRuntimeActions.put("org.springframework.util.ClassUtils.isCacheSafe", Arrays.asList(new String[]{
                "getClassLoader"
        }));
        authorizedRuntimeActions.put("org.springframework.scheduling.quartz.QuartzJobBean.execute", Arrays.asList(new String[]{
                "getClassLoader"
        }));  
        authorizedRuntimeActions.put("org.eclipse.osgi.util.NLS.initializeMessages", 
                Arrays.asList(new String[]{"accessDeclaredMembers", "getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.core.framework.eclipse.EclipsePlatform.enterPlatformContext",
                Arrays.asList(new String[]{"setContextClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.api.impl.EngineTask.createContentEmitter", 
                Arrays.asList(new String[]{"createClassLoader" ,"getClassLoader", "accessDeclaredMembers",
                        "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.api.impl.EngineTask.switchToOsgiClassLoader",
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.api.impl.EngineTask.switchClassLoaderBack", 
                Arrays.asList(new String[]{"setContextClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.chart.reportitem.i18n.Messages.<clinit>", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.executor.ExecutionContext.getReport",
                Arrays.asList(new String[]{"createClassLoader", "getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.executor.ExecutionContext.getDataEngine"
                , Arrays.asList(new String[]{"getClassLoader", "suppressAccessChecks",
                        "createClassLoader", "getProtectionDomain"}));
        authorizedRuntimeActions.put("org.eclipse.osgi.baseadaptor.BaseData.createClassLoader", 
                Arrays.asList(new String[]{"createClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.osgi.internal.loader.BundleLoader.getParentPrivileged",
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.core.script.ScriptContext.compile", 
                Arrays.asList(new String[]{"createClassLoader", "getProtectionDomain",
                        "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.internal.util.BundleVersionUtil.getBundleVersion",
                Arrays.asList(new String[]{"getProtectionDomain"}));
        authorizedRuntimeActions.put("java.util.ResourceBundle.loadBundle",
                Arrays.asList(new String[]{"accessClassInPackage.sun.util.resources.de",
                        "accessClassInPackage.sun.util.resources",
                        "suppressAccessChecks", "accessClassInPackage.sun.awt.resources",
                        "accessClassInPackage.sun.util.resources.en"}));
        authorizedRuntimeActions.put("org.eclipse.birt.core.i18n.ResourceHandle.<init>", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.data.engine.odaconsumer.Driver.createNewDriverHelper", 
                Arrays.asList(new String[]{"getClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.eclipse.core.runtime.internal.adaptor.ContextFinder.basicFindClassLoaders", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.osgi.framework.internal.core.BundleContextImpl.setContextFinder", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.datatools.connectivity.oda.consumer.helper.OdaConnection.incrOrDecrOpenedConnectionCountMap",
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.datatools.connectivity.oda.consumer.util.manifest.PropertyProviderManifest.createProvider(PropertyProviderManifest",
                Arrays.asList("suppressAccessChecks"));
        authorizedRuntimeActions.put("sernet.verinice.security.report.ReportClassLoader.<init>", 
                Arrays.asList(new String[]{"createClassLoader"}));
        authorizedRuntimeActions.put("sernet.verinice.oda.driver.impl.Query.<init>",
                Arrays.asList("suppressAccessChecks", "getClassLoader"));
        authorizedRuntimeActions.put("bsh.classpath.ClassManagerImpl.classForName", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor.doWriteRemoteInvocation",
                Arrays.asList(new String[]{"accessDeclaredMembers", "createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.apache.xerces.parsers.SecuritySupport.getContextClassLoader", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.apache.xerces.parsers.SecuritySupport.getSystemClassLoader", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.apache.xerces.parsers.SecuritySupport.getParentClassLoader",
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.apache.xerces.impl.dv.SecuritySupport.getContextClassLoader", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.apache.xerces.impl.dv.SecuritySupport.getSystemClassLoader", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.apache.xerces.impl.dv.SecuritySupport.getParentClassLoader",
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.apache.xerces.impl.dv.ObjectFactory.newInstance", 
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.apache.xerces.parsers.ObjectFactory.newInstance",
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.registerJavaFonts",
                Arrays.asList(new String[]{"suppressAccessChecks", "accessClassInPackage.sun.misc"}));
        authorizedRuntimeActions.put("javax.xml.parsers.FactoryFinder.findServiceProvider",
                Arrays.asList(new String[]{"suppressAccessChecks", "createClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.registerFontPath",
                Arrays.asList(new String[]{"suppressAccessChecks", "accessClassInPackage.sun.misc"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.createFont",
                Arrays.asList(new String[]{"getClassLoader", "suppressAccessChecks", "accessClassInPackage.sun.misc"}));
        authorizedRuntimeActions.put("com.ibm.icu.text.BreakIterator.getLineInstance",
                Arrays.asList(new String[]{"suppressAccessChecks"}));
        authorizedRuntimeActions.put("java.awt.Toolkit.loadLibraries",
                Arrays.asList(new String[]{"loadLibrary.awt", "getenv.DISPLAY", "loadLibrary." + System.getProperty("sun.boot.library.path") +  "/libawt_xawt.so"}));
        authorizedRuntimeActions.put("org.eclipse.core.internal.registry.osgi.RegistryStrategyOSGI.createExecutableExtension", 
                Arrays.asList(new String[]{"suppressAccessChecks", "createClassLoader"}));
        authorizedRuntimeActions.put("org.apache.log4j.Category.error",
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.eclipse.birt.data.engine.executor.cache.ResultObjectUtil.readData", 
                Arrays.asList(new String[]{"suppressAccessChecks", "accessDeclaredMembers"}));
        authorizedRuntimeActions.put("org.eclipse.birt.data.engine.impl.PreparedDummyQuery$QueryResults.getResultIterator",
                Arrays.asList(new String[]{"createClassLoader", "getProtectionDomain", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("bsh.Interpreter.initRootSystemObject", 
                Arrays.asList(new String[]{"createClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.data.engine.impl.QueryResults.getResultIterator",
                Arrays.asList(new String[]{"createClassLoader", "getProtectionDomain",
                        "suppressAccessChecks", "accessDeclaredMembers", "writeFileDescriptor"}));
        authorizedRuntimeActions.put("org.eclipse.birt.chart.reportitem.ChartReportItemGenerationImpl.serialize",
                Arrays.asList(new String[]{"accessDeclaredMembers", "suppressAccessChecks", "createClassLoader"}));
        authorizedRuntimeActions.put("org.eclipse.birt.chart.reportitem.ChartReportItemPresentationBase.deserialize",
                Arrays.asList(new String[]{"accessDeclaredMembers"}));
        authorizedRuntimeActions.put("java.awt.GraphicsEnvironment.createGE", 
                Arrays.asList(new String[]{"loadLibrary.awt", "loadLibrary.fontmanager"}));
        authorizedRuntimeActions.put("sun.java2d.SunGraphicsEnvironment.getFontManagerForSGE",
                Arrays.asList(new String[]{"accessClassInPackage.sun.awt", "loadLibrary.awt", 
                        "modifyThreadGroup", "accessDeclaredMembers", "modifyThread", "setContextClassLoader",
                        "getenv.JAVA2D_USEPLATFORMFONT"}));
        authorizedRuntimeActions.put("org.eclipse.birt.chart.util.SecurityUtil.getClassLoader",
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("java.awt.event.NativeLibLoader.loadLibraries", 
                Arrays.asList(new String[]{"loadLibrary.awt"}));
        authorizedRuntimeActions.put("sun.awt.AppContext.initMainAppContext", 
                Arrays.asList(new String[]{"modifyThreadGroup"}));
        authorizedRuntimeActions.put("sun.awt.image.NativeLibLoader.loadLibraries", 
                Arrays.asList(new String[]{"loadLibrary.awt"}));
        authorizedRuntimeActions.put("org.eclipse.birt.chart.device.util.ChartTextMetrics.reuse",
                Arrays.asList(new String[]{"loadLibrary.t2k"}));
        authorizedRuntimeActions.put("org.eclipse.birt.chart.device.svg.SVGRendererImpl.writeDocumentToOutputStream",
                Arrays.asList(new String[]{"suppressAccessChecks", "charsetProvider"}));
        authorizedRuntimeActions.put("org.eclipse.birt.core.script.JavascriptEvalUtil.getCompiledScript",
                Arrays.asList(new String[]{"createClassLoader","getProtectionDomain", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.apache.batik.dom.ExtensibleDOMImplementation.getDomExtensions", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.xml.sax.helpers.XMLReaderFactory.createXMLReader", 
                Arrays.asList(new String[]{"suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.apache.batik.dom.svg.SVGDOMImplementation.createDocument", 
                Arrays.asList(new String[]{"getClassLoader"}));
        authorizedRuntimeActions.put("org.apache.batik.util.CleanerThread.getReferenceQueue", 
                Arrays.asList(new String[]{"accessDeclaredMembers"}));
        authorizedRuntimeActions.put("org.apache.batik.util.Service.providers", 
                Arrays.asList(new String[]{"getClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.apache.batik.dom.ExtensibleDOMImplementation.createCSSEngine", 
                Arrays.asList(new String[]{"suppressAccessChecks"}));
        authorizedRuntimeActions.put("java.awt.BasicStroke.createStrokedShape", 
                Arrays.asList(new String[]{"loadLibrary.dcpr"}));
        authorizedRuntimeActions.put("org.eclipse.birt.data.engine.impl.ParameterUtil.resolveDataSetParameters",
                Arrays.asList(new String[]{"createClassLoader"}));
        authorizedRuntimeActions.put("sernet.verinice.oda.driver.impl.Query$Helper.execute",
                Arrays.asList(new String[]{"readFileDescriptor"}));
        authorizedRuntimeActions.put("bsh.BshClassManager.createClassManager", 
                Arrays.asList(new String[]{"createClassLoader"}));
        authorizedRuntimeActions.put("bsh.Reflect.invokeMethod",
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"})); // TODO: handle with extraordinary care
        authorizedRuntimeActions.put("org.hibernate.proxy.pojo.cglib.SerializableProxy.readResolve", 
                Arrays.asList(new String[]{"accessDeclaredMembers"}));
        authorizedRuntimeActions.put("bsh.CollectionManager.getCollectionManager",
                Arrays.asList(new String[]{"suppressAccessChecks"}));
        authorizedRuntimeActions.put("bsh.BSHIfStatement.eval", 
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("javax.xml.transform.FactoryFinder.newInstance", 
                Arrays.asList(new String[]{"createClassLoader"}));
        authorizedRuntimeActions.put("java.awt.GraphicsEnvironment.createGE", 
                Arrays.asList(new String[]{"getenv.DISPLAY", "loadLibrary.awt", 
                        "loadLibrary." + System.getProperty("sun.boot.library.path") +  "/libawt_xawt.so",
                        "loadLibrary.fontmanager"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.executor.StyledItemExecutor.createHighlightStyle",
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("value)org.eclipse.birt.data.engine.impl.DataEngineSession.cancel", 
                Arrays.asList(new String[]{"accessDeclaredMembers"}));
        authorizedRuntimeActions.put("org.eclipse.birt.report.engine.script.internal.ScriptExecutor.addException",
                Arrays.asList(new String[]{"createClassLoader"}));
        authorizedRuntimeActions.put("org.mozilla.javascript.Context.throwAsScriptRuntimeEx",
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.eclipse.birt.data.engine.executor.DataSourceQuery.prepareColumns",
                Arrays.asList(new String[]{"accessDeclaredMembers", "createClassLoader", 
                        "suppressAccessChecks", "writeFileDescriptor"}));
        authorizedRuntimeActions.put("org.mozilla.javascript.NativeJavaClass.construct",
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.mozilla.javascript.NativeJavaMethod.call",
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.mozilla.javascript.ScriptRuntime.name",
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
        authorizedRuntimeActions.put("org.mozilla.javascript.JavaMembers.put", 
                Arrays.asList(new String[]{"createClassLoader", "suppressAccessChecks"}));
    }
    
    private ReportSecurityContext reportSecurityContext;
    
    public ReportSecurityManager(ReportSecurityContext reportSecurityContext) { 
        super();
        this.reportSecurityContext = reportSecurityContext;
    }
    
    

    
    /**
     * return
     */
    @Override
    public void checkPermission(Permission perm){
        if(perm instanceof RuntimePermission){
            "exec".hashCode();
        }
        // do the stacktrace / caller inspection for javascript also and we are done here,aren't we?
        if(isCalledByRunQuery()){
            if(!protectionEnabled){
                return;
            }
            // enabling reflextpermission("suppressAccessChecks") and several RuntimePermissions in authorized context only
            if(perm instanceof ReflectPermission || perm instanceof RuntimePermission) {
                if(!isAuthorizedStackTrace(perm.getName())){
                    throwSecurityException(perm);
                } else {
                    return;
                }
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
                throwSecurityException(perm);
            }
        }
    }

    /**
     * preventes use of Runtime.getRuntime().exec("rm -rf");
     */
      @Override
      public void checkExec(String cmd){
          if(isCalledByRunQuery()){
              throw new ReportSecurityException(Messages.UNAUTHORIZED_EXECUTION_CALL_DETECTED);
          } 
      }


    private void throwSecurityException(Permission perm) {
        throw new ReportSecurityException(NLS.bind(Messages.REPORT_SECURITY_EXCEPTION_0, new Object[]{perm.getClass().getCanonicalName(), perm.getName(), perm.getActions()}));
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
        throwSecurityException(perm);
    }

    /**
     *  allow writing && deleting files on some custom defined places
     * @param perm
     * @throws ReportSecurityException
     */
    private void handleFilePermission(Permission perm) throws ReportSecurityException{
        FilePermission filePermission = (FilePermission)perm;
        
        if(perm.getName().contains("verinice-client.log")){
            "".hashCode();
        }

        if(filePermission.getActions().contains("delete") || filePermission.getActions().contains("write")){
            if(perm.getName().startsWith(reportSecurityContext.getLogFileLocation())){
                return;
            } else if (reportSecurityContext.getReportOptions().getOutputFile().getAbsolutePath().equals(perm.getName())) {
                return;
            } else if(("file:" + filePermission.getName()).equals(System.getProperty("osgi.instance.area") + "log")){
                return;
            } else if (("file:" + filePermission.getName()).startsWith(System.getProperty("osgi.instance.area") + "log")){
                return;
            } else if(filePermission.getName().equals(System.getProperty("osgi.instance.area") + File.separator + ".metadata" + File.separator + ".log")){
                return;
            } else if(("file:" + filePermission.getName()).startsWith(System.getProperty("osgi.configuration.area"))){
                return;
            } else if((filePermission.getName()).startsWith(System.getProperty("java.io.tmpdir"))){
                return;
            } else if((filePermission.getName()).startsWith(System.getProperty("user.home") + File.separator + ".java" + File.separator + "fonts")) {
                return;
            } else {
                throwSecurityException(perm); 
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
    protected synchronized void setProtectionEnabled(boolean protectionEnabled){
        this.protectionEnabled = protectionEnabled;
    }
    
    private boolean isAuthorizedStackTrace(String permissionName){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        boolean authorizedCall = false;
        StringBuilder sb = new StringBuilder();
        for(Entry<String, List<String>> entry : authorizedRuntimeActions.entrySet()){
            sb.setLength(0); // reset stacktrace logging
            for(String value : entry.getValue()){
                if(permissionName.equals(value)){
                    for (int i = 0; i < stackTrace.length; i++){
                        sb.append(stackTrace[i].toString()).append("\n");
                        if(stackTrace[i].toString().startsWith(entry.getKey())){
                            authorizedCall = true;
                            break;
                        }
                    }
                } 
                if(authorizedCall){
                    break;
                }
            }
            if(authorizedCall){
                break;
            }            
        }
        if(!authorizedCall){
            LOG.error(NLS.bind(Messages.REPORT_SECURITY_EXCEPTION_1, new Object[]{permissionName, sb.toString()}));
        }
        return authorizedCall;
    }
    
    private boolean isCalledByRunQuery(){
        for(StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()){
            if(stackTraceElement.toString().startsWith(VERINICE_RUN_QUERY_METHOD)){
                return true;
            }
        }
        return false;
    }
    
    protected String getReportOutputName(){
        return this.reportSecurityContext.getOutputName();
    }
}
