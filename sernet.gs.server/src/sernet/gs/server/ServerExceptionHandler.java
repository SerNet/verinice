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
package sernet.gs.server;

import org.springframework.security.SpringSecurityException;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.BaseExceptionHandler;

/**
 * Translate certain exceptions before returning them to the client, i.e. to prevent
 * unknown class exceptions for exceptions that are not known to the client. (Packages only available
 * on the server).
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ServerExceptionHandler extends BaseExceptionHandler {

	@Override
	public void handle(Exception e) throws CommandException {
		// logging is done in HibernateCommandService
		if (e instanceof SpringSecurityException) {
			throw new CommandException("Sicherheitsverstoß.", new Exception("Sicherheitsüberprüfung fehlgeschlagen. Prüfen Sie, ob Benutzername und Passwort " +
					"korrekt sind und Sie über die nötige Berechtigung für die Operation verfügen. Details: " + e.getMessage()));
		}
		if (e instanceof sernet.gs.service.SecurityException) {
			throw new CommandException("Sicherheitsverstoß.", new sernet.gs.service.SecurityException("Sicherheitsüberprüfung fehlgeschlagen. Prüfen Sie, ob Sie über die nötige Berechtigung für die Operation verfügen."));
		}
		super.handle(e);
	}

}
