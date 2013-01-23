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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;


/**
 * SNCAMessages loads labels for HUI entities from resource bundles.
 * SNCAMessages can access resource bundles remotely by http urls
 * or from a local file by jndi urls.
 * 
 * Remote access is done by a ordinary {@link ResourceBundle} which contains a 
 * {@link SNCAResourceBundleLoader}. SNCAResourceBundleLoader created the
 * ResourceBundle by loading it by http or jndi URL.
 * 
 * @see SNCAResourceBundleLoader
 * @author Daniel Murygin <dm@sernet.de>
 */
public class SNCAMessages {
	
	private static final Logger LOG = Logger.getLogger(SNCAMessages.class);
	
	public static final String BUNDLE_NAME = "snca-messages"; //$NON-NLS-1$
	public static final String BUNDLE_EXTENSION = "properties";

	private ResourceBundle resourceBundle;

	private String baseUrl;

	public SNCAMessages(String baseUrl) {
		setBaseUrl(baseUrl);
	}

	String getString(String key) {
		String resource = null;
		try {
			final ResourceBundle bundle = getResourceBundle();
			if(bundle!=null) {
				resource = bundle.getString(key);
			}
		} catch (MissingResourceException e) {
		    if (LOG.isDebugEnabled()) {        
    			StringBuilder sb = new StringBuilder();
    			sb.append("missing resource: ").append(key);
    			sb.append(", baseUrl: ").append(baseUrl);
    			sb.append(", bundle-name: ").append(BUNDLE_NAME);
    			LOG.debug(sb.toString());
		    }
			return null;
		}
		return resource;
	}
	
	private ResourceBundle getResourceBundle() {
		if(resourceBundle==null) {
			if(getBaseUrl()==null) {
				LOG.error("Can not load resource bundle. Base url is null");
			} else {
				String protocol = SNCAResourceBundleLoader.getProtocol(getBaseUrl());
				if(protocol==null || !SNCAResourceBundleLoader.PROTOCOL_LIST.contains(protocol)) {
					LOG.error("Can not load resource bundle. Protocol is not supported: " + protocol);
				} else {
					resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME,new SNCAResourceBundleLoader(getBaseUrl()));
				}
			}		
		}
		return resourceBundle;
	}
	
	

	public void setBaseUrl(String baseUrl) {
		if(baseUrl!=null && baseUrl.endsWith(HUITypeFactory.HUI_CONFIGURATION_FILE)) {
			this.baseUrl = baseUrl.substring(0,baseUrl.indexOf(HUITypeFactory.HUI_CONFIGURATION_FILE));
		} else {
		    this.baseUrl = baseUrl;
		}
	}

	public String getBaseUrl() {
		return baseUrl;
	}
}
