package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class LoadBSIModelComplete extends GenericCommand {

	private BSIModel model;
	private boolean includingMassnahmen;

	public LoadBSIModelComplete(boolean loadMassnahmen) {
		this.includingMassnahmen = loadMassnahmen;
	}
	
	public void execute() {
		LoadBSIModel command = new LoadBSIModel();
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		model = command.getModel();
		hydrate(model);
	}

	private void hydrate(CnATreeElement element) {
		if (element == null)
			return;
		
		HydratorUtil.hydrateElement(getDaoFactory().getDAOForObject(element), 
				element, true);
		
		Set<CnATreeElement> children = element.getChildren();
		for (CnATreeElement child : children) {
			if ((!includingMassnahmen) && child instanceof BausteinUmsetzung) {
				// next element:
				continue;
			}
			
			hydrate(child);
		}
	}

	public BSIModel getModel() {
		return model;
	}
	
	
	
	

}
