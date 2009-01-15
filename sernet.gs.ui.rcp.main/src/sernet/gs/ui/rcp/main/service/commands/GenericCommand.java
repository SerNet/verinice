package sernet.gs.ui.rcp.main.service.commands;

import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.IResourceCollection;

public abstract class GenericCommand implements ICommand {
	
	private transient DAOFactory daoFactory;
	private transient ICommandService commandService;

	public void setCommandService(ICommandService service) {
		this.commandService = service;
	}
	
	public void setDaoFactory(DAOFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public DAOFactory getDaoFactory() {
		return daoFactory;
	}
	
	public ICommandService getCommandService() {
		return commandService;
	}
	
	

}
