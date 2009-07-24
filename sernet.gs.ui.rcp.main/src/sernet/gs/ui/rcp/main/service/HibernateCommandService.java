/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service;

import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand;
import sernet.gs.ui.rcp.main.service.commands.ICommand;

/**
 * Command service that executes commands using hibernate DAOs to access the
 * database.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HibernateCommandService implements ICommandService {

	// injected by spring
	private DAOFactory daoFactory;
	
	private ICommandExceptionHandler exceptionHandler;
	
	private IAuthService authService;
	
	private boolean dbOpen = false;

	/**
	 * This method is encapsulated in a transaction by the Spring container.
	 * Hibernate session will be opened before this method executes the given
	 * command and closed afterwards.
	 * 
	 * Database access in a single transaction is thereby enabled for the
	 * command, the necessary data access objects can be requested from the
	 * given DAO factory.
	 * 
	 * A command can execute other commands to fulfill its purpose using the
	 * reference to the command service.
	 */
	public <T extends ICommand> T executeCommand(T command) throws CommandException {
		if (!dbOpen)
			throw new CommandException("DB connection closed.");

//		Logger.getLogger(this.getClass()).debug(
//				"Service executing command: "
//						+ command.getClass().getSimpleName() 
//				+ " / user: " + getAuthService().getUsername());
		
		Logger.getLogger(this.getClass()).debug(
				"Service executing command: "
				+ command.getClass().getSimpleName()); 
		
		try {
			// inject service and database access:
			command.setDaoFactory(daoFactory);
			command.setCommandService(this);

			// inject authentication service if command is aware of it:
			if (command instanceof IAuthAwareCommand) {
				IAuthAwareCommand authCommand = (IAuthAwareCommand) command;
				authCommand.setAuthService(authService);
			}
			
			// execute actions, compute results:
			command.execute();
			
			// log changes:
			if (command instanceof IChangeLoggingCommand) {
				log((IChangeLoggingCommand) command);
			}			
			
			// clean up:
			command.clear();
		} 
		catch (Exception e) {
			if (exceptionHandler != null)
				exceptionHandler.handle(e);
		}
		return command;
	}

	private void log(IChangeLoggingCommand notifyCommand) {
		List<CnATreeElement> changedElements = notifyCommand.getChangedElements();
		for (CnATreeElement changedElement : changedElements) {
			
			// save reference to element, if it has not been deleted:
			CnATreeElement referencedElement = null;
			if (notifyCommand.getChangeType() != ChangeLogEntry.TYPE_DELETE)
				referencedElement = changedElement;
				
			ChangeLogEntry logEntry = new ChangeLogEntry(changedElement,
					notifyCommand.getChangeType(),
					getAuthService().getUsername(),
					notifyCommand.getStationId(),
					GregorianCalendar.getInstance().getTime());
			log(logEntry, referencedElement);
		}
	}

	/**
	 * @param logEntry
	 */
	private void log(ChangeLogEntry logEntry, CnATreeElement referencedElement) {
		Logger.getLogger(this.getClass()).debug("Logging change type '" + logEntry.getChangeDescription() 
				+ "' for element of type " + logEntry.getElementClass() + " with ID " + logEntry.getElementId());
		daoFactory.getDAO(ChangeLogEntry.class).saveOrUpdate(logEntry);
	}

	/**
	 * Injected by spring framework
	 * 
	 * @param daoFactory
	 */
	public void setDaoFactory(DAOFactory daoFactory) {
		dbOpen = true;
		this.daoFactory = daoFactory;
	}

	public ICommandExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	public void setExceptionHandler(ICommandExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	public IAuthService getAuthService() {
		return authService;
	}

	public void setAuthService(IAuthService authService) {
		this.authService = authService;
	}

	
}
