package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * NZobSb entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.NZobSb
 * @author MyEclipse Persistence Tools
 */

public class NZobSbDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(NZobSbDAO.class);
	// property constants
	public static final String ZSB_UEBERTRAGUNG = "zsbUebertragung";
	public static final String ZSB_ANGEBUNDEN = "zsbAngebunden";
	public static final String ZSB_VERTRAULICH = "zsbVertraulich";
	public static final String ZSB_INTEGRITAET = "zsbIntegritaet";
	public static final String ZSB_VERFUEGBAR = "zsbVerfuegbar";
	public static final String ZSB_VERTR_SBK_ID_ERM = "zsbVertrSbkIdErm";
	public static final String ZSB_VERTR_SBK_ID = "zsbVertrSbkId";
	public static final String ZSB_VERTR_BEGR = "zsbVertrBegr";
	public static final String ZSB_VERFU_SBK_ID_ERM = "zsbVerfuSbkIdErm";
	public static final String ZSB_VERFU_SBK_ID = "zsbVerfuSbkId";
	public static final String ZSB_VERFU_BEGR = "zsbVerfuBegr";
	public static final String ZSB_INTEG_SBK_ID_ERM = "zsbIntegSbkIdErm";
	public static final String ZSB_INTEG_SBK_ID = "zsbIntegSbkId";
	public static final String ZSB_INTEG_BEGR = "zsbIntegBegr";
	public static final String ZSB_AUTEN_SBK_ID_ERM = "zsbAutenSbkIdErm";
	public static final String ZSB_AUTEN_SBK_ID = "zsbAutenSbkId";
	public static final String ZSB_AUTEN_BEGR = "zsbAutenBegr";
	public static final String ZSB_REVIS_SBK_ID_ERM = "zsbRevisSbkIdErm";
	public static final String ZSB_REVIS_SBK_ID = "zsbRevisSbkId";
	public static final String ZSB_REVIS_BEGR = "zsbRevisBegr";
	public static final String ZSB_TRANS_SBK_ID_ERM = "zsbTransSbkIdErm";
	public static final String ZSB_TRANS_SBK_ID = "zsbTransSbkId";
	public static final String ZSB_TRANS_BEGR = "zsbTransBegr";
	public static final String ZSB_PERS_SBK_ID = "zsbPersSbkId";
	public static final String ZSB_PERS_BEGR = "zsbPersBegr";
	public static final String ZSB_GESAMT_SBK_ID = "zsbGesamtSbkId";
	public static final String ZSB_PERS_DATEN_ERM = "zsbPersDatenErm";
	public static final String ZSB_PERS_DATEN = "zsbPersDaten";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID = "guid";
	public static final String GUID_ORG = "guidOrg";
	public static final String USN = "usn";

	public void save(NZobSb transientInstance) {
		log.debug("saving NZobSb instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(NZobSb persistentInstance) {
		log.debug("deleting NZobSb instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public NZobSb findById(sernet.gs.reveng.NZobSbId id) {
		log.debug("getting NZobSb instance with id: " + id);
		try {
			NZobSb instance = (NZobSb) getSession().get(
					"sernet.gs.reveng.NZobSb", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(NZobSb instance) {
		log.debug("finding NZobSb instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.NZobSb").add(Example.create(instance))
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
		log.debug("finding NZobSb instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from NZobSb as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByZsbUebertragung(Object zsbUebertragung) {
		return findByProperty(ZSB_UEBERTRAGUNG, zsbUebertragung);
	}

	public List findByZsbAngebunden(Object zsbAngebunden) {
		return findByProperty(ZSB_ANGEBUNDEN, zsbAngebunden);
	}

	public List findByZsbVertraulich(Object zsbVertraulich) {
		return findByProperty(ZSB_VERTRAULICH, zsbVertraulich);
	}

	public List findByZsbIntegritaet(Object zsbIntegritaet) {
		return findByProperty(ZSB_INTEGRITAET, zsbIntegritaet);
	}

	public List findByZsbVerfuegbar(Object zsbVerfuegbar) {
		return findByProperty(ZSB_VERFUEGBAR, zsbVerfuegbar);
	}

	public List findByZsbVertrSbkIdErm(Object zsbVertrSbkIdErm) {
		return findByProperty(ZSB_VERTR_SBK_ID_ERM, zsbVertrSbkIdErm);
	}

	public List findByZsbVertrSbkId(Object zsbVertrSbkId) {
		return findByProperty(ZSB_VERTR_SBK_ID, zsbVertrSbkId);
	}

	public List findByZsbVertrBegr(Object zsbVertrBegr) {
		return findByProperty(ZSB_VERTR_BEGR, zsbVertrBegr);
	}

	public List findByZsbVerfuSbkIdErm(Object zsbVerfuSbkIdErm) {
		return findByProperty(ZSB_VERFU_SBK_ID_ERM, zsbVerfuSbkIdErm);
	}

	public List findByZsbVerfuSbkId(Object zsbVerfuSbkId) {
		return findByProperty(ZSB_VERFU_SBK_ID, zsbVerfuSbkId);
	}

	public List findByZsbVerfuBegr(Object zsbVerfuBegr) {
		return findByProperty(ZSB_VERFU_BEGR, zsbVerfuBegr);
	}

	public List findByZsbIntegSbkIdErm(Object zsbIntegSbkIdErm) {
		return findByProperty(ZSB_INTEG_SBK_ID_ERM, zsbIntegSbkIdErm);
	}

	public List findByZsbIntegSbkId(Object zsbIntegSbkId) {
		return findByProperty(ZSB_INTEG_SBK_ID, zsbIntegSbkId);
	}

	public List findByZsbIntegBegr(Object zsbIntegBegr) {
		return findByProperty(ZSB_INTEG_BEGR, zsbIntegBegr);
	}

	public List findByZsbAutenSbkIdErm(Object zsbAutenSbkIdErm) {
		return findByProperty(ZSB_AUTEN_SBK_ID_ERM, zsbAutenSbkIdErm);
	}

	public List findByZsbAutenSbkId(Object zsbAutenSbkId) {
		return findByProperty(ZSB_AUTEN_SBK_ID, zsbAutenSbkId);
	}

	public List findByZsbAutenBegr(Object zsbAutenBegr) {
		return findByProperty(ZSB_AUTEN_BEGR, zsbAutenBegr);
	}

	public List findByZsbRevisSbkIdErm(Object zsbRevisSbkIdErm) {
		return findByProperty(ZSB_REVIS_SBK_ID_ERM, zsbRevisSbkIdErm);
	}

	public List findByZsbRevisSbkId(Object zsbRevisSbkId) {
		return findByProperty(ZSB_REVIS_SBK_ID, zsbRevisSbkId);
	}

	public List findByZsbRevisBegr(Object zsbRevisBegr) {
		return findByProperty(ZSB_REVIS_BEGR, zsbRevisBegr);
	}

	public List findByZsbTransSbkIdErm(Object zsbTransSbkIdErm) {
		return findByProperty(ZSB_TRANS_SBK_ID_ERM, zsbTransSbkIdErm);
	}

	public List findByZsbTransSbkId(Object zsbTransSbkId) {
		return findByProperty(ZSB_TRANS_SBK_ID, zsbTransSbkId);
	}

	public List findByZsbTransBegr(Object zsbTransBegr) {
		return findByProperty(ZSB_TRANS_BEGR, zsbTransBegr);
	}

	public List findByZsbPersSbkId(Object zsbPersSbkId) {
		return findByProperty(ZSB_PERS_SBK_ID, zsbPersSbkId);
	}

	public List findByZsbPersBegr(Object zsbPersBegr) {
		return findByProperty(ZSB_PERS_BEGR, zsbPersBegr);
	}

	public List findByZsbGesamtSbkId(Object zsbGesamtSbkId) {
		return findByProperty(ZSB_GESAMT_SBK_ID, zsbGesamtSbkId);
	}

	public List findByZsbPersDatenErm(Object zsbPersDatenErm) {
		return findByProperty(ZSB_PERS_DATEN_ERM, zsbPersDatenErm);
	}

	public List findByZsbPersDaten(Object zsbPersDaten) {
		return findByProperty(ZSB_PERS_DATEN, zsbPersDaten);
	}

	public List findByImpNeu(Object impNeu) {
		return findByProperty(IMP_NEU, impNeu);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByGuidOrg(Object guidOrg) {
		return findByProperty(GUID_ORG, guidOrg);
	}

	public List findByUsn(Object usn) {
		return findByProperty(USN, usn);
	}

	public List findAll() {
		log.debug("finding all NZobSb instances");
		try {
			String queryString = "from NZobSb";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public NZobSb merge(NZobSb detachedInstance) {
		log.debug("merging NZobSb instance");
		try {
			NZobSb result = (NZobSb) getSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(NZobSb instance) {
		log.debug("attaching dirty NZobSb instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(NZobSb instance) {
		log.debug("attaching clean NZobSb instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}