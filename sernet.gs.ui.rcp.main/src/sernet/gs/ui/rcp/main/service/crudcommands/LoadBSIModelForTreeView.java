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
package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.INoAccessControl;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.service.commands.LoadBSIModel;

@SuppressWarnings("serial")
public class LoadBSIModelForTreeView extends GenericCommand implements INoAccessControl {

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
		
		HydratorUtil.hydrateElement(getDaoFactory().getDAOforTypedElement(element), element, true);
		
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
