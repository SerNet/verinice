package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * NZielobjZielobj entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.NZielobjZielobj
 * @author MyEclipse Persistence Tools
 */

public class NZielobjZielobjDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(NZielobjZielobjDAO.class);
	// property constants
	public static final String ORG_IMP_ID = "orgImpId";
	public static final String ZOT_ID1 = "zotId1";
	public static final String ZOT_ID2 = "zotId2";
	public static final String IMP_NEU = "impNeu";
	public static final String USN = "usn";
	public static final String GUID = "guid";
	public static final String GUID_ORG = "guidOrg";
	public static final String ERFASST_DURCH = "erfasstDurch";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";

	public void save(NZielobjZielobj transientInstance) {
		log.debug("saving NZielobjZielobj instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(NZielobjZielobj persistentInstance) {
		log.debug("deleting NZielobjZielobj instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public NZielobjZielobj findById(sernet.gs.reveng.NZielobjZielobjId id) {
		log.debug("getting NZielobjZielobj instance with id: " + id);
		try {
			NZielobjZielobj instance = (NZielobjZielobj) getSession().get(
					"sernet.gs.reveng.NZielobjZielobj", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(NZielobjZielobj instance) {
		log.debug("finding NZielobjZielobj instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.NZielobjZielobj").add(
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
		log.debug("finding NZielobjZielobj instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from NZielobjZielobj as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByOrgImpId(Object orgImpId) {
		return findByProperty(ORG_IMP_ID, orgImpId);
	}

	public List findByZotId1(Object zotId1) {
		return findByProperty(ZOT_ID1, zotId1);
	}

	public List findByZotId2(Object zotId2) {
		return findByProperty(ZOT_ID2, zotId2);
	}

	public List findByImpNeu(Object impNeu) {
		return findByProperty(IMP_NEU, impNeu);
	}

	public List findByUsn(Object usn) {
		return findByProperty(USN, usn);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByGuidOrg(Object guidOrg) {
		return findByProperty(GUID_ORG, guidOrg);
	}

	public List findByErfasstDurch(Object erfasstDurch) {
		return findByProperty(ERFASST_DURCH, erfasstDurch);
	}

	public List findByGeloeschtDurch(Object geloeschtDurch) {
		return findByProperty(GELOESCHT_DURCH, geloeschtDurch);
	}

	public List findAll() {
		log.debug("finding all NZielobjZielobj instances");
		try {
			String queryString = "from NZielobjZielobj";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public NZielobjZielobj merge(NZielobjZielobj detachedInstance) {
		log.debug("merging NZielobjZielobj instance");
		try {
			NZielobjZielobj result = (NZielobjZielobj) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(NZielobjZielobj instance) {
		log.debug("attaching dirty NZielobjZielobj instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(NZielobjZielobj instance) {
		log.debug("attaching clean NZielobjZielobj instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}