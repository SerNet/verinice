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
 * Home object for domain model class StgFilterSuchenFelderTxt.
 * @see sernet.gs.reveng.StgFilterSuchenFelderTxt
 * @author Hibernate Tools
 */
public class StgFilterSuchenFelderTxtHome {

	private static final Log log = LogFactory
			.getLog(StgFilterSuchenFelderTxtHome.class);

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

	public void persist(StgFilterSuchenFelderTxt transientInstance) {
		log.debug("persisting StgFilterSuchenFelderTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgFilterSuchenFelderTxt instance) {
		log.debug("attaching dirty StgFilterSuchenFelderTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgFilterSuchenFelderTxt instance) {
		log.debug("attaching clean StgFilterSuchenFelderTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgFilterSuchenFelderTxt persistentInstance) {
		log.debug("deleting StgFilterSuchenFelderTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgFilterSuchenFelderTxt merge(
			StgFilterSuchenFelderTxt detachedInstance) {
		log.debug("merging StgFilterSuchenFelderTxt instance");
		try {
			StgFilterSuchenFelderTxt result = (StgFilterSuchenFelderTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgFilterSuchenFelderTxt findById(
			sernet.gs.reveng.StgFilterSuchenFelderTxtId id) {
		log.debug("getting StgFilterSuchenFelderTxt instance with id: " + id);
		try {
			StgFilterSuchenFelderTxt instance = (StgFilterSuchenFelderTxt) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgFilterSuchenFelderTxt", id);
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

	public List findByExample(StgFilterSuchenFelderTxt instance) {
		log.debug("finding StgFilterSuchenFelderTxt instance by example");
		try {
			List results = sessionFactory
					.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgFilterSuchenFelderTxt")
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
