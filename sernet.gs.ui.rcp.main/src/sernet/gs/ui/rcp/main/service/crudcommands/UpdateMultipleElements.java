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

import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class UpdateMultipleElements<T extends ITypedElement> extends GenericCommand implements IChangeLoggingCommand {

	private List<T> elements;
	private String stationId;
	private int changeType;

	public UpdateMultipleElements(List<T> elements, String stationId) {
		this(elements, stationId, ChangeLogEntry.TYPE_UPDATE);
	}

	public UpdateMultipleElements(List<T> elements, String stationId, int changeType) {
		this.elements = elements;
		this.stationId = stationId;
		this.changeType = changeType;
	}
	
	public void execute() {
		ArrayList<T> mergedElements = new ArrayList<T>(elements.size());
		if (elements != null && elements.size()>0) {
			IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory()
				.getDAO(elements.get(0).getTypeId());
			for (T element : elements) {
				T mergedElement = dao.merge(element, true);
				mergedElements.add(mergedElement);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.GenericCommand#clear()
	 */
	@Override
	public void clear() {
		elements = null;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getChangeType()
	 */
	public int getChangeType() {
		return this.changeType;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getStationId()
	 */
	public String getStationId() {
		return stationId;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(elements.size());
		for (Object object : elements) {
			if (object instanceof CnATreeElement) {
				CnATreeElement cnaElement = (CnATreeElement) object;
				result.add(cnaElement);
			}
		}
		return result;
	}

	

}
