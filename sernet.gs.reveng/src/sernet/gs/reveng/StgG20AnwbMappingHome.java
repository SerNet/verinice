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
 * Home object for domain model class StgG20AnwbMapping.
 * @see sernet.gs.reveng.StgG20AnwbMapping
 * @author Hibernate Tools
 */
public class StgG20AnwbMappingHome {

	private static final Log log = LogFactory
			.getLog(StgG20AnwbMappingHome.class);

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

	public void persist(StgG20AnwbMapping transientInstance) {
		log.debug("persisting StgG20AnwbMapping instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgG20AnwbMapping instance) {
		log.debug("attaching dirty StgG20AnwbMapping instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgG20AnwbMapping instance) {
		log.debug("attaching clean StgG20AnwbMapping instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgG20AnwbMapping persistentInstance) {
		log.debug("deleting StgG20AnwbMapping instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgG20AnwbMapping merge(StgG20AnwbMapping detachedInstance) {
		log.debug("merging StgG20AnwbMapping instance");
		try {
			StgG20AnwbMapping result = (StgG20AnwbMapping) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgG20AnwbMapping findById(sernet.gs.reveng.StgG20AnwbMappingId id) {
		log.debug("getting StgG20AnwbMapping instance with id: " + id);
		try {
			StgG20AnwbMapping instance = (StgG20AnwbMapping) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgG20AnwbMapping", id);
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

	public List findByExample(StgG20AnwbMapping instance) {
		log.debug("finding StgG20AnwbMapping instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgG20AnwbMapping")
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
