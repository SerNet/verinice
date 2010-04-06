package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * NmbNotiz entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.NmbNotiz
 * @author MyEclipse Persistence Tools
 */

public class NmbNotizDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(NmbNotizDAO.class);
	// property constants
	public static final String NOTIZ_TEXT = "notizText";
	public static final String URL = "url";
	public static final String GUID = "guid";
	public static final String GUID_ORG = "guidOrg";
	public static final String CREATED_BY = "createdBy";
	public static final String CHANGED_BY = "changedBy";

	public void save(NmbNotiz transientInstance) {
		log.debug("saving NmbNotiz instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(NmbNotiz persistentInstance) {
		log.debug("deleting NmbNotiz instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public NmbNotiz findById(sernet.gs.reveng.NmbNotizId id) {
		log.debug("getting NmbNotiz instance with id: " + id);
		try {
			NmbNotiz instance = (NmbNotiz) getSession().get(
					"sernet.gs.reveng.NmbNotiz", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(NmbNotiz instance) {
		log.debug("finding NmbNotiz instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.NmbNotiz").add(Example.create(instance))
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
		log.debug("finding NmbNotiz instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from NmbNotiz as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByNotizText(Object notizText) {
		return findByProperty(NOTIZ_TEXT, notizText);
	}

	public List findByUrl(Object url) {
		return findByProperty(URL, url);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByGuidOrg(Object guidOrg) {
		return findByProperty(GUID_ORG, guidOrg);
	}

	public List findByCreatedBy(Object createdBy) {
		return findByProperty(CREATED_BY, createdBy);
	}

	public List findByChangedBy(Object changedBy) {
		return findByProperty(CHANGED_BY, changedBy);
	}

	public List findAll() {
		log.debug("finding all NmbNotiz instances");
		try {
			String queryString = "from NmbNotiz";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public NmbNotiz merge(NmbNotiz detachedInstance) {
		log.debug("merging NmbNotiz instance");
		try {
			NmbNotiz result = (NmbNotiz) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(NmbNotiz instance) {
		log.debug("attaching dirty NmbNotiz instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(NmbNotiz instance) {
		log.debug("attaching clean NmbNotiz instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}