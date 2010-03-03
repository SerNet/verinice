/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster@tarent.de - reworked to use LocalSessionFactoryBean
 ******************************************************************************/
 package sernet.gs.reveng;

import java.net.URL;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Configures and provides access to Hibernate sessions, tied to the
 * current thread of execution.  Follows the Thread Local Session
 * pattern, see {@link http://hibernate.org/42.html }.
 * 
 * <p>The creation of {@link Session} instances is managed through the Spring
 * class {@link LocalSessionFactoryBean}. By using this class the loading
 * of the mappings can be done using the right class loader in an OSGi
 * environment. For a non-OSGi environment this has no effect.</p>
 * 
 * <p>This class needs spring-orm and spring-core.</p>
 */
public class HibernateSessionFactory {

	private static final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
	
    /** 
     * Location of hibernate.cfg.xml file.
     * Location should be on the classpath as Hibernate uses  
     * #resourceAsStream style lookup for its configuration file. 
     * The default classpath location of the hibernate config file is 
     * in the default package. Use #setConfigFile() to update 
     * the location of the configuration file for the current session.   
     */
    private static String CONFIG_FILE_LOCATION = "/hibernate-vampire.cfg.xml";
    
	private static Resource configLocation = new ClassPathResource(CONFIG_FILE_LOCATION);
	
	private static LocalSessionFactoryBean sessionFactoryBean;
	
    private static SessionFactory sessionFactory;
	
    private HibernateSessionFactory() {
    	// Intentionally do nothing.
    }
    
	/**
     * Returns the ThreadLocal Session instance.  Lazy initialize
     * the <code>SessionFactory</code> if needed.
     *
     *  @return Session
     *  @throws HibernateException
     */
    static Session getSession() throws HibernateException {
        Session session = (Session) threadLocal.get();

		if (session == null || !session.isOpen()) {
			if (sessionFactory == null) {
				rebuildSessionFactory();
			}
			session = (sessionFactory != null) ? sessionFactory.openSession()
					: null;
			threadLocal.set(session);
		}

        return session;
    }

	/**
     *  Rebuild hibernate session factory
     *
     */
	private static void rebuildSessionFactory() {
		try {
			Class.forName("net.sourceforge.jtds.jdbc.Driver",
					true,
					SessionFactory.class.getClassLoader());
			
        	sessionFactoryBean = new LocalSessionFactoryBean();
        	sessionFactoryBean.setConfigLocation(configLocation);
        	sessionFactoryBean.setBeanClassLoader(HibernateSessionFactory.class.getClassLoader());
        	
        	sessionFactoryBean.afterPropertiesSet();
        	
        	sessionFactory = (org.hibernate.SessionFactory) sessionFactoryBean.getObject();
		} catch (Exception e) {
			System.err
					.println("%%%% Error Creating SessionFactory %%%%");
			e.printStackTrace();
		}
	}

	/**
     *  Closes the single hibernate session instance.
     *
     *  @throws HibernateException
     */
    public static void closeSession() throws HibernateException {
        Session session = (Session) threadLocal.get();
        threadLocal.set(null);

        if (session != null) {
            session.close();
        }
    }

	/**
     *  Returns the session factory
     *
     */
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	private static void setConfigResource(Resource r)
	{
		if (threadLocal.get() != null)
			throw new IllegalStateException("A session instance is still available. Close it first!");
		
		if (sessionFactoryBean != null)
		{
			sessionFactoryBean.destroy();
			sessionFactoryBean = null;
		}
		sessionFactory = null;
		
		configLocation = r;
	}
	
	/**
	 * Provides a location in the local filesystem where
	 * the hibernate configuration is located.
	 * 
	 * <p>Changing the location requires all previously opened
	 * sessions to be closed. An {@link IllegalStateException} is
	 * thrown if this precondition is violated.</p>
     */
	public static void setConfigFile(String configFile) {
		setConfigResource(new FileSystemResource(configFile));
	}

	/**
	 * Provides an arbitrary URL which points to a
	 * hibernate configuration.
	 * 
	 * <p>Changing the location requires all previously opened
	 * sessions to be closed. An {@link IllegalStateException} is
	 * thrown if this precondition is violated.</p>
     */
	public static void setConfigURL(URL url) {
		setConfigResource(new UrlResource(url));
	}
}