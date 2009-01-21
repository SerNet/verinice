package sernet.gs.ui.rcp.main.service;

import sernet.gs.ui.rcp.main.service.commands.CommandException;

public class BaseExceptionHandler implements ICommandExceptionHandler {

	public void handle(Exception e) throws CommandException {
		throw new CommandException(
				"Ausführungsfehler in DB-Service-Layer", e);
	}

}
