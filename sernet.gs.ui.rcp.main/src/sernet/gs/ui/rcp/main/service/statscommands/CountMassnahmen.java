package sernet.gs.ui.rcp.main.service.statscommands;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementByType;

public class CountMassnahmen extends GenericCommand {

	private int totalCount;

	public void execute() {
		LoadElementByType<MassnahmenUmsetzung> command = new LoadElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		getCommandService().executeCommand(command);
		totalCount = command.getElements().size();
	}

	public int getTotalCount() {
		return totalCount;
	}
	

}
