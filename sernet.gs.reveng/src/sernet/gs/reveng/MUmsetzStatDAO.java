package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MUmsetzStat entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MUmsetzStat
 * @author MyEclipse Persistence Tools
 */

public class MUmsetzStatDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MUmsetzStatDAO.class);
	// property constants
	public static final String GUID = "guid";

	public void save(MUmsetzStat transientInstance) {
		log.debug("saving MUmsetzStat instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MUmsetzStat persistentInstance) {
		log.debug("deleting MUmsetzStat instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MUmsetzStat findById(java.lang.Short id) {
		log.debug("getting MUmsetzStat instance with id: " + id);
		try {
			MUmsetzStat instance = (MUmsetzStat) getSession().get(
					"sernet.gs.reveng.MUmsetzStat", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MUmsetzStat instance) {
		log.debug("finding MUmsetzStat instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MUmsetzStat").add(
					Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	public List findByProperty(String propertyName, Object value) {
		log.debug("finding MUmsetzStat instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MUmsetzStat as model where model."
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
		log.debug("finding all MUmsetzStat instances");
		try {
			String queryString = "from MUmsetzStat";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MUmsetzStat merge(MUmsetzStat detachedInstance) {
		log.debug("merging MUmsetzStat instance");
		try {
			MUmsetzStat result = (MUmsetzStat) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MUmsetzStat instance) {
		log.debug("attaching dirty MUmsetzStat instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MUmsetzStat instance) {
		log.debug("attaching clean MUmsetzStat instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}