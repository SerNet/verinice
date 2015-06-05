package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * ModZobjBstMass entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.ModZobjBstMass
 * @author MyEclipse Persistence Tools
 */

public class ModZobjBstMassDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(ModZobjBstMassDAO.class);
	// property constants
	public static final String ID_1 = "id_1";
	public static final String ORG_IMP_ID = "orgImpId";
	public static final String UST_ID = "ustId";
	public static final String WAE_ID = "waeId";
	public static final String WAE_IMP_ID = "waeImpId";
	public static final String NOTIZ_ID = "notizId";
	public static final String DATEI_KOSTEN = "dateiKosten";
	public static final String DATEI_PROZESS = "dateiProzess";
	public static final String UMS_BESCHR = "umsBeschr";
	public static final String KOST_PERS_FIX = "kostPersFix";
	public static final String KOST_PERS_VAR = "kostPersVar";
	public static final String KOST_PERS_ZEI_ID = "kostPersZeiId";
	public static final String KOST_PERS_ZEI_IMP_ID = "kostPersZeiImpId";
	public static final String KOST_SACH_FIX = "kostSachFix";
	public static final String KOST_SACH_VAR = "kostSachVar";
	public static final String KOST_SACH_ZEI_ID = "kostSachZeiId";
	public static final String KOST_SACH_ZEI_IMP_ID = "kostSachZeiImpId";
	public static final String REV_BESCHR = "revBeschr";
	public static final String REV_ZOB_ID_MIT = "revZobIdMit";
	public static final String REV_ZOB_ID_MIT_NEXT = "revZobIdMitNext";
	public static final String IMP_NEU = "impNeu";
	public static final String USN = "usn";
	public static final String GUID = "guid";
	public static final String GUID_ORG = "guidOrg";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";
	public static final String ERFASST_DURCH = "erfasstDurch";
	public static final String CHANGED_BY = "changedBy";
	public static final String CM_USERNAME = "cmUsername";
	public static final String CM_IMP_ID = "cmImpId";
	public static final String CM_VER_ID1 = "cmVerId1";
	public static final String CM_VER_ID2 = "cmVerId2";
	public static final String CM_STA_ID = "cmStaId";
	public static final String ZYK_ID = "zykId";

	public void save(ModZobjBstMass transientInstance) {
		log.debug("saving ModZobjBstMass instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(ModZobjBstMass persistentInstance) {
		log.debug("deleting ModZobjBstMass instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public ModZobjBstMass findById(sernet.gs.reveng.ModZobjBstMassId id) {
		log.debug("getting ModZobjBstMass instance with id: " + id);
		try {
			ModZobjBstMass instance = (ModZobjBstMass) getSession().get(
					"sernet.gs.reveng.ModZobjBstMass", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(ModZobjBstMass instance) {
		log.debug("finding ModZobjBstMass instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.ModZobjBstMass").add(
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
		log.debug("finding ModZobjBstMass instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from ModZobjBstMass as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findById_1(Object id_1) {
		return findByProperty(ID_1, id_1);
	}

	public List findByOrgImpId(Object orgImpId) {
		return findByProperty(ORG_IMP_ID, orgImpId);
	}

	public List findByUstId(Object ustId) {
		return findByProperty(UST_ID, ustId);
	}

	public List findByWaeId(Object waeId) {
		return findByProperty(WAE_ID, waeId);
	}

	public List findByWaeImpId(Object waeImpId) {
		return findByProperty(WAE_IMP_ID, waeImpId);
	}

	public List findByNotizId(Object notizId) {
		return findByProperty(NOTIZ_ID, notizId);
	}

	public List findByDateiKosten(Object dateiKosten) {
		return findByProperty(DATEI_KOSTEN, dateiKosten);
	}

	public List findByDateiProzess(Object dateiProzess) {
		return findByProperty(DATEI_PROZESS, dateiProzess);
	}

	public List findByUmsBeschr(Object umsBeschr) {
		return findByProperty(UMS_BESCHR, umsBeschr);
	}

	public List findByKostPersFix(Object kostPersFix) {
		return findByProperty(KOST_PERS_FIX, kostPersFix);
	}

	public List findByKostPersVar(Object kostPersVar) {
		return findByProperty(KOST_PERS_VAR, kostPersVar);
	}

	public List findByKostPersZeiId(Object kostPersZeiId) {
		return findByProperty(KOST_PERS_ZEI_ID, kostPersZeiId);
	}

	public List findByKostPersZeiImpId(Object kostPersZeiImpId) {
		return findByProperty(KOST_PERS_ZEI_IMP_ID, kostPersZeiImpId);
	}

	public List findByKostSachFix(Object kostSachFix) {
		return findByProperty(KOST_SACH_FIX, kostSachFix);
	}

	public List findByKostSachVar(Object kostSachVar) {
		return findByProperty(KOST_SACH_VAR, kostSachVar);
	}

	public List findByKostSachZeiId(Object kostSachZeiId) {
		return findByProperty(KOST_SACH_ZEI_ID, kostSachZeiId);
	}

	public List findByKostSachZeiImpId(Object kostSachZeiImpId) {
		return findByProperty(KOST_SACH_ZEI_IMP_ID, kostSachZeiImpId);
	}

	public List findByRevBeschr(Object revBeschr) {
		return findByProperty(REV_BESCHR, revBeschr);
	}

	public List findByRevZobIdMit(Object revZobIdMit) {
		return findByProperty(REV_ZOB_ID_MIT, revZobIdMit);
	}

	public List findByRevZobIdMitNext(Object revZobIdMitNext) {
		return findByProperty(REV_ZOB_ID_MIT_NEXT, revZobIdMitNext);
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

	public List findByGeloeschtDurch(Object geloeschtDurch) {
		return findByProperty(GELOESCHT_DURCH, geloeschtDurch);
	}

	public List findByErfasstDurch(Object erfasstDurch) {
		return findByProperty(ERFASST_DURCH, erfasstDurch);
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

	public List findByZykId(Object zykId) {
		return findByProperty(ZYK_ID, zykId);
	}

	public List findAll() {
		log.debug("finding all ModZobjBstMass instances");
		try {
			String queryString = "from ModZobjBstMass";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public ModZobjBstMass merge(ModZobjBstMass detachedInstance) {
		log.debug("merging ModZobjBstMass instance");
		try {
			ModZobjBstMass result = (ModZobjBstMass) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(ModZobjBstMass instance) {
		log.debug("attaching dirty ModZobjBstMass instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(ModZobjBstMass instance) {
		log.debug("attaching clean ModZobjBstMass instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}