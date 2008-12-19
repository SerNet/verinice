package sernet.gs.ui.rcp.main.common.model.migration;

import sernet.gs.ui.rcp.main.common.model.IProgress;

public abstract class DbMigration {
	public abstract double getVersion();
	public abstract void run(IProgress progress) throws Exception;
}
