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
 * Home object for domain model class StgFilterSuchenSeite1.
 * @see sernet.gs.reveng.StgFilterSuchenSeite1
 * @author Hibernate Tools
 */
public class StgFilterSuchenSeite1Home {

	private static final Log log = LogFactory
			.getLog(StgFilterSuchenSeite1Home.class);

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

	public void persist(StgFilterSuchenSeite1 transientInstance) {
		log.debug("persisting StgFilterSuchenSeite1 instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgFilterSuchenSeite1 instance) {
		log.debug("attaching dirty StgFilterSuchenSeite1 instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgFilterSuchenSeite1 instance) {
		log.debug("attaching clean StgFilterSuchenSeite1 instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgFilterSuchenSeite1 persistentInstance) {
		log.debug("deleting StgFilterSuchenSeite1 instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgFilterSuchenSeite1 merge(StgFilterSuchenSeite1 detachedInstance) {
		log.debug("merging StgFilterSuchenSeite1 instance");
		try {
			StgFilterSuchenSeite1 result = (StgFilterSuchenSeite1) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgFilterSuchenSeite1 findById(
			sernet.gs.reveng.StgFilterSuchenSeite1Id id) {
		log.debug("getting StgFilterSuchenSeite1 instance with id: " + id);
		try {
			StgFilterSuchenSeite1 instance = (StgFilterSuchenSeite1) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgFilterSuchenSeite1", id);
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

	public List findByExample(StgFilterSuchenSeite1 instance) {
		log.debug("finding StgFilterSuchenSeite1 instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgFilterSuchenSeite1")
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
