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
