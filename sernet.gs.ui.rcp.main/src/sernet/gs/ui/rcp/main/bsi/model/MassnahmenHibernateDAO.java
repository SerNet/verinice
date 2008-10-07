package sernet.gs.ui.rcp.main.bsi.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.bsi.views.CnAImageProvider;
import sernet.gs.ui.rcp.main.bsi.views.chart.MassnahmeCount;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;

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

}
