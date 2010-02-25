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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.connect.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class FindRelationsFor extends GenericCommand {

	private Integer dbId;
	private CnATreeElement elmt;
	private Class<? extends CnATreeElement> clazz;

	/**
	 * @param dbId
	 */
	public FindRelationsFor(CnATreeElement elmt) {
		this.dbId = elmt.getDbId();
		this.clazz = elmt.getClass();
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(clazz);
		RetrieveInfo ri = new RetrieveInfo();
		ri.setLinksDown(true).setLinksUp(true).setLinksDownProperties(true).setLinksUpProperties(true);
		elmt = dao.retrieve(dbId, ri);
	}

	public CnATreeElement getElmt() {
		return elmt;
	}

}
