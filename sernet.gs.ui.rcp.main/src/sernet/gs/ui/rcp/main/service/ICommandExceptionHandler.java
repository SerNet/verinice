package sernet.gs.ui.rcp.main.service;

import sernet.gs.ui.rcp.main.service.commands.CommandException;

public interface ICommandExceptionHandler {

	void handle(Exception e) throws CommandException;
	
}
