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
 ******************************************************************************/
package sernet.hui.server.connect;

import java.net.URL;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;


public class TestHibernate extends TestCase {
	
	private SessionFactory sessionFactory;
	private Session session;

	public void testCreate() throws Exception  {

			try {
				HUITypeFactory htf = HUITypeFactory
				.createInstance(new URL("/home/aprack/java/workspace/sernet.hui.common/"
								+ "xml/opengt.xml"));
				EntityType system = HUITypeFactory.getInstance()
						.getEntityType("system");
	
				sessionFactory = new Configuration().configure().buildSessionFactory();
				session = sessionFactory.openSession();
				Transaction tx = session.beginTransaction();
				Entity entity = new Entity("system");
				
				PropertyType propertyType = system.getPropertyType("leitungsnr");
				entity.createNewProperty(propertyType, "4711");
				entity.createNewProperty(propertyType, "0815");
				
				session.save(entity);
				session.flush();
				tx.commit();
				
				session = sessionFactory.openSession();
				List<Entity> list = session.createQuery("from " + Entity.class.getName()).list();
				assertTrue("Entities wurden nicht gespeichert.", list.size() > 0);
				for (Entity loadedEnt : list) {
					System.out.println("Hibernate test saved properties: ");
					System.out.println(loadedEnt.getProperties("leitungsnr")
							.getProperty(0).getPropertyValue());
					System.out.println(loadedEnt.getProperties("leitungsnr")
							.getProperty(1).getPropertyValue());
				}
				
				
			} catch (Exception e) {
				throw e;
			}
			finally {
				if (session != null) {
					session.close();
				}
			}
	}
	

}
