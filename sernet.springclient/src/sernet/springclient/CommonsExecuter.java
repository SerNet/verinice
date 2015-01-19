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
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.net.auth.Authentication;
import org.eclipse.ui.internal.net.auth.UserValidationDialog;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CommonsExecuter extends AbstractVeriniceExecuter {

    /**
     * Create a new CommonsHttpInvokerRequestExecutor with a default HttpClient
     * that uses a default MultiThreadedHttpConnectionManager. Sets the socket
     * read timeout to {@link #DEFAULT_READ_TIMEOUT_MILLISECONDS}.
     * 
     * @see org.apache.commons.httpclient.HttpClient
     * @see org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
     */
    public CommonsExecuter() {
        super();
    }

    protected RemoteInvocationResult doExecuteRequest(HttpInvokerClientConfiguration config, ByteArrayOutputStream baos) throws IOException, ClassNotFoundException {
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

    class AuthProvider implements CredentialsProvider {

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.apache.commons.httpclient.auth.CredentialsProvider#getCredentials
         * (org.apache.commons.httpclient.auth.AuthScheme, java.lang.String,
         * int, boolean)
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
        if (!canceled) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        canceled = true;
        super.cancelPressed();
    }
}
