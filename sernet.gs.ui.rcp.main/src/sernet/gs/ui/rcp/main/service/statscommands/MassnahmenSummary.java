package sernet.gs.ui.rcp.main.service.statscommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;

public abstract class MassnahmenSummary extends GenericCommand {

	private BSIModel model;
	private List<String> tags;
	
	private Map<String, Integer> summary;

	public void execute() {
		LoadBSIModel command = new LoadBSIModel();
		getCommandService().executeCommand(command);
		model = command.getModel();
	}

	public BSIModel getModel() {
		return model;
	}

	public Map<String, Integer> getSummary() {
		return summary;
	}

	public void setSummary(Map<String, Integer> summary) {
		this.summary = summary;
	}

	
	
	

	
	
}
