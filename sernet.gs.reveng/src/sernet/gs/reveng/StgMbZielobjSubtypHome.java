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
 * Home object for domain model class StgMbZielobjSubtyp.
 * @see sernet.gs.reveng.StgMbZielobjSubtyp
 * @author Hibernate Tools
 */
public class StgMbZielobjSubtypHome {

	private static final Log log = LogFactory
			.getLog(StgMbZielobjSubtypHome.class);

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

	public void persist(StgMbZielobjSubtyp transientInstance) {
		log.debug("persisting StgMbZielobjSubtyp instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMbZielobjSubtyp instance) {
		log.debug("attaching dirty StgMbZielobjSubtyp instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMbZielobjSubtyp instance) {
		log.debug("attaching clean StgMbZielobjSubtyp instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMbZielobjSubtyp persistentInstance) {
		log.debug("deleting StgMbZielobjSubtyp instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMbZielobjSubtyp merge(StgMbZielobjSubtyp detachedInstance) {
		log.debug("merging StgMbZielobjSubtyp instance");
		try {
			StgMbZielobjSubtyp result = (StgMbZielobjSubtyp) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMbZielobjSubtyp findById(sernet.gs.reveng.StgMbZielobjSubtypId id) {
		log.debug("getting StgMbZielobjSubtyp instance with id: " + id);
		try {
			StgMbZielobjSubtyp instance = (StgMbZielobjSubtyp) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMbZielobjSubtyp", id);
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

	public List findByExample(StgMbZielobjSubtyp instance) {
		log.debug("finding StgMbZielobjSubtyp instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMbZielobjSubtyp")
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
