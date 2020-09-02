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
 * Home object for domain model class StgMbDringlichkeit.
 * @see sernet.gs.reveng.StgMbDringlichkeit
 * @author Hibernate Tools
 */
public class StgMbDringlichkeitHome {

	private static final Log log = LogFactory
			.getLog(StgMbDringlichkeitHome.class);

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

	public void persist(StgMbDringlichkeit transientInstance) {
		log.debug("persisting StgMbDringlichkeit instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMbDringlichkeit instance) {
		log.debug("attaching dirty StgMbDringlichkeit instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMbDringlichkeit instance) {
		log.debug("attaching clean StgMbDringlichkeit instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMbDringlichkeit persistentInstance) {
		log.debug("deleting StgMbDringlichkeit instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMbDringlichkeit merge(StgMbDringlichkeit detachedInstance) {
		log.debug("merging StgMbDringlichkeit instance");
		try {
			StgMbDringlichkeit result = (StgMbDringlichkeit) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMbDringlichkeit findById(sernet.gs.reveng.StgMbDringlichkeitId id) {
		log.debug("getting StgMbDringlichkeit instance with id: " + id);
		try {
			StgMbDringlichkeit instance = (StgMbDringlichkeit) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMbDringlichkeit", id);
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

	public List findByExample(StgMbDringlichkeit instance) {
		log.debug("finding StgMbDringlichkeit instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMbDringlichkeit")
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
