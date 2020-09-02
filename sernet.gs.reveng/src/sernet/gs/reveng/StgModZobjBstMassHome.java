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
 * Home object for domain model class StgModZobjBstMass.
 * @see sernet.gs.reveng.StgModZobjBstMass
 * @author Hibernate Tools
 */
public class StgModZobjBstMassHome {

	private static final Log log = LogFactory
			.getLog(StgModZobjBstMassHome.class);

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

	public void persist(StgModZobjBstMass transientInstance) {
		log.debug("persisting StgModZobjBstMass instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgModZobjBstMass instance) {
		log.debug("attaching dirty StgModZobjBstMass instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgModZobjBstMass instance) {
		log.debug("attaching clean StgModZobjBstMass instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgModZobjBstMass persistentInstance) {
		log.debug("deleting StgModZobjBstMass instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgModZobjBstMass merge(StgModZobjBstMass detachedInstance) {
		log.debug("merging StgModZobjBstMass instance");
		try {
			StgModZobjBstMass result = (StgModZobjBstMass) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgModZobjBstMass findById(sernet.gs.reveng.StgModZobjBstMassId id) {
		log.debug("getting StgModZobjBstMass instance with id: " + id);
		try {
			StgModZobjBstMass instance = (StgModZobjBstMass) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgModZobjBstMass", id);
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

	public List findByExample(StgModZobjBstMass instance) {
		log.debug("finding StgModZobjBstMass instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgModZobjBstMass")
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
