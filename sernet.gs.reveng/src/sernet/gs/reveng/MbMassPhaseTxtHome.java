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
 * Home object for domain model class MbMassPhaseTxt.
 * @see sernet.gs.reveng.MbMassPhaseTxt
 * @author Hibernate Tools
 */
public class MbMassPhaseTxtHome {

	private static final Log log = LogFactory.getLog(MbMassPhaseTxtHome.class);

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

	public void persist(MbMassPhaseTxt transientInstance) {
		log.debug("persisting MbMassPhaseTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(MbMassPhaseTxt instance) {
		log.debug("attaching dirty MbMassPhaseTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbMassPhaseTxt instance) {
		log.debug("attaching clean MbMassPhaseTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(MbMassPhaseTxt persistentInstance) {
		log.debug("deleting MbMassPhaseTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbMassPhaseTxt merge(MbMassPhaseTxt detachedInstance) {
		log.debug("merging MbMassPhaseTxt instance");
		try {
			MbMassPhaseTxt result = (MbMassPhaseTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public MbMassPhaseTxt findById(sernet.gs.reveng.MbMassPhaseTxtId id) {
		log.debug("getting MbMassPhaseTxt instance with id: " + id);
		try {
			MbMassPhaseTxt instance = (MbMassPhaseTxt) sessionFactory
					.getCurrentSession().get("sernet.gs.reveng.MbMassPhaseTxt",
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

	public List findByExample(MbMassPhaseTxt instance) {
		log.debug("finding MbMassPhaseTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.MbMassPhaseTxt")
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
