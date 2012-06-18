/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster[at]tarent[dot]de>.
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
 *     Robert Schuster <r.schuster[at]tarent[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.sync;

import java.util.List;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.service.commands.SyncCommand;
import de.sernet.sync.sync.SyncResponse;
import de.sernet.sync.sync_service.SyncService;

@WebService(targetNamespace = "http://www.sernet.de/sync/sync-service", name = "sync-service")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class SyncServiceImpl implements SyncService {

    private static final Logger LOG = Logger.getLogger(SyncServiceImpl.class);

    private ICommandService commandService;

    @Override
    public de.sernet.sync.sync.SyncResponse sync(de.sernet.sync.sync.SyncRequest request) {
        SyncResponse response = new SyncResponse();
        List<String> errors = response.getReplyMessage();

        SyncCommand command = null;
        try {
            command = commandService.executeCommand(new SyncCommand(request));
            errors.addAll(command.getErrors());
            response.setDeleted(command.getDeleted());
            response.setInserted(command.getInserted());
            response.setUpdated(command.getUpdated());
        } catch (CommandException ce) {
            LOG.error("Error while executing command: SyncCommand", ce);
            errors.add(ce.getLocalizedMessage());
        }

        return response;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

}
