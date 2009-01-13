package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.IBSIStrukturElement;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;

public class FindAllTags extends GenericCommand {

	private BSIModel model;
	private List<String> tags;

	public void execute() {
		LoadBSIModel command = new LoadBSIModel();
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		model = command.getModel();
		tags = model.getTags();
	}

	public List<String> getTags() {
		return tags;
	}
	
	

	
	
}
