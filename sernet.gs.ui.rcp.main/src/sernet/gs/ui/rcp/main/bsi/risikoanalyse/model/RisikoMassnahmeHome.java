package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class RisikoMassnahmeHome {
	
	private static RisikoMassnahmeHome instance;

	private RisikoMassnahmeHome() {
	}
	
	public synchronized static RisikoMassnahmeHome getInstance() {
		if (instance == null)
			instance = new RisikoMassnahmeHome();
		return instance;
	}
	
	public void saveNew(RisikoMassnahme mn) throws Exception {
		Session session = CnAElementHome.getInstance().getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(mn);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	}
	
	public void saveUpdate(RisikoMassnahme mn) throws Exception {
		Session session = CnAElementHome.getInstance().getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(mn);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	
	}
	
	public void remove(RisikoMassnahme mn) throws Exception {
		Session session = CnAElementHome.getInstance().getSession();
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(mn);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	
	}
	
	public ArrayList<RisikoMassnahme> loadAll() throws RuntimeException {
		Session session = CnAElementHome.getInstance().getSession();
		Transaction transaction = session.beginTransaction();
		Criteria criteria = session.createCriteria(RisikoMassnahme.class);
		List models = criteria.list();
		transaction.commit();
		
		ArrayList<RisikoMassnahme> result = new ArrayList<RisikoMassnahme>();
		result.addAll(models);
		return result;
	}
	
	private static final String QUERY_FIND_BY_ID = "from "
		+ RisikoMassnahme.class.getName() + " as element "
		+ "where element.number = ?";

	public RisikoMassnahme loadByNumber(String number) {
		Session session = CnAElementHome.getInstance().getSession();
		Query query = session.createQuery(QUERY_FIND_BY_ID);
		query.setString(0, number);
		List list = query.list();
		if (list == null || list.size() == 0)
			return null;
		return (RisikoMassnahme) list.get(0);
	}

	
	
	
}
