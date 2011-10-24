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
import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;

/**
 * Loads all elements for a given entity type id and hydrates their titles so they can be displayed in a list.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadCnAElementByEntityTypeId extends GenericCommand {

	private String id;

	private List<CnATreeElement> list = new ArrayList<CnATreeElement>();
	
	private static final String QUERY = "from CnATreeElement elmt " +
		"where elmt.entity.entityType = ?"; 

	public LoadCnAElementByEntityTypeId( String id) {
		this.id = id;
	}

	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
		list = dao.findByQuery(QUERY, new Object[] {id});
		
		// hydrate titles and abbreviations
		for (CnATreeElement elmt : list) {
		    elmt.getTitle();
		    if (elmt instanceof IISO27kElement) {
		        ((IISO27kElement)elmt).getAbbreviation();
		    }
		}
	}

	public List<CnATreeElement> getElements() {
		return list;
	}


}
