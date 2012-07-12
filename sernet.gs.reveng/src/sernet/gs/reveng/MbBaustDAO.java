package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbBaust entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbBaust
 * @author MyEclipse Persistence Tools
 */

public class MbBaustDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbBaustDAO.class);
	// property constants
	public static final String NR_NUM = "nrNum";
	public static final String NR = "nr";
	public static final String LINK = "link";
	public static final String AUDITRELEVANT_JN = "auditrelevantJn";
	public static final String META_VERS = "metaVers";
	public static final String OBSOLET_VERS = "obsoletVers";
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

	public void save(MbBaust transientInstance) {
		log.debug("saving MbBaust instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbBaust persistentInstance) {
		log.debug("deleting MbBaust instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbBaust findById(sernet.gs.reveng.MbBaustId id) {
		log.debug("getting MbBaust instance with id: " + id);
		try {
			MbBaust instance = (MbBaust) getSession().get(
					"sernet.gs.reveng.MbBaust", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbBaust instance) {
		log.debug("finding MbBaust instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbBaust").add(Example.create(instance))
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
		log.debug("finding MbBaust instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MbBaust as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByNrNum(Object nrNum) {
		return findByProperty(NR_NUM, nrNum);
	}

	public List findByNr(Object nr) {
		return findByProperty(NR, nr);
	}

	public List findByLink(Object link) {
		return findByProperty(LINK, link);
	}

	public List findByAuditrelevantJn(Object auditrelevantJn) {
		return findByProperty(AUDITRELEVANT_JN, auditrelevantJn);
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
		log.debug("finding all MbBaust instances");
		try {
			String queryString = "from MbBaust";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbBaust merge(MbBaust detachedInstance) {
		log.debug("merging MbBaust instance");
		try {
			MbBaust result = (MbBaust) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbBaust instance) {
		log.debug("attaching dirty MbBaust instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbBaust instance) {
		log.debug("attaching clean MbBaust instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}