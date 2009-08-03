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
package sernet.gs.server;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.springframework.osgi.web.context.support.OsgiBundleXmlWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderServlet;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

public class Activator extends Plugin {

	Logger log = Logger.getLogger(Activator.class);

	// The plug-in ID
	public static final String PLUGIN_ID = "veriniceServer";

	// The shared instance
	private static Activator plugin;

	private static final String OSGI_EXTENDER_SYMBOLIC_NAME = "org.springframework.osgi.extender";

	private static final String PAX_WEB_SYMBOLIC_NAME = "org.ops4j.pax.web.pax-web-bundle";

	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);

		plugin = this;

		// Starts the Spring OSGi Extender which provides registering the Spring
		// namespace handlers. If you get exception saying there is no schema 
		// for Spring security, then the OSGi extender is not running.
		for (Bundle bundle : context.getBundles()) {
			if (bundle.getSymbolicName().equals(OSGI_EXTENDER_SYMBOLIC_NAME)) {
				if (bundle.getState() == Bundle.INSTALLED
						|| bundle.getState() == Bundle.RESOLVED) {
					log.debug("Manually starting Spring's OSGi Extender");
					bundle.start();
				}
			}
		}

		ServiceReference sr = context.getServiceReference(WebContainer.class
				.getName());

		// Starts the Pax Web Bundle. This makes the HTTP service available.
		if (sr == null) {
			for (Bundle bundle : context.getBundles()) {
				if (bundle.getSymbolicName().equals(PAX_WEB_SYMBOLIC_NAME)) {
					if (bundle.getState() == Bundle.INSTALLED
							|| bundle.getState() == Bundle.RESOLVED) {
						log.debug("Manually starting Pax Web Http Service.");
						bundle.start();
					} else
						throw new IllegalStateException(
								"pax-web bundle is not in a proper state to get started.");

					sr = context.getServiceReference(WebContainer.class
							.getName());
					if (sr == null)
						throw new IllegalStateException(
								"pax-web bundle was started but there is still no http service available. Giving up.");
				}
			}
		}
		if (sr == null)
			throw new IllegalStateException("No http service. Giving up.");

		WebContainer wc = (WebContainer) context.getService(sr);

		HttpContext ctx = wc.createDefaultHttpContext();

		Dictionary<String, String> dict = new Hashtable<String, String>();
		dict.put("contextConfigLocation",
				"WebContent/WEB-INF/applicationContext-veriniceserver.xml");
		dict.put(ContextLoader.CONTEXT_CLASS_PARAM,
				OsgiBundleXmlWebApplicationContext.class.getName());
		wc.setContextParam(dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "GetHitroConfig");
		wc.registerServlet(new GetHitroConfig(),
				new String[] { "/GetHitroConfig" }, dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "context");
		wc.registerServlet("/context", new ContextLoaderServlet(), dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "springDispatcher");
		dict.put("contextConfigLocation",
				"WebContent/WEB-INF/springDispatcher-servlet.xml");
		dict.put(ContextLoader.CONTEXT_CLASS_PARAM,
				OsgiBundleXmlWebApplicationContext.class.getName());
		wc.registerServlet(new DispatcherServlet(),
				new String[] { "/service/*" }, dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "serverTest");
		wc.registerServlet(new ServerTestServlet(),
				new String[] { "/servertest" }, dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("filter-name", "springSecurityFilterChain");

		wc.registerFilter(new DelegatingFilterProxy(),
				new String[] { "/service/*" }, null, dict, ctx);

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

}
