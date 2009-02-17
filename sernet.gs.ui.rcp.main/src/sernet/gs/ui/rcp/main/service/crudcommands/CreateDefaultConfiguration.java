package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class CreateDefaultConfiguration extends GenericCommand {

	private Configuration configuration;
	private String pass;
	private String user;

	public CreateDefaultConfiguration(String user, String pass) {
		this.user = user;
		this.pass = pass;
	}

	public void execute() {
		LoadConfiguration command = new LoadConfiguration(null);
		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
		}
		
		if (command.getConfiguration() != null)
			throw new RuntimeCommandException("Das Default Passwort wurde bereits gesetzt!");
		
		configuration = new Configuration();
		configuration.setUser(user);
		configuration.setPass(pass);
		configuration.addRole("ROLE_USER");
		configuration.addRole("ROLE_ADMIN");
		
		getDaoFactory().getDAO(Configuration.class).saveOrUpdate(configuration);
	}

	public Configuration getConfiguration() {
		return configuration;
	}
	
	

}
