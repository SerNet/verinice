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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.encoding.ShaPasswordEncoder;
import org.springframework.security.ui.digestauth.DigestProcessingFilter;
import org.springframework.security.ui.digestauth.DigestProcessingFilterEntryPoint;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadEntityByType;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

public class DbUserDetailsService implements UserDetailsService {

	// injected by spring
	private ICommandService commandService;
	private String adminuser = "";
	private String adminpass = "";

	

	private final static Map<String, String[]> roleMap = new HashMap<String, String[]>();

	{
		roleMap.put("configuration_rolle_ciso",
				new String[] { ApplicationRoles.ROLE_USER });
		roleMap.put("configuration_rolle_isbeauftragter",
				new String[] { ApplicationRoles.ROLE_USER });
		roleMap.put("configuration_rolle_user",
				new String[] { ApplicationRoles.ROLE_USER });
		roleMap.put("configuration_rolle_admin", new String[] {
				ApplicationRoles.ROLE_USER, ApplicationRoles.ROLE_ADMIN });
		roleMap.put("configuration_rolle_umsverantw",
				new String[] { ApplicationRoles.ROLE_USER });
		roleMap.put("configuration_rolle_auditor",
				new String[] { ApplicationRoles.ROLE_USER });
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {

		if (adminuser.length() > 0 && adminpass.length() > 0
				&& username.equals(adminuser))
			return defaultUser();

		try {
			LoadEntityByType command = new LoadEntityByType("configuration");
			command = commandService.executeCommand(command);
			List<Entity> entities = command.getEntities();

			for (Entity entity : entities) {
				if (isUser(username, entity)) {
					return newUserDetails(entity);
				}
			}
		} catch (CommandException e) {
			throw new UsernameNotFoundException(
					"Fehler beim Zugriff auf Benutzer in DB.", e);
		}
		throw new UsernameNotFoundException(Messages
				.getString("DbUserDetailsService.4")); //$NON-NLS-1$
	}

	private UserDetails defaultUser() {
		VeriniceUserDetails user = new VeriniceUserDetails(adminuser, adminpass);
		user.addRole(ApplicationRoles.ROLE_ADMIN);
		user.addRole(ApplicationRoles.ROLE_USER);
		return user;
	}

//	private String encrypt(String adminpass2, String username) {
//		return adminpass2;
////		return DigestProcessingFilter.encodePasswordInA1Format(username,
////				getEntryPoint().getRealmName(), adminpass2);
//	}

	private UserDetails newUserDetails(Entity entity) {
		VeriniceUserDetails userDetails = new VeriniceUserDetails(entity
				.getSimpleValue(Configuration.PROP_USERNAME), entity
				.getSimpleValue(Configuration.PROP_PASSWORD));
		List<Property> properties = entity.getProperties(
				Configuration.PROP_ROLES).getProperties();
		for (Property displayedRoles : properties) {
			String[] appRoles = translateToApplicationRole(displayedRoles
					.getPropertyValue());
			if (appRoles != null) {
				for (String appRole : appRoles) {
					userDetails.addRole(appRole);
				}
			}
		}
		return userDetails;
	}

	private String[] translateToApplicationRole(String displayRole) {
		return roleMap.get(displayRole);
	}

	private boolean isUser(String username, Entity entity) {
		return entity.getSimpleValue(Configuration.PROP_USERNAME).equals(
				username);

	}

	public ICommandService getCommandService() {
		return commandService;
	}

	public void setCommandService(ICommandService commandService) {
		this.commandService = commandService;
	}

	public void setAdminuser(String adminuser) {
		this.adminuser = adminuser;
	}

	public void setAdminpass(String adminpass) {
		this.adminpass = adminpass;
	}



}
