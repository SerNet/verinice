package sernet.gs.ui.rcp.main.service.migrationcommands;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;

public abstract class DbMigration extends GenericCommand {
	public abstract double getVersion();
	
	protected void updateVersion() {
		Logger.getLogger(this.getClass()).debug("Settings DB version to " + getVersion());
		try {
			LoadBSIModel command2 = new LoadBSIModel();
			command2 = getCommandService().executeCommand(command2);
			BSIModel model = command2.getModel();
			model.setDbVersion(getVersion());
			SaveElement<BSIModel> command4 = new SaveElement<BSIModel>(model);
			command4 = getCommandService().executeCommand(command4);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}
}
