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
 * Home object for domain model class StgMbZielobjSubtypTxt.
 * @see sernet.gs.reveng.StgMbZielobjSubtypTxt
 * @author Hibernate Tools
 */
public class StgMbZielobjSubtypTxtHome {

	private static final Log log = LogFactory
			.getLog(StgMbZielobjSubtypTxtHome.class);

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

	public void persist(StgMbZielobjSubtypTxt transientInstance) {
		log.debug("persisting StgMbZielobjSubtypTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMbZielobjSubtypTxt instance) {
		log.debug("attaching dirty StgMbZielobjSubtypTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMbZielobjSubtypTxt instance) {
		log.debug("attaching clean StgMbZielobjSubtypTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMbZielobjSubtypTxt persistentInstance) {
		log.debug("deleting StgMbZielobjSubtypTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMbZielobjSubtypTxt merge(StgMbZielobjSubtypTxt detachedInstance) {
		log.debug("merging StgMbZielobjSubtypTxt instance");
		try {
			StgMbZielobjSubtypTxt result = (StgMbZielobjSubtypTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMbZielobjSubtypTxt findById(
			sernet.gs.reveng.StgMbZielobjSubtypTxtId id) {
		log.debug("getting StgMbZielobjSubtypTxt instance with id: " + id);
		try {
			StgMbZielobjSubtypTxt instance = (StgMbZielobjSubtypTxt) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMbZielobjSubtypTxt", id);
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

	public List findByExample(StgMbZielobjSubtypTxt instance) {
		log.debug("finding StgMbZielobjSubtypTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMbZielobjSubtypTxt")
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
