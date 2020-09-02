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
 * Home object for domain model class MbMassnnkatTxt.
 * @see sernet.gs.reveng.MbMassnnkatTxt
 * @author Hibernate Tools
 */
public class MbMassnnkatTxtHome {

	private static final Log log = LogFactory.getLog(MbMassnnkatTxtHome.class);

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

	public void persist(MbMassnnkatTxt transientInstance) {
		log.debug("persisting MbMassnnkatTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(MbMassnnkatTxt instance) {
		log.debug("attaching dirty MbMassnnkatTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbMassnnkatTxt instance) {
		log.debug("attaching clean MbMassnnkatTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(MbMassnnkatTxt persistentInstance) {
		log.debug("deleting MbMassnnkatTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbMassnnkatTxt merge(MbMassnnkatTxt detachedInstance) {
		log.debug("merging MbMassnnkatTxt instance");
		try {
			MbMassnnkatTxt result = (MbMassnnkatTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public MbMassnnkatTxt findById(sernet.gs.reveng.MbMassnnkatTxtId id) {
		log.debug("getting MbMassnnkatTxt instance with id: " + id);
		try {
			MbMassnnkatTxt instance = (MbMassnnkatTxt) sessionFactory
					.getCurrentSession().get("sernet.gs.reveng.MbMassnnkatTxt",
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

	public List findByExample(MbMassnnkatTxt instance) {
		log.debug("finding MbMassnnkatTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.MbMassnnkatTxt")
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
