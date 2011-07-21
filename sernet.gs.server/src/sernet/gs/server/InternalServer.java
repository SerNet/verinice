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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ops4j.pax.web.service.WebContainer;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.NamespaceException;
import org.springframework.osgi.web.context.support.OsgiBundleXmlWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderServlet;
import org.springframework.web.servlet.DispatcherServlet;

import sernet.verinice.interfaces.IInternalServer;

/**
 * Implementation of the {@link IInternalServer} interface which allows managing
 * the internal verinice server.
 * 
 * <p>
 * An instance of this class is supposed to be registered as an OSGi service.
 * The verinice client will lookup this service and interact with the server
 * component through it.
 * </p>
 * 
 */
public class InternalServer implements IInternalServer {

	private final Logger log = Logger.getLogger(InternalServer.class);
	
	boolean running = false;

	private ContextLoaderServlet contextLoaderServlet;

	private DispatcherServlet dispatcherServlet;

	private WebContainer wc;

	private HttpContext ctx;

	/**
	 * Applies the given database credentials to the verinice server and checks
	 * their validity.
	 * 
	 * <p>
	 * If the credentials are invalid an {@link IllegalStateException} is
	 * thrown.
	 * </p>
	 * 
	 * <p>
	 * The credentials will be used when the server is started next time.
	 * </p>
	 */
	public void configure(String url, String user, String pass, String driver, String dialect) {

		boolean fail = false;
		try {
			Class.forName(driver);

			Connection c = DriverManager.getConnection(url, user, pass);

			c.close();
		} catch (ClassNotFoundException cnfe) {
			throw new IllegalStateException(Messages.InternalServer_0 + driver);
		} catch (SQLException sqle) {
			log.error("Could not connect to Database", sqle);
			fail = true;
		}

		if (fail) {
			ServerPropertyPlaceholderConfigurer.setDatabaseProperties("InternalServer.configure.failed", "InternalServer.configure.failed", "InternalServer.configure.failed", "InternalServer.configure.failed", "InternalServer.configure.failed");

		} else {
			ServerPropertyPlaceholderConfigurer.setDatabaseProperties(url, user, pass, driver, dialect);
		}
	}

	public void setGSCatalogURL(URL url) {
		ServerPropertyPlaceholderConfigurer.setGSCatalogURL(url);
	}

	public void setDSCatalogURL(URL url) {
		ServerPropertyPlaceholderConfigurer.setDSCatalogURL(url);
	}

	/**
	 * Starts the verinice server.
	 * 
	 * <p>
	 * Before each start the server must be stopped.
	 * </p>
	 * 
	 * <p>
	 * The first start will also initialize the underlying servlet container and
	 * as such will take longer.
	 * </p>
	 * 
	 * <p>
	 * In case the server could not be started an @{link IllegalStateException}
	 * is thrown.
	 * </p>
	 */
	public synchronized void start() throws IllegalStateException {
		if (log.isDebugEnabled()) {
			log.debug("start(), starting internal server...");
		}
		if (running) {
			throw new IllegalStateException(Messages.InternalServer_2);
		}
		try {
			if (wc == null) {
				initialSetup();
			}
			setupSpringServlets();
		} catch (ServletException se) {
			log.error("Error while starting internal server.",se);
			throw new IllegalStateException(Messages.InternalServer_3, se);
		} catch (NamespaceException nse) {
			log.error("Error while starting internal server.",nse);
			throw new IllegalStateException(Messages.InternalServer_3, nse);
		} catch (Exception e) {
			log.error("Error while starting internal server.",e);
			throw new IllegalStateException(Messages.InternalServer_3, e);
		}
		running = true;
		if (log.isInfoEnabled()) {
			log.info("Internal server is running now");
		}
	}

	/**
	 * Stops the verinice server.
	 * 
	 * <p>
	 * By purpose this does (yet) shutdown the servlet container.
	 * </p>
	 * 
	 * <p>
	 * When the server is already stopped this method has no effect.
	 * </p>
	 */
	public void stop() {
		if (!running)
			return;

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
	 * Performs the initial setup of the server which means configuring things
	 * that cannot be changed afterwards or are not dependent upon the Spring
	 * configuration.</p>
	 * 
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	private void initialSetup() throws ServletException, NamespaceException {
		wc = Activator.getDefault().getWebContainer();

		ctx = wc.createDefaultHttpContext();

		/*Spring config in gs.server web.xml
		classpath:sernet/gs/server/spring/veriniceserver-common.xml
        classpath:sernet/gs/server/spring/veriniceserver-plain.xml
        classpath:sernet/gs/server/spring/veriniceserver-daos-common.xml
        classpath:sernet/gs/server/spring/veriniceserver-daos-plain.xml  
        classpath:sernet/gs/server/spring/veriniceserver-security-plain-internal.xml  
        classpath:sernet/gs/server/spring/veriniceserver-webservice.xml   
        classpath:sernet/gs/server/spring/veriniceserver-jbpm.xml
		*/
		
		
		Dictionary<String, String> dict = new Hashtable<String, String>();
		dict.put("contextConfigLocation", "\n" //$NON-NLS-1$ //$NON-NLS-2$	        
				+ "classpath:/sernet/gs/server/spring/veriniceserver-common.xml \n" //$NON-NLS-1$
				+ "classpath:/sernet/gs/server/spring/veriniceserver-osgi.xml \n" //$NON-NLS-1$
				+ "classpath:/sernet/gs/server/spring/veriniceserver-daos-common.xml \n" //$NON-NLS-1$
				+ "classpath:/sernet/gs/server/spring/veriniceserver-daos-osgi.xml \n" //$NON-NLS-1$
				+ "classpath:/sernet/gs/server/spring/veriniceserver-security-osgi.xml \n" //$NON-NLS-1$
				+ "classpath:/sernet/gs/server/spring/veriniceserver-ldap.xml \n" //$NON-NLS-1$
				+ "classpath:/sernet/gs/server/spring/veriniceserver-jbpm-dummy.xml"); //$NON-NLS-1$
		dict.put(ContextLoader.CONTEXT_CLASS_PARAM, OsgiBundleXmlWebApplicationContext.class.getName());
		wc.setContextParam(dict, ctx);

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "GetHitroConfig"); //$NON-NLS-1$ //$NON-NLS-2$
		dict.put("snca.xml.path", "/WebContent/WEB-INF/"); //$NON-NLS-1$ //$NON-NLS-2$
		wc.registerServlet(new GetHitroConfig(), new String[] { "/GetHitroConfig" }, dict, ctx); //$NON-NLS-1$

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "serverTest"); //$NON-NLS-1$ //$NON-NLS-2$
		wc.registerServlet(new ServerTestServlet(), new String[] { "/servertest" }, dict, ctx); //$NON-NLS-1$
	}

	/**
	 * Registers the Spring servlets which effectively starts the verinice
	 * server.
	 * 
	 * @throws ServletException
	 * @throws NamespaceException
	 */
	private void setupSpringServlets() throws ServletException, NamespaceException {
		Dictionary<String, String> dict = new Hashtable<String, String>();
		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "context"); //$NON-NLS-1$ //$NON-NLS-2$
		dict.put(ContextLoader.CONTEXT_CLASS_PARAM, OsgiBundleXmlWebApplicationContext.class.getName());
		contextLoaderServlet = new ContextLoaderServlet();
		wc.registerServlet("/context", contextLoaderServlet, dict, ctx); //$NON-NLS-1$

		dict = new Hashtable<String, String>();
		dict.put("servlet-name", "springDispatcher"); //$NON-NLS-1$ //$NON-NLS-2$
		dict.put("contextConfigLocation", "classpath:/sernet/gs/server/spring/springDispatcher-servlet.xml"); //$NON-NLS-1$ //$NON-NLS-2$      
		dispatcherServlet = new DispatcherServlet();
		wc.registerServlet(dispatcherServlet, new String[] { "/service/*" }, dict, ctx); //$NON-NLS-1$
				
	}

	/**
	 * Unregisters the Spring servlets which effectively stops the verinice
	 * server.
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
	 * <p>
	 * The servlet's output can be seen with a web browser at <a
	 * href="localhost:8800/servertest">localhost:8800/servertest</a> .
	 * </p>
	 */
	private class ServerTestServlet extends HttpServlet {

		private static final long serialVersionUID = 131427514191056452L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			System.err.println("doGet"); //$NON-NLS-1$

			resp.setContentType("text/html"); //$NON-NLS-1$

			PrintWriter w = resp.getWriter();

			if (running)
				w.println(Messages.InternalServer_4);
			else
				w.println(Messages.InternalServer_5);

			w.flush();
		}

	}
}
