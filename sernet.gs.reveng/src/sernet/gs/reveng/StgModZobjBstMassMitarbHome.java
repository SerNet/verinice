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
 * Home object for domain model class StgModZobjBstMassMitarb.
 * @see sernet.gs.reveng.StgModZobjBstMassMitarb
 * @author Hibernate Tools
 */
public class StgModZobjBstMassMitarbHome {

	private static final Log log = LogFactory
			.getLog(StgModZobjBstMassMitarbHome.class);

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

	public void persist(StgModZobjBstMassMitarb transientInstance) {
		log.debug("persisting StgModZobjBstMassMitarb instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgModZobjBstMassMitarb instance) {
		log.debug("attaching dirty StgModZobjBstMassMitarb instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgModZobjBstMassMitarb instance) {
		log.debug("attaching clean StgModZobjBstMassMitarb instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgModZobjBstMassMitarb persistentInstance) {
		log.debug("deleting StgModZobjBstMassMitarb instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgModZobjBstMassMitarb merge(
			StgModZobjBstMassMitarb detachedInstance) {
		log.debug("merging StgModZobjBstMassMitarb instance");
		try {
			StgModZobjBstMassMitarb result = (StgModZobjBstMassMitarb) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgModZobjBstMassMitarb findById(
			sernet.gs.reveng.StgModZobjBstMassMitarbId id) {
		log.debug("getting StgModZobjBstMassMitarb instance with id: " + id);
		try {
			StgModZobjBstMassMitarb instance = (StgModZobjBstMassMitarb) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgModZobjBstMassMitarb", id);
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

	public List findByExample(StgModZobjBstMassMitarb instance) {
		log.debug("finding StgModZobjBstMassMitarb instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgModZobjBstMassMitarb")
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
