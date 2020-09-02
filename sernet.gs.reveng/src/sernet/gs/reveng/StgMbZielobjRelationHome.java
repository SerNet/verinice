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
 * Home object for domain model class StgMbZielobjRelation.
 * @see sernet.gs.reveng.StgMbZielobjRelation
 * @author Hibernate Tools
 */
public class StgMbZielobjRelationHome {

	private static final Log log = LogFactory
			.getLog(StgMbZielobjRelationHome.class);

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

	public void persist(StgMbZielobjRelation transientInstance) {
		log.debug("persisting StgMbZielobjRelation instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(StgMbZielobjRelation instance) {
		log.debug("attaching dirty StgMbZielobjRelation instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(StgMbZielobjRelation instance) {
		log.debug("attaching clean StgMbZielobjRelation instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(StgMbZielobjRelation persistentInstance) {
		log.debug("deleting StgMbZielobjRelation instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public StgMbZielobjRelation merge(StgMbZielobjRelation detachedInstance) {
		log.debug("merging StgMbZielobjRelation instance");
		try {
			StgMbZielobjRelation result = (StgMbZielobjRelation) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public StgMbZielobjRelation findById(
			sernet.gs.reveng.StgMbZielobjRelationId id) {
		log.debug("getting StgMbZielobjRelation instance with id: " + id);
		try {
			StgMbZielobjRelation instance = (StgMbZielobjRelation) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.StgMbZielobjRelation", id);
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

	public List findByExample(StgMbZielobjRelation instance) {
		log.debug("finding StgMbZielobjRelation instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.StgMbZielobjRelation")
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
