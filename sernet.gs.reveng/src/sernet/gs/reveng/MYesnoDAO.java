package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MYesno entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MYesno
 * @author MyEclipse Persistence Tools
 */

public class MYesnoDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MYesnoDAO.class);
	// property constants
	public static final String GUID = "guid";

	public void save(MYesno transientInstance) {
		log.debug("saving MYesno instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MYesno persistentInstance) {
		log.debug("deleting MYesno instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MYesno findById(java.lang.Short id) {
		log.debug("getting MYesno instance with id: " + id);
		try {
			MYesno instance = (MYesno) getSession().get(
					"sernet.gs.reveng.MYesno", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MYesno instance) {
		log.debug("finding MYesno instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MYesno").add(Example.create(instance))
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
		log.debug("finding MYesno instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MYesno as model where model."
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
		log.debug("finding all MYesno instances");
		try {
			String queryString = "from MYesno";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MYesno merge(MYesno detachedInstance) {
		log.debug("merging MYesno instance");
		try {
			MYesno result = (MYesno) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MYesno instance) {
		log.debug("attaching dirty MYesno instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MYesno instance) {
		log.debug("attaching clean MYesno instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}