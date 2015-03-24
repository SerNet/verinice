package sernet.springclient;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.eclipse.core.internal.runtime.Activator;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;

import sernet.verinice.service.auth.KerberosStatusService;

abstract public class AbstractExecuter extends CommonsHttpInvokerRequestExecutor {

    public static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 1000;
    public static final int DEFAULT_READ_TIMEOUT_MILLISECONDS = (30 * 60 * 1000);

    public int readTimeout = DEFAULT_READ_TIMEOUT_MILLISECONDS;
    public int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;

    public static final int MAX_TOTAL_CONNECTIONS = 20;
    public static final int MAX_CONNECTIONS_PER_HOST = 5;

    private static final Logger LOG = Logger.getLogger(AbstractExecuter.class);

    public AbstractExecuter() {
        super();

        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

        connectionManager.getParams().setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, MAX_CONNECTIONS_PER_HOST);
        connectionManager.getParams().setMaxTotalConnections(MAX_TOTAL_CONNECTIONS);

        // set connection timeout (how long it takes to connect to remote host)
        connectionManager.getParams().setConnectionTimeout(getConnectionTimeout());
        connectionManager.getParams().setSoTimeout(getReadTimeout());

        setHttpClient(new HttpClient(connectionManager));

        configureProxy();

    }

    /**
     * Exceuted by the {@link ExecuterFactoryBean}. Do special initialization stuff in
     * this method.
     */
    abstract void init();

    protected void configureProxy() {
        String proxyHost = System.getProperty("http.proxyHost");
        Integer proxyPort = null;
        if (System.getProperty("http.proxyPort") != null) {
            proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
        }

        if (proxyHost != null && proxyPort != null && !proxyHost.isEmpty()) {
            getHttpClient().getHostConfiguration().setProxy(proxyHost, proxyPort);
            if (LOG.isInfoEnabled()) {
                LOG.info("Using proxy host: " + proxyHost + ", port: " + proxyPort);
            }
            String proxyName = System.getProperty("http.proxyName");
            String proxyPassword = System.getProperty("http.proxyPassword");

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

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

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