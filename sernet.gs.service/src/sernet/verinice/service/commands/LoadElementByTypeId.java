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
package sernet.verinice.service.commands;

import java.util.List;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class LoadElementByTypeId extends GenericCommand {

    private String typeId;
    private RetrieveInfo ri;
	private List<? extends CnATreeElement> elementList;

	public LoadElementByTypeId(String typeId) {
	    super();
	    this.typeId = typeId;
	}
	
	public LoadElementByTypeId(String typeId, RetrieveInfo ri) {
        super();
        this.typeId = typeId;
        this.ri = ri;
    }

    public void execute() {
        if(ri==null) {
            RetrieveInfo ri = new RetrieveInfo();
        }
		elementList = getDaoFactory().getDAO(typeId).findAll(ri);		
	}

	public List<? extends CnATreeElement> getElementList() {
		return elementList;
	}
	
}
