/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
import java.net.HttpURLConnection;

import org.apache.log4j.Logger;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class RequestExecuter extends SimpleHttpInvokerRequestExecutor {

    private static final Logger LOG = Logger.getLogger(RequestExecuter.class);
    
    private int readTimeout = -1;

    /**
     * Prepare the given HTTP connection.
     * <p>The default implementation specifies POST as method,
     * "application/x-java-serialized-object" as "Content-Type" header,
     * and the given content length as "Content-Length" header.
     * @param con the HTTP connection to prepare
     * @param contentLength the length of the content to send
     * @throws IOException if thrown by HttpURLConnection methods
     * @see java.net.HttpURLConnection#setRequestMethod
     * @see java.net.HttpURLConnection#setRequestProperty
     */
    protected void prepareConnection(HttpURLConnection con, int contentLength) throws IOException {
        super.prepareConnection(con, contentLength);
        if(getReadTimeout()!=-1) {
            con.setReadTimeout(getReadTimeout());
        }
    }
    
    /**
     * Returns the read timeout
     * 
     * @return the read timeout
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout
     * You set the timeout in your spring configuration:
     * 
     * <bean name="myExecuter" class="sernet.springclient.RequestExecuter">
     *   <!-- Request-timeout in ms -->
     *   <property name="readTimeout" value="500000"/>
     * </bean>
     * 
     * @param readTimeout the read timeout in ms
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor#doExecuteRequest(org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration, java.io.ByteArrayOutputStream)
     */
    @Override
    protected RemoteInvocationResult doExecuteRequest(HttpInvokerClientConfiguration config, ByteArrayOutputStream baos) throws IOException, ClassNotFoundException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doExecuteRequest: " + config.getServiceUrl(), new RuntimeException());
        }
        return super.doExecuteRequest(config, baos);
    }
}
