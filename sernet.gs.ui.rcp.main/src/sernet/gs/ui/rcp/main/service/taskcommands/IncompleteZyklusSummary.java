package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;

public class IncompleteZyklusSummary extends MassnahmenSummary {


	public void execute() {
		setSummary(getNotCompletedZyklusSummary());
	}
	
	public Map<String, Integer> getNotCompletedZyklusSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (Object object : getModel().getMassnahmen()) {
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
