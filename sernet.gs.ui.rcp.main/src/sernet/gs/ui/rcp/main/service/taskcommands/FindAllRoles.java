/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadGenericElementByType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Retrieves all the roles that have been used in the database.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
@SuppressWarnings("serial")
public class FindAllRoles extends GenericCommand {

	private Set<String> roles = new HashSet<String>();
	private boolean withUserRoles;

	public FindAllRoles(boolean withUserRoles) {
		this.withUserRoles = withUserRoles;
	}

	public void execute() {
		LoadGenericElementByType<Configuration> lc = new LoadGenericElementByType<Configuration>(Configuration.class);

		try {
			lc = getCommandService().executeCommand(lc);
		} catch (CommandException e) {
			throw new RuntimeException("Exception while retrieving configuration elements.", e);
		}

		List<Configuration> confs = lc.getElements();
		for (Configuration c : confs) {
			roles.addAll(c.getRoles(withUserRoles));
		}

	}

	public Set<String> getRoles() {
		return roles;
	}

}
