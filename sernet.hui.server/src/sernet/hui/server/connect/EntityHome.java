


package sernet.hui.server.connect;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

import sernet.hui.common.connect.Entity;

public class EntityHome {
	
	private static final String QUERY_FIND_BY_CATEGORY 
		= "from " + Entity.class.getName() + " as entity " +
				"where entity.entityType = ?";

	private static EntityHome instance;
	
	private SessionFactory sessionFactory;
	private Session session;
	
	private EntityHome() {}
	
	public static EntityHome getInstance() {
		if (instance == null)
			instance = new EntityHome();
		return instance;
	}

	public void open() {
		sessionFactory = new Configuration().configure().buildSessionFactory();
		session = sessionFactory.openSession();
	}

	public void close() {
		if (session != null)
			session.close();
		if (sessionFactory != null)
			sessionFactory.close();
	}

	public void create(Entity entity) throws Exception {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(entity);
			tx.commit();
		} catch (Exception e) {
			 if (tx!=null) 
				 tx.rollback();
		     throw e;
		}
	}
	
	public void update(Entity entity) throws Exception {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.merge(entity);
			tx.commit();
		} catch (Exception e) {
			 if (tx!=null) 
				 tx.rollback();
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
