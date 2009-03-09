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
