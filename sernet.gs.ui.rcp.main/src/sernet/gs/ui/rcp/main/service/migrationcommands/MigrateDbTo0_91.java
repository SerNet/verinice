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

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.SonstigeITKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadBSIModel;

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
