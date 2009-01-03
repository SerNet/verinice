package sernet.gs.ui.rcp.main.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.ICommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;

/**
 * Command service that uses hibernate DAOs to access the database. 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class HibernateCommandService implements ICommandService {
	
	// injected by spring
	private DAOFactory daoFactory;
	
	private boolean dbOpen = false;
	
	public ICommand executeCommand(ICommand command) throws CommandException {
		if (!dbOpen)
			throw new CommandException("DB connection closed.");
		
		Logger.getLogger(this.getClass()).debug("Executing command: " + command.getClass().getSimpleName());
		try {
			command.setDaoFactory(daoFactory);
			command.setCommandService(this);
			command.execute();
		} catch (Exception e) {
			throw new CommandException("Lokaler Ausführungsfehler.", e);
		}
		return command;
	}

	/**
	 * Injected by spring framework
	 * 
	 * @param daoFactory
	 */
	public void setDaoFactory(DAOFactory daoFactory) {
		// FIXME allow application to set db open or closed after it has reinitialized spring config
		dbOpen = true;
		this.daoFactory = daoFactory;
	}
}
