package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbMassn entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbMassn
 * @author MyEclipse Persistence Tools
 */

public class MbMassnDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbMassnDAO.class);
	// property constants
	public static final String MSK_ID = "mskId";
	public static final String MSK_IMP_ID = "mskImpId";
	public static final String NR = "nr";
	public static final String NR_ALT = "nrAlt";
	public static final String NOTIZ_ID = "notizId";
	public static final String LINK = "link";
	public static final String META_NEU = "metaNeu";
	public static final String META_VERS = "metaVers";
	public static final String OBSOLET_VERS = "obsoletVers";
	public static final String USERDEF = "userdef";
	public static final String GUID = "guid";
	public static final String USN = "usn";
	public static final String ERFASST_DURCH = "erfasstDurch";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID_ORG = "guidOrg";
	public static final String CHANGED_BY = "changedBy";
	public static final String CM_USERNAME = "cmUsername";
	public static final String CM_IMP_ID = "cmImpId";
	public static final String CM_VER_ID1 = "cmVerId1";
	public static final String CM_VER_ID2 = "cmVerId2";
	public static final String CM_STA_ID = "cmStaId";

	public void save(MbMassn transientInstance) {
		log.debug("saving MbMassn instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbMassn persistentInstance) {
		log.debug("deleting MbMassn instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbMassn findById(sernet.gs.reveng.MbMassnId id) {
		log.debug("getting MbMassn instance with id: " + id);
		try {
			MbMassn instance = (MbMassn) getSession().get(
					"sernet.gs.reveng.MbMassn", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbMassn instance) {
		log.debug("finding MbMassn instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbMassn").add(Example.create(instance))
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
		log.debug("finding MbMassn instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MbMassn as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByMskId(Object mskId) {
		return findByProperty(MSK_ID, mskId);
	}

	public List findByMskImpId(Object mskImpId) {
		return findByProperty(MSK_IMP_ID, mskImpId);
	}

	public List findByNr(Object nr) {
		return findByProperty(NR, nr);
	}

	public List findByNrAlt(Object nrAlt) {
		return findByProperty(NR_ALT, nrAlt);
	}

	public List findByNotizId(Object notizId) {
		return findByProperty(NOTIZ_ID, notizId);
	}

	public List findByLink(Object link) {
		return findByProperty(LINK, link);
	}

	public List findByMetaNeu(Object metaNeu) {
		return findByProperty(META_NEU, metaNeu);
	}

	public List findByMetaVers(Object metaVers) {
		return findByProperty(META_VERS, metaVers);
	}

	public List findByObsoletVers(Object obsoletVers) {
		return findByProperty(OBSOLET_VERS, obsoletVers);
	}

	public List findByUserdef(Object userdef) {
		return findByProperty(USERDEF, userdef);
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
		log.debug("finding all MbMassn instances");
		try {
			String queryString = "from MbMassn";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbMassn merge(MbMassn detachedInstance) {
		log.debug("merging MbMassn instance");
		try {
			MbMassn result = (MbMassn) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbMassn instance) {
		log.debug("attaching dirty MbMassn instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbMassn instance) {
		log.debug("attaching clean MbMassn instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}