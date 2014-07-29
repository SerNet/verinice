package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MUmsetzStatTxt entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MUmsetzStatTxt
 * @author MyEclipse Persistence Tools
 */

public class MUmsetzStatTxtDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MUmsetzStatTxtDAO.class);
	// property constants
	public static final String NAME = "name";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String GUID = "guid";
	public static final String IMP_NEU = "impNeu";

	public void save(MUmsetzStatTxt transientInstance) {
		log.debug("saving MUmsetzStatTxt instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MUmsetzStatTxt persistentInstance) {
		log.debug("deleting MUmsetzStatTxt instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MUmsetzStatTxt findById(sernet.gs.reveng.MUmsetzStatTxtId id) {
		log.debug("getting MUmsetzStatTxt instance with id: " + id);
		try {
			MUmsetzStatTxt instance = (MUmsetzStatTxt) getSession().get(
					"sernet.gs.reveng.MUmsetzStatTxt", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MUmsetzStatTxt instance) {
		log.debug("finding MUmsetzStatTxt instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MUmsetzStatTxt").add(
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
		log.debug("finding MUmsetzStatTxt instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from MUmsetzStatTxt as model where model."
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

	public List findAll() {
		log.debug("finding all MUmsetzStatTxt instances");
		try {
			String queryString = "from MUmsetzStatTxt";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MUmsetzStatTxt merge(MUmsetzStatTxt detachedInstance) {
		log.debug("merging MUmsetzStatTxt instance");
		try {
			MUmsetzStatTxt result = (MUmsetzStatTxt) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MUmsetzStatTxt instance) {
		log.debug("attaching dirty MUmsetzStatTxt instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MUmsetzStatTxt instance) {
		log.debug("attaching clean MUmsetzStatTxt instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}