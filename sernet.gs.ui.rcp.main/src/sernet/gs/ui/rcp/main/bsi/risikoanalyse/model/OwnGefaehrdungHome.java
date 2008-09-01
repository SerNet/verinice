package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;

public class OwnGefaehrdungHome {
	
	private static OwnGefaehrdungHome instance;
	private Session session;

	private OwnGefaehrdungHome() {
		session = CnAElementHome.getInstance().getSession();
	}
	
	public synchronized static OwnGefaehrdungHome getInstance() {
		if (instance == null)
			instance = new OwnGefaehrdungHome();
		return instance;
	}
	
	public void saveNew(OwnGefaehrdung gefaehrdung) throws Exception {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(gefaehrdung);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	}
	
	public void saveUpdate(OwnGefaehrdung gefaehrdung) throws Exception {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(gefaehrdung);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	
	}
	
	public void remove(OwnGefaehrdung gefaehrdung) throws Exception {
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(gefaehrdung);
			tx.commit();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(e);
			if (tx != null)
				tx.rollback();
			throw e;
		}
	
	}
	
	public ArrayList<OwnGefaehrdung> loadAll() throws RuntimeException {
		Transaction transaction = session.beginTransaction();
		Criteria criteria = session.createCriteria(OwnGefaehrdung.class);
		List models = criteria.list();
		transaction.commit();
		
		ArrayList<OwnGefaehrdung> result = new ArrayList<OwnGefaehrdung>();
		result.addAll(models);
		return result;
	}
	
	
}
