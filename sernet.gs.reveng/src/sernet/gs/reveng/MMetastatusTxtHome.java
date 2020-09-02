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
 * Home object for domain model class MMetastatusTxt.
 * @see sernet.gs.reveng.MMetastatusTxt
 * @author Hibernate Tools
 */
public class MMetastatusTxtHome {

	private static final Log log = LogFactory.getLog(MMetastatusTxtHome.class);

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

	public void persist(MMetastatusTxt transientInstance) {
		log.debug("persisting MMetastatusTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(MMetastatusTxt instance) {
		log.debug("attaching dirty MMetastatusTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MMetastatusTxt instance) {
		log.debug("attaching clean MMetastatusTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(MMetastatusTxt persistentInstance) {
		log.debug("deleting MMetastatusTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MMetastatusTxt merge(MMetastatusTxt detachedInstance) {
		log.debug("merging MMetastatusTxt instance");
		try {
			MMetastatusTxt result = (MMetastatusTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public MMetastatusTxt findById(sernet.gs.reveng.MMetastatusTxtId id) {
		log.debug("getting MMetastatusTxt instance with id: " + id);
		try {
			MMetastatusTxt instance = (MMetastatusTxt) sessionFactory
					.getCurrentSession().get("sernet.gs.reveng.MMetastatusTxt",
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

	public List findByExample(MMetastatusTxt instance) {
		log.debug("finding MMetastatusTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.MMetastatusTxt")
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
