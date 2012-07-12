package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.Set;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.service.commands.LoadBSIModel;

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
		
		HydratorUtil.hydrateElement(getDaoFactory().getDAOforTypedElement(element), 
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
