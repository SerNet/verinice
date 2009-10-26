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
package sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * Remove a control instance from a threat instance.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RemoveMassnahmeFromGefaherdung extends GenericCommand {

	private RisikoMassnahmenUmsetzung child;
	private GefaehrdungsUmsetzung parent;

	/**
	 * @param parent
	 * @param child
	 */
	public RemoveMassnahmeFromGefaherdung(GefaehrdungsUmsetzung parent,
			RisikoMassnahmenUmsetzung child) {
		this.child = child;
		this.parent = parent;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		IBaseDao<Object, Serializable> childDao = getDaoFactory().getDAOForObject(child);
		childDao.reload(child, child.getDbId());
		getDaoFactory().getDAOForObject(parent).reload(parent, parent.getDbId());
		
		child.remove();
		childDao.delete(child);
		child=null;
	}

	public GefaehrdungsUmsetzung getParent() {
		return parent;
	}

}
