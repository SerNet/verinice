package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * SysImport entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.SysImport
 * @author MyEclipse Persistence Tools
 */

public class SysImportDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(SysImportDAO.class);
	// property constants
	public static final String TRG_IMP_ID = "trgImpId";
	public static final String SRC_IMP_ID = "srcImpId";
	public static final String IMP_TYP = "impTyp";
	public static final String IMP_STATUS = "impStatus";
	public static final String NAME = "name";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String IMP_EXP_TYP = "impExpTyp";
	public static final String IMP_EXP_NAME = "impExpName";
	public static final String IMP_EXP_BESCHREIBUNG = "impExpBeschreibung";
	public static final String IMP_EXP_SIGNATUR_QUELLE = "impExpSignaturQuelle";
	public static final String IMP_EXP_SIGNATUR_ZIEL = "impExpSignaturZiel";
	public static final String IMP_USN_BASELINE = "impUsnBaseline";
	public static final String IMP_USN_CURRENT = "impUsnCurrent";
	public static final String IMP_LOESCHEN = "impLoeschen";
	public static final String IMP_KONKRET = "impKonkret";
	public static final String IMP_MODELL = "impModell";
	public static final String IMP_MAPUSERDEF = "impMapuserdef";
	public static final String GUID = "guid";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";
	public static final String CM_IMP_ID = "cmImpId";
	public static final String CM_VER_ID = "cmVerId";
	public static final String EDIT_IMP_ID = "editImpId";
	public static final String CM_LEVEL = "cmLevel";
	public static final String IMP_NUR_META = "impNurMeta";
	public static final String META_VERS = "metaVers";
	public static final String IMP_MAP_GUID = "impMapGuid";
	public static final String IMP_MAP_NUR = "impMapNur";

	public void save(SysImport transientInstance) {
		log.debug("saving SysImport instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(SysImport persistentInstance) {
		log.debug("deleting SysImport instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public SysImport findById(java.lang.Integer id) {
		log.debug("getting SysImport instance with id: " + id);
		try {
			SysImport instance = (SysImport) getSession().get(
					"sernet.gs.reveng.SysImport", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(SysImport instance) {
		log.debug("finding SysImport instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.SysImport").add(Example.create(instance))
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
		log.debug("finding SysImport instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from SysImport as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByTrgImpId(Object trgImpId) {
		return findByProperty(TRG_IMP_ID, trgImpId);
	}

	public List findBySrcImpId(Object srcImpId) {
		return findByProperty(SRC_IMP_ID, srcImpId);
	}

	public List findByImpTyp(Object impTyp) {
		return findByProperty(IMP_TYP, impTyp);
	}

	public List findByImpStatus(Object impStatus) {
		return findByProperty(IMP_STATUS, impStatus);
	}

	public List findByName(Object name) {
		return findByProperty(NAME, name);
	}

	public List findByBeschreibung(Object beschreibung) {
		return findByProperty(BESCHREIBUNG, beschreibung);
	}

	public List findByImpExpTyp(Object impExpTyp) {
		return findByProperty(IMP_EXP_TYP, impExpTyp);
	}

	public List findByImpExpName(Object impExpName) {
		return findByProperty(IMP_EXP_NAME, impExpName);
	}

	public List findByImpExpBeschreibung(Object impExpBeschreibung) {
		return findByProperty(IMP_EXP_BESCHREIBUNG, impExpBeschreibung);
	}

	public List findByImpExpSignaturQuelle(Object impExpSignaturQuelle) {
		return findByProperty(IMP_EXP_SIGNATUR_QUELLE, impExpSignaturQuelle);
	}

	public List findByImpExpSignaturZiel(Object impExpSignaturZiel) {
		return findByProperty(IMP_EXP_SIGNATUR_ZIEL, impExpSignaturZiel);
	}

	public List findByImpUsnBaseline(Object impUsnBaseline) {
		return findByProperty(IMP_USN_BASELINE, impUsnBaseline);
	}

	public List findByImpUsnCurrent(Object impUsnCurrent) {
		return findByProperty(IMP_USN_CURRENT, impUsnCurrent);
	}

	public List findByImpLoeschen(Object impLoeschen) {
		return findByProperty(IMP_LOESCHEN, impLoeschen);
	}

	public List findByImpKonkret(Object impKonkret) {
		return findByProperty(IMP_KONKRET, impKonkret);
	}

	public List findByImpModell(Object impModell) {
		return findByProperty(IMP_MODELL, impModell);
	}

	public List findByImpMapuserdef(Object impMapuserdef) {
		return findByProperty(IMP_MAPUSERDEF, impMapuserdef);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByGeloeschtDurch(Object geloeschtDurch) {
		return findByProperty(GELOESCHT_DURCH, geloeschtDurch);
	}

	public List findByCmImpId(Object cmImpId) {
		return findByProperty(CM_IMP_ID, cmImpId);
	}

	public List findByCmVerId(Object cmVerId) {
		return findByProperty(CM_VER_ID, cmVerId);
	}

	public List findByEditImpId(Object editImpId) {
		return findByProperty(EDIT_IMP_ID, editImpId);
	}

	public List findByCmLevel(Object cmLevel) {
		return findByProperty(CM_LEVEL, cmLevel);
	}

	public List findByImpNurMeta(Object impNurMeta) {
		return findByProperty(IMP_NUR_META, impNurMeta);
	}

	public List findByMetaVers(Object metaVers) {
		return findByProperty(META_VERS, metaVers);
	}

	public List findByImpMapGuid(Object impMapGuid) {
		return findByProperty(IMP_MAP_GUID, impMapGuid);
	}

	public List findByImpMapNur(Object impMapNur) {
		return findByProperty(IMP_MAP_NUR, impMapNur);
	}

	public List findAll() {
		log.debug("finding all SysImport instances");
		try {
			String queryString = "from SysImport";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public SysImport merge(SysImport detachedInstance) {
		log.debug("merging SysImport instance");
		try {
			SysImport result = (SysImport) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(SysImport instance) {
		log.debug("attaching dirty SysImport instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(SysImport instance) {
		log.debug("attaching clean SysImport instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}