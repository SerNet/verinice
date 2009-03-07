package sernet.gs.ui.rcp.main.service.migrationcommands;

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

/**
 * Version check.
 * 
 * Compares client version to server and to compatible DB version.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DbVersion extends GenericCommand  {
	
	private double clientVersion;

	public DbVersion(double clientVersion) {
		this.clientVersion = clientVersion;
	}

	/**
	 * Version number of DB that can be used:
	 */
	public static final double COMPATIBLE_DB_VERSION = 0.95D;
	
	/**
	 * Version number of client that can be used.
	 * (Must be the same in client / server code of this class.)
	 * 
	 * This value is submitted by the client on every first connect in the
	 * instance variable <code>clientVersion</code>. If this value differs from
	 * the static field, the server throws an exception to prevent incompatible clients
	 * from connecting.
	 * 
	 * If verinice runs standalone (just on a client without server), the version 
	 * number will always be the same.
	 */
	public static final double COMPATIBLE_CLIENT_VERSION = 0.95D;

	/**
	 * Update DB version to compatible DB version.
	 * @param dbVersion
	 * @throws CommandException
	 */
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
			 
			 if (dbVersion < 0.95D) {
				 DbMigration migration = new MigrateDbTo0_95();
				 getCommandService().executeCommand(migration);
			 }
	}


	public void execute() {
		if (clientVersion != COMPATIBLE_CLIENT_VERSION ) {
			throw new RuntimeCommandException("Inkompatible Client Version. " +
					"Server akzeptiert nur V " + COMPATIBLE_CLIENT_VERSION
					+ ". Vorhandene Client Version: " + clientVersion);
		}
		
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
