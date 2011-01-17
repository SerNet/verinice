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
package sernet.verinice.iso27k.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
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

    private transient Logger log = Logger.getLogger(NaturalizeCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(NaturalizeCommand.class);
        }
        return log;
    }
    
    private Set<String> uuidSet;
    
    private List<CnATreeElement> changedElements = Collections.emptyList();
    
    private transient IBaseDao<CnATreeElement, Serializable> cnaTreeElementDao;

    private String stationId;
    
    /**
     * @param uuidSet
     */
    public NaturalizeCommand(Set<String> uuidSet) {
        this.uuidSet = uuidSet;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
            if(uuidSet!=null && !uuidSet.isEmpty()) {
                changedElements = new ArrayList<CnATreeElement>(uuidSet.size());
                for (String uuid : uuidSet) {
                    CnATreeElement element = getCnaTreeElementDao().findByUuid(uuid, null);
                    element.setSourceId(null);
                    element.setExtId(null);
                    SaveElement<CnATreeElement> command = new SaveElement<CnATreeElement>(element);
                    try {
                        command = getCommandService().executeCommand(command);
                    } catch (CommandException e) {
                        getLog().error("Error while saving element", e);
                        throw new RuntimeException("Error while saving element", e);
                    }
                    changedElements.add(command.getElement());
                }
                
            }
       
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }
    
    protected IBaseDao<CnATreeElement, Serializable> getCnaTreeElementDao() {
        if(cnaTreeElementDao==null) {
            cnaTreeElementDao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return cnaTreeElementDao;
    }

}
