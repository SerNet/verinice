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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.orm.hibernate3.SpringSessionContext;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.gs.ui.rcp.main.service.commands.ICommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;

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
//				"Hibernate service executing command: "
//						+ command.getClass().getSimpleName());
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
			
			// clean up:
			command.clear();
		} 
		catch (Exception e) {
			if (exceptionHandler != null)
				exceptionHandler.handle(e);
		}
		return command;
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
