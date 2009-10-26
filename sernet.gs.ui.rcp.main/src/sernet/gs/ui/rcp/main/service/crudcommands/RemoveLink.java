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

import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand;

@SuppressWarnings("serial")
public class RemoveLink<T extends CnALink> extends GenericCommand implements IChangeLoggingCommand {

	private T element;
	private String stationId;

	public RemoveLink(T element) {
		this.element = element;
		this.stationId = ChangeLogEntry.STATION_ID;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getClass());
		element = dao.findById(element.getId());
		element.remove();
		dao.delete(element);
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		element = null;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_DELETE;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		// return link category item:
		List<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
		result.add(element.getParent().getParent());
		return result;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId()
	 */
	public String getStationId() {
		return stationId;
	}

}
