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
 * Home object for domain model class StgMMetastatusTxt.
 * @see sernet.gs.reveng.StgMMetastatusTxt
 * @author Hibernate Tools
 */
public class StgMMetastatusTxtHome {

	private static final Log log = LogFactory
			.getLog(StgMMetastatusTxtHome.class);

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

	public void persist(StgMMetastatusTxt transientInstance) {
		log.debug("persisting StgMMetastatusTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMMetastatusTxt instance) {
		log.debug("attaching dirty StgMMetastatusTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMMetastatusTxt instance) {
		log.debug("attaching clean StgMMetastatusTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMMetastatusTxt persistentInstance) {
		log.debug("deleting StgMMetastatusTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMMetastatusTxt merge(StgMMetastatusTxt detachedInstance) {
		log.debug("merging StgMMetastatusTxt instance");
		try {
			StgMMetastatusTxt result = (StgMMetastatusTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMMetastatusTxt findById(sernet.gs.reveng.StgMMetastatusTxtId id) {
		log.debug("getting StgMMetastatusTxt instance with id: " + id);
		try {
			StgMMetastatusTxt instance = (StgMMetastatusTxt) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMMetastatusTxt", id);
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

	public List findByExample(StgMMetastatusTxt instance) {
		log.debug("finding StgMMetastatusTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMMetastatusTxt")
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
