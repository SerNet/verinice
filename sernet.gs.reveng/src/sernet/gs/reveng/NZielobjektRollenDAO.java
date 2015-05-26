package sernet.gs.reveng;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.criterion.Example;

/**
 * A data access object (DAO) providing persistence and search support for
 * NZielobjektRollen entities. Transaction control of the save(), update() and
 * delete() operations can directly support Spring container-managed
 * transactions or they can be augmented to handle user-managed Spring
 * transactions. Each of these methods provides additional information for how
 * to configure it for the desired type of transaction control.
 * 
 * @see sernet.gs.reveng.NZielobjektRollen
 * @author MyEclipse Persistence Tools
 */

public class NZielobjektRollenDAO extends BaseHibernateDAO {
	private static final Logger log = Logger.getLogger(NZielobjektRollenDAO.class);
	// property constants
	public static final String USN = "usn";
	public static final String GUID = "guid";
	public static final String GUID_ORG = "guidOrg";
	public static final String ERFASST_DURCH = "erfasstDurch";
	public static final String GELOESCHT_DURCH = "geloeschtDurch";
	public static final String IMP_NEU = "impNeu";

	public void save(NZielobjektRollen transientInstance) {
		log.debug("saving NZielobjektRollen instance");
		try {
			getSession().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(NZielobjektRollen persistentInstance) {
		log.debug("deleting NZielobjektRollen instance");
		try {
			getSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public NZielobjektRollen findById(sernet.gs.reveng.NZielobjektRollenId id) {
		log.debug("getting NZielobjektRollen instance with id: " + id);
		try {
			NZielobjektRollen instance = (NZielobjektRollen) getSession().get(
					"sernet.gs.reveng.NZielobjektRollen", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(NZielobjektRollen instance) {
		log.debug("finding NZielobjektRollen instance by example");
		try {
			List results = getSession().createCriteria(
					"sernet.gs.reveng.NZielobjektRollen").add(
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
		log.debug("finding NZielobjektRollen instance with property: "
				+ propertyName + ", value: " + value);
		try {
			String queryString = "from NZielobjektRollen as model where model."
					+ propertyName + "= ?";
			Query queryObject = getSession().createQuery(queryString);
			queryObject.setParameter(0, value);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
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

	public List findByErfasstDurch(Object erfasstDurch) {
		return findByProperty(ERFASST_DURCH, erfasstDurch);
	}

	public List findByGeloeschtDurch(Object geloeschtDurch) {
		return findByProperty(GELOESCHT_DURCH, geloeschtDurch);
	}

	public List findByImpNeu(Object impNeu) {
		return findByProperty(IMP_NEU, impNeu);
	}

	public List findAll() {
		log.debug("finding all NZielobjektRollen instances");
		try {
			String queryString = "from NZielobjektRollen";
			Query queryObject = getSession().createQuery(queryString);
			return queryObject.list();
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	public NZielobjektRollen merge(NZielobjektRollen detachedInstance) {
		log.debug("merging NZielobjektRollen instance");
		try {
			NZielobjektRollen result = (NZielobjektRollen) getSession().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(NZielobjektRollen instance) {
		log.debug("attaching dirty NZielobjektRollen instance");
		try {
			getSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(NZielobjektRollen instance) {
		log.debug("attaching clean NZielobjektRollen instance");
		try {
			getSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}
}