package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MMetatyp entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MMetatyp
 * @author MyEclipse Persistence Tools
 */

public class MMetatypDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MMetatypDAO.class);
	// property constants
	public static final String GUID = "guid";

	public void save(MMetatyp transientInstance) {
		log.debug("saving MMetatyp instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MMetatyp persistentInstance) {
		log.debug("deleting MMetatyp instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MMetatyp findById(java.lang.Short id) {
		log.debug("getting MMetatyp instance with id: " + id);
		try {
			MMetatyp instance = (MMetatyp) getSession().get(
					"sernet.gs.reveng.MMetatyp", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MMetatyp instance) {
		log.debug("finding MMetatyp instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MMetatyp").add(Example.create(instance))
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
		log.debug("finding MMetatyp instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MMetatyp as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findAll() {
		log.debug("finding all MMetatyp instances");
		try {
			String queryString = "from MMetatyp";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MMetatyp merge(MMetatyp detachedInstance) {
		log.debug("merging MMetatyp instance");
		try {
			MMetatyp result = (MMetatyp) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MMetatyp instance) {
		log.debug("attaching dirty MMetatyp instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MMetatyp instance) {
		log.debug("attaching clean MMetatyp instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}