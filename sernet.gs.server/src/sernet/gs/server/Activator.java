package sernet.gs.server;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.Plugin;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.springframework.web.context.ContextLoaderServlet;
import org.springframework.web.servlet.DispatcherServlet;

public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "veriniceServer";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		ServerInitializer si = new ServerInitializer();
		si.setConfiguration(new ServerConfiguration());
		si.initialize();
		
		ServiceReference sr = context.getServiceReference(WebContainer.class.getName());
		
		if (sr == null)
			return;
		
		WebContainer wc = (WebContainer) context.getService(sr);
		
		HttpContext ctx = wc.createDefaultHttpContext();
		
		Dictionary<String, String> dict = new Hashtable<String, String>();
		dict.put("contextConfigLocation", "WEB-INF/applicationContext-veriniceserver.xml");
		wc.setContextParam(dict, ctx);
		
		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "springDispatcher");
		wc.registerServlet(new DispatcherServlet(), new String[] { "/service/*" }, dict, ctx); 

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "context");
		wc.registerServlet(new ContextLoaderServlet(), new String[] { "/context" }, dict, ctx); 

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "serverTest");
		wc.registerServlet(new ServerTestServlet(), new String[] { "/servertest" }, dict, ctx); 

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "GetHitroConfig");
		wc.registerServlet(new GetHitroConfig(), new String[] { "/GetHitroConfig" }, dict, ctx);
		
/*		
		dict = new Hashtable<String, String>();
		dict.put("filter-name", "springSecurityFilterChain");
		
		wc.registerFilter(new DelegatingFilterProxy(), new String[] { "/service/*" }, null, dict, ctx); 
*/		
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
