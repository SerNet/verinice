/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.bsi.model.SubtypenZielobjekte;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateMultipleElements;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BausteinVorschlag;
import sernet.verinice.model.common.ChangeLogEntry;

/**
 * Insert subtype-module mapping tables into DB.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class MigrateDbTo0_95 extends DbMigration {

	@Override
	public double getVersion() {
		return 0.95D;
	}

	public void execute() throws RuntimeCommandException {
		Logger.getLogger(this.getClass()).debug("Updating DB to V 0.95. ");
		Logger.getLogger(this.getClass()).debug("Inserting: Bausteinvorschläge");
		
		SubtypenZielobjekte mapping = new SubtypenZielobjekte();
		List<BausteinVorschlag> list = mapping.getMapping();
		UpdateMultipleElements<BausteinVorschlag> command 
			= new UpdateMultipleElements<BausteinVorschlag>(list, ChangeLogEntry.STATION_ID);
		try {
			command = getCommandService().executeCommand(command);
			super.updateVersion();
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}


}
