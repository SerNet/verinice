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
package sernet.verinice.iso27k.service.commands;

import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.connect.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.commands.INoAccessControl;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;

@SuppressWarnings("serial")
public class LoadElementByClass extends GenericCommand implements INoAccessControl {

    
    private String typeId;
	private List<? extends CnATreeElement> elementList;

	public LoadElementByClass(String typeId) {
	    this.typeId = typeId;
	}
	
	public void execute() {
		RetrieveInfo ri = new RetrieveInfo();
		//ri.setProperties(true).setChildren(true).setChildrenProperties(true).setGrandchildren(true);
		elementList = getDaoFactory().getDAO(typeId).findAll(ri);		
	}


	public List<? extends CnATreeElement> getElementList() {
		return elementList;
	}
	
	

}
