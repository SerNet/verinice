package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbZeiteinheitenTxt entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbZeiteinheitenTxt
 * @author MyEclipse Persistence Tools
 */

public class MbZeiteinheitenTxtDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbZeiteinheitenTxtDAO.class);
	// property constants
	public static final String NAME = "name";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String GUID = "guid";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID_ORG = "guidOrg";

	public void save(MbZeiteinheitenTxt transientInstance) {
		log.debug("saving MbZeiteinheitenTxt instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbZeiteinheitenTxt persistentInstance) {
		log.debug("deleting MbZeiteinheitenTxt instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbZeiteinheitenTxt findById(sernet.gs.reveng.MbZeiteinheitenTxtId id) {
		log.debug("getting MbZeiteinheitenTxt instance with id: " + id);
		try {
			MbZeiteinheitenTxt instance = (MbZeiteinheitenTxt) getSession()
					.get("sernet.gs.reveng.MbZeiteinheitenTxt", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbZeiteinheitenTxt instance) {
		log.debug("finding MbZeiteinheitenTxt instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbZeiteinheitenTxt").add(
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
		log.debug("finding MbZeiteinheitenTxt instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from MbZeiteinheitenTxt as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	public List findByName(Object name) {
		return findByProperty(NAME, name);
	}

	public List findByBeschreibung(Object beschreibung) {
		return findByProperty(BESCHREIBUNG, beschreibung);
	}

	public List findByGuid(Object guid) {
		return findByProperty(GUID, guid);
	}

	public List findByImpNeu(Object impNeu) {
		return findByProperty(IMP_NEU, impNeu);
	}

	public List findByGuidOrg(Object guidOrg) {
		return findByProperty(GUID_ORG, guidOrg);
	}

	public List findAll() {
		log.debug("finding all MbZeiteinheitenTxt instances");
		try {
			String queryString = "from MbZeiteinheitenTxt";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbZeiteinheitenTxt merge(MbZeiteinheitenTxt detachedInstance) {
		log.debug("merging MbZeiteinheitenTxt instance");
		try {
			MbZeiteinheitenTxt result = (MbZeiteinheitenTxt) getSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbZeiteinheitenTxt instance) {
		log.debug("attaching dirty MbZeiteinheitenTxt instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbZeiteinheitenTxt instance) {
		log.debug("attaching clean MbZeiteinheitenTxt instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}