package sernet.gs.server;

import org.springframework.security.SpringSecurityException;

import sernet.gs.ui.rcp.main.service.BaseExceptionHandler;
import sernet.gs.ui.rcp.main.service.ICommandExceptionHandler;
import sernet.gs.ui.rcp.main.service.commands.CommandException;

/**
 * Translate certain exceptions before returning them to the client, i.e. to prevent
 * unknown class exceptions for exceptions that are not known to the client. (Packages only available
 * on the server).
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ServerExceptionHandler extends BaseExceptionHandler {

	@Override
	public void handle(Exception e) throws CommandException {
		if (e instanceof SpringSecurityException) {
			throw new CommandException("Sicherheitsverstoß.", new Exception("Sicherheitsüberprüfung fehlgeschlagen. Prüfen Sie, ob Benutzername und Passwort " +
					"korrekt sind und Sie über die nötige Berechtigung (Rolle) für die Operation verfügen. Details: " + e.getMessage()));
		}
		super.handle(e);
	}

}
