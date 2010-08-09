package sernet.gs.server.sync;

import java.util.List;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import sernet.gs.ui.rcp.main.sync.commands.SyncCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import de.sernet.sync.sync.SyncResponse;
import de.sernet.sync.sync_service.SyncService;
 
@WebService(targetNamespace = "http://www.sernet.de/sync/sync-service", name = "sync-service")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public class SyncServiceImpl implements SyncService {
	
	private ICommandService commandService;
	
    @Override
    public de.sernet.sync.sync.SyncResponse sync(de.sernet.sync.sync.SyncRequest request)
    {
    	SyncResponse response = new SyncResponse();
    	List<String> errors = response.getReplyMessage();
    	
    	SyncCommand command = null;
    	try
    	{
    		command = commandService.executeCommand(new SyncCommand(request));
        	errors.addAll(command.getErrors());
    	} catch (CommandException ce)
    	{
    		errors.add(ce.getLocalizedMessage());
    	}
    	
    	response.setDeleted(command.getDeleted());
    	response.setInserted(command.getInserted());
    	response.setUpdated(command.getUpdated());
    	
    	return response;
    }

	public void setCommandService(ICommandService commandService) {
		this.commandService = commandService;
	}

}
