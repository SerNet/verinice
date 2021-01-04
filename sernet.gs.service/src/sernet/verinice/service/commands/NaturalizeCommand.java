/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class NaturalizeCommand extends GenericCommand implements IChangeLoggingCommand {

    private static final Logger log = Logger.getLogger(NaturalizeCommand.class);

    private Set<String> uuidSet;

    private List<CnATreeElement> changedElements = Collections.emptyList();

    private String stationId;

    public NaturalizeCommand(Set<String> uuidSet) {
        this.uuidSet = uuidSet;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /*
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if (uuidSet != null && !uuidSet.isEmpty()) {
            IBaseDao<@NonNull CnATreeElement, Serializable> cnaTreeElementDao = getDaoFactory()
                    .getDAO(CnATreeElement.class);
            changedElements = new ArrayList<>(uuidSet.size());
            Set<@NonNull CnATreeElement> elements = uuidSet.stream()
                    .map(uuid -> cnaTreeElementDao.findByUuid(uuid, null))
                    .collect(Collectors.toSet());
            for (CnATreeElement element : elements) {
                element.setSourceId(null);
                element.setExtId(null);
                SaveElement<CnATreeElement> command = new SaveElement<>(element);
                try {
                    command = getCommandService().executeCommand(command);
                } catch (CommandException e) {
                    log.error("Error while saving element", e);
                    throw new RuntimeException("Error while saving element", e);
                }
                changedElements.add(command.getElement());
            }

        }

    }

    /*
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }

    /*
     * @see
     * sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

    /*
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

}