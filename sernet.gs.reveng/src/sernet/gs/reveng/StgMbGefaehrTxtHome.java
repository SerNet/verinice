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
 * Home object for domain model class StgMbGefaehrTxt.
 * @see sernet.gs.reveng.StgMbGefaehrTxt
 * @author Hibernate Tools
 */
public class StgMbGefaehrTxtHome {

	private static final Log log = LogFactory.getLog(StgMbGefaehrTxtHome.class);

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

	public void persist(StgMbGefaehrTxt transientInstance) {
		log.debug("persisting StgMbGefaehrTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMbGefaehrTxt instance) {
		log.debug("attaching dirty StgMbGefaehrTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMbGefaehrTxt instance) {
		log.debug("attaching clean StgMbGefaehrTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMbGefaehrTxt persistentInstance) {
		log.debug("deleting StgMbGefaehrTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMbGefaehrTxt merge(StgMbGefaehrTxt detachedInstance) {
		log.debug("merging StgMbGefaehrTxt instance");
		try {
			StgMbGefaehrTxt result = (StgMbGefaehrTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMbGefaehrTxt findById(sernet.gs.reveng.StgMbGefaehrTxtId id) {
		log.debug("getting StgMbGefaehrTxt instance with id: " + id);
		try {
			StgMbGefaehrTxt instance = (StgMbGefaehrTxt) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMbGefaehrTxt", id);
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

	public List findByExample(StgMbGefaehrTxt instance) {
		log.debug("finding StgMbGefaehrTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMbGefaehrTxt")
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
