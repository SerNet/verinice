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
 * Home object for domain model class StgMsRaBehandTxt.
 * @see sernet.gs.reveng.StgMsRaBehandTxt
 * @author Hibernate Tools
 */
public class StgMsRaBehandTxtHome {

	private static final Log log = LogFactory
			.getLog(StgMsRaBehandTxtHome.class);

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

	public void persist(StgMsRaBehandTxt transientInstance) {
		log.debug("persisting StgMsRaBehandTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMsRaBehandTxt instance) {
		log.debug("attaching dirty StgMsRaBehandTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMsRaBehandTxt instance) {
		log.debug("attaching clean StgMsRaBehandTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMsRaBehandTxt persistentInstance) {
		log.debug("deleting StgMsRaBehandTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMsRaBehandTxt merge(StgMsRaBehandTxt detachedInstance) {
		log.debug("merging StgMsRaBehandTxt instance");
		try {
			StgMsRaBehandTxt result = (StgMsRaBehandTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMsRaBehandTxt findById(sernet.gs.reveng.StgMsRaBehandTxtId id) {
		log.debug("getting StgMsRaBehandTxt instance with id: " + id);
		try {
			StgMsRaBehandTxt instance = (StgMsRaBehandTxt) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMsRaBehandTxt", id);
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

	public List findByExample(StgMsRaBehandTxt instance) {
		log.debug("finding StgMsRaBehandTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMsRaBehandTxt")
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
