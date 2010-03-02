/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.migrationcommands;

import org.apache.log4j.Logger;

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
	public static final double COMPATIBLE_DB_VERSION = 0.97D;
	
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
	public static final double COMPATIBLE_CLIENT_VERSION = 0.97D;

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

			 if (dbVersion < 0.96D) {
				 // schema update must have been done by SchemaCreator.java, before Hibernate session was started:
				 Logger.getLogger(this.getClass()).debug("Database schema was not correctly updated to V 0.96.");
				 throw new CommandException("Datenbank konnte nicht auf V0.96 upgedated werden.");
			 }
			 
			 if (dbVersion < 0.97D) {
				 DbMigration migration = new MigrateDbTo0_97();
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
		double dbVersion;
		try {
			command = getCommandService().executeCommand(command);
			if (command.getModel() == null) {
					Logger.getLogger(this.getClass()).debug("Not migrating database: could not determine current database version or no database created yet.");
					return;
			}
			// set current db version as determined from database:
			dbVersion = command.getModel().getDbVersion();
			updateDBVersion(dbVersion);
		} catch (CommandException e) {
			throw new RuntimeCommandException("Fehler beim Migrieren der Datenbank auf aktuelle Version.", e);
		}
		
	}

	

}
