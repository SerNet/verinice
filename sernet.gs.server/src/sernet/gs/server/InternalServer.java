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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ops4j.pax.web.service.WebContainer;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.springframework.osgi.web.context.support.OsgiBundleXmlWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderServlet;
import org.springframework.web.servlet.DispatcherServlet;

import sernet.gs.ui.rcp.main.service.IInternalServer;

/**
 * Implementation of the {@link IInternalServer} interface which allows
 * managing the internal verinice server.
 * 
 * <p>An instance of this class is supposed to be registered as an
 * OSGi service. The verinice client will lookup this service and
 * interact with the server component through it.</p>
 * 
 */
public class InternalServer implements IInternalServer {

	boolean running = false;

	private ContextLoaderServlet contextLoaderServlet;

	private DispatcherServlet dispatcherServlet;

	private WebContainer wc;

	private HttpContext ctx;

	/**
	 * Applies the given database credentials to the verinice server.
	 * 
	 * <p>The credentials will be used when the server is started next
	 * time.</p>
	 */
	public void configure(String url, String user, String pass, String driver,
			String dialect) {
		ServerPropertyPlaceholderConfigurer.setDatabaseProperties(url, user,
				pass, driver, dialect);
	}

	/**
	 * Starts the verinice server.
	 * 
	 * <p>Before each start the server must be stopped.</p>
	 * 
	 * <p>The first start will also initialize the underlying servlet
	 * container and as such will take longer.</p>
	 */
	public void start() {
		if (running)
			throw new IllegalStateException("Server is still running.");

		try {
			if (wc == null)
				initialSetup();

			setupSpringServlets();
		} catch (ServletException se) {
			throw new IllegalStateException("Could not start internal server.",
					se);
		} catch (NamespaceException nse) {
			throw new IllegalStateException("Could not start internal server.",
					nse);
		}

		running = true;
	}

	/**
	 * Stops the verinice server.
	 * 
	 * <p>By purpose this does (yet) shutdown the servlet container.</p>
	 */
	public void stop() {
		if (!running)
			throw new IllegalStateException("Server is not running.");

		teardownSpringServlets();

		running = false;
	}

	/**
	 * Returns whether the server is currently active.
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Performs the initial setup of the server which means configuring
	 * things that cannot be changed afterwards or are not dependent upon
	 * the Spring configuration.</p>
	 * 
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	private void initialSetup() throws ServletException, NamespaceException {
		wc = Activator.getDefault().getWebContainer();

		ctx = wc.createDefaultHttpContext();

		Dictionary<String, String> dict = new Hashtable<String, String>();
		dict.put("contextConfigLocation", "\n"
				+ "WebContent/WEB-INF/veriniceserver-common.xml \n"
				+ "WebContent/WEB-INF/veriniceserver-osgi.xml \n"
				+ "WebContent/WEB-INF/veriniceserver-daos-common.xml \n"
				+ "WebContent/WEB-INF/veriniceserver-daos-osgi.xml \n"
				+ "WebContent/WEB-INF/veriniceserver-security-osgi.xml \n");
		dict.put(ContextLoader.CONTEXT_CLASS_PARAM,
				OsgiBundleXmlWebApplicationContext.class.getName());
		wc.setContextParam(dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "GetHitroConfig");
		wc.registerServlet(new GetHitroConfig(),
				new String[] { "/GetHitroConfig" }, dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "serverTest");
		wc.registerServlet(new ServerTestServlet(),
				new String[] { "/servertest" }, dict, ctx);
	}

	/**
	 * Registers the Spring servlets which effectively starts the
	 * verinice server.
	 * 
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	private void setupSpringServlets() throws ServletException,
			NamespaceException {
		Dictionary<String, String> dict = new Hashtable<String, String>();
		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "context");
		contextLoaderServlet = new ContextLoaderServlet();
		wc.registerServlet("/context", contextLoaderServlet, dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "springDispatcher");
		dict.put("contextConfigLocation",
				"WebContent/WEB-INF/springDispatcher-servlet.xml");
		dict.put(ContextLoader.CONTEXT_CLASS_PARAM,
				OsgiBundleXmlWebApplicationContext.class.getName());
		dispatcherServlet = new DispatcherServlet();
		wc.registerServlet(dispatcherServlet, new String[] { "/service/*" },
				dict, ctx);
	}

	/**
	 * Unregisters the Spring servlets which effectively stops the
	 * verinice server.
	 * 
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	private void teardownSpringServlets() {
		wc.unregisterServlet(dispatcherServlet);
		wc.unregisterServlet(contextLoaderServlet);
	}

	/**
	 * Helper servlet which tells the state of the internal server.
	 * 
	 * <p>The servlet's output can be seen with a web browser at
	 * <a href="localhost:8800/servertest">localhost:8800/servertest</a> .</p>
	 */
	private class ServerTestServlet extends HttpServlet {

		private static final long serialVersionUID = 131427514191056452L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			System.err.println("doGet");

			resp.setContentType("text/html");

			PrintWriter w = resp.getWriter();

			w.println("internal verinice server is running: " + running);

			w.flush();
		}

	}
}
