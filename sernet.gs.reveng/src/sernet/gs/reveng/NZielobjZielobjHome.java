package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:34 PM by Hibernate Tools 3.4.0.CR1

import java.util.List;

import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class NZielobjZielobj.
 * @see sernet.gs.reveng.NZielobjZielobj
 * @author Hibernate Tools
 */
public class NZielobjZielobjHome {

	private static final Log log = LogFactory.getLog(NZielobjZielobjHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(NZielobjZielobj transientInstance) {
		log.debug("persisting NZielobjZielobj instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(NZielobjZielobj instance) {
		log.debug("attaching dirty NZielobjZielobj instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(NZielobjZielobj instance) {
		log.debug("attaching clean NZielobjZielobj instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(NZielobjZielobj persistentInstance) {
		log.debug("deleting NZielobjZielobj instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public NZielobjZielobj merge(NZielobjZielobj detachedInstance) {
		log.debug("merging NZielobjZielobj instance");
		try {
			NZielobjZielobj result = (NZielobjZielobj) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public NZielobjZielobj findById(sernet.gs.reveng.NZielobjZielobjId id) {
		log.debug("getting NZielobjZielobj instance with id: " + id);
		try {
			NZielobjZielobj instance = (NZielobjZielobj) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.NZielobjZielobj", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(NZielobjZielobj instance) {
		log.debug("finding NZielobjZielobj instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.NZielobjZielobj")
					.add(Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
