package sernet.springclient;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.log4j.Logger;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;

public class AbstractExecuter extends CommonsHttpInvokerRequestExecutor {
    
    public static final int DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS =  1000;
    public static final int DEFAULT_READ_TIMEOUT_MILLISECONDS =  (30 * 60 * 1000);
    
    public int readTimeout = DEFAULT_READ_TIMEOUT_MILLISECONDS;
    public int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;
    
    public static final int MAX_TOTAL_CONNECTIONS = 20;
    public static final int MAX_CONNECTIONS_PER_HOST = 5;

    private static final Logger LOG = Logger.getLogger(AbstractExecuter.class);
    
    public AbstractExecuter() {
        super();
    }

    public AbstractExecuter(HttpClient httpClient) {
        super(httpClient);
    }

    protected void configureProxy(HttpClient httpClient) {
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
}