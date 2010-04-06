package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MGsiegel entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MGsiegel
 * @author MyEclipse Persistence Tools
 */

public class MGsiegelDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MGsiegelDAO.class);
	// property constants
	public static final String SIEGELSTUFE = "siegelstufe";
	public static final String GUID = "guid";

	public void save(MGsiegel transientInstance) {
		log.debug("saving MGsiegel instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MGsiegel persistentInstance) {
		log.debug("deleting MGsiegel instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MGsiegel findById(java.lang.Short id) {
		log.debug("getting MGsiegel instance with id: " + id);
		try {
			MGsiegel instance = (MGsiegel) getSession().get(
					"sernet.gs.reveng.MGsiegel", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MGsiegel instance) {
		log.debug("finding MGsiegel instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MGsiegel").add(Example.create(instance))
					.list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	public List findByProperty(String propertyName, Object value) {
		log.debug("finding MGsiegel instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MGsiegel as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findBySiegelstufe(Object siegelstufe) {
		return findByProperty(SIEGELSTUFE, siegelstufe);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findAll() {
		log.debug("finding all MGsiegel instances");
		try {
			String queryString = "from MGsiegel";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MGsiegel merge(MGsiegel detachedInstance) {
		log.debug("merging MGsiegel instance");
		try {
			MGsiegel result = (MGsiegel) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MGsiegel instance) {
		log.debug("attaching dirty MGsiegel instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MGsiegel instance) {
		log.debug("attaching clean MGsiegel instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}