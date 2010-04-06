package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * NZielobjekt entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.NZielobjekt
 * @author MyEclipse Persistence Tools
 */

public class NZielobjektDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(NZielobjektDAO.class);
	// property constants
	public static final String ORG_IMP_ID = "orgImpId";
	public static final String NAME = "name";
	public static final String NAME_ORG = "nameOrg";
	public static final String NAME_SAME = "nameSame";
	public static final String KUERZEL = "kuerzel";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String ANW_BESCHR_INF = "anwBeschrInf";
	public static final String ANW_INF1_BESCHR = "anwInf1Beschr";
	public static final String ANW_INF2_BESCHR = "anwInf2Beschr";
	public static final String ITV_AUDITOR = "itvAuditor";
	public static final String VERTRAGSGR = "vertragsgr";
	public static final String UNTERSUCHUNGSG = "untersuchungsg";
	public static final String PROJEKTIERUNG = "projektierung";
	public static final String VERTEILER = "verteiler";
	public static final String SICHTUNG = "sichtung";
	public static final String TELEFON = "telefon";
	public static final String EMAIL = "email";
	public static final String ABTEILUNG = "abteilung";
	public static final String ANZAHL = "anzahl";
	public static final String IMP_NEU = "impNeu";
	public static final String ERFASST_DURCH = "erfasstDurch";
	public static final String USN = "usn";
	public static final String GUID = "guid";
	public static final String GUID_ORG = "guidOrg";
	public static final String EXPORTIERT = "exportiert";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";
	public static final String CHANGED_BY = "changedBy";
	public static final String CM_USERNAME = "cmUsername";
	public static final String CM_IMP_ID = "cmImpId";
	public static final String CM_VER_ID1 = "cmVerId1";
	public static final String CM_VER_ID2 = "cmVerId2";
	public static final String RA_FARBE = "raFarbe";
	public static final String RA_FARBE_ITV = "raFarbeItv";
	public static final String KUERZEL_ORG = "kuerzelOrg";
	public static final String KUERZEL_SAME = "kuerzelSame";
	public static final String SET_DEFAULT = "setDefault";

	public void save(NZielobjekt transientInstance) {
		log.debug("saving NZielobjekt instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(NZielobjekt persistentInstance) {
		log.debug("deleting NZielobjekt instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public NZielobjekt findById(sernet.gs.reveng.NZielobjektId id) {
		log.debug("getting NZielobjekt instance with id: " + id);
		try {
			NZielobjekt instance = (NZielobjekt) getSession().get(
					"sernet.gs.reveng.NZielobjekt", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(NZielobjekt instance) {
		log.debug("finding NZielobjekt instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.NZielobjekt").add(
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
		log.debug("finding NZielobjekt instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from NZielobjekt as model where model."
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

	public List findByName(Object name) {
		return findByProperty(NAME, name);
	}

	public List findByNameOrg(Object nameOrg) {
		return findByProperty(NAME_ORG, nameOrg);
	}

	public List findByNameSame(Object nameSame) {
		return findByProperty(NAME_SAME, nameSame);
	}

	public List findByKuerzel(Object kuerzel) {
		return findByProperty(KUERZEL, kuerzel);
	}

	public List findByBeschreibung(Object beschreibung) {
		return findByProperty(BESCHREIBUNG, beschreibung);
	}

	public List findByAnwBeschrInf(Object anwBeschrInf) {
		return findByProperty(ANW_BESCHR_INF, anwBeschrInf);
	}

	public List findByAnwInf1Beschr(Object anwInf1Beschr) {
		return findByProperty(ANW_INF1_BESCHR, anwInf1Beschr);
	}

	public List findByAnwInf2Beschr(Object anwInf2Beschr) {
		return findByProperty(ANW_INF2_BESCHR, anwInf2Beschr);
	}

	public List findByItvAuditor(Object itvAuditor) {
		return findByProperty(ITV_AUDITOR, itvAuditor);
	}

	public List findByVertragsgr(Object vertragsgr) {
		return findByProperty(VERTRAGSGR, vertragsgr);
	}

	public List findByUntersuchungsg(Object untersuchungsg) {
		return findByProperty(UNTERSUCHUNGSG, untersuchungsg);
	}

	public List findByProjektierung(Object projektierung) {
		return findByProperty(PROJEKTIERUNG, projektierung);
	}

	public List findByVerteiler(Object verteiler) {
		return findByProperty(VERTEILER, verteiler);
	}

	public List findBySichtung(Object sichtung) {
		return findByProperty(SICHTUNG, sichtung);
	}

	public List findByTelefon(Object telefon) {
		return findByProperty(TELEFON, telefon);
	}

	public List findByEmail(Object email) {
		return findByProperty(EMAIL, email);
	}

	public List findByAbteilung(Object abteilung) {
		return findByProperty(ABTEILUNG, abteilung);
	}

	public List findByAnzahl(Object anzahl) {
		return findByProperty(ANZAHL, anzahl);
	}

	public List findByImpNeu(Object impNeu) {
		return findByProperty(IMP_NEU, impNeu);
	}

	public List findByErfasstDurch(Object erfasstDurch) {
		return findByProperty(ERFASST_DURCH, erfasstDurch);
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

	public List findByExportiert(Object exportiert) {
		return findByProperty(EXPORTIERT, exportiert);
	}

	public List findByGeloeschtDurch(Object geloeschtDurch) {
		return findByProperty(GELOESCHT_DURCH, geloeschtDurch);
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

	public List findByRaFarbe(Object raFarbe) {
		return findByProperty(RA_FARBE, raFarbe);
	}

	public List findByRaFarbeItv(Object raFarbeItv) {
		return findByProperty(RA_FARBE_ITV, raFarbeItv);
	}

	public List findByKuerzelOrg(Object kuerzelOrg) {
		return findByProperty(KUERZEL_ORG, kuerzelOrg);
	}

	public List findByKuerzelSame(Object kuerzelSame) {
		return findByProperty(KUERZEL_SAME, kuerzelSame);
	}

	public List findBySetDefault(Object setDefault) {
		return findByProperty(SET_DEFAULT, setDefault);
	}

	public List findAll() {
		log.debug("finding all NZielobjekt instances");
		try {
			String queryString = "from NZielobjekt";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public NZielobjekt merge(NZielobjekt detachedInstance) {
		log.debug("merging NZielobjekt instance");
		try {
			NZielobjekt result = (NZielobjekt) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(NZielobjekt instance) {
		log.debug("attaching dirty NZielobjekt instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(NZielobjekt instance) {
		log.debug("attaching clean NZielobjekt instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}