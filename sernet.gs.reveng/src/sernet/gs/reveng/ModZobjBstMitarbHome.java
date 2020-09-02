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
 * Home object for domain model class ModZobjBstMitarb.
 * @see sernet.gs.reveng.ModZobjBstMitarb
 * @author Hibernate Tools
 */
public class ModZobjBstMitarbHome {

	private static final Log log = LogFactory
			.getLog(ModZobjBstMitarbHome.class);

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

	public void persist(ModZobjBstMitarb transientInstance) {
		log.debug("persisting ModZobjBstMitarb instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(ModZobjBstMitarb instance) {
		log.debug("attaching dirty ModZobjBstMitarb instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(ModZobjBstMitarb instance) {
		log.debug("attaching clean ModZobjBstMitarb instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(ModZobjBstMitarb persistentInstance) {
		log.debug("deleting ModZobjBstMitarb instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public ModZobjBstMitarb merge(ModZobjBstMitarb detachedInstance) {
		log.debug("merging ModZobjBstMitarb instance");
		try {
			ModZobjBstMitarb result = (ModZobjBstMitarb) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public ModZobjBstMitarb findById(sernet.gs.reveng.ModZobjBstMitarbId id) {
		log.debug("getting ModZobjBstMitarb instance with id: " + id);
		try {
			ModZobjBstMitarb instance = (ModZobjBstMitarb) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.ModZobjBstMitarb", id);
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

	public List findByExample(ModZobjBstMitarb instance) {
		log.debug("finding ModZobjBstMitarb instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.ModZobjBstMitarb")
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
