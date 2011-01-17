package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbDringlichkeitTxt entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbDringlichkeitTxt
 * @author MyEclipse Persistence Tools
 */

public class MbDringlichkeitTxtDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbDringlichkeitTxtDAO.class);
	// property constants
	public static final String NAME = "name";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String GUID = "guid";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID_ORG = "guidOrg";

	public void save(MbDringlichkeitTxt transientInstance) {
		log.debug("saving MbDringlichkeitTxt instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbDringlichkeitTxt persistentInstance) {
		log.debug("deleting MbDringlichkeitTxt instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbDringlichkeitTxt findById(sernet.gs.reveng.MbDringlichkeitTxtId id) {
		log.debug("getting MbDringlichkeitTxt instance with id: " + id);
		try {
			MbDringlichkeitTxt instance = (MbDringlichkeitTxt) getSession()
					.get("sernet.gs.reveng.MbDringlichkeitTxt", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbDringlichkeitTxt instance) {
		log.debug("finding MbDringlichkeitTxt instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbDringlichkeitTxt").add(
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
		log.debug("finding MbDringlichkeitTxt instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from MbDringlichkeitTxt as model where model."
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
		log.debug("finding all MbDringlichkeitTxt instances");
		try {
			String queryString = "from MbDringlichkeitTxt";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbDringlichkeitTxt merge(MbDringlichkeitTxt detachedInstance) {
		log.debug("merging MbDringlichkeitTxt instance");
		try {
			MbDringlichkeitTxt result = (MbDringlichkeitTxt) getSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbDringlichkeitTxt instance) {
		log.debug("attaching dirty MbDringlichkeitTxt instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbDringlichkeitTxt instance) {
		log.debug("attaching clean MbDringlichkeitTxt instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}