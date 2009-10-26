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
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;

public class MigrateDbTo0_91 extends DbMigration {
	

	public void run() throws Exception {
			Logger.getLogger(this.getClass()).debug("Updating DB model to V 0.91.");
			LoadBSIModel command = new LoadBSIModel();
			command = getCommandService().executeCommand(command);
			BSIModel model = command.getModel();
			
			ITVerbund verbund = model.getItverbuende().iterator().next();
			for (CnATreeElement child : verbund.getChildren()) {
				if (child instanceof SonstigeITKategorie)
					return;

			}
			SonstigeITKategorie kategorie = new SonstigeITKategorie(verbund);
			verbund.addChild(kategorie);
			
			
			SaveElement<SonstigeITKategorie> command2 = new SaveElement<SonstigeITKategorie>(kategorie);
			command2 = getCommandService().executeCommand(command2);
			
			model.setDbVersion(getVersion());
			SaveElement<BSIModel> command3 = new SaveElement<BSIModel>(model);
			command3 = getCommandService().executeCommand(command3);
			
	}

	@Override
	public double getVersion() {
		return 0.91D;
	}

	public void execute() {
		try {
			run();
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}


}
