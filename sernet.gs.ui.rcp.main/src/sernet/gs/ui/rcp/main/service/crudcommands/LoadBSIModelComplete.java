package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadBSIModelComplete extends GenericCommand {

	private BSIModel model;

	public void execute() {
		LoadBSIModel command = new LoadBSIModel();
		getCommandService().executeCommand(command);
		model = command.getModel();
		hydrate(model);
	}

	private void hydrate(CnATreeElement elmt) {
		Set<CnATreeElement> children = elmt.getChildren();
		for (CnATreeElement child : children) {
			child.getEntity();
			child.getLinksDown();
			child.getLinksUp();
			hydrate(child);
		}
	}

	public BSIModel getModel() {
		return model;
	}
	
	
	
	

}
