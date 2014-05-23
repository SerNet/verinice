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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

public class UpdateElement<T extends ITypedElement> extends ChangeLoggingCommand implements IChangeLoggingCommand {

	private T element;
	private boolean fireupdates;
	private boolean logChanges = true;
	private String stationId;

	public UpdateElement(T element, boolean fireUpdates, String stationId) {
		this.element = element;
		this.fireupdates = fireUpdates;
		this.stationId = stationId;
	}

	public void execute() {
		IBaseDao dao =  getDaoFactory().getDAOforTypedElement(element);
		element = (T) dao.merge(element, fireupdates);
	}

	public T getElement() {
		return element;
	}

	public void setLogChanges(boolean logChanges) {
        this.logChanges = logChanges;
    }

    public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_UPDATE;
	}


	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		if (element instanceof CnATreeElement && logChanges) {
			ArrayList<CnATreeElement> list = new ArrayList<CnATreeElement>(1);
			list.add((CnATreeElement) element);
			return list;
		}
		return Collections.emptyList();
	}

}
