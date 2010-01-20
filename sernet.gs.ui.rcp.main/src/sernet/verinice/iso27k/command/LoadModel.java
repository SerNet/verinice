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
package sernet.verinice.iso27k.command;

import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.INoAccessControl;
import sernet.verinice.iso27k.model.ISO27KModel;

@SuppressWarnings("serial")
public class LoadModel extends GenericCommand implements INoAccessControl {

	private ISO27KModel model;

	public LoadModel() {
	}
	
	public void execute() {
		RetrieveInfo ri = new RetrieveInfo();
		//ri.setChildren(true);
		List<ISO27KModel> modelList = getDaoFactory().getDAO(ISO27KModel.class).findAll(ri);
		if(modelList != null) {
			if(modelList.size()>1) {
				throw new RuntimeException("More than one ISO27KModel found.");
			} else if(modelList.size()==1) {			
				model = modelList.get(0);
			}
		}
	}


	public ISO27KModel getModel() {
		return model;
	}
	
	

}
