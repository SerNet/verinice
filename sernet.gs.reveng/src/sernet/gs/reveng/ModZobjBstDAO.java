package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * ModZobjBst entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.ModZobjBst
 * @author MyEclipse Persistence Tools
 */

public class ModZobjBstDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(ModZobjBstDAO.class);
	// property constants
	public static final String ORG_IMP_ID = "orgImpId";
	public static final String BEGRUENDUNG = "begruendung";
	public static final String IMP_NEU = "impNeu";
	public static final String USN = "usn";
	public static final String GUID = "guid";
	public static final String GUID_ORG = "guidOrg";
	public static final String SET_DEFAULT = "setDefault";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";
	public static final String MMT_ID = "mmtId";
	public static final String ERFASST_DURCH = "erfasstDurch";
	public static final String BEARBEITET_ORG = "bearbeitetOrg";
	public static final String CHANGED_BY = "changedBy";
	public static final String CM_USERNAME = "cmUsername";
	public static final String CM_IMP_ID = "cmImpId";
	public static final String CM_VER_ID1 = "cmVerId1";
	public static final String CM_VER_ID2 = "cmVerId2";
	public static final String CM_STA_ID = "cmStaId";

	public void save(ModZobjBst transientInstance) {
		log.debug("saving ModZobjBst instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(ModZobjBst persistentInstance) {
		log.debug("deleting ModZobjBst instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public ModZobjBst findById(sernet.gs.reveng.ModZobjBstId id) {
		log.debug("getting ModZobjBst instance with id: " + id);
		try {
			ModZobjBst instance = (ModZobjBst) getSession().get(
					"sernet.gs.reveng.ModZobjBst", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(ModZobjBst instance) {
		log.debug("finding ModZobjBst instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.ModZobjBst")
					.add(Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	public List findByProperty(String propertyName, Object value) {
		log.debug("finding ModZobjBst instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from ModZobjBst as model where model."
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

	public List findByBegruendung(Object begruendung) {
		return findByProperty(BEGRUENDUNG, begruendung);
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

	public List findBySetDefault(Object setDefault) {
		return findByProperty(SET_DEFAULT, setDefault);
	}

	public List findByGeloeschtDurch(Object geloeschtDurch) {
		return findByProperty(GELOESCHT_DURCH, geloeschtDurch);
	}

	public List findByMmtId(Object mmtId) {
		return findByProperty(MMT_ID, mmtId);
	}

	public List findByErfasstDurch(Object erfasstDurch) {
		return findByProperty(ERFASST_DURCH, erfasstDurch);
	}

	public List findByBearbeitetOrg(Object bearbeitetOrg) {
		return findByProperty(BEARBEITET_ORG, bearbeitetOrg);
	}

	public List findByChangedBy(Object changedBy) {
		return findByProperty(CHANGED_BY, changedBy);
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

	public List findByCmStaId(Object cmStaId) {
		return findByProperty(CM_STA_ID, cmStaId);
	}

	public List findAll() {
		log.debug("finding all ModZobjBst instances");
		try {
			String queryString = "from ModZobjBst";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public ModZobjBst merge(ModZobjBst detachedInstance) {
		log.debug("merging ModZobjBst instance");
		try {
			ModZobjBst result = (ModZobjBst) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(ModZobjBst instance) {
		log.debug("attaching dirty ModZobjBst instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(ModZobjBst instance) {
		log.debug("attaching clean ModZobjBst instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}