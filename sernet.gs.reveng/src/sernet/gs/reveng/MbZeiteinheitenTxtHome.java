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
 * Home object for domain model class MbZeiteinheitenTxt.
 * @see sernet.gs.reveng.MbZeiteinheitenTxt
 * @author Hibernate Tools
 */
public class MbZeiteinheitenTxtHome {

	private static final Log log = LogFactory
			.getLog(MbZeiteinheitenTxtHome.class);

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

	public void persist(MbZeiteinheitenTxt transientInstance) {
		log.debug("persisting MbZeiteinheitenTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(MbZeiteinheitenTxt instance) {
		log.debug("attaching dirty MbZeiteinheitenTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbZeiteinheitenTxt instance) {
		log.debug("attaching clean MbZeiteinheitenTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(MbZeiteinheitenTxt persistentInstance) {
		log.debug("deleting MbZeiteinheitenTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbZeiteinheitenTxt merge(MbZeiteinheitenTxt detachedInstance) {
		log.debug("merging MbZeiteinheitenTxt instance");
		try {
			MbZeiteinheitenTxt result = (MbZeiteinheitenTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public MbZeiteinheitenTxt findById(sernet.gs.reveng.MbZeiteinheitenTxtId id) {
		log.debug("getting MbZeiteinheitenTxt instance with id: " + id);
		try {
			MbZeiteinheitenTxt instance = (MbZeiteinheitenTxt) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.MbZeiteinheitenTxt", id);
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

	public List findByExample(MbZeiteinheitenTxt instance) {
		log.debug("finding MbZeiteinheitenTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.MbZeiteinheitenTxt")
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
