/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.bpm;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.service.commands.RetrieveCnATreeElement;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.LoadUsername;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProzessExecution {

    private static final Logger LOG = Logger.getLogger(ProzessExecution.class);
    
    private ICommandService commandService;
    
    public String loadAssignee(String uuid, String relationId) {
        ServerInitializer.inheritVeriniceContextState();
        String username = null;
        try {
            LoadUsername command = new LoadUsername(uuid,relationId);
            command = getCommandService().executeCommand(command);
            username = command.getUsername();         
        } catch(CommandException t) {
            LOG.error("Error while loading assignee.", t); //$NON-NLS-1$
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("uuid: " + uuid + ", username: " + username); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return username;
    }
    
    public CnATreeElement loadElement(String typeId, Integer dbId) throws CommandException {
        return loadElement(typeId, dbId, RetrieveInfo.getPropertyInstance());
    }
    
    public CnATreeElement loadElement(String typeId, Integer dbId, RetrieveInfo ri) throws CommandException {
        RetrieveCnATreeElement command = new RetrieveCnATreeElement(typeId, dbId, ri);
        command = getCommandService().executeCommand(command);
        return command.getElement();
    }
    
    public CnATreeElement loadElementByUuid(String uuid) throws CommandException {
        return loadElementByUuid(uuid, RetrieveInfo.getPropertyInstance());
    }
    
    public CnATreeElement loadElementByUuid(String uuid, RetrieveInfo ri) throws CommandException {
        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid, ri);
        command = getCommandService().executeCommand(command);
        return command.getElement();
    }
    
    protected ICommandService getCommandService() {
        if(commandService==null) {
            commandService = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
        }
        return commandService;
    }
    
    protected String setValue(String value) {
        return value;
    }
    
}
