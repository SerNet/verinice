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
 * Home object for domain model class StgRaZobGefMit.
 * @see sernet.gs.reveng.StgRaZobGefMit
 * @author Hibernate Tools
 */
public class StgRaZobGefMitHome {

	private static final Log log = LogFactory.getLog(StgRaZobGefMitHome.class);

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

	public void persist(StgRaZobGefMit transientInstance) {
		log.debug("persisting StgRaZobGefMit instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgRaZobGefMit instance) {
		log.debug("attaching dirty StgRaZobGefMit instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgRaZobGefMit instance) {
		log.debug("attaching clean StgRaZobGefMit instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgRaZobGefMit persistentInstance) {
		log.debug("deleting StgRaZobGefMit instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgRaZobGefMit merge(StgRaZobGefMit detachedInstance) {
		log.debug("merging StgRaZobGefMit instance");
		try {
			StgRaZobGefMit result = (StgRaZobGefMit) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgRaZobGefMit findById(sernet.gs.reveng.StgRaZobGefMitId id) {
		log.debug("getting StgRaZobGefMit instance with id: " + id);
		try {
			StgRaZobGefMit instance = (StgRaZobGefMit) sessionFactory
					.getCurrentSession().get("sernet.gs.reveng.StgRaZobGefMit",
							id);
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

	public List findByExample(StgRaZobGefMit instance) {
		log.debug("finding StgRaZobGefMit instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgRaZobGefMit")
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
