package sernet.gs.ui.rcp.main.common.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

/**
 * DAO class for model objects. Uses Hibernate as persistence framework.
 * 
 * @author koderman@sernet.de
 * 
 */

public class CnAElementHome {

	private static int[] mutex = new int[] {};

	private static CnAElementHome instance;

	private static final String QUERY_FIND_BY_ID = "from "
			+ CnATreeElement.class.getName() + " as element "
			+ "where element.dbId = ?";

	private SessionFactory sessionFactory = null;

	private Session session;

	private CnAElementHome() {

	}

	public static CnAElementHome getInstance() {
		if (instance == null) {
			instance = new CnAElementHome();
		}
		return instance;
	}

	public boolean isOpen() {
		return sessionFactory != null;
	}
	
	public void open(IProgressMonitor monitor) throws Exception {
		open(CnAWorkspace.getInstance().getConfDir(), monitor);
	}
	
	public void preload(String confDir) {
		Logger.getLogger(this.getClass()).debug("Preloading Hibernate...");
		try {
			File conf = new File(confDir
					+ File.separator + "hibernate.cfg.xml");
			SessionFactory tempFactory = new Configuration().configure(conf)
				.buildSessionFactory();
			Session tempSession = tempFactory.openSession();
			tempSession.close();
			tempFactory.close();
		} catch (Exception e) {
			// do nothing
		}
		Logger.getLogger(this.getClass()).debug("Finished preloading hibernate");
	}

	public void open(String confDir, IProgressMonitor monitor) throws Exception {
		try {
			File conf = new File(confDir
					+ File.separator + "hibernate.cfg.xml");
			sessionFactory = new Configuration().configure(conf)
					.buildSessionFactory();
			monitor.setTaskName("Öffne DB-Session...");
			session = sessionFactory.openSession();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error("Hibernate Fehler: ", e);
			throw new Exception(
					"Fehler beim Initialisieren der Datenbankverbindung.", e);
		}
	}

	public void close() {
		if (sessionFactory != null) {
			sessionFactory.close();
			sessionFactory = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	// TODO make use of application transactions

	public void save(CnATreeElement element) throws Exception {
		synchronized (mutex) {
			Logger.getLogger(this.getClass()).debug(
					"Saving new element: " + element);
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				session.save(element);
			//	session.save(new ChangeLogEntry(element));
				tx.commit();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
				if (tx != null)
					tx.rollback();
				throw e;
			}
		}
	}
	
	
	public void remove(Object element) throws Exception {
		synchronized (mutex) {

			Logger.getLogger(this.getClass()).debug(
					"Deleting element: " + element);
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				session.delete(element);
				tx.commit();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
				if (tx != null)
					tx.rollback();
				throw e;
			}

		}
	}

	public void update(CnATreeElement element) throws Exception {
		synchronized (mutex) {
			Logger.getLogger(this.getClass()).debug(
					"Updating element " + element.getTitle());
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				session.persist(element);
		//		session.save(new ChangeLogEntry(element));
				tx.commit();
			} catch (StaleObjectStateException se) {
				Logger.getLogger(this.getClass()).error(se);
				if (tx != null)
					tx.rollback();
				refresh(element);
				throw se;
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
				if (tx != null)
					tx.rollback();
				throw e;
			}

		}
	}
	
	public void update(List elements) throws StaleObjectStateException {
		synchronized (mutex) {
			Logger.getLogger(this.getClass()).debug(
					"Updating multiple elements");
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				for (Object element : elements) {
					session.persist(element);
				//	session.save(new ChangeLogEntry());
				}
				tx.commit();
			} catch (StaleObjectStateException se) {
				Logger.getLogger(this.getClass()).error(se);
				if (tx != null)
					tx.rollback();
				refresh(elements);
				throw se;
			} catch (RuntimeException re) {
				Logger.getLogger(this.getClass()).error(re);
				if (tx != null)
					tx.rollback();
				throw re;
			}

		}
	}

	public void refresh(List elements) {
		for (Object object : elements) {
			refresh((CnATreeElement)object);
		}
	}

	/**
	 * Load object with given ID for given class.
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public CnATreeElement loadById(Class<? extends CnATreeElement> clazz, int id) {
		Logger.getLogger(this.getClass()).debug(
				"Load " + clazz.getSimpleName() + " for id: " + id);
		Query query = session.createQuery(QUERY_FIND_BY_ID);
		query.setInteger(0, id);
		List list = query.list();
		if (list == null || list.size() == 0)
			return null;
		return (CnATreeElement) list.get(0);
	}


	

	/**
	 * Load whole model from DB.
	 * @param nullMonitor 
	 * 
	 * @return BSIModel object which is the top level object of the model
	 *         hierarchy.
	 * @throws Exception
	 */
	public BSIModel loadModel(IProgressMonitor nullMonitor) throws Exception {
		Logger.getLogger(this.getClass()).debug("Loading model instance");

		Transaction transaction = session.beginTransaction();
		nullMonitor.setTaskName("Lade Grundschutz Modell...");
		Criteria criteria = session.createCriteria(BSIModel.class);
		List models = criteria.list();
		transaction.commit();

		for (Iterator iter = models.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof BSIModel)
				Logger.getLogger(this.getClass()).debug("Loaded model instance.");
				return (BSIModel) o;
		}
		Logger.getLogger(this.getClass()).debug("No model instance found");
		return null;
	}

	/**
	 * Refresh given object from the database, looses all changes made
	 * in memory, sets element and all properties to actual state in database.
	 * 
	 * @param cnAElement
	 */
	public void refresh(CnATreeElement cnAElement) {
		Logger.getLogger(this.getClass()).debug("Refreshing object " + cnAElement.getTitle());
		session.refresh(cnAElement);
// should be sufficient
//		session.refresh(cnAElement.getEntity());
//		for (PropertyList list : cnAElement.getEntity().getTypedPropertyLists().values() ) {
//			session.refresh(list);	
//		}
	}

	public Session getSession() {
		return session;
	}

	public void save(CnALink link) throws Exception {

		synchronized (mutex) {
			Logger.getLogger(this.getClass()).debug(
					"Saving new link: " + link);
			Transaction tx = null;
			try {
				tx = session.beginTransaction();
				session.save(link);
				//session.save(new ChangeLogEntry(link.getDependant()));
				tx.commit();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
				if (tx != null)
					tx.rollback();
				throw e;
			}
		}
	
	}
}
