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

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class NetPermissionHandler extends AbstractPermissionHandler {

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

}
