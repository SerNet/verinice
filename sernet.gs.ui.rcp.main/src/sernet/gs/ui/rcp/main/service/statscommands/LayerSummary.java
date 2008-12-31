package sernet.gs.ui.rcp.main.service.statscommands;

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

public class LayerSummary extends MassnahmenSummary {


	public void execute() {
		setSummary(getSchichtenSummary());
	}
	
	public Map<String, Integer> getSchichtenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		ArrayList<BausteinUmsetzung> bausteine =getModel().getBausteine();
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

	
	
}
