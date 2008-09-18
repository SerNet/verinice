package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class FinishedRiskAnalysisListsHome {
	
	private static final String QUERY_FIND_BY_PARENT_ID = "from "
		+ FinishedRiskAnalysisLists.class.getName() + " as element "
		+ "where element.finishedRiskAnalysisId = ?";
	
	private Session session;
	private static FinishedRiskAnalysisListsHome instance;

	private FinishedRiskAnalysisListsHome() {
		session = CnAElementHome.getInstance().getSession();
	}
	
	public synchronized static FinishedRiskAnalysisListsHome getInstance() {
		if (instance == null)
			instance = new FinishedRiskAnalysisListsHome();
		return instance;
	}
	
	public void saveNew(FinishedRiskAnalysisLists list) throws Exception {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(list);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	}

	public void update(FinishedRiskAnalysisLists list) throws Exception {

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(list);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	}
	
	public void remove(FinishedRiskAnalysisLists list) throws Exception {

		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(list);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	
	
	}
	
	public FinishedRiskAnalysisLists loadById(int id) {
		Query query = session.createQuery(QUERY_FIND_BY_PARENT_ID);
		query.setInteger(0, id);
		List list = query.list();
		if (list == null || list.size() == 0)
			return null;
		return (FinishedRiskAnalysisLists) list.get(0);
	
	}
}
