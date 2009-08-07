/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

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
	
	private static final Logger log = Logger.getLogger(ClientPropertyPlaceholderConfigurer.class);
	
	protected String resolvePlaceholder(String placeholder, Properties props) {
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		
		// When verinice.serverURL is requested, return the string from the application's
		// preferences or the internal server's URL. 
		if (placeholder.equals("verinice.serverURL"))
		{
			String server = null;
			if (prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER))
				server = PreferenceConstants.VNSERVER_URI_INTERNAL;
			else
				server = correctServerURI(prefs.getString(PreferenceConstants.VNSERVER_URI));
			
			return server;
		}
		
		return props.getProperty(placeholder);
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
		while (i > 0 && uri.codePointAt(i) == '/')
			i--;

		uri = uri.substring(0, i + 1); 
		
		log.debug("corrected server URI to: " + uri);
		
		return uri;
	}


}
