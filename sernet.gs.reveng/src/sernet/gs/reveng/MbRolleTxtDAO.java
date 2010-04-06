package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbRolleTxt entities. Transaction control of the save(), update() and delete()
 * operations can directly support Spring container-managed transactions or they
 * can be augmented to handle user-managed Spring transactions. Each of these
 * methods provides additional information for how to configure it for the
 * desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbRolleTxt
 * @author MyEclipse Persistence Tools
 */

public class MbRolleTxtDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbRolleTxtDAO.class);
	// property constants
	public static final String NAME = "name";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String GUID = "guid";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID_ORG = "guidOrg";

	public void save(MbRolleTxt transientInstance) {
		log.debug("saving MbRolleTxt instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbRolleTxt persistentInstance) {
		log.debug("deleting MbRolleTxt instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbRolleTxt findById(sernet.gs.reveng.MbRolleTxtId id) {
		log.debug("getting MbRolleTxt instance with id: " + id);
		try {
			MbRolleTxt instance = (MbRolleTxt) getSession().get(
					"sernet.gs.reveng.MbRolleTxt", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbRolleTxt instance) {
		log.debug("finding MbRolleTxt instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbRolleTxt")
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
		log.debug("finding MbRolleTxt instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from MbRolleTxt as model where model."
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
		log.debug("finding all MbRolleTxt instances");
		try {
			String queryString = "from MbRolleTxt";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbRolleTxt merge(MbRolleTxt detachedInstance) {
		log.debug("merging MbRolleTxt instance");
		try {
			MbRolleTxt result = (MbRolleTxt) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbRolleTxt instance) {
		log.debug("attaching dirty MbRolleTxt instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbRolleTxt instance) {
		log.debug("attaching clean MbRolleTxt instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}