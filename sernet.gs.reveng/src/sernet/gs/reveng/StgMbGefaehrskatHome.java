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
 * Home object for domain model class StgMbGefaehrskat.
 * @see sernet.gs.reveng.StgMbGefaehrskat
 * @author Hibernate Tools
 */
public class StgMbGefaehrskatHome {

	private static final Log log = LogFactory
			.getLog(StgMbGefaehrskatHome.class);

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

	public void persist(StgMbGefaehrskat transientInstance) {
		log.debug("persisting StgMbGefaehrskat instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMbGefaehrskat instance) {
		log.debug("attaching dirty StgMbGefaehrskat instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMbGefaehrskat instance) {
		log.debug("attaching clean StgMbGefaehrskat instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMbGefaehrskat persistentInstance) {
		log.debug("deleting StgMbGefaehrskat instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMbGefaehrskat merge(StgMbGefaehrskat detachedInstance) {
		log.debug("merging StgMbGefaehrskat instance");
		try {
			StgMbGefaehrskat result = (StgMbGefaehrskat) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMbGefaehrskat findById(sernet.gs.reveng.StgMbGefaehrskatId id) {
		log.debug("getting StgMbGefaehrskat instance with id: " + id);
		try {
			StgMbGefaehrskat instance = (StgMbGefaehrskat) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMbGefaehrskat", id);
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

	public List findByExample(StgMbGefaehrskat instance) {
		log.debug("finding StgMbGefaehrskat instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMbGefaehrskat")
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
