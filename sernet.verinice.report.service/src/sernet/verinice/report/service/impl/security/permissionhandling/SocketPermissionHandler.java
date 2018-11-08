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

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.Permission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sernet.verinice.security.report.ReportSecurityContext;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class SocketPermissionHandler extends AbstractPermissionHandler {
    
    private final static Logger LOG = LoggerFactory.getLogger(SocketPermissionHandler.class);
    private ReportSecurityContext reportSecurityContext;

    public SocketPermissionHandler(ReportSecurityContext reportSecurityContext) {
        this.reportSecurityContext = reportSecurityContext;
    }

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

}
