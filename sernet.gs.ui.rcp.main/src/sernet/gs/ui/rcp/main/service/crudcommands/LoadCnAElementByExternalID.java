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
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Load elements with matching data source and external ID.
 * Should usually return only one element - if the external ID was really unique. 
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadCnAElementByExternalID extends GenericCommand {

	private String id;

	private List<CnATreeElement> list = new ArrayList<CnATreeElement>();

	private String sourceID;

	private static final String QUERY = "from CnATreeElement elmt " +
		"where elmt.sourceId = ? and elmt.extId = ?"; 
	
	public LoadCnAElementByExternalID( String sourceID, String id) {
		this.id = id;
		this.sourceID = sourceID;
	}

	public void execute() {
		IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);
		list = dao.findByQuery(QUERY, new Object[] {sourceID, id});
	}

	public List<CnATreeElement> getElements() {
		return list;
	}


}
