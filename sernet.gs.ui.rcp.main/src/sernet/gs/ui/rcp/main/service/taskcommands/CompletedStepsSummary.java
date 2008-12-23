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

public class CompletedStepsSummary extends MassnahmenSummary {


	public void execute() {
		setSummary(getCompletedStufenSummary());
	}
	
	public Map<String, Integer> getCompletedStufenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (Object object : getModel().getMassnahmen()) {
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

	
	
}
