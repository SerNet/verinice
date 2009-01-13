package sernet.gs.ui.rcp.main.service;

import java.util.List;

import org.eclipse.help.ui.internal.views.SeeAlsoPart;


import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.ICommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;

/**
 * Service to execute commands. 
 * 
 * The command's state after execution is returned by this method.
 * 
 * This is important if results in the command are expected by the executing code:
 * when remotely executed, the results will only be present in the returned command,
 * not in the parameter!
 * 
 * For local execution both reference the same object, but to keep the transparency between
 * local and remote execution you are required to use the returned object for further processing. 
 * 
 * @see ICommand
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface ICommandService {
	public  <T extends ICommand> T executeCommand(T command) throws CommandException;
	
}
