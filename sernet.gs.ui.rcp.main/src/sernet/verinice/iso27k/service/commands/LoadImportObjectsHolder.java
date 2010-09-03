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

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.commands.INoAccessControl;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.ImportedObjectsHolder;

@SuppressWarnings("serial")
public class LoadImportObjectsHolder extends GenericCommand implements INoAccessControl {

	private ImportedObjectsHolder holder;

	public LoadImportObjectsHolder() {
	}
	
	public void execute() {
		RetrieveInfo ri = new RetrieveInfo();
		List<ImportedObjectsHolder> resultList = getDaoFactory().getDAO(ImportedObjectsHolder.TYPE_ID).findAll(ri);
		if(resultList != null) {
			if(resultList.size()>1) {
				throw new RuntimeException("More than one ImportedObjectsHolder found.");
			} else if(resultList.size()==1) {			
			    holder = resultList.get(0);
			}
		}
	}


	public ImportedObjectsHolder getHolder() {
		return holder;
	}
	
	

}
