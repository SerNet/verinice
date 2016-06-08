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
package sernet.verinice.report.service.impl.security;

import java.io.File;
import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.net.InetAddress;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;

import sernet.verinice.interfaces.report.IReportPermissionHandler;
import sernet.verinice.interfaces.report.IReportPermissionHandler.PermissionClassname;
import sernet.verinice.security.report.ReportSecurityContext;
import sernet.verinice.security.report.ReportSecurityException;

/**
 * 
 * Class defines how to handle a specific inheriting class of {@link SecurityPermission}
 * while generating a report by defining the behaviour in an implementation
 * of {@link IReportPermissionHandler} for each Permission-Classtype. 
 * 
 * Class is used by {@link ReportSecurityManager} during
 * report generation progress and defines several whitelists (which are 
 * quite long)
 * 
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ReportSecurityPermissionManager {

    private static final Logger LOG 
        = Logger.getLogger(ReportSecurityPermissionManager.class);
    private static final String RUNTIME_ACTIONNAME_GETCLASSLOADER 
        = "getClassLoader";
    private static final String RUNTIME_ACTIONNAME_SUPPRESSACCESS 
        = "suppressAccessChecks";
    private static final String RUNTIME_ACTIONNAME_ACCESSDECLARED 
        = "accessDeclaredMembers";
    private static final String RUNTIME_ACTIONNAME_READFILEDESC 
        = "readFileDescriptor";
    private static final String RUNTIME_ACTIONNAME_SETCONTEXTCL 
        = "setContextClassLoader";
    private static final String RUNTIME_ACTIONNAME_CREATECLASSLOADER 
        = "createClassLoader";
    private static final String RUNTIME_ACTIONNAME_LOADLIBAWT 
        = "loadLibrary.awt";
    private static final String RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN 
        = "getProtectionDomain";
    private static final String RUNTIME_ACTIONNAME_ACCESSSUNMISC 
        = "accessClassInPackage.sun.misc";
    private static final String RUNTIME_ACTIONNAME_GETENVDISPLAY 
        = "getenv.DISPLAY";
    private static final String RUNTIME_ACTIONNAME_LOADLIB 
        = "loadLibrary.";
    private static final String RUNTIME_ACTIONNAME_WRITEFILEDESC 
        = "writeFileDescriptor";
    private static final String RUNTIME_ACTIONNAME_MODIFYTHREADGROUP 
        = "modifyThreadGroup";
    private static final String RUNTIME_ACTIONNAME_LOADLIBFONTMGR 
        = "loadLibrary.fontmanager";
    
    private static final String METHODNAME_GETRESULTITERATOR 
        = "org.eclipse.birt.data.engine.impl.QueryResults.getResultIterator";
    private static final String METHODNAME_GETPROJECTEDCOLUMNS 
        = "org.eclipse.birt.data.engine.odaconsumer."
                + "PreparedStatement.getProjectedColumns";
    
    private static final String PREFIX_FILE = "file:";
    
    private Map<PermissionClassname, IReportPermissionHandler>
    permissionHandlerMap = null;
    private ReportSecurityContext reportSecurityContext;
    private static Map<String, List<String>> allowedPermissionsAndActionsMap;
    private static Map<String, List<String>> runtimeActionsWhitelist ;

    private final IReportPermissionHandler reflectAndRuntimeHandler 
        = new IReportPermissionHandler() {

        @Override
        public void handlePermission(Permission permission) {
            if (!isRuntimePermissionWhitelisted(permission.getName())){
                throwSecurityException(permission);
                if (LOG.isDebugEnabled()){
                    LOG.debug(Arrays.toString(Thread.currentThread().getStackTrace()));
                }
            } else {
                return;
            }                
        }
        
        /**
         *  checks if the name of a given permission (which is an
         *  actionname like "getClassloader") is on whitelist, and if thats the case the 
         *  key of that value (@param permissionName) is retrieved from the 
         *  whitelist (map) to check if that methodCall (the key) 
         *  is on the current stacktrace.
         *  
         *  return true in that case, false otherwise
         */
        private boolean isRuntimePermissionWhitelisted(String permissionName){
            boolean authorizedCall = false;
            for (Entry<String, List<String>> entry : runtimeActionsWhitelist.entrySet()){
                for (String value : entry.getValue()){
                    if (permissionName.equals(value)){
                        authorizedCall = stacktraceContains(entry.getKey());
                    } 
                    if (authorizedCall){
                        break;
                    }
                }
                if (authorizedCall){
                    break;
                }            
            }
            if (!authorizedCall){
                LOG.error(NLS.bind(Messages.REPORT_SECURITY_EXCEPTION_1, new Object[]{permissionName,""}));
            }
            return authorizedCall;
        }
    };


    static {
        allowedPermissionsAndActionsMap = new HashMap<>();
        allowedPermissionsAndActionsMap.put(RuntimePermission.class.getCanonicalName(), Arrays.asList(new String[]{
        }));

        allowedPermissionsAndActionsMap.put(LoggingPermission.class.getCanonicalName(), Arrays.asList(new String[]{"control"}));
        allowedPermissionsAndActionsMap.put(NetPermission.class.getCanonicalName(), Arrays.asList(new String[]{"specifyStreamHandler"}));
        allowedPermissionsAndActionsMap.put("org.eclipse.equinox.log.LogPermission", Arrays.asList(new String[]{"*"}));

    }

    static{
        runtimeActionsWhitelist  = new HashMap<String, List<String>>();
        runtimeActionsWhitelist .put("org.eclipse.osgi.framework.eventmgr.EventManager.dispatchEvent", Arrays.asList(new String[]{
                RUNTIME_ACTIONNAME_GETCLASSLOADER,
                RUNTIME_ACTIONNAME_READFILEDESC,
                RUNTIME_ACTIONNAME_SUPPRESSACCESS,
                RUNTIME_ACTIONNAME_ACCESSDECLARED
        }));
        runtimeActionsWhitelist .put("org.eclipse.osgi.framework.util.SecureAction.start", Arrays.asList(new String[]{
                RUNTIME_ACTIONNAME_SUPPRESSACCESS
        }));
        runtimeActionsWhitelist .put("org.apache.commons.logging.LogFactory.directGetContextClassLoader", Arrays.asList(new String[]{
                RUNTIME_ACTIONNAME_GETCLASSLOADER
        }));
        runtimeActionsWhitelist .put("org.eclipse.equinox.internal.util.impl.tpt.threadpool.Executor.run", Arrays.asList(new String[]{
                RUNTIME_ACTIONNAME_GETCLASSLOADER
        }));
        runtimeActionsWhitelist .put("sernet.gs.ui.rcp.main.service.TransactionLogWatcher.checkLog", Arrays.asList(new String[]{
                RUNTIME_ACTIONNAME_ACCESSDECLARED
        }));
        runtimeActionsWhitelist .put("org.springframework.util.ClassUtils.isCacheSafe", Arrays.asList(new String[]{
                RUNTIME_ACTIONNAME_GETCLASSLOADER
        }));
        runtimeActionsWhitelist .put("org.springframework.scheduling.quartz.QuartzJobBean.execute", Arrays.asList(new String[]{
                RUNTIME_ACTIONNAME_GETCLASSLOADER
        }));  
        runtimeActionsWhitelist .put("org.eclipse.osgi.util.NLS.initializeMessages", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED, RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.core.framework.eclipse.EclipsePlatform.enterPlatformContext",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SETCONTEXTCL}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.api.impl.EngineTask.createContentEmitter", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER ,RUNTIME_ACTIONNAME_GETCLASSLOADER, RUNTIME_ACTIONNAME_ACCESSDECLARED,
                RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.api.impl.EngineTask.switchToOsgiClassLoader",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.api.impl.EngineTask.switchClassLoaderBack", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SETCONTEXTCL}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.reportitem.i18n.Messages.<clinit>", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.executor.ExecutionContext.getReport",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.executor.ExecutionContext.getDataEngine"
                , Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS,
                        RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN}));
        runtimeActionsWhitelist .put("org.eclipse.osgi.baseadaptor.BaseData.createClassLoader", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.osgi.internal.loader.BundleLoader.getParentPrivileged",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.core.script.ScriptContext.compile", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN,
                RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.internal.util.BundleVersionUtil.getBundleVersion",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN}));
        runtimeActionsWhitelist .put("java.util.ResourceBundle.loadBundle",
                Arrays.asList(new String[]{"accessClassInPackage.sun.util.resources.de",
                        "accessClassInPackage.sun.util.resources",
                        RUNTIME_ACTIONNAME_SUPPRESSACCESS, "accessClassInPackage.sun.awt.resources",
                        "accessClassInPackage.sun.util.resources.en",
                        "accessClassInPackage.sun.text.resources.de",
                "accessClassInPackage.sun.text.resources.en"}));
        runtimeActionsWhitelist .put("org.eclipse.birt.core.i18n.ResourceHandle.<init>", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.data.engine.odaconsumer.Driver.createNewDriverHelper", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.core.runtime.internal.adaptor.ContextFinder.basicFindClassLoaders", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.osgi.framework.internal.core.BundleContextImpl.setContextFinder", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.datatools.connectivity.oda.consumer.helper.OdaConnection.incrOrDecrOpenedConnectionCountMap",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.datatools.connectivity.oda.consumer.util.manifest.PropertyProviderManifest.createProvider(PropertyProviderManifest",
                Arrays.asList(RUNTIME_ACTIONNAME_SUPPRESSACCESS));
        runtimeActionsWhitelist .put("sernet.verinice.security.report.ReportClassLoader.<init>", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("sernet.verinice.oda.driver.impl.Query.<init>",
                Arrays.asList(RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_GETCLASSLOADER));
        runtimeActionsWhitelist .put("bsh.classpath.ClassManagerImpl.classForName", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor.doWriteRemoteInvocation",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED, RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.apache.xerces.parsers.SecuritySupport.getContextClassLoader", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.apache.xerces.parsers.SecuritySupport.getSystemClassLoader", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.apache.xerces.parsers.SecuritySupport.getParentClassLoader",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.apache.xerces.impl.dv.SecuritySupport.getContextClassLoader", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.apache.xerces.impl.dv.SecuritySupport.getSystemClassLoader", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.apache.xerces.impl.dv.SecuritySupport.getParentClassLoader",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.apache.xerces.impl.dv.ObjectFactory.newInstance", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.apache.xerces.parsers.ObjectFactory.newInstance",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.registerJavaFonts",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_ACCESSSUNMISC }));
        runtimeActionsWhitelist .put("javax.xml.parsers.FactoryFinder.findServiceProvider",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.registerFontPath",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_ACCESSSUNMISC , RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.createFont",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_ACCESSSUNMISC }));
        runtimeActionsWhitelist .put("com.ibm.icu.text.BreakIterator.getLineInstance",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("java.awt.Toolkit.loadLibraries",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_LOADLIBAWT, RUNTIME_ACTIONNAME_GETENVDISPLAY, RUNTIME_ACTIONNAME_LOADLIB + System.getProperty("sun.boot.library.path") +  "/libawt_xawt.so"}));
        runtimeActionsWhitelist .put("org.eclipse.core.internal.registry.osgi.RegistryStrategyOSGI.createExecutableExtension", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("org.apache.log4j.Category.error",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.data.engine.executor.cache.ResultObjectUtil.readData", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_ACCESSDECLARED}));
        runtimeActionsWhitelist .put("org.eclipse.birt.data.engine.impl.PreparedDummyQuery$QueryResults.getResultIterator",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN, RUNTIME_ACTIONNAME_SUPPRESSACCESS,
                        RUNTIME_ACTIONNAME_GETCLASSLOADER, RUNTIME_ACTIONNAME_READFILEDESC}));
        runtimeActionsWhitelist .put("bsh.Interpreter.initRootSystemObject", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put(METHODNAME_GETRESULTITERATOR,
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN,
                        RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_ACCESSDECLARED, RUNTIME_ACTIONNAME_WRITEFILEDESC,
                        RUNTIME_ACTIONNAME_GETCLASSLOADER, RUNTIME_ACTIONNAME_READFILEDESC}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.reportitem.ChartReportItemGenerationImpl.serialize",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED, RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.reportitem.ChartReportItemPresentationBase.deserialize",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED}));
        runtimeActionsWhitelist .put("sun.java2d.SunGraphicsEnvironment.getFontManagerForSGE",
                Arrays.asList(new String[]{"accessClassInPackage.sun.awt", "accessClassInPackage.sun.font", RUNTIME_ACTIONNAME_LOADLIBAWT, 
                        RUNTIME_ACTIONNAME_MODIFYTHREADGROUP, RUNTIME_ACTIONNAME_ACCESSDECLARED, "modifyThread", RUNTIME_ACTIONNAME_SETCONTEXTCL,
                        "getenv.JAVA2D_USEPLATFORMFONT", RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN, RUNTIME_ACTIONNAME_LOADLIBFONTMGR}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.util.SecurityUtil.getClassLoader",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("java.awt.event.NativeLibLoader.loadLibraries", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_LOADLIBAWT}));
        runtimeActionsWhitelist .put("sun.awt.AppContext.initMainAppContext", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_MODIFYTHREADGROUP}));
        runtimeActionsWhitelist .put("sun.awt.image.NativeLibLoader.loadLibraries", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_LOADLIBAWT}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.device.util.ChartTextMetrics.reuse",
                Arrays.asList(new String[]{"loadLibrary.t2k", RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_MODIFYTHREADGROUP, "modifyThread", RUNTIME_ACTIONNAME_SETCONTEXTCL}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.device.swing.SwingTextMetrics.reuse", 
                Arrays.asList(new String[]{"loadLibrary.t2k"}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.device.svg.SVGRendererImpl.writeDocumentToOutputStream",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS, "charsetProvider"}));
        runtimeActionsWhitelist .put("org.eclipse.birt.core.script.JavascriptEvalUtil.getCompiledScript",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER,RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.apache.batik.dom.ExtensibleDOMImplementation.getDomExtensions", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.xml.sax.helpers.XMLReaderFactory.createXMLReader", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.apache.batik.dom.svg.SVGDOMImplementation.createDocument", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.apache.batik.util.CleanerThread.getReferenceQueue", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED}));
        runtimeActionsWhitelist .put("org.apache.batik.util.Service.providers", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETCLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.apache.batik.dom.ExtensibleDOMImplementation.createCSSEngine", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("java.awt.BasicStroke.createStrokedShape", 
                Arrays.asList(new String[]{"loadLibrary.dcpr"}));
        runtimeActionsWhitelist .put("org.eclipse.birt.data.engine.impl.ParameterUtil.resolveDataSetParameters",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("sernet.verinice.oda.driver.impl.Query$Helper.execute",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_READFILEDESC}));
        runtimeActionsWhitelist .put("bsh.BshClassManager.createClassManager", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("bsh.Reflect.invokeMethod",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS})); // TODO: handle with extraordinary care
        runtimeActionsWhitelist .put("org.hibernate.proxy.pojo.cglib.SerializableProxy.readResolve", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED}));
        runtimeActionsWhitelist .put("bsh.CollectionManager.getCollectionManager",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("bsh.BSHIfStatement.eval", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("javax.xml.transform.FactoryFinder.newInstance", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("java.awt.GraphicsEnvironment.createGE", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_GETENVDISPLAY, RUNTIME_ACTIONNAME_LOADLIBAWT, 
                        RUNTIME_ACTIONNAME_LOADLIB + System.getProperty("sun.boot.library.path") +  "/libawt_xawt.so",
                        RUNTIME_ACTIONNAME_LOADLIBFONTMGR,
                        RUNTIME_ACTIONNAME_LOADLIB + System.getProperty("java.home") + File.separatorChar + "lib" + File.separatorChar + "libawt_lwawt.dylib"}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.executor.StyledItemExecutor.createHighlightStyle",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("value)org.eclipse.birt.data.engine.impl.DataEngineSession.cancel", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.script.internal.ScriptExecutor.addException",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER}));
        runtimeActionsWhitelist .put("org.mozilla.javascript.Context.throwAsScriptRuntimeEx",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.data.engine.executor.DataSourceQuery.prepareColumns",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED, RUNTIME_ACTIONNAME_CREATECLASSLOADER, 
                        RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_WRITEFILEDESC, RUNTIME_ACTIONNAME_GETCLASSLOADER}));
        runtimeActionsWhitelist .put("org.mozilla.javascript.NativeJavaClass.construct",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.mozilla.javascript.NativeJavaMethod.call",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.mozilla.javascript.ScriptRuntime.name",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.mozilla.javascript.JavaMembers.put", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.mozilla.javascript.JavaMembers.get", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));

        runtimeActionsWhitelist .put(METHODNAME_GETPROJECTEDCOLUMNS,
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED, RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS,
                        RUNTIME_ACTIONNAME_GETCLASSLOADER, RUNTIME_ACTIONNAME_WRITEFILEDESC, RUNTIME_ACTIONNAME_READFILEDESC}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.emitter.excel.layout.ExcelContext.parseSheetName",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.emitter.excel.layout.Page.needOutputInMasterPage", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.emitter.ods.OdsEmitter.parseSheetName", 
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.factory.Generator.render", 
                Arrays.asList(new String[]{"loadLibrary.dcpr"}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.reportitem.ChartReportItemPresentationBase.renderToImageFile",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_CREATECLASSLOADER, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.data.engine.impl.DataEngineSession.cancel",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_ACCESSDECLARED}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.computation.LabelLimiter.limitLabelSize",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.nLayout.area.impl.TextAreaLayout.buildTextStyle",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_GETENVDISPLAY, 
                        RUNTIME_ACTIONNAME_LOADLIB + System.getProperty("java.home") + File.separatorChar + "lib" + File.separatorChar + "libawt_lwawt.dylib",
                        RUNTIME_ACTIONNAME_MODIFYTHREADGROUP, RUNTIME_ACTIONNAME_GETPROTECTIONDOMAIN}));
        runtimeActionsWhitelist .put("sun.java2d.HeadlessGraphicsEnvironment.getAvailableFontFamilyNames",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_LOADLIBAWT, RUNTIME_ACTIONNAME_LOADLIBFONTMGR}));
        runtimeActionsWhitelist .put("org.eclipse.birt.chart.device.swing.SwingDisplayServer.getGraphicsContext",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_LOADLIBAWT, RUNTIME_ACTIONNAME_SUPPRESSACCESS}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.emitter.excel.StyleBuilder.populateColor",
                Arrays.asList(new String[]{"getProtectionDomain", RUNTIME_ACTIONNAME_SUPPRESSACCESS, RUNTIME_ACTIONNAME_GETENVDISPLAY}));
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.emitter.odt.OdtEmitter.end",
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS, "accessClassInPackage.sun.util.logging.resources"}));
        runtimeActionsWhitelist .put("org.mozilla.javascript.ScriptRuntime.checkRegExpProxy", // needed to execute regexes within datasets
                Arrays.asList(new String[]{RUNTIME_ACTIONNAME_SUPPRESSACCESS})); 
        runtimeActionsWhitelist .put("org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.loadFontMappingConfig",
                Arrays.asList(new String[]{"accessClassInPackage.sun.util.logging.resources"}));
    }

    private List<String> osgiAdminPermissionList = Arrays.asList(new String[]{
            "org.eclipse.birt.report.engine.api.impl.EngineTask.createContentEmitter",
            "org.eclipse.birt.core.script.ScriptContext.getScriptEngine",
            "org.eclipse.birt.report.engine.executor.ExecutionContext.getDataEngine",
            "org.eclipse.birt.data.engine.odaconsumer.Driver.createNewDriverHelper",
            "org.eclipse.birt.data.engine.odaconsumer.ConnectionManager.openConnection",
            "org.eclipse.osgi.internal.baseadaptor.DefaultClassLoader.loadClass",
            "org.eclipse.birt.data.engine.odaconsumer.PreparedStatement.setParameterValue",
            METHODNAME_GETPROJECTEDCOLUMNS,
            METHODNAME_GETRESULTITERATOR,
            "org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.getEmbededFontPath",
            "org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.getFontMappingManager",
            "org.eclipse.birt.report.engine.data.dte.ReportQueryBuilder$QueryBuilderVisitor.visitExtendedItem",
            "org.eclipse.birt.report.engine.executor.ReportExecutor.getNextChild",
            "org.eclipse.birt.report.engine.presentation.LocalizedContentVisitor.processExtendedContent", 
            "org.apache.batik.transcoder.print.PrintTranscoder.print",
            "org.eclipse.birt.report.engine.executor.ExecutorManager$ExecutorFactory.visitExtendedItem",
            "org.eclipse.birt.data.engine.api.aggregation.AggregationManager.populateAggregations"
    });

    private List<String> specifyStreamHandlerSet = Arrays.asList(new String[]{
            "org.eclipse.birt.report.engine.api.impl.EngineTask$2.visitScalarParameter",
            "org.eclipse.birt.report.data.adapter.api.DataRequestSession.newSession",
            "org.eclipse.birt.report.engine.emitter.excel.layout.ExcelContext.parseSheetName",
            "org.eclipse.osgi.util.NLS.load",
            "org.eclipse.birt.report.engine.emitter.html.HTMLReportEmitter.outputHtmlText",
            "org.eclipse.birt.report.engine.emitter.excel.ExcelEmitter.startForeign",
            "org.eclipse.birt.report.engine.emitter.ods.OdsEmitter.startForeign",
            "org.eclipse.birt.report.engine.emitter.ods.OdsEmitter.parseSheetName",
            "org.eclipse.birt.report.engine.emitter.odt.OdtEmitter.startForeign",
            "org.eclipse.birt.report.engine.emitter.wpml.DocEmitterImpl.startForeign",
            "org.eclipse.birt.report.engine.layout.pdf.font.FontMappingManagerFactory.createFont",
            "org.eclipse.birt.chart.reportitem.i18n.Messages.<clinit>",
            "org.eclipse.birt.chart.engine.i18n.Messages.<clinit>",
            "org.eclipse.birt.data.aggregation.impl.TotalSum.getParameterDefn",
            "org.eclipse.birt.report.engine.parser.HTMLTextParser.<clinit>",
            "org.w3c.tidy.Tidy.parseDOM",
            "javax.xml.parsers.DocumentBuilderFactory.newInstance",
            "org.eclipse.birt.report.engine.layout.pdf.text.BidiSplitter.createBidi",
            "org.eclipse.birt.report.engine.layout.pdf.WordRecognizerWrapper.<init>",
            "org.eclipse.birt.chart.device.extension.i18n.Messages.<clinit>",
            "org.eclipse.birt.chart.computation.withaxes.PlotWith2DAxes.computeCommon",
            "org.eclipse.birt.chart.factory.Generator.render",
            "org.eclipse.birt.report.engine.layout.emitter.AbstractPage.drawImage",
            "org.eclipse.birt.data.engine.i18n.DataResourceHandle.getInstance",
            "org.eclipse.birt.data.aggregation.i18n.Messages.<clinit>",
            "org.eclipse.birt.report.model.core.ModuleImpl.getMessage",
            "org.eclipse.birt.core.data.DataTypeUtil.toInteger",
            "org.mozilla.javascript.ScriptRuntime.getMessage",
            "com.ibm.icu.impl.ICULocaleService$ICUResourceBundleFactory.getSupportedIDs"

    });




    public ReportSecurityPermissionManager(ReportSecurityContext reportSecurityContext){
        this.reportSecurityContext = reportSecurityContext;
        initHandlerMap();
    }

    /**
     * fills handlingMap with commands that should be executed when a specific 
     * type of permission needs to be handled by the {@link ReportSecurityManager}
     * @return
     */
    private Map<PermissionClassname, IReportPermissionHandler> initHandlerMap(){


        if(permissionHandlerMap == null){
            permissionHandlerMap = new HashMap<PermissionClassname, IReportPermissionHandler>();
        }

        // adding handling command for permissions of type FilePermission
        permissionHandlerMap.put(PermissionClassname.FILE, new IReportPermissionHandler() {

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
        });

        // adding handling command for permissions of type NetPermission
        permissionHandlerMap.put(PermissionClassname.NET, new IReportPermissionHandler(){

            @Override
            public void handlePermission(Permission permission) {
                if ("getProxySelector".equals(permission.getName())){
                    if (stacktraceContains(METHODNAME_GETPROJECTEDCOLUMNS)||
                            stacktraceContains(METHODNAME_GETRESULTITERATOR)){
                        return;
                    }
                } else if ("specifyStreamHandler".equals(permission.getName())){
                    for(String methodCall : specifyStreamHandlerSet){
                        if(stacktraceContains(methodCall)){
                            return;
                        }                
                    }
                }
                throwSecurityException(permission);
            }
        });

        permissionHandlerMap.put(PermissionClassname.NULL, new IReportPermissionHandler() {

            @Override
            public void handlePermission(Permission permission) {
                // PermissionClassname.NULL equals "not on whitelist"
                // so throw exception
                throwSecurityException(permission);
            }
        });

        permissionHandlerMap.put(PermissionClassname.OSGI, new IReportPermissionHandler() {

            @Override
            public void handlePermission(Permission permission) {
                if ("org.osgi.framework.AdminPermission".equals(permission.getClass().getCanonicalName())){
                    for(String methodCall : osgiAdminPermissionList){
                        if (stacktraceContains(methodCall)) {
                            return;
                        } 
                    }
                } else if ("org.osgi.framework.ServicePermission".equals(permission.getClass().getCanonicalName())) { 
                    if (stacktraceContains("org.eclipse.birt.report.engine.api.impl.EngineTask.createContentEmitter") ||
                            stacktraceContains("org.eclipse.birt.core.plugin.BIRTPlugin.start")){
                        return;
                    } else {
                        throwSecurityException(permission);
                    }
                } else {
                    throwSecurityException(permission);
                }           

            }
        });

        permissionHandlerMap.put(PermissionClassname.OTHER, new IReportPermissionHandler() {

            @Override
            public void handlePermission(Permission permission) {
                List<String> allowedActions = allowedPermissionsAndActionsMap.get(permission.getClass().getCanonicalName());
                for (String allowedAction : allowedActions){
                    if (allowedAction.equals(permission.getName())){
                        return;
                    }
                }
                throwSecurityException(permission);

            }
        });

        permissionHandlerMap.put(PermissionClassname.PROPERTY, new IReportPermissionHandler() {

            @Override
            public void handlePermission(Permission permission) {
                // enable reading, writing and deleting of all(!) properties
                // RunAndRenderTask.setReportRunnable(..) requires ("java.util.PropertyPermission" "*" "read,write")
                return;
            }
        });

        permissionHandlerMap.put(PermissionClassname.REFLECT, reflectAndRuntimeHandler);

        permissionHandlerMap.put(PermissionClassname.RUNTIME, reflectAndRuntimeHandler);

        permissionHandlerMap.put(PermissionClassname.SOCKET, new IReportPermissionHandler(){

            @Override
            public void handlePermission(Permission permission) {
                try {
                    final String serverHost = reportSecurityContext.getReportOptions().getServerURL().trim();
                    final URI serverHostURI = new URI(serverHost);

                    if (permission.getName().equals(InetAddress.getLocalHost().getHostName())){
                        return;
                    } else if (permission.getName().startsWith("localhost") || permission.getName().startsWith("127.0.0.1")){
                        return;
                    } else if (permission.getName().startsWith(serverHostURI.getHost() + ":" + serverHostURI.getPort()) ){
                        return;
                    } else {
                        throwSecurityException(permission);
                    }
                } catch (UnknownHostException e) {
                    LOG.error("Unable to determine local machines hostname", e);
                    throwSecurityException(permission, e);
                } catch (URISyntaxException e){
                    LOG.error("ServerURI is not a valid uri", e);
                }                
            }

        });


        return permissionHandlerMap;

    }

    /**
     * @return the permissionHandlerMap
     */
    public Map<PermissionClassname, IReportPermissionHandler> getPermissionHandlerMap() {
        return permissionHandlerMap;
    }





    /**
     * returns if current thread contains a call within stacktrace that
     * equals a given fully qualified classname @param qualifiedClassname
     */
    private boolean stacktraceContains(String qualifiedClassname){
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTrace.length; i++){
            if (stackTrace[i].toString().startsWith(qualifiedClassname)){
                return true;
            }
        }
        return false;

    }

    /**
     * returns an instance of the enum PermissionClassname with a value
     * that represents the class of the @param permission to use it within a
     * switch-statement 
     */
    private PermissionClassname getClassType(Permission permission){
        PermissionClassname classname = ReflectPermission.class.getCanonicalName().
                equals(permission.getClass().getCanonicalName()) 
                ? PermissionClassname.REFLECT : null;
        if(classname == null){
            classname = RuntimePermission.class.getCanonicalName().
                    equals(permission.getClass().getCanonicalName())
                    ? PermissionClassname.RUNTIME : null;
        }
        if(classname == null){
        classname = permission.getClass().getCanonicalName().
                startsWith("org.osgi.framework")
                ? PermissionClassname.OSGI : null;
        }
        if(classname == null){
        classname = PropertyPermission.class.getCanonicalName().
                equals(permission.getClass().getCanonicalName())
                ? PermissionClassname.PROPERTY : null;
        }
        if(classname == null){
        classname = FilePermission.class.getCanonicalName().
                equals(permission.getClass().getCanonicalName())
                ? PermissionClassname.FILE : null;
        }
        if(classname == null){
        classname = SocketPermission.class.getCanonicalName().
                equals(permission.getClass().getCanonicalName())
                ? PermissionClassname.SOCKET : null;
        }
        if(classname == null){
        classname = NetPermission.class.getCanonicalName().
                equals(permission.getClass().getCanonicalName())
                ? PermissionClassname.NET : null;
        }
        if(classname == null){
        classname = allowedPermissionsAndActionsMap.containsKey(
                permission.getClass().getCanonicalName()) 
                ? PermissionClassname.OTHER : null;
        }
        if(classname == null){
            classname = PermissionClassname.NULL;
        }
        return classname;
    }

    public IReportPermissionHandler getHandlerForPermission(Permission permission){
        PermissionClassname classname = getClassType(permission);
        return getPermissionHandlerMap().get(classname);
    }

    private void throwSecurityException(Permission perm) {
        ReportSecurityException exception = new ReportSecurityException(NLS.bind(Messages.REPORT_SECURITY_EXCEPTION_0, 
                new Object[]{perm.getClass().getCanonicalName(), perm.getName(), perm.getActions()}));
        LOG.debug("throwing the following exception:\t", exception);
        throw exception;
    }

    private void throwSecurityException(Permission perm, Throwable rootCause){
        throw new ReportSecurityException(NLS.bind(Messages.REPORT_SECURITY_EXCEPTION_0, 
                new Object[]{perm.getClass().getCanonicalName(), perm.getName(), perm.getActions()}), rootCause);
    }

}
