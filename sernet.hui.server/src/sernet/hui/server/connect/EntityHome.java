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

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

import sernet.hui.common.connect.Entity;

public final class EntityHome {
	
	private static final String QUERY_FIND_BY_CATEGORY 
		= "from " + Entity.class.getName() + " as entity " +
				"where entity.entityType = ?";

	private static EntityHome instance;
	
	private SessionFactory sessionFactory;
	private Session session;
	
	private EntityHome() {}
	
	public static EntityHome getInstance() {
		if (instance == null){
			instance = new EntityHome();
		}
		return instance;
	}

	public void open() {
		sessionFactory = new Configuration().configure().buildSessionFactory();
		session = sessionFactory.openSession();
	}

	public void close() {
		if (session != null){
			session.close();
		}
		if (sessionFactory != null){
			sessionFactory.close();
		}
	}

	public void create(Entity entity) throws HibernateException {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(entity);
			tx.commit();
		} catch (HibernateException e) {
			 if (tx!=null) {
				 tx.rollback();
			 }
		     throw e;
		}
	}
	
	public void update(Entity entity) throws HibernateException {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.merge(entity);
			tx.commit();
		} catch (HibernateException e) {
			 if (tx!=null){ 
				 tx.rollback();
			 }
		     throw e;
		}
	}

	public Entity findEntityById(int id) {
		return (Entity) session.get(Entity.class, id);
	}

	public List<Entity> findByCategory(String type) {
		Query query = session.createQuery(QUERY_FIND_BY_CATEGORY);
		query.setString(0, type);
		return query.list();
	}
}
