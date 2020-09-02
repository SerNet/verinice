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
 * Home object for domain model class StgMbZielobjTypTxt.
 * @see sernet.gs.reveng.StgMbZielobjTypTxt
 * @author Hibernate Tools
 */
public class StgMbZielobjTypTxtHome {

	private static final Log log = LogFactory
			.getLog(StgMbZielobjTypTxtHome.class);

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

	public void persist(StgMbZielobjTypTxt transientInstance) {
		log.debug("persisting StgMbZielobjTypTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMbZielobjTypTxt instance) {
		log.debug("attaching dirty StgMbZielobjTypTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMbZielobjTypTxt instance) {
		log.debug("attaching clean StgMbZielobjTypTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMbZielobjTypTxt persistentInstance) {
		log.debug("deleting StgMbZielobjTypTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMbZielobjTypTxt merge(StgMbZielobjTypTxt detachedInstance) {
		log.debug("merging StgMbZielobjTypTxt instance");
		try {
			StgMbZielobjTypTxt result = (StgMbZielobjTypTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMbZielobjTypTxt findById(sernet.gs.reveng.StgMbZielobjTypTxtId id) {
		log.debug("getting StgMbZielobjTypTxt instance with id: " + id);
		try {
			StgMbZielobjTypTxt instance = (StgMbZielobjTypTxt) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMbZielobjTypTxt", id);
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

	public List findByExample(StgMbZielobjTypTxt instance) {
		log.debug("finding StgMbZielobjTypTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMbZielobjTypTxt")
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
