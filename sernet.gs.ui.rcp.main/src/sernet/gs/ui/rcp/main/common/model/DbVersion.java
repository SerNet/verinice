package sernet.gs.ui.rcp.main.common.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.migrationcommands.DbMigration;
import sernet.gs.ui.rcp.main.service.migrationcommands.MigrateDbTo0_91;
import sernet.gs.ui.rcp.main.service.migrationcommands.MigrateDbTo0_92;
import sernet.gs.ui.rcp.main.service.migrationcommands.MigrateDbTo0_93;
import sernet.gs.ui.rcp.main.service.migrationcommands.MigrateDbTo0_94;

public class DbVersion extends GenericCommand  {

	
	public static final double CURRENT_DB_VERSION = 0.94D;

	public void updateDBVersion(double dbVersion) throws CommandException {
			if (dbVersion < 0.91D) {
				DbMigration migration = new MigrateDbTo0_91();
				getCommandService().executeCommand(migration);
			}

			 if (dbVersion < 0.92D) {
				 DbMigration migration = new MigrateDbTo0_92();
				 getCommandService().executeCommand(migration);
			 }
			 
			 if (dbVersion < 0.93D) {
				 DbMigration migration = new MigrateDbTo0_93();
				 getCommandService().executeCommand(migration);
			 }
			 
			 if (dbVersion < 0.94D) {
				 DbMigration migration = new MigrateDbTo0_94();
				 getCommandService().executeCommand(migration);
			 }
	}


	public void execute() {
		LoadBSIModel command = new LoadBSIModel();
		try {
			command = getCommandService().executeCommand(command);
			if (command.getModel() == null)
				return;
			
			double dbVersion = command.getModel().getDbVersion();
			updateDBVersion(dbVersion);
		} catch (CommandException e) {
			throw new RuntimeCommandException("Fehler beim Migrieren der Datenbank auf aktuelle Version.", e);
		}
		
	}

	

}
