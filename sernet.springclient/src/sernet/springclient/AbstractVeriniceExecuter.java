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
 *     Benjamin Weiﬂenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.springclient;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;

abstract public class AbstractVeriniceExecuter extends CommonsHttpInvokerRequestExecutor {

    protected static final int MAX_TOTAL_CONNECTIONS = 20;

    protected static final int MAX_CONNECTIONS_PER_HOST = 5;

    static final Logger LOG = Logger.getLogger(AbstractVeriniceExecuter.class);

    protected static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 1000;
    protected static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (30 * 60 * 1000);
    protected int readTimeout = DEFAULT_READ_TIMEOUT_MILLISECONDS;
    protected int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;

    public AbstractVeriniceExecuter() {
        super();
    }

    public AbstractVeriniceExecuter(HttpClient httpClient) {
        super(httpClient);
    }

    /**
     * This method is configured as Spring init-method in veriniceclient.xml
     */
    public void init() {
        final int maxConPerHost = MAX_CONNECTIONS_PER_HOST;
        final int maxTotalCon = MAX_TOTAL_CONNECTIONS;
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, maxConPerHost);
        connectionManager.getParams().setMaxTotalConnections(maxTotalCon);
        
        connectionManager.getParams().setConnectionTimeout(getConnectionTimeout()); 
        connectionManager.getParams().setSoTimeout(getReadTimeout());
        connectionManager.getParams();

        HttpClient httpClient = new HttpClient(connectionManager);

        configureProxy(httpClient);
        setHttpClient(httpClient);
    }

    /**
     * @param httpClient
     */
    protected void configureProxy(HttpClient httpClient) {
        String proxyHost = System.getProperty("http.proxyHost");
        Integer proxyPort = null;
        if (System.getProperty("http.proxyPort") != null) {
            proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
        }

        if (proxyHost != null && proxyPort != null && !proxyHost.isEmpty()) {
            httpClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
            if (LOG.isInfoEnabled()) {
                LOG.info("Using proxy host: " + proxyHost + ", port: " + proxyPort);
            }
            String proxyName = System.getProperty("http.proxyName");
            String proxyPassword = System.getProperty("http.proxyPassword");

            if (proxyName != null && proxyPassword != null) {
                httpClient.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyName, proxyPassword));
                if (LOG.isInfoEnabled()) {
                    LOG.info("Using proxy user name: " + proxyHost + " and password");
                }
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No proxy is used.");
        }
    }

    /**
     * @return the readTimeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * @param readTimeout
     *            the readTimeout to set
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
     * @param connectionTimeout
     *            the connectionTimeout to set
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

}