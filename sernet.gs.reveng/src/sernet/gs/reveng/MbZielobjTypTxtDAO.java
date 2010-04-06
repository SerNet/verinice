package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * MbZielobjTypTxt entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.MbZielobjTypTxt
 * @author MyEclipse Persistence Tools
 */

public class MbZielobjTypTxtDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(MbZielobjTypTxtDAO.class);
	// property constants
	public static final String NAME = "name";
	public static final String NAME2 = "name2";
	public static final String BESCHREIBUNG = "beschreibung";
	public static final String HTMLTEXT = "htmltext";
	public static final String GUID = "guid";
	public static final String IMP_NEU = "impNeu";
	public static final String GUID_ORG = "guidOrg";
	public static final String ABSTRACT_ = "abstract_";
	public static final String CHANGED_BY = "changedBy";

	public void save(MbZielobjTypTxt transientInstance) {
		log.debug("saving MbZielobjTypTxt instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(MbZielobjTypTxt persistentInstance) {
		log.debug("deleting MbZielobjTypTxt instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbZielobjTypTxt findById(sernet.gs.reveng.MbZielobjTypTxtId id) {
		log.debug("getting MbZielobjTypTxt instance with id: " + id);
		try {
			MbZielobjTypTxt instance = (MbZielobjTypTxt) getSession().get(
					"sernet.gs.reveng.MbZielobjTypTxt", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbZielobjTypTxt instance) {
		log.debug("finding MbZielobjTypTxt instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.MbZielobjTypTxt").add(
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
		log.debug("finding MbZielobjTypTxt instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from MbZielobjTypTxt as model where model."
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

	public List findByName2(Object name2) {
		return findByProperty(NAME2, name2);
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
		log.debug("finding all MbZielobjTypTxt instances");
		try {
			String queryString = "from MbZielobjTypTxt";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public MbZielobjTypTxt merge(MbZielobjTypTxt detachedInstance) {
		log.debug("merging MbZielobjTypTxt instance");
		try {
			MbZielobjTypTxt result = (MbZielobjTypTxt) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(MbZielobjTypTxt instance) {
		log.debug("attaching dirty MbZielobjTypTxt instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbZielobjTypTxt instance) {
		log.debug("attaching clean MbZielobjTypTxt instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}