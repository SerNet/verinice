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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Save element of type T to the database using its class to lookup
 * the DAO from the factory.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 * @param <T>
 */
public class SaveElement<T extends ITypedElement> extends ChangeLoggingCommand implements IChangeLoggingCommand {

	protected T element;
	protected String stationId;
	private boolean logChanges = true;
	protected SaveElement() {
	 
	}
	
	public SaveElement(T element) {
		this.element = element;
		this.stationId = ChangeLogEntry.STATION_ID;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getTypeId());
		//dao.saveOrUpdate(element);
		element = dao.merge(element);
	}

	public T getElement() {
		return element;
	}
	
	public void setLogChanges(boolean logChanges) {
        this.logChanges = logChanges;
    }

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_INSERT;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		ArrayList<CnATreeElement> list = new ArrayList<CnATreeElement>(1);
		if (element instanceof CnATreeElement && logChanges) {
			list.add((CnATreeElement) element);
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId()
	 */
	
	public String getStationId() {
		return stationId;
	}
	
	public void clear() {
		
	}

}
