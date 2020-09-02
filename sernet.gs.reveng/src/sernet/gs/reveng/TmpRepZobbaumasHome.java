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
 * Home object for domain model class TmpRepZobbaumas.
 * @see sernet.gs.reveng.TmpRepZobbaumas
 * @author Hibernate Tools
 */
public class TmpRepZobbaumasHome {

	private static final Log log = LogFactory.getLog(TmpRepZobbaumasHome.class);

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

	public void persist(TmpRepZobbaumas transientInstance) {
		log.debug("persisting TmpRepZobbaumas instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(TmpRepZobbaumas instance) {
		log.debug("attaching dirty TmpRepZobbaumas instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(TmpRepZobbaumas instance) {
		log.debug("attaching clean TmpRepZobbaumas instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(TmpRepZobbaumas persistentInstance) {
		log.debug("deleting TmpRepZobbaumas instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public TmpRepZobbaumas merge(TmpRepZobbaumas detachedInstance) {
		log.debug("merging TmpRepZobbaumas instance");
		try {
			TmpRepZobbaumas result = (TmpRepZobbaumas) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public TmpRepZobbaumas findById(sernet.gs.reveng.TmpRepZobbaumasId id) {
		log.debug("getting TmpRepZobbaumas instance with id: " + id);
		try {
			TmpRepZobbaumas instance = (TmpRepZobbaumas) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.TmpRepZobbaumas", id);
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

	public List findByExample(TmpRepZobbaumas instance) {
		log.debug("finding TmpRepZobbaumas instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.TmpRepZobbaumas")
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
