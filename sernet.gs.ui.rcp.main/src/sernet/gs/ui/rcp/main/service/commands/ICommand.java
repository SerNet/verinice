package sernet.gs.ui.rcp.main.service.commands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.main.service.ICommandService;

/**
 * Interface for data access commands that can be executed by a local or remote CommandService. 
 * 
 * Commands use a DAO factory provided by the service to access the database. 
 * The DAO factory must be provided before the service executes the command.
 * 
 * It is possible for a service to hand over execution and setting of DAO factory to another service,
 * i.e. refer the command to be executed on a remote location.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface ICommand extends Serializable 
{
	/**
	 * The commands main operation.
	 */
	public void execute();

	/**
	 * The executing service will be injected here for commands that need to be aware of it, i.e. to
	 * execute other commands in their execute method.
	 * 
	 * Must always be set by the command service before execution.
	 * 
	 * This can be used to chain commands or to build composite commands.
	 * 
	 * @param service the command service executing this command
	 */
	public void setCommandService(ICommandService service);
	
	/**
	 * The DAO factory that can be used by the command to access the backend database.
	 * Will be injected by the command service before execution.
	 * 
	 * Must be set by the command service executing this command.
	 * 
	 * @param daoFactory
	 */
	public void setDaoFactory(DAOFactory daoFactory);
}
