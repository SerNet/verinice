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

import java.io.FilePermission;
import java.lang.reflect.ReflectPermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.logging.LoggingPermission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sernet.verinice.interfaces.report.IReportPermissionHandler;
import sernet.verinice.interfaces.report.IReportPermissionHandler.PermissionClassname;
import sernet.verinice.report.service.impl.security.permissionhandling.DefaultPermissionHandler;
import sernet.verinice.report.service.impl.security.permissionhandling.FilePermissionHandler;
import sernet.verinice.report.service.impl.security.permissionhandling.NetPermissionHandler;
import sernet.verinice.report.service.impl.security.permissionhandling.NullPermissionHandler;
import sernet.verinice.report.service.impl.security.permissionhandling.OSGIPermissionHandler;
import sernet.verinice.report.service.impl.security.permissionhandling.PropertyPermissionHandler;
import sernet.verinice.report.service.impl.security.permissionhandling.ReflectAndRuntimePermissionHandler;
import sernet.verinice.report.service.impl.security.permissionhandling.SocketPermissionHandler;
import sernet.verinice.security.report.ReportSecurityContext;

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
        = LoggerFactory.getLogger(ReportSecurityPermissionManager.class);

    private Map<PermissionClassname, IReportPermissionHandler>
    permissionHandlerMap = null;
    private ReportSecurityContext reportSecurityContext;
    static Map<String, List<String>> allowedPermissionsAndActionsMap;
    
    static {
        allowedPermissionsAndActionsMap = new HashMap<>();
        allowedPermissionsAndActionsMap.put(RuntimePermission.class.getCanonicalName(), Arrays.asList(new String[]{
        }));

        allowedPermissionsAndActionsMap.put(LoggingPermission.class.getCanonicalName(), Arrays.asList(new String[]{"control"}));
        allowedPermissionsAndActionsMap.put(NetPermission.class.getCanonicalName(), Arrays.asList(new String[]{"specifyStreamHandler"}));
        allowedPermissionsAndActionsMap.put("org.eclipse.equinox.log.LogPermission", Arrays.asList(new String[]{"*"}));

    }

    private ReflectAndRuntimePermissionHandler reflectAndRuntimeHandler = new ReflectAndRuntimePermissionHandler();

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
        permissionHandlerMap.put(PermissionClassname.FILE, 
                new FilePermissionHandler(reportSecurityContext));

        // adding handling command for permissions of type NetPermission
        permissionHandlerMap.put(PermissionClassname.NET, 
                new NetPermissionHandler());
        permissionHandlerMap.put(PermissionClassname.NULL, 
                new NullPermissionHandler());
        permissionHandlerMap.put(PermissionClassname.OTHER, 
                new DefaultPermissionHandler(allowedPermissionsAndActionsMap));
        permissionHandlerMap.put(PermissionClassname.PROPERTY, 
                new PropertyPermissionHandler());
        permissionHandlerMap.put(PermissionClassname.REFLECT, 
                reflectAndRuntimeHandler);
        permissionHandlerMap.put(PermissionClassname.RUNTIME, 
                reflectAndRuntimeHandler);
        permissionHandlerMap.put(PermissionClassname.SOCKET,
                new SocketPermissionHandler(reportSecurityContext));
        permissionHandlerMap.put(PermissionClassname.OSGI, 
                new OSGIPermissionHandler());

        return permissionHandlerMap;

    }

    /**
     * @return the permissionHandlerMap
     */
    public Map<PermissionClassname, IReportPermissionHandler> getPermissionHandlerMap() {
        return permissionHandlerMap;
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

}
