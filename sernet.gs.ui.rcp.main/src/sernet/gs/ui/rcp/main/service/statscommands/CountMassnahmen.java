package sernet.gs.ui.rcp.main.service.statscommands;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByType;

public class CountMassnahmen extends GenericCommand {

	private int totalCount;

	public void execute() {
		LoadCnAElementByType<MassnahmenUmsetzung> command = new LoadCnAElementByType<MassnahmenUmsetzung>(MassnahmenUmsetzung.class);
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		totalCount = command.getElements().size();
	}

	public int getTotalCount() {
		return totalCount;
	}
	

}
