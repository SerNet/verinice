package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.DAOFactory;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

/**
 * Load configuration items for person, or global configuration if person is null.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class LoadConfiguration extends GenericCommand {

	private Person person;
	private Configuration configuration;
	
	private static final String QUERY = "from Configuration as conf " +
			"join fetch conf.person as p where p.uuid = ?";

	private static final String QUERY_NULL = "from Configuration as conf where conf.person is null";

	public LoadConfiguration(Person elmt) {
		this.person = elmt;
	}

	public void execute() {
		IBaseDao<Configuration, Serializable> dao = getDaoFactory().getDAO(Configuration.class);
		List queryResult;
		if (person == null) {
			queryResult = dao.findByQuery(QUERY_NULL, new Object[] {});
		} else
			queryResult = dao.findByQuery(QUERY, new Object[] {person.getUuid()});
		
		if (queryResult != null && queryResult.size()>0) {
			configuration = (Configuration) queryResult.get(0);
			HydratorUtil.hydrateElement(dao, configuration, false);
		}
		
	}

	public Person getPerson() {
		return person;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

}
