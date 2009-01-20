package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class CreateConfiguration extends GenericCommand {

	private Person person;
	private Configuration configuration;

	public CreateConfiguration(Person elmt) {
		this.person = elmt;
	}

	public void execute() {
		configuration = new Configuration();
		if (person != null)
			configuration.setPerson(person);
		getDaoFactory().getDAO(Configuration.class).saveOrUpdate(configuration);
	}

	public Configuration getConfiguration() {
		return configuration;
	}
	
	

}
