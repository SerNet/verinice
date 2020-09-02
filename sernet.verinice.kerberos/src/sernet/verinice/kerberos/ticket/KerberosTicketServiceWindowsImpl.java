/*******************************************************************************
 * Copyright (c) 2015 verinice.
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
 *     verinice <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.kerberos.ticket;

import javax.naming.AuthenticationNotSupportedException;

import org.apache.log4j.Logger;

import com.google.common.io.BaseEncoding;
import com.sun.jna.platform.win32.Sspi;
import com.sun.jna.platform.win32.Sspi.SecBufferDesc;

import sernet.verinice.kerberos.Activator;
import sernet.verinice.kerberos.preferences.PreferenceConstants;
import sernet.verinice.service.auth.KerberosTicketService;
import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.impl.WindowsAccountImpl;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

/**
 * @author Benjamin Wei�enfels <bw[at]sernet[dot]de>
 *
 */
public class KerberosTicketServiceWindowsImpl implements KerberosTicketService {

    private static Logger LOG = Logger.getLogger(KerberosStatusServiceImpl.class);
    
    private String clientToken;

    private IWindowsCredentialsHandle clientCredentials;

    private WindowsSecurityContextImpl clientContext;

    @Override
    public String getClientToken() {
        clientCredentials = WindowsCredentialsHandleImpl.getCurrent(SECURITY_PACKAGE);
        clientCredentials.initialize();
        clientContext = new WindowsSecurityContextImpl();
        clientContext.setPrincipalName(WindowsAccountImpl.getCurrentUsername());
        clientContext.setCredentialsHandle(clientCredentials.getHandle());
        clientContext.setSecurityPackage(SECURITY_PACKAGE);
        clientContext.initialize(clientContext.getHandle(), null, getADServiceName());

        clientToken = SECURITY_PACKAGE + " " + BaseEncoding.base64().encode(clientContext.getToken());
        
        if(LOG.isDebugEnabled()){
            LOG.debug("client AD token: " + clientToken);
        }
        
        return clientToken;
    }
 

    @Override
    public String updateClientToken(String serviceNegatiationAnswer) {

        byte[] continueTokenBytes = BaseEncoding.base64().decode(serviceNegatiationAnswer);

        if (continueTokenBytes.length > 0) {

            SecBufferDesc continueTokenBuffer = new SecBufferDesc(Sspi.SECBUFFER_TOKEN, continueTokenBytes);
            clientContext.initialize(clientContext.getHandle(), continueTokenBuffer, getADServiceName());
            return SECURITY_PACKAGE + " " + BaseEncoding.base64().encode(clientContext.getToken());
        }

        throw new RuntimeException(new AuthenticationNotSupportedException("no continue token was sent by the server"));
    }
    
    private String getADServiceName() {
        return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.VERINICEPRO_SERVICE_NAME);
    }
}
