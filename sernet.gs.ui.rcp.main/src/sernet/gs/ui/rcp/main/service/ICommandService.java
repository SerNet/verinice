package sernet.gs.ui.rcp.main.service;

import java.util.List;


import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.ICommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;

public interface ICommandService {
	public ICommand executeCommand(ICommand command) throws CommandException;
}
