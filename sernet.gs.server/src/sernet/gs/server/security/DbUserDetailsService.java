package sernet.gs.server.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;


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
	
	private static final String ROLE_USER = "ROLE_USER";
	private static final String ROLE_ADMIN = "ROLE_ADMIN";

	private final static Map<String, String[]> roleMap = new HashMap<String, String[]>();
	
	{
		roleMap.put("configuration_rolle_ciso", new String[] {ROLE_USER});
		roleMap.put("configuration_rolle_isbeauftragter", new String[] {ROLE_USER});
		roleMap.put("configuration_rolle_user", new String[] {ROLE_USER});
		roleMap.put("configuration_rolle_admin", new String[] {ROLE_USER, ROLE_ADMIN});
		roleMap.put("configuration_rolle_umsverantw", new String[] {ROLE_USER});
		roleMap.put("configuration_rolle_auditor", new String[] {ROLE_USER});
	}
	
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		
		if (adminuser.length()>0 && adminpass.length()>0 && username.equals(adminuser))
			return defaultUser();
		
		try {
			LoadEntityByType command = new LoadEntityByType("configuration");
			command = commandService.executeCommand(command);
			List<Entity> entities = command.getEntities();
			
			for (Entity entity :entities) {
				if (isUser(username, entity)) {
					return newUserDetails(entity);
				}
			}
		} catch (CommandException e) {
			throw new UsernameNotFoundException("Fehler beim Zugriff auf Benutzer in DB.", e); 
		}
		throw new UsernameNotFoundException(Messages.getString("DbUserDetailsService.4")); //$NON-NLS-1$
	}

	private UserDetails defaultUser() {
		VeriniceUserDetails user = new VeriniceUserDetails(adminuser, adminpass);
		user.addRole(ROLE_ADMIN);
		user.addRole(ROLE_USER);
		return user;
	}

	private UserDetails newUserDetails(Entity entity) {
		VeriniceUserDetails userDetails = new VeriniceUserDetails(
				entity.getSimpleValue(Configuration.PROP_USERNAME),
				entity.getSimpleValue(Configuration.PROP_PASSWORD));
		List<Property> properties = entity.getProperties(Configuration.PROP_ROLES).getProperties();
		for (Property property : properties) {
			String[] appRoles = translateToApplicationRole(property.getPropertyValue());
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
		return entity.getSimpleValue(Configuration.PROP_USERNAME).equals(username);
			
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
