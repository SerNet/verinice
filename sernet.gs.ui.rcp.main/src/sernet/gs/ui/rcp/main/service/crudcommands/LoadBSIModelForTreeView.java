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
package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class LoadBSIModelForTreeView extends GenericCommand {

	private BSIModel model;

	public LoadBSIModelForTreeView() {
	}
	
	public void execute() {
		LoadBSIModel command = new LoadBSIModel();
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		model = command.getModel();
		hydrate(model);
	}

	private void hydrate(CnATreeElement element) {
		if (element == null)
			return;
		
		HydratorUtil.hydrateElement(getDaoFactory().getDAOForObject(element), 
				element, false);
		
//		Set<CnATreeElement> children = element.getChildren();
//		for (CnATreeElement child : children) {
//			if ((!includingMassnahmen) && child instanceof BausteinUmsetzung) {
//				// next element:
//				continue;
//			}
//			
//			hydrate(child);
//		}
	}

	public BSIModel getModel() {
		return model;
	}
	
	
	
	

}
