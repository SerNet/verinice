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

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class UpdateMultipleElementEntities extends GenericCommand implements IChangeLoggingCommand {

    private static final Logger logger = Logger.getLogger(UpdateMultipleElementEntities.class);

    private List<CnATreeElement> elements;
    private List<CnATreeElement> changedElements;
    private String stationId;
    private int changeType;

    public UpdateMultipleElementEntities(List<CnATreeElement> elements) {
        this.elements = elements;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        List<CnATreeElement> processed = new ArrayList<>(elements.size());
        for (final CnATreeElement element : elements) {
            @SuppressWarnings("rawtypes")
            IBaseDao dao = getDaoFactory().getDAO(element.getTypeId());
            UpdateElementEntity<? extends CnATreeElement> command = new UpdateElementEntity<>(
                    element, stationId);
            try {
                command = getCommandService().executeCommand(command);
                dao.flush();
                dao.clear();
                processed.addAll(command.getChangedElements());
            } catch (CommandException e) {
                logger.error("Error while updating element entity", e);
            }
        }
        this.changedElements = Collections.unmodifiableList(processed);
    }

    /*
     * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#
     * getChangeType()
     */
    @Override
    public int getChangeType() {
        return this.changeType;
    }

    /*
     * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#
     * getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /*
     * @see sernet.gs.ui.rcp.main.service.commands.IClientNotifyingCommand#
     * getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

}
