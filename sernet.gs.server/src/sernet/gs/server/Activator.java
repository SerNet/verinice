/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
package sernet.gs.server;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import sernet.verinice.interfaces.IInternalServer;

public class Activator extends Plugin {

	private Logger log = Logger.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "veriniceServer";

	// The shared instance
	private static Activator plugin;

	private static final String PAX_WEB_SYMBOLIC_NAME = "org.ops4j.pax.web.pax-web-bundle";
	
	private WebContainer webContainer;
	
	private Object lock = new Object();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

		plugin = this;

		if (log.isInfoEnabled()) {
            final Bundle bundle = context.getBundle();
            log.info("Starting bundle " + bundle.getSymbolicName() + " " + bundle.getVersion());
        }
		
		// Only make sure that PAX WEB is available.
		Bundle bundle = Platform.getBundle(PAX_WEB_SYMBOLIC_NAME);
		if (bundle == null) {
			log.error("Pax Web bundle is not available. Giving up!");
			throw new RuntimeException();
		}
		
		// Allow access to internal server control.
		context.registerService(IInternalServer.class.getName(), new InternalServer(), null);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);

	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	WebContainer getWebContainer() {
		if (webContainer != null){
			return webContainer;
		}
		
		synchronized (lock) {
			if (webContainer != null){
				return webContainer;
			}
			BundleContext context = getBundle().getBundleContext();
			
			ServiceReference sr = context.getServiceReference(WebContainer.class
					.getName());

			// Starts the Pax Web Bundle. This makes the HTTP service available.
			if (sr == null) {
				Bundle bundle = Platform.getBundle(PAX_WEB_SYMBOLIC_NAME);
				if (bundle == null) {
					log.error("Pax Web bundle is not available. Giving up!");
					throw new RuntimeException();
				} else {
					if (bundle.getState() == Bundle.INSTALLED
							|| bundle.getState() == Bundle.RESOLVED) {
						log.debug("Manually starting Pax Web Http Service.");
						try {
							bundle.start();
						} catch (BundleException e) {
							throw new IllegalStateException(
							"starting pax-web bundle failed.");
						}
					} else {
						throw new IllegalStateException(
								"pax-web bundle is not in a proper state to get started.");
					}
					sr = context.getServiceReference(WebContainer.class.getName());
					if (sr == null){
						throw new IllegalStateException(
								"pax-web bundle was started but there is still no http service available. Giving up.");
					}
				}
			}

			if (sr == null){
				throw new IllegalStateException("No http service. Giving up.");
			}
			webContainer = (WebContainer) context.getService(sr);
		}
		return webContainer;
	}

}
