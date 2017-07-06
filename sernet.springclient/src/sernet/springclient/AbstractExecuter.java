package sernet.springclient;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;

import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.service.auth.KerberosStatusService;

public abstract class AbstractExecuter extends CommonsHttpInvokerRequestExecutor {

    public static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 1000;
    public static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = 30 * 60 * 1000;

    private int readTimeout = DEFAULT_READ_TIMEOUT_MILLISECONDS;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;

    public static final int MAX_TOTAL_CONNECTIONS = 20;
    public static final int MAX_CONNECTIONS_PER_HOST = 5;

    private static final Logger LOG = Logger.getLogger(AbstractExecuter.class);

    public AbstractExecuter() {
        super();
        configureConnectionManager();
        configureProxy();
    }
    
    /**
     * @param connectionTimeout The time to wait for a new connection
     * @param readTimeout The time to wait for the result of a connection
     */
    public AbstractExecuter(int connectionTimeout, int readTimeout) {
        super();
        setConnectionTimeout(connectionTimeout);
        setReadTimeout(readTimeout);
        configureConnectionManager();
        configureProxy();
    }

    private void configureConnectionManager() {
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        connectionManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, MAX_CONNECTIONS_PER_HOST);
        connectionManager.getParams().setMaxTotalConnections(MAX_TOTAL_CONNECTIONS);

        // set connection timeout (how long it takes to connect to remote host)
        connectionManager.getParams().setConnectionTimeout(getConnectionTimeout());
        connectionManager.getParams().setSoTimeout(getReadTimeout());

        setHttpClient(new HttpClient(connectionManager));
        
        if (LOG.isInfoEnabled()) {
            LOG.info("HttpClient created, connection timeout (ms): " + getConnectionTimeout() + ", read timeout (ms): " + getReadTimeout() );
        }
    }

    /**
     * Exceuted by the {@link ExecuterFactoryBean}. Do special initialization stuff in
     * this method.
     */
    abstract void init();

    protected void configureProxy() {
        String proxyHost = System.getProperty(
                IVeriniceConstants.HTTP_PROXY_HOST);
        Integer proxyPort = null;
        if (System.getProperty(IVeriniceConstants.HTTP_PROXY_PORT) != null) {
            proxyPort = Integer.parseInt(System.getProperty(
                    IVeriniceConstants.HTTP_PROXY_PORT));
        }

        if (proxyHost != null && proxyPort != null && !proxyHost.isEmpty()) {
            getHttpClient().getHostConfiguration().setProxy(proxyHost, proxyPort);
            if (LOG.isInfoEnabled()) {
                LOG.info("Using proxy host: " + proxyHost + ", port: " + proxyPort);
            }
            String proxyName = System.getProperty(
                    IVeriniceConstants.HTTP_PROXY_NAME);
            String proxyPassword = System.getProperty(
                    IVeriniceConstants.HTTP_PROXY_PASSWORD);

            if (proxyName != null && proxyPassword != null) {
                getHttpClient().getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyName, proxyPassword));
                if (LOG.isInfoEnabled()) {
                    LOG.info("Using proxy user name: " + proxyHost + " and password");
                }
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("No proxy is used.");
        }
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * @param readTimeout The time to wait for the result of a connection
     * @see org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor#setReadTimeout(int)
     */
    @Override
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @param connectionTimeout The time to wait for a new connection
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public static AbstractExecuter initExecuter() {

        KerberosStatusService kerberosStatusService = SpringClientPlugin.getDefault().getKerberosStatusService();

        if (kerberosStatusService != null && kerberosStatusService.isActive()) {
            KerberosExecuter executer = new KerberosExecuter();
            executer.init();
            return executer;
        } else {
            CommonsExecuter commonsExecuter = new CommonsExecuter();
            commonsExecuter.init();
            return commonsExecuter;
        }
    }
}