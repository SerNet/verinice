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
 * Home object for domain model class MUmsetzStatTxt.
 * @see sernet.gs.reveng.MUmsetzStatTxt
 * @author Hibernate Tools
 */
public class MUmsetzStatTxtHome {

	private static final Log log = LogFactory.getLog(MUmsetzStatTxtHome.class);

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

	public void persist(MUmsetzStatTxt transientInstance) {
		log.debug("persisting MUmsetzStatTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(MUmsetzStatTxt instance) {
		log.debug("attaching dirty MUmsetzStatTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MUmsetzStatTxt instance) {
		log.debug("attaching clean MUmsetzStatTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(MUmsetzStatTxt persistentInstance) {
		log.debug("deleting MUmsetzStatTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MUmsetzStatTxt merge(MUmsetzStatTxt detachedInstance) {
		log.debug("merging MUmsetzStatTxt instance");
		try {
			MUmsetzStatTxt result = (MUmsetzStatTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public MUmsetzStatTxt findById(sernet.gs.reveng.MUmsetzStatTxtId id) {
		log.debug("getting MUmsetzStatTxt instance with id: " + id);
		try {
			MUmsetzStatTxt instance = (MUmsetzStatTxt) sessionFactory
					.getCurrentSession().get("sernet.gs.reveng.MUmsetzStatTxt",
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

	public List findByExample(MUmsetzStatTxt instance) {
		log.debug("finding MUmsetzStatTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.MUmsetzStatTxt")
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
