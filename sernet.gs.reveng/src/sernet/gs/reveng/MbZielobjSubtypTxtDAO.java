package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbZielobjSubtypTxt entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbZielobjSubtypTxt
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjSubtypTxtDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbZielobjSubtypTxtDAO.class);
	// property constants
	public static final String NAME = "name";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String HTMLTEXT = "htmltext";
	public static final String GUID = "guid";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID_ORG = "guidOrg";
	public static final String ABSTRACT_ = "abstract_";
	public static final String CHANGED_BY = "changedBy";

	public void save(MbZielobjSubtypTxt transientInstance) {
		log.debug("saving MbZielobjSubtypTxt instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbZielobjSubtypTxt persistentInstance) {
		log.debug("deleting MbZielobjSubtypTxt instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbZielobjSubtypTxt findById(sernet.gs.reveng.MbZielobjSubtypTxtId id) {
		log.debug("getting MbZielobjSubtypTxt instance with id: " + id);
		try {
			MbZielobjSubtypTxt instance = (MbZielobjSubtypTxt) getSession()
					.get("sernet.gs.reveng.MbZielobjSubtypTxt", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbZielobjSubtypTxt instance) {
		log.debug("finding MbZielobjSubtypTxt instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbZielobjSubtypTxt").add(
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
		log.debug("finding MbZielobjSubtypTxt instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from MbZielobjSubtypTxt as model where model."
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

	public List findByHtmltext(Object htmltext) {
		return findByProperty(HTMLTEXT, htmltext);
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

	public List findByAbstract_(Object abstract_) {
		return findByProperty(ABSTRACT_, abstract_);
	}

	public List findByChangedBy(Object changedBy) {
		return findByProperty(CHANGED_BY, changedBy);
	}

	public List findAll() {
		log.debug("finding all MbZielobjSubtypTxt instances");
		try {
			String queryString = "from MbZielobjSubtypTxt";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbZielobjSubtypTxt merge(MbZielobjSubtypTxt detachedInstance) {
		log.debug("merging MbZielobjSubtypTxt instance");
		try {
			MbZielobjSubtypTxt result = (MbZielobjSubtypTxt) getSession()
					.merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbZielobjSubtypTxt instance) {
		log.debug("attaching dirty MbZielobjSubtypTxt instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbZielobjSubtypTxt instance) {
		log.debug("attaching clean MbZielobjSubtypTxt instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}