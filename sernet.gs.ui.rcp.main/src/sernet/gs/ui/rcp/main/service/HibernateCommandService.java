package sernet.gs.ui.rcp.main.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.ICommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;

public class HibernateCommandService implements ICommandService {
	
	// injected by spring
	private DAOFactory daoFactory;
	
	public ICommand executeCommand(ICommand command) {
		command.setDaoFactory(daoFactory);
		command.setCommandService(this);
		command.execute();
		return command;
	}

	/**
	 * Injected by spring framework
	 * 
	 * @param daoFactory
	 */
	public void setDaoFactory(DAOFactory daoFactory) {
		this.daoFactory = daoFactory;
	}
}
