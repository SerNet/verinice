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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.net.auth.Authentication;
import org.eclipse.ui.internal.net.auth.UserValidationDialog;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CommonsExecuter extends CommonsHttpInvokerRequestExecutor {

    private static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS =  1000;
    // 30min = 30*60*1000 = 1.800.000 ms
    private static final int DEFAULT_READ_TIMEOUT_MILLISECONDS =  (30 * 60 * 1000);
    private static final Logger LOG = Logger.getLogger(CommonsExecuter.class);
    
    private int readTimeout = DEFAULT_READ_TIMEOUT_MILLISECONDS;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;
    
    
    /**
     * Create a new CommonsHttpInvokerRequestExecutor with a default
     * HttpClient that uses a default MultiThreadedHttpConnectionManager.
     * Sets the socket read timeout to {@link #DEFAULT_READ_TIMEOUT_MILLISECONDS}.
     * @see org.apache.commons.httpclient.HttpClient
     * @see org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
     */
    public CommonsExecuter() {
        super();
    }

    /**
     * This method is configured as Spring init-method in veriniceclient.xml
     */
    public void init() {
        final int maxConPerHost = 5;
        final int maxTotalCon = 20;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, maxConPerHost);
        connectionManager.getParams().setMaxTotalConnections(maxTotalCon);
        connectionManager.getParams().setConnectionTimeout(getConnectionTimeout()); //set connection timeout (how long it takes to connect to remote host)
        connectionManager.getParams().setSoTimeout(getReadTimeout()); 
        HttpClient httpClient = new HttpClient(connectionManager);
        httpClient.getParams().setParameter(CredentialsProvider.PROVIDER, new AuthProvider());           
        configureProxy(httpClient);       
        setHttpClient(httpClient);
    }
    
    /**
     * @param httpClient
     */
    private void configureProxy(HttpClient httpClient) {
        String proxyHost = System.getProperty("http.proxyHost"); 
        Integer proxyPort = null;
        if(System.getProperty("http.proxyPort")!=null) {
            proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
        }
        
        if(proxyHost!=null && proxyPort!=null && !proxyHost.isEmpty() ) {
            httpClient.getHostConfiguration().setProxy(proxyHost,proxyPort);
            if (LOG.isInfoEnabled()) {
                LOG.info("Using proxy host: " + proxyHost + ", port: " + proxyPort);
            }
            String proxyName = System.getProperty("http.proxyName");
            String proxyPassword = System.getProperty("http.proxyPassword");
            
            if(proxyName!=null && proxyPassword!=null) {
                httpClient.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyName, proxyPassword));
                if (LOG.isInfoEnabled()) {
                    LOG.info("Using proxy user name: " + proxyHost + " and password");
                }
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No proxy is used.");
        }    
    }
 
    protected RemoteInvocationResult doExecuteRequest(
            HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
            throws IOException, ClassNotFoundException {
        if (LOG.isInfoEnabled()) {
            LOG.info("doExecuteRequest: " + config.getServiceUrl());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Debug Stacktrace: ", new RuntimeException("This is not an error. Exception is thrown for debug only."));
            }
        }
        try {
            return super.doExecuteRequest(config, baos);
        } catch (IOException e) {
            LOG.error("IOException while executing request.", e);
            throw e;
        } catch (ClassNotFoundException e) {
            LOG.error("ClassNotFoundException while executing request.", e);
            throw e;
        } catch (RuntimeException re) {
            LOG.error("Error while executing request.", re);
            throw re;
        } catch (Exception t) {
            LOG.error("Error while executing request.", t);
            throw new RuntimeException(t);
        }
    }
    
    /**
     * @return the readTimeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * @param readTimeout the readTimeout to set
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;      
    }

    /**
     * @return the connectionTimeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @param connectionTimeout the connectionTimeout to set
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    class AuthProvider implements CredentialsProvider {

        /* (non-Javadoc)
         * @see org.apache.commons.httpclient.auth.CredentialsProvider#getCredentials(org.apache.commons.httpclient.auth.AuthScheme, java.lang.String, int, boolean)
         */
        @Override
        public Credentials getCredentials(AuthScheme authScheme, String host, int port, boolean arg3) throws CredentialsNotAvailableException {
            Authentication auth = AuthDialog.getAuthentication(host + ", Port: " + port, authScheme.getRealm());
            return new UsernamePasswordCredentials(auth.getUser(), auth.getPassword());
        }
        
    }
    
}

class AuthDialog extends UserValidationDialog {
    
    private static boolean canceled = false;
    
    public static Authentication getAuthentication(final String host, final String message) {
        class UIOperation implements Runnable {
            private Authentication authentication;
            public void run() {
                authentication = AuthDialog.askForAuthentication(host, message);
            }
        }
        UIOperation uio = new UIOperation();
        if (Display.getCurrent() != null) {
            uio.run();
        } else {
            Display.getDefault().syncExec(uio);
        }
        return uio.authentication;
    }
    /**
     * Gets user and password from a user Must be called from UI thread
     * 
     * @return UserAuthentication that contains the userid and the password or
     *         <code>null</code> if the dialog has been cancelled
     */
    protected static Authentication askForAuthentication(String host, String message) {
        Authentication authentication = null; 
        UserValidationDialog ui = new AuthDialog(null, host, message); 
        if(!canceled) {
            ui.open();         
        } 
        authentication = ui.getAuthentication();
        return authentication;
    }
     
    /**
     * @param parentShell
     * @param host
     * @param message
     */
    protected AuthDialog(Shell parentShell, String host, String message) {
        super(parentShell, host, message);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("verinice.PRO - Login"); 
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        canceled=true;
        super.cancelPressed();
    }
}


