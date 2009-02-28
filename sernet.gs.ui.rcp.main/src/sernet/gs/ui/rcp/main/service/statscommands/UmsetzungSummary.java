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
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;

public class UmsetzungSummary extends MassnahmenSummary {


	public void execute() {
		super.execute();
		setSummary(getUmsetzungenSummary());
	}
	
	public Map<String, Integer> getUmsetzungenSummary() {
		Map<String, Integer> result = new HashMap<String, Integer>();
		LoadCnAElementByType<MassnahmenUmsetzung> command = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		try {
			command = getCommandService().executeCommand(command);
			for (MassnahmenUmsetzung ums : command.getElements()) {
				if (result.get(ums.getUmsetzung()) == null)
					result.put(ums.getUmsetzung(), 0);
				Integer count = result.get(ums.getUmsetzung());
				result.put(ums.getUmsetzung(), ++count);
			}
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		
		return result;
	}
	
	
}
