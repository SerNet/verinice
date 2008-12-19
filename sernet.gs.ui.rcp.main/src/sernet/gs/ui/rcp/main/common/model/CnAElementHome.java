package sernet.gs.ui.rcp.main.common.model;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.hibernate.UnresolvableObjectException;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisListsHome;
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

	private static final String QUERY_FIND_CHANGES_SINCE = "from "
			+ ChangeLogEntry.class.getName() + " as change "
			+ "where change.changetime > ? " + "and not change.stationId = ? "
			+ "order by changetime";

	private SessionFactory sessionFactory = null;

	private Session session;

	private Transaction tx;
	private boolean applicationTransactionPresent = false;

	private CnAElementHome() {
		// singleton
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

	public void open(IProgress monitor) throws Exception {
		open(CnAWorkspace.getInstance().getConfDir(), monitor);
	}

	public void preload(String confDir) {
		Logger.getLogger(this.getClass()).debug("Preloading Hibernate...");
		try {
			File conf = new File(confDir + File.separator + "hibernate.cfg.xml");
			SessionFactory tempFactory = new Configuration().configure(conf)
					.buildSessionFactory();
			Session tempSession = tempFactory.openSession();
			tempSession.close();
			tempFactory.close();
		} catch (Exception e) {
			// do nothing
		}
		Logger.getLogger(this.getClass())
				.debug("Finished preloading hibernate");
	}

	public void open(String confDir, IProgress monitor) throws Exception {
		try {
			File conf = new File(confDir + File.separator + "hibernate.cfg.xml");
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
			if (!applicationTransactionPresent) {
				try {
					tx = session.beginTransaction();
					session.save(element);
					logChange(element, ChangeLogEntry.INSERT);
					tx.commit();
				} catch (Exception e) {
					Logger.getLogger(this.getClass()).error(e);
					
					throw e;
				}
			} else {
				try {
					session.save(element);
					logChange(element, ChangeLogEntry.INSERT);
				} catch (Exception e) {
					Logger.getLogger(this.getClass()).error(e);
					rollbackApplicationTransaction();
					tx = null;
					throw e;
				}
			}
		}
	}

	private void logChange(CnATreeElement element, int changeType) {
		ChangeLogEntry logEntry = new ChangeLogEntry(element, changeType);
		logEntry.setChangetime(Calendar.getInstance().getTime());
		session.save(logEntry);

		// Query query = session.createQuery("insert into ChangeLogEntry( " +
		// "elementId, elementClass, change, stationId, changetime) " +
		// "select el.dbId, :elementClass, :changeType, :stationId, current_timestamp() "
		// +
		// "from CnATreeElement el where el.dbId = :elementId");
		//		
		// query.setInteger("elementId", element.getDbId());
		// query.setString("elementClass", element.getClass().getName());
		// query.setInteger("changeType", changeType);
		// query.setString("stationId", ChangeLogEntry.STATION_ID);
		//		
		//		
		// query.executeUpdate();

	}

	// FIXME parameters are not set check syntax

	public void remove(CnATreeElement element) throws Exception {

		synchronized (mutex) {

			// if this is a finishedRiskanalysis we need to delete the
			// intermediate steps as well:
			FinishedRiskAnalysisLists analysisLists = null;
			if (element instanceof FinishedRiskAnalysis) {
				analysisLists = FinishedRiskAnalysisListsHome.getInstance()
						.loadById(((FinishedRiskAnalysis) element).getDbId());
			}

			Logger.getLogger(this.getClass()).debug(
					"Deleting element: " + element.getTitel());
			try {
				if (!applicationTransactionPresent)
					tx = session.beginTransaction();
				session.delete(element);
				if (analysisLists != null)
					session.delete(analysisLists);
				logChange(element, ChangeLogEntry.DELETE);
				if (!applicationTransactionPresent)
					tx.commit();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
				rollbackApplicationTransaction();
				throw e;
			}

		}
	}

	private void rollbackApplicationTransaction() {
		if (tx != null) {
			try {
				tx.rollback();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
			}

		}
		tx = null;
		applicationTransactionPresent = false;
	}

	public void remove(CnALink element) throws Exception {
		synchronized (mutex) {

			Logger.getLogger(this.getClass()).debug(
					"Deleting element: " + element);
			try {
				if (!applicationTransactionPresent)
					tx = session.beginTransaction();
				session.delete(element);
				logChange(element.getDependant(), ChangeLogEntry.DELETE);
				if (!applicationTransactionPresent)
					tx.commit();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
				rollbackApplicationTransaction();
				throw e;
			}

		}
	}

	public void update(CnATreeElement element) throws Exception {
		synchronized (mutex) {
			Logger.getLogger(this.getClass()).debug(
					"Updating element " + element.getTitel());
			try {
				if (!applicationTransactionPresent)
					tx = session.beginTransaction();
				session.persist(element);
				logChange(element, ChangeLogEntry.UPDATE);
				if (!applicationTransactionPresent)
					tx.commit();
			} catch (StaleObjectStateException se) {
				Logger.getLogger(this.getClass()).error(se);
				rollbackApplicationTransaction();
				refresh(element);
				throw se;
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
				rollbackApplicationTransaction();
				throw e;
			}
		}
	}

	public void update(List<? extends CnATreeElement> elements)
			throws StaleObjectStateException {
		synchronized (mutex) {
			Logger.getLogger(this.getClass()).debug(
					"Updating multiple elements");
			try {
				if (!applicationTransactionPresent)
					tx = session.beginTransaction();
				for (CnATreeElement element : elements) {
					session.persist(element);
					logChange(element, ChangeLogEntry.UPDATE);
				}
				if (!applicationTransactionPresent)
					tx.commit();
			} catch (StaleObjectStateException se) {
				Logger.getLogger(this.getClass()).error(se);
				rollbackApplicationTransaction();
				refresh(elements);
				throw se;
			} catch (RuntimeException re) {
				Logger.getLogger(this.getClass()).error(re);
				rollbackApplicationTransaction();
				throw re;
			}

		}
	}

	public void refresh(List<? extends CnATreeElement> elements) {
		for (CnATreeElement object : elements) {
			refresh(object);
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
	 * Load object with given ID of given class name. Object must exist in the
	 * database.
	 * 
	 * @param className
	 *            Simple name of the object's class.
	 * @param id
	 *            database ID of the object to load
	 * @return object of the given class with given ID
	 */
	public CnATreeElement loadById(String className, int id) {
		Logger.getLogger(this.getClass()).debug(
				"Load " + className + " for id: " + id);
		Query query = session.createQuery(QUERY_FIND_BY_ID);
		query.setInteger(0, id);
		List list = query.list();
		if (list == null || list.size() == 0)
			return null;
		return (CnATreeElement) list.get(0);
	}

	/**
	 * Load whole model from DB (lazy). Proxies will be instantiated by
	 * hibernate on first access.
	 * 
	 * @param nullMonitor
	 * 
	 * @return BSIModel object which is the top level object of the model
	 *         hierarchy.
	 * @throws Exception
	 */
	public BSIModel loadModel(IProgress nullMonitor) throws Exception {
		Logger.getLogger(this.getClass()).debug("Loading model instance");

		if (!applicationTransactionPresent)
			tx = session.beginTransaction();
		nullMonitor.setTaskName("Lade Grundschutz Modell...");
		Criteria criteria = session.createCriteria(BSIModel.class);
		List models = criteria.list();
		if (!applicationTransactionPresent)
			tx.commit();

		for (Iterator iter = models.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof BSIModel)
				Logger.getLogger(this.getClass()).debug(
						"Loaded model instance.");
			return (BSIModel) o;
		}
		Logger.getLogger(this.getClass()).debug("No model instance found");
		return null;
	}

	/**
	 * Refresh given object from the database, looses all changes made in
	 * memory, sets element and all properties to actual state in database.
	 * 
	 * @param cnAElement
	 */
	public void refresh(CnATreeElement cnAElement) {
		Logger.getLogger(this.getClass()).debug(
				"Refreshing object " + cnAElement.getTitel());
		try {
			session.refresh(cnAElement);

		} catch (UnresolvableObjectException e) {
			session.close();
			session = sessionFactory.openSession();
			try {
				CnAElementFactory.getInstance().loadOrCreateModel(
						new IProgress() {

							public void beginTask(String name, int totalWork) {

							}

							public void done() {

							}

							public void setTaskName(String name) {

							}

							public void worked(int work) {

							}

						});
			} catch (Exception e1) {
				ExceptionUtil.log(e, "");
			}
		}

	}

	public Session getSession() {
		return session;
	}

	public void save(CnALink link) throws Exception {

		synchronized (mutex) {
			Logger.getLogger(this.getClass()).debug("Saving new link: " + link);
			try {
				if (!applicationTransactionPresent)
					tx = session.beginTransaction();
				session.save(link);
				logChange(link.getDependant(), ChangeLogEntry.INSERT);
				if (!applicationTransactionPresent)
					tx.commit();
			} catch (Exception e) {
				Logger.getLogger(this.getClass()).error(e);
				rollbackApplicationTransaction();
				throw e;
			}
		}
	}

	public List<ChangeLogEntry> loadChangesSince(Date lastUpdate) {
		Logger.getLogger(this.getClass()).debug(
				"Looking for changes since " + lastUpdate);
		Query query = session.createQuery(QUERY_FIND_CHANGES_SINCE);
		query.setTimestamp(0, lastUpdate);
		query.setString(1, ChangeLogEntry.STATION_ID);
		List<ChangeLogEntry> list = query.list();
		if (list == null)
			list = new ArrayList<ChangeLogEntry>();
		return list;
	}

	public Object getElementInSession(String clazz, Integer id) {
		return session.get(clazz, id);
	}

	public synchronized void endApplicationTransaction()  {
		if (instance != null && instance.session != null) {
			try {
				tx.commit();
				tx = null;
				applicationTransactionPresent = false;
			} catch (Exception e) {
				Logger.getLogger(CnAElementHome.class).error(e);
				rollbackApplicationTransaction();
			}
		}
	}

	public synchronized void startApplicationTransaction() {
		if (instance != null && instance.session != null) {
			try {
				tx = session.beginTransaction();
				applicationTransactionPresent = true;
			} catch (Exception e) {
				Logger.getLogger(CnAElementHome.class).error(e);
				applicationTransactionPresent = false;
				tx = null;
			}
		}

	}

	// public Timestamp getCurrentTime() {
	// Query query =
	// session.createQuery("select current_timestamp() from ITVerbund itv");
	// return (Timestamp) query.list().get(0);
	// }

}
