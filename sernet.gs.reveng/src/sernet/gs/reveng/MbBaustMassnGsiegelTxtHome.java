package sernet.gs.reveng;

// Generated Jun 5, 2015 1:28:34 PM by Hibernate Tools 3.4.0.CR1

import java.util.List;

import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;

/**
 * Home object for domain model class MbBaustMassnGsiegelTxt.
 * @see sernet.gs.reveng.MbBaustMassnGsiegelTxt
 * @author Hibernate Tools
 */
public class MbBaustMassnGsiegelTxtHome {

	private static final Log log = LogFactory
			.getLog(MbBaustMassnGsiegelTxtHome.class);

	private final SessionFactory sessionFactory = getSessionFactory();

	protected SessionFactory getSessionFactory() {
		try {
			return (SessionFactory) new InitialContext()
					.lookup("SessionFactory");
		} catch (Exception e) {
			log.error("Could not locate SessionFactory in JNDI", e);
			throw new IllegalStateException(
					"Could not locate SessionFactory in JNDI");
		}
	}

	public void persist(MbBaustMassnGsiegelTxt transientInstance) {
		log.debug("persisting MbBaustMassnGsiegelTxt instance");
		try {
			sessionFactory.getCurrentSession().persist(transientInstance);
			log.debug("persist successful");
		} catch (RuntimeException re) {
			log.error("persist failed", re);
			throw re;
		}
	}

	public void attachDirty(MbBaustMassnGsiegelTxt instance) {
		log.debug("attaching dirty MbBaustMassnGsiegelTxt instance");
		try {
			sessionFactory.getCurrentSession().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(MbBaustMassnGsiegelTxt instance) {
		log.debug("attaching clean MbBaustMassnGsiegelTxt instance");
		try {
			sessionFactory.getCurrentSession().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void delete(MbBaustMassnGsiegelTxt persistentInstance) {
		log.debug("deleting MbBaustMassnGsiegelTxt instance");
		try {
			sessionFactory.getCurrentSession().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	public MbBaustMassnGsiegelTxt merge(MbBaustMassnGsiegelTxt detachedInstance) {
		log.debug("merging MbBaustMassnGsiegelTxt instance");
		try {
			MbBaustMassnGsiegelTxt result = (MbBaustMassnGsiegelTxt) sessionFactory
					.getCurrentSession().merge(detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public MbBaustMassnGsiegelTxt findById(
			sernet.gs.reveng.MbBaustMassnGsiegelTxtId id) {
		log.debug("getting MbBaustMassnGsiegelTxt instance with id: " + id);
		try {
			MbBaustMassnGsiegelTxt instance = (MbBaustMassnGsiegelTxt) sessionFactory
					.getCurrentSession().get(
							"sernet.gs.reveng.MbBaustMassnGsiegelTxt", id);
			if (instance == null) {
				log.debug("get successful, no instance found");
			} else {
				log.debug("get successful, instance found");
			}
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(MbBaustMassnGsiegelTxt instance) {
		log.debug("finding MbBaustMassnGsiegelTxt instance by example");
		try {
			List results = sessionFactory.getCurrentSession()
					.createCriteria("sernet.gs.reveng.MbBaustMassnGsiegelTxt")
					.add(Example.create(instance)).list();
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}
}
