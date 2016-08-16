/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.springclient;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.eclipse.ui.internal.net.auth.Authentication;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CommonsExecuter extends AbstractExecuter {

    /**
     * Create a new CommonsHttpInvokerRequestExecutor with a default HttpClient
     * that uses a default MultiThreadedHttpConnectionManager.
     * 
     * @see org.apache.commons.httpclient.HttpClient
     * @see org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
     */
    public CommonsExecuter() {
        super();
    }
    
    /**
     * Create a new CommonsHttpInvokerRequestExecutor with a default HttpClient
     * that uses a default MultiThreadedHttpConnectionManager. 

     * @param connectionTimeout The time to wait for a new connection
     * @param readTimeout The time to wait for the result of a connection
     * @see org.apache.commons.httpclient.HttpClient
     * @see org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
     */
    public CommonsExecuter(int connectionTimeout, int readTimeout) {
        super(connectionTimeout, readTimeout);
    }

    @Override
    void init() {

        /**
         * Sets the verinice password dialog.
         */
        getHttpClient().getParams().setParameter(CredentialsProvider.PROVIDER, new CredentialsProvider() {

            @Override
            public Credentials getCredentials(AuthScheme authScheme, String host, int port, boolean arg3) throws CredentialsNotAvailableException {
                Authentication auth = AuthDialog.getAuthentication(host + ", Port: " + port, authScheme.getRealm());
                return new UsernamePasswordCredentials(auth.getUser(), auth.getPassword());
            }
        });
    }
}
