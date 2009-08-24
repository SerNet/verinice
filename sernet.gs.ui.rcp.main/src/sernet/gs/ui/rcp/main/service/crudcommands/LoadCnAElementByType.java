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

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

@SuppressWarnings("serial")
public class LoadCnAElementByType<T extends CnATreeElement> extends GenericCommand {

	private List<T> elements;
	private Class<T> clazz;
	
	private boolean hydrateElements;
	
	public LoadCnAElementByType(Class<T> type) {
		this(type, true);
	}
	
	public LoadCnAElementByType(Class<T> type, boolean hydrateElements) {
		this.clazz = type;
		this.hydrateElements = hydrateElements;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao = getDaoFactory().getDAO(clazz);
		elements = dao.findAll();
		if (hydrateElements)
			HydratorUtil.hydrateElements(dao, elements, false);
	}

	public List<T> getElements() {
		return elements;
	}
	
}
