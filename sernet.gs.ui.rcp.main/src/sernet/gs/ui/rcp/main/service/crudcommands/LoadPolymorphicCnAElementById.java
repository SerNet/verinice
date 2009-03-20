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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadPolymorphicCnAElementById extends GenericCommand {


	public LoadPolymorphicCnAElementById(Integer[] ds) {
		IDs = ds;
	}

	private Integer[] IDs;

	private List<CnATreeElement> list = new ArrayList<CnATreeElement>();
	
	private static final String QUERY = "from CnATreeElement elmt " +
		"where elmt.dbId  = ? "; 

	

	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
		
		List<CnATreeElement> loaded = new ArrayList<CnATreeElement>(IDs.length);
		for (Integer id : IDs) {
			list = dao.findByQuery(QUERY, new Integer[] {id});
			if (list != null)  {
				loaded.addAll(list);
			}
				
		}
		list = loaded;
		HydratorUtil.hydrateElements(dao, loaded, false);
	}

	public List<CnATreeElement> getElements() {
		return list;
	}


}
