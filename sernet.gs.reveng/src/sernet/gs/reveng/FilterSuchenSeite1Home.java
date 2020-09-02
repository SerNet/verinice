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
 * Home object for domain model class FilterSuchenSeite1.
 * @see sernet.gs.reveng.FilterSuchenSeite1
 * @author Hibernate Tools
 */
public class FilterSuchenSeite1Home {

	private static final Log log = LogFactory
			.getLog(FilterSuchenSeite1Home.class);

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

	public void persist(FilterSuchenSeite1 transientInstance) {
		log.debug("persisting FilterSuchenSeite1 instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(FilterSuchenSeite1 instance) {
		log.debug("attaching dirty FilterSuchenSeite1 instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(FilterSuchenSeite1 instance) {
		log.debug("attaching clean FilterSuchenSeite1 instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(FilterSuchenSeite1 persistentInstance) {
		log.debug("deleting FilterSuchenSeite1 instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public FilterSuchenSeite1 merge(FilterSuchenSeite1 detachedInstance) {
		log.debug("merging FilterSuchenSeite1 instance");
		try {
			FilterSuchenSeite1 result = (FilterSuchenSeite1) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public FilterSuchenSeite1 findById(sernet.gs.reveng.FilterSuchenSeite1Id id) {
		log.debug("getting FilterSuchenSeite1 instance with id: " + id);
		try {
			FilterSuchenSeite1 instance = (FilterSuchenSeite1) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.FilterSuchenSeite1", id);
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

	public List findByExample(FilterSuchenSeite1 instance) {
		log.debug("finding FilterSuchenSeite1 instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.FilterSuchenSeite1")
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
