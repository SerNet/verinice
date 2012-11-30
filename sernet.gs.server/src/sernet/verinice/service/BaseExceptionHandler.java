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
package sernet.verinice.service;

import sernet.gs.ui.rcp.main.service.commands.UsernameExistsException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ldap.SizeLimitExceededException;
import sernet.verinice.service.commands.UsernameExistsRuntimeException;

public class BaseExceptionHandler implements ICommandExceptionHandler {

	public void handle(Exception e) throws CommandException {
		if(e instanceof UsernameExistsRuntimeException) {
			throw new UsernameExistsException(e);
		} else if(e instanceof SizeLimitExceededException) {
		    throw (SizeLimitExceededException)e;
		} else {
			throw new CommandException("Ausführungsfehler in DB-Service-Layer\n\n", getDetails(e));
		}
	}

	/**
	 * @param e
	 */
	private CommandException getDetails(Exception e) {
		if (e == null) {
			return null;
		}

		Throwable cause;
		if (e.getCause() != null && e.getCause().getMessage() != null) {
			cause = e.getCause();
		} else {
			cause = e;
		}

		CommandException commandException = new CommandException(cause.getClass().getSimpleName() + getMessage(cause));
		commandException.setStackTrace(cause.getStackTrace());
		return commandException;

	}

	/**
	 * @param cause
	 * @return
	 */
	private String getMessage(Throwable cause) {
		return cause.getLocalizedMessage() != null ? ", " + cause.getLocalizedMessage() : "";
	}

}
