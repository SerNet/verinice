package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MBstnStatus entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MBstnStatus
 * @author MyEclipse Persistence Tools
 */

public class MBstnStatusDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MBstnStatusDAO.class);
	// property constants
	public static final String LINK = "link";
	public static final String META_VERS = "metaVers";
	public static final String OBSOLET_VERS = "obsoletVers";
	public static final String META_NEU = "metaNeu";
	public static final String GUID = "guid";
	public static final String IMP_NEU = "impNeu";

	public void save(MBstnStatus transientInstance) {
		log.debug("saving MBstnStatus instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MBstnStatus persistentInstance) {
		log.debug("deleting MBstnStatus instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MBstnStatus findById(java.lang.Short id) {
		log.debug("getting MBstnStatus instance with id: " + id);
		try {
			MBstnStatus instance = (MBstnStatus) getSession().get(
					"sernet.gs.reveng.MBstnStatus", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MBstnStatus instance) {
		log.debug("finding MBstnStatus instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MBstnStatus").add(
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
		log.debug("finding MBstnStatus instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MBstnStatus as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByLink(Object link) {
		return findByProperty(LINK, link);
	}

	public List findByMetaVers(Object metaVers) {
		return findByProperty(META_VERS, metaVers);
	}

	public List findByObsoletVers(Object obsoletVers) {
		return findByProperty(OBSOLET_VERS, obsoletVers);
	}

	public List findByMetaNeu(Object metaNeu) {
		return findByProperty(META_NEU, metaNeu);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByImpNeu(Object impNeu) {
		return findByProperty(IMP_NEU, impNeu);
	}

	public List findAll() {
		log.debug("finding all MBstnStatus instances");
		try {
			String queryString = "from MBstnStatus";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MBstnStatus merge(MBstnStatus detachedInstance) {
		log.debug("merging MBstnStatus instance");
		try {
			MBstnStatus result = (MBstnStatus) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MBstnStatus instance) {
		log.debug("attaching dirty MBstnStatus instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MBstnStatus instance) {
		log.debug("attaching clean MBstnStatus instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}