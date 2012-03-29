package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbZielobjTyp entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbZielobjTyp
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjTypDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbZielobjTypDAO.class);
	// property constants
	public static final String LINK = "link";
	public static final String META_VERS = "metaVers";
	public static final String OBSOLET_VERS = "obsoletVers";
	public static final String GUID = "guid";
	public static final String USN = "usn";
	public static final String ERFASST_DURCH = "erfasstDurch";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID_ORG = "guidOrg";
	public static final String CHANGED_BY = "changedBy";
	public static final String REIHENFOLGE = "reihenfolge";
	public static final String CM_USERNAME = "cmUsername";
	public static final String CM_IMP_ID = "cmImpId";
	public static final String CM_VER_ID1 = "cmVerId1";
	public static final String CM_VER_ID2 = "cmVerId2";

	public void save(MbZielobjTyp transientInstance) {
		log.debug("saving MbZielobjTyp instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbZielobjTyp persistentInstance) {
		log.debug("deleting MbZielobjTyp instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbZielobjTyp findById(sernet.gs.reveng.MbZielobjTypId id) {
		log.debug("getting MbZielobjTyp instance with id: " + id);
		try {
			MbZielobjTyp instance = (MbZielobjTyp) getSession().get(
					"sernet.gs.reveng.MbZielobjTyp", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbZielobjTyp instance) {
		log.debug("finding MbZielobjTyp instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbZielobjTyp").add(
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
		log.debug("finding MbZielobjTyp instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from MbZielobjTyp as model where model."
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

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByUsn(Object usn) {
		return findByProperty(USN, usn);
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

	public List findByChangedBy(Object changedBy) {
		return findByProperty(CHANGED_BY, changedBy);
	}

	public List findByReihenfolge(Object reihenfolge) {
		return findByProperty(REIHENFOLGE, reihenfolge);
	}

	public List findByCmUsername(Object cmUsername) {
		return findByProperty(CM_USERNAME, cmUsername);
	}

	public List findByCmImpId(Object cmImpId) {
		return findByProperty(CM_IMP_ID, cmImpId);
	}

	public List findByCmVerId1(Object cmVerId1) {
		return findByProperty(CM_VER_ID1, cmVerId1);
	}

	public List findByCmVerId2(Object cmVerId2) {
		return findByProperty(CM_VER_ID2, cmVerId2);
	}

	public List findAll() {
		log.debug("finding all MbZielobjTyp instances");
		try {
			String queryString = "from MbZielobjTyp";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbZielobjTyp merge(MbZielobjTyp detachedInstance) {
		log.debug("merging MbZielobjTyp instance");
		try {
			MbZielobjTyp result = (MbZielobjTyp) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbZielobjTyp instance) {
		log.debug("attaching dirty MbZielobjTyp instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbZielobjTyp instance) {
		log.debug("attaching clean MbZielobjTyp instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}