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

import java.security.Permission;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.eclipse.osgi.util.NLS;

import sernet.verinice.report.service.impl.security.Messages;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ReflectAndRuntimePermissionHandler extends AbstractPermissionHandler {
    
    private final static Logger LOG = Logger.getLogger(ReflectAndRuntimePermissionHandler.class);
    

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

}
