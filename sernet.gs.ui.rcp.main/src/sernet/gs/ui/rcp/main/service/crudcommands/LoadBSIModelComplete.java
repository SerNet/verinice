package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
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

	private void hydrate(BSIModel model2) {
		if (model2 == null)
			return;
		List<CnATreeElement> flatList = model2.getAllElementsFlatList(includingMassnahmen);
		if (flatList != null) {
			HydratorUtil.hydrateElement(getDaoFactory().getDAO(BSIModel.class), model2, true);
		}
		
		for (CnATreeElement cnATreeElement : flatList) {
			HydratorUtil.hydrateElement(getDaoFactory().getDAO(BSIModel.class), cnATreeElement, true);
		}
	}

	public BSIModel getModel() {
		return model;
	}
	
	
	
	

}
