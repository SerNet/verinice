/*******************************************************************************
 * Copyright (c) 2009,2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import static sernet.gs.ui.rcp.main.preferences.PreferenceConstants.VNSERVER_HOST_INTERNAL;
import static sernet.gs.ui.rcp.main.preferences.PreferenceConstants.VNSERVER_SCHEME_INTERNAL;
import static sernet.gs.ui.rcp.main.preferences.PreferenceConstants.VNSERVER_URI_INTERNAL_PORT;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.IVeriniceConstants;
import sernet.verinice.interfaces.licensemanagement.ILicenseManagementService;

/**
 * This class provides keyword replacement in a Spring configuration when it
 * is instantiated therein.
 * 
 * <p>The actual use of this class is to wire the keyword replacement to
 * the API of the verinice client. E.g. whenever a certain property is
 * requested the request can be intercepted and a programmatically generated
 * value can be returned.</p>
 * 
 * <p>The class also works together with a properties file in case that is
 * needed.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de> 
 */
public class ClientPropertyPlaceholderConfigurer extends
		PropertyPlaceholderConfigurer {
	
	// port can be configured via jvm parameter
    private static final String ORG_OSGI_SERVICE_HTTP_PORT = "org.osgi.service.http.port";

    private static final Logger LOG = Logger.getLogger(ClientPropertyPlaceholderConfigurer.class);
	
	private static ServerModeAccessor serverModeAccessor = new RCPServerModeAccessor();
	
	protected String resolvePlaceholder(String placeholder, Properties props) {
		
		// When verinice.serverURL is requested, return the string from the application's
		// preferences or the internal server's URL. 
		if (placeholder.equals("verinice.serverURL"))
		{
			String server = null;
			if (isInternalServerMode()){
			    try{
                    String port = System.getProperty(ORG_OSGI_SERVICE_HTTP_PORT, VNSERVER_URI_INTERNAL_PORT);
                    URI uri = new URI(VNSERVER_SCHEME_INTERNAL + "://" + VNSERVER_HOST_INTERNAL + ":" + port);
                    server = uri.toString();
			    } catch(Throwable ex){
			        LOG.error("cannot set the internal server uri: " + ex.getLocalizedMessage(), ex);
			    }
			} else {
				server = correctServerURI(getServerURI());
			}
			return server;
		} else if(placeholder.equals(ILicenseManagementService.VNL_STORAGE_FOLDER)){
		    
		    // in standalone mode, vnl-files are stored in workspace
		    
		    StringBuilder sb = new StringBuilder(); 
		    sb.append(System.getProperty(IVeriniceConstants.OSGI_INSTANCE_AREA));
		    if(!sb.toString().endsWith(File.pathSeparator)){
		        sb.append(File.pathSeparator);
		    }
		    sb.append("licenseFiles").append(File.pathSeparator);
		    return sb.toString();
		}
		
		return props.getProperty(placeholder);
	}
	
	private boolean isInternalServerMode()
	{
		return serverModeAccessor.isInternalServerMode();
	}
	
	private String getServerURI()
	{
		return serverModeAccessor.getServerURI();
	}
	
	

	/**
	 * Takes a server URI and removes unwanted characters like trailing slashes
	 * from it.
	 * 
	 * @param uri
	 * @return
	 */
	private static String correctServerURI(String uri)
	{
		// Trailing slashes are a problem for the server. As such strip them away.
		int i = uri.length() - 1;
		while (i > 0 && uri.codePointAt(i) == '/'){
			i--;
		}
		String returnedUri = uri.substring(0, i + 1); 
		
		LOG.debug("corrected server URI to: " + uri);
		
		return returnedUri;
	}
	
	/**
	 * Call this method to set the URI of the verinice server that should
	 * be connected to.
	 * 
	 * <p>This method must be called before the Spring IoC container that uses
	 * instances of this class is being initialized. Otherwise calling this method
	 * has no effect.</p>
	 * 
	 * <p>After calling this method the verinice workobjects are initialized
	 * as if a remote server is in use. Regardless of what any user-set property
	 * setting may say.</p>
	 *  
	 * @param serverURI
	 */
	public static void setRemoteServerMode(final String serverURI)
	{
		serverModeAccessor = new ServerModeAccessor()
		{
			public boolean isInternalServerMode()
			{
				return false;
			}
			
			public String getServerURI()
			{
				return serverURI;
			}
		};
	}

	/** Little helper interface to abstract accessing server configuration properties. 
	 * 
	 * @author Robert Schuster <r.schuster@tarent.de> 
	 */
	private interface ServerModeAccessor
	{
		boolean isInternalServerMode();
		
		String getServerURI();
	}
	
	/**
	 * Default implementation which configures the server according to what was
	 * set in the client application's property windows.
	 * 
	 * <p>Note: Do not use this class when it is not about running the whole
	 * client RCP application.</p>
	 *
	 * @author Robert Schuster <r.schuster@tarent.de> 
	 *
	 */
	private static class RCPServerModeAccessor implements ServerModeAccessor
	{
		private Preferences prefs = Activator.getDefault().getPluginPreferences();
		
		public boolean isInternalServerMode()
		{
			return prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);
		}
		
		public String getServerURI()
		{
			return prefs.getString(PreferenceConstants.VNSERVER_URI);
		}
	}
	
}
