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
package sernet.hui.common.connect;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;


/**
 * SNCAResourceBundleLoader creates a resource bundle
 * by loading it remotely via HTTP or from a local file by JNDI Url.
 * 
 * @see SNCAMessages
 * @author Daniel Murygin <dm@sernet.de>
 */
public class SNCAResourceBundleLoader extends ResourceBundle.Control {
    
	private final Logger log = Logger.getLogger(SNCAResourceBundleLoader.class);
	
	private static final String HTTP = "http";
	private static final String HTTPS = "https";
	private static final String JNDI = "jndi";
	private static final String BUNDLERESOURCE = "bundleresource";
	public static final List<String> PROTOCOL_LIST;
	
	static {
		PROTOCOL_LIST = Arrays.asList(HTTP,HTTPS,JNDI,BUNDLERESOURCE);
	}
	
    private String baseUrl;
 
    public SNCAResourceBundleLoader(String baseUrl) {
    	this.baseUrl = baseUrl;
    }

    // Only "properties" files are used (e.g., autoparts.properties)
    @Override
    public List<String> getFormats(String baseName) {
        return Collections.singletonList(SNCAMessages.BUNDLE_EXTENSION);
    }

    /**
     * @o
     * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
     * Loads a resource bundle via HTTP.
     * @param   baseName the name of the bundle to instantiate.
     * @param   locale the given locale
     * @param   format the format: "properities"
     * @param   loader the classloader to use
     * @param   reload if reload the resource
     * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
     */
    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format,
              ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException,
              IOException {
    	
        ResourceBundle bundle = null;

        if ((baseName == null) || (locale == null) || (format == null) || (loader == null)) {
            return null;
        }
        
        // format must be '.properties'
        if (!format.equals(SNCAMessages.BUNDLE_EXTENSION)) {
            return null;
        }

        // Create bundle name from baseName and locale (e.g., "autoparts" (no locale)
        //   autoparts_fr (French)
        String bundleName = toBundleName(baseName, locale);
        
        // Create resource name (e.g., "autoparts.properties", "autoparts_fr.properties"
        String resourceName = toResourceName(bundleName, format);
        
        InputStream stream = null;
        String protocol = SNCAResourceBundleLoader.getProtocol(baseUrl);
        if(HTTP.equals(protocol) || HTTPS.equals(protocol)) {
	        stream = createHttpStream(resourceName, reload);
        } else if(PROTOCOL_LIST.contains(protocol)) {
        	stream = createUrlStream(resourceName);
        } else {
        	log.error("Url is not supported: " + baseUrl + ". Only http(s) or jndi Urls are supported");
        	// no exception is thrown since its only about labels...
        }
        
        BufferedInputStream bis = null;

        // Instantiate the bundle with the stream.
        try {
            bis = new BufferedInputStream(stream);
            bundle = new StreamResourceBundle(bis);
            bis.close();
        } finally {
            if (bis != null){
                try {
                    bis.close();
                } catch (Exception ignore) {
                }
            }
        }
        return bundle;
    }

	private InputStream createUrlStream(String resourceName) throws IOException {
		InputStream stream;
		String fullResourceName = baseUrl + resourceName;
		if (log.isDebugEnabled()) {
			log.debug("Loading resource bundle via url: " + fullResourceName);
		}
		URL url = new URL(fullResourceName);
		stream = url.openStream();
		return stream;
	}

	private InputStream createHttpStream(String resourceName, boolean reload) throws IOException {
		InputStream stream;
		String separator = "?";
		if(baseUrl.contains("?")) {
			separator = "&";
		}
		
		String fullResourceName=baseUrl + separator + "resource=" + resourceName;
		if (log.isDebugEnabled()) {
			log.debug("Loading resource bundle via url: " + fullResourceName);
		}
		
		// Create HttpURLConnection for the resource file
		URL proxy=new URL(fullResourceName);
		HttpURLConnection httpProxy = (HttpURLConnection)proxy.openConnection();
		if (httpProxy == null) {
		  return null;
		}
		if (reload) {
		  httpProxy.setUseCaches(false);
		}

		// Instantiate the input stream
		stream = httpProxy.getInputStream();
		return stream;
	}
	
	public static String getProtocol(String url) {
		String protocol = null;
		if(url!=null && url.contains(":")) {
			protocol = url.substring(0, url.indexOf(':'));
		}
		return protocol;
	}
}

