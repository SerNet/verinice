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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.Set;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;

/**
 * Loads an element with all links (up and down) for the relation view.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class FindRelationsFor extends GenericCommand {

	private Integer dbId;
	private CnATreeElement elmt;
	private String typeId;

	/**
	 * @param dbId
	 */
	public FindRelationsFor(CnATreeElement elmt) {
		this.dbId = elmt.getDbId();
		this.typeId = elmt.getTypeId();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(typeId);
		RetrieveInfo ri = new RetrieveInfo();
		ri.setLinksDown(true).setLinksUp(true);
		elmt = dao.retrieve(dbId, ri);
		
		if(elmt!=null) {
    		Set<CnALink> linksDown = elmt.getLinksDown();
    		for (CnALink cnALink : linksDown) {
    			HydratorUtil.hydrateElement(dao, cnALink.getDependency(), false);
    			
    		}
    		
    		Set<CnALink> linksUp = elmt.getLinksUp();
    		for (CnALink cnALink : linksUp) {
    			HydratorUtil.hydrateElement(dao, cnALink.getDependant(), false);
    			
    		}
		}
	}

	public CnATreeElement getElmt() {
		return elmt;
	}

}
