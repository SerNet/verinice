/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Load configuration items for person, or global configuration if person is null.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class LoadConfiguration extends GenericCommand {

	private CnATreeElement person;
	private Configuration configuration;
	
	private static final String QUERY = "from Configuration as conf " +
			"join fetch conf.person as p where p.uuid = ?";

	private static final String QUERY_NULL = "from Configuration as conf where conf.person is null";
	
	private boolean hydrateElement;

	public LoadConfiguration(CnATreeElement elmt) {
		this(elmt, true);
	}
	
	public LoadConfiguration(CnATreeElement elmt, boolean hydrateElement) {
		this.person = elmt;
		this.hydrateElement = hydrateElement;
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		IBaseDao<Configuration, Serializable> dao = getDaoFactory().getDAO(Configuration.class);
		List<Configuration> queryResult;
		if (person == null) {
			queryResult = (List<Configuration>) dao.findByQuery(QUERY_NULL, new Object[] {});
		} else
			queryResult = (List<Configuration>) dao.findByQuery(QUERY, new Object[] {person.getUuid()});
		
		if (queryResult != null && queryResult.size()>0) {
			configuration = (Configuration) queryResult.get(0);
			if (hydrateElement)
				HydratorUtil.hydrateElement(dao, configuration, false);
		}
		
	}

	public CnATreeElement getPerson() {
		return person;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

}
