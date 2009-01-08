package sernet.gs.ui.rcp.main.service.migrationcommands;

import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public abstract class DbMigration extends GenericCommand {
	public abstract double getVersion();
}
