/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman.
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
 *     Alexander Koderman - Initial API and implementation
 *     Daniel Murygin - Refactoring
 ******************************************************************************/
package sernet.gs.server;

import org.springframework.security.SpringSecurityException;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.UsernameExistsException;
import sernet.verinice.interfaces.ldap.SizeLimitExceededException;
import sernet.verinice.service.ICommandExceptionHandler;
import sernet.verinice.service.commands.UsernameExistsRuntimeException;
import sernet.verinice.service.commands.unify.UnifyValidationException;
import sernet.verinice.service.sync.VnaSchemaException;

/**
 * This class handles exceptions on the server.
 * 
 * The class is configured as an exception handler for the HibernateCommandService in
 * the spring configuration file 'veriniceserver-common.xml'.
 * 
 * @author Alexander Koderman
 * @author Daniel Murygin
 */
public class ServerExceptionHandler implements ICommandExceptionHandler {

    @Override
    public void handle(Exception e) throws CommandException {
        if (e instanceof SpringSecurityException) {
            throw new CommandException("Security violation",
                    new Exception(
                            "Security check failed. Check user name, password and necessary authorizations for the operation. Details: "
                                    + e.getMessage()));
        }
        if (e instanceof sernet.gs.service.SecurityException) {
            throw new CommandException("Security violation",
                    new sernet.gs.service.SecurityException(
                            "Security check failed. Check necessary authorizations for the operation"));
        }
        rethrowKnownException(e);
    }

    private void rethrowKnownException(Exception e) throws CommandException {
        if (e instanceof UsernameExistsRuntimeException) {
            throw new UsernameExistsException(e);
        } else if (e instanceof SizeLimitExceededException) {
            throw (SizeLimitExceededException) e;
        } else if (e instanceof UnifyValidationException) {
            throw (UnifyValidationException) e;
        } else if (e instanceof VnaSchemaException) {
            throw (VnaSchemaException) e;
        } else {
            wrapUnknownException(e);
        }
    }

    private void wrapUnknownException(Exception e) throws CommandException {
        throw new CommandException(
                "An error occurred when calling a function on the verinice server or in the backend of the standalone client.",
                e);
    }

}
