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
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class UpdateMultipleElementEntities extends ChangeLoggingCommand implements IChangeLoggingCommand {

    private transient Logger log = Logger.getLogger(UpdateMultipleElementEntities.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(UpdateMultipleElementEntities.class);
        }
        return log;
    }
    
	private List<CnATreeElement> elements;
	private String stationId;
	private int changeType;

	public UpdateMultipleElementEntities(List<CnATreeElement> elements) {
		this.elements = elements;
		this.stationId = ChangeLogEntry.STATION_ID;
	}

	
	/* (non-Javadoc)
	 * @see sernet.verinice.interfaces.ICommand#execute()
	 */
	public void execute() {
	    for (CnATreeElement element : elements) {
	        UpdateElementEntity<? extends CnATreeElement> command = new UpdateElementEntity<CnATreeElement>(element, stationId);
	        try {
                command = getCommandService().executeCommand(command);
                element = command.getElement();
            } catch (CommandException e) {
                getLog().error("Error while updating element entity", e);
            }
        }
	    
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
