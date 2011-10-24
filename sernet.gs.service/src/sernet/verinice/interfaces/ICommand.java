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
package sernet.verinice.interfaces;

import java.io.Serializable;

/**
 * Interface for data access commands that can be executed by a local or remote CommandService. 
 * 
 * Commands use a DAO factory provided by the service to access the database. 
 * The DAO factory must be provided before the service executes the command.
 * 
 * It is possible for a service to hand over execution and setting of DAO factory to another service,
 * i.e. refer the command to be executed on a remote location.
 * 
 * 
 * @see ICommandService
 * 
 * @author koderman[at]sernet[dot]de
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
	public void setDaoFactory(IDAOFactory daoFactory);

	/**
	 * Should be implemented to empty all fields that are no longer needed before
	 * the command object is returned with the result to the client.
	 */
	public void clear();
	
}
