package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbDringlichkeit entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbDringlichkeit
 * @author MyEclipse Persistence Tools
 */

public class MbDringlichkeitDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbDringlichkeitDAO.class);
	// property constants
	public static final String META_VERS = "metaVers";
	public static final String OBSOLET_VERS = "obsoletVers";
	public static final String LINK = "link";
	public static final String NOTIZ_ID = "notizId";
	public static final String GUID = "guid";
	public static final String ERFASST_DURCH = "erfasstDurch";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID_ORG = "guidOrg";

	public void save(MbDringlichkeit transientInstance) {
		log.debug("saving MbDringlichkeit instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbDringlichkeit persistentInstance) {
		log.debug("deleting MbDringlichkeit instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbDringlichkeit findById(sernet.gs.reveng.MbDringlichkeitId id) {
		log.debug("getting MbDringlichkeit instance with id: " + id);
		try {
			MbDringlichkeit instance = (MbDringlichkeit) getSession().get(
					"sernet.gs.reveng.MbDringlichkeit", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbDringlichkeit instance) {
		log.debug("finding MbDringlichkeit instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbDringlichkeit").add(
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
		log.debug("finding MbDringlichkeit instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from MbDringlichkeit as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByMetaVers(Object metaVers) {
		return findByProperty(META_VERS, metaVers);
	}

	public List findByObsoletVers(Object obsoletVers) {
		return findByProperty(OBSOLET_VERS, obsoletVers);
	}

	public List findByLink(Object link) {
		return findByProperty(LINK, link);
	}

	public List findByNotizId(Object notizId) {
		return findByProperty(NOTIZ_ID, notizId);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByErfasstDurch(Object erfasstDurch) {
		return findByProperty(ERFASST_DURCH, erfasstDurch);
	}

	public List findByGeloeschtDurch(Object geloeschtDurch) {
		return findByProperty(GELOESCHT_DURCH, geloeschtDurch);
	}

	public List findByImpNeu(Object impNeu) {
		return findByProperty(IMP_NEU, impNeu);
	}

	public List findByGuidOrg(Object guidOrg) {
		return findByProperty(GUID_ORG, guidOrg);
	}

	public List findAll() {
		log.debug("finding all MbDringlichkeit instances");
		try {
			String queryString = "from MbDringlichkeit";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbDringlichkeit merge(MbDringlichkeit detachedInstance) {
		log.debug("merging MbDringlichkeit instance");
		try {
			MbDringlichkeit result = (MbDringlichkeit) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbDringlichkeit instance) {
		log.debug("attaching dirty MbDringlichkeit instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbDringlichkeit instance) {
		log.debug("attaching clean MbDringlichkeit instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}