/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
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
