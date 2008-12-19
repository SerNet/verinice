package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.classic.Session;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.bsi.views.chart.MassnahmeCount;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;

// FIXME change this to HQL queries

public class MassnahmenHibernateDAO implements IMassnahmenDAO {
	

	public Map<String, Integer> getUmsetzungenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (Object object : CnAElementFactory.getCurrentModel().getMassnahmen()) {
			MassnahmenUmsetzung ums = (MassnahmenUmsetzung) object;
			if (result.get(ums.getUmsetzung()) == null)
				result.put(ums.getUmsetzung(), 0);
			Integer count = result.get(ums.getUmsetzung());
			result.put(ums.getUmsetzung(), ++count);
		}
		return result;
	}

	public Map<String, Integer> getNotCompletedStufenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (Object object : CnAElementFactory.getCurrentModel().getMassnahmen()) {
			MassnahmenUmsetzung ums = (MassnahmenUmsetzung) object;
			if (ums.isCompleted())
				continue;
			String stufe = Character.toString(ums.getStufe());
			if (result.get(stufe) == null)
				result.put(stufe, 0);
			Integer count = result.get(stufe);
			result.put(stufe, ++count);
		}
		return result;
	}

	public Map<String, Integer> getCompletedStufenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (Object object : CnAElementFactory.getCurrentModel().getMassnahmen()) {
			MassnahmenUmsetzung ums = (MassnahmenUmsetzung) object;
			if (!ums.isCompleted())
				continue;
			String stufe = Character.toString(ums.getStufe());
			if (result.get(stufe) == null)
				result.put(stufe, 0);
			Integer count = result.get(stufe);
			result.put(stufe, ++count);
		}
		return result;
	}

	public Map<String, Integer> getCompletedZyklusSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (Object object : CnAElementFactory.getCurrentModel().getMassnahmen()) {
			MassnahmenUmsetzung ums = (MassnahmenUmsetzung) object;
			if (!ums.isCompleted())
				continue;
			String lz = ums.getLebenszyklus();
			if (lz == null || lz.length() <5)
				lz = "sonstige";
			if (result.get(lz) == null)
				result.put(lz, 0);
			Integer count = result.get(lz);
			result.put(lz, ++count);
		}
		return result;
	}

	public Map<String, Integer> getNotCompletedZyklusSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (Object object : CnAElementFactory.getCurrentModel().getMassnahmen()) {
			MassnahmenUmsetzung ums = (MassnahmenUmsetzung) object;
			if (ums.isCompleted())
				continue;
			String lz = ums.getLebenszyklus();
			
			if (lz == null || lz.length() <5)
				lz = "sonstige";
			
			if (result.get(lz) == null)
				result.put(lz, 0);
			Integer count = result.get(lz);
			result.put(lz, ++count);
		}
		return result;
	}

	public Map<String, Integer> getSchichtenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		ArrayList<BausteinUmsetzung> bausteine = CnAElementFactory.getCurrentModel().getBausteine();
		for (BausteinUmsetzung baustein: bausteine) {
			Baustein baustein2 = BSIKatalogInvisibleRoot.getInstance().getBaustein(baustein.getKapitel());
			if (baustein2 == null) {
				Logger.getLogger(this.getClass()).debug("Kein Baustein gefunden für ID" + baustein.getId());
				continue;
			}
			
			String schicht = Integer.toString(baustein2.getSchicht());

			if (result.get(schicht) == null)
				result.put(schicht, baustein.getMassnahmenUmsetzungen().size());
			else {
				Integer count = result.get(schicht);
				result.put(schicht, count + baustein.getMassnahmenUmsetzungen().size());
			}
		}
		return result;
	}

	public Map<String, Integer> getCompletedSchichtenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		ArrayList<BausteinUmsetzung> bausteine = CnAElementFactory.getCurrentModel().getBausteine();
		for (BausteinUmsetzung baustein: bausteine) {
			Baustein baustein2 = BSIKatalogInvisibleRoot.getInstance().getBaustein(baustein.getKapitel());
			if (baustein2 == null) {
				Logger.getLogger(this.getClass()).debug("Kein Baustein gefunden für ID" + baustein.getId());
				continue;
			}
			String schicht = Integer.toString(baustein2.getSchicht());
			int umgesetztSum = 0;
			for (MassnahmenUmsetzung ums: baustein.getMassnahmenUmsetzungen()) {
				if (ums.isCompleted())
					umgesetztSum++;
			}
			
			if (result.get(schicht) == null)
				result.put(schicht, umgesetztSum);
			else {
				Integer count = result.get(schicht);
				result.put(schicht, count + umgesetztSum);
			}
		}
		return result;
	}

}
