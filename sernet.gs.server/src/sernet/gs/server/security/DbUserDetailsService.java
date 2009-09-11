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
package sernet.gs.server.security;

import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.crudcommands.ILoadUserConfiguration;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;

/**
 * Provides access to user details in the verinice database.
 * These can be created by any admin-user in the verinice frontend itself.
 * 
 * Additionally, one initial user can be configured in applicationContext.xml itself,
 * as a backup administrative account and for initial setting up of the database.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DbUserDetailsService implements UserDetailsService {

	// injected by spring
	private ILoadUserConfiguration loadUserConfigurationCommand;
	
	private VeriniceContext.State workObjects;
	
	// injected by spring
	private String adminuser = "";

	// injected by spring
	private String adminpass = "";

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		VeriniceContext.setState(workObjects);

		if (adminuser.length() > 0 && adminpass.length() > 0
				&& username.equals(adminuser))
			return privilegedUser();

		// This command is created and maintained by Spring. We bypass the command
		// service because we cannot use it at this time since the authentication
		// information is missing yet.
		loadUserConfigurationCommand.execute();
		
		List<Entity> entities = loadUserConfigurationCommand.getEntities();

		for (Entity entity : entities) {
			if (isUser(username, entity)) {
				return nonPrivilegedUser(entity);
			}
		}
		throw new UsernameNotFoundException(Messages
				.getString("DbUserDetailsService.4")); //$NON-NLS-1$
	}

	private UserDetails privilegedUser() {
		VeriniceUserDetails user = new VeriniceUserDetails(adminuser, adminpass);
		user.addRole(ApplicationRoles.ROLE_ADMIN);
		user.addRole(ApplicationRoles.ROLE_USER);
		return user;
	}

	private UserDetails nonPrivilegedUser(Entity entity) {
		VeriniceUserDetails userDetails = new VeriniceUserDetails(entity
				.getSimpleValue(Configuration.PROP_USERNAME), entity
				.getSimpleValue(Configuration.PROP_PASSWORD));
		
		// All non-privileged users have the role "ROLE_USER".
		userDetails.addRole(ApplicationRoles.ROLE_USER);
		
		return userDetails;
	}

	private boolean isUser(String username, Entity entity) {
		return entity.getSimpleValue(Configuration.PROP_USERNAME).equals(
				username);

	}

	public void setAdminuser(String adminuser) {
		this.adminuser = adminuser;
	}

	public void setAdminpass(String adminpass) {
		this.adminpass = adminpass;
	}

	public ILoadUserConfiguration getLoadUserConfigurationCommand() {
		return loadUserConfigurationCommand;
	}

	public void setLoadUserConfigurationCommand(
			ILoadUserConfiguration loadUserConfigurationCommand) {
		this.loadUserConfigurationCommand = loadUserConfigurationCommand;
	}

	public VeriniceContext.State getWorkObjects() {
		return workObjects;
	}

	public void setWorkObjects(VeriniceContext.State workObjects) {
		this.workObjects = workObjects;
	}



}
