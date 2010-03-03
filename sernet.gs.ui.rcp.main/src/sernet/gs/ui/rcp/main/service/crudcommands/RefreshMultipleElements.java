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

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class RefreshMultipleElements<T> extends GenericCommand {

	private List<T> elements;

	public RefreshMultipleElements(List<T> elements) {
		this.elements = elements;
	}
	
	public void execute() {
		if (elements != null && elements.size()>0) {
			IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory()
				.getDAO(elements.get(0).getClass());
			for (T element : elements) {
				dao.refresh(element);
			}
		}
	}

}
