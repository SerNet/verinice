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
package sernet.gs.server.security;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Provides access to user details in the verinice database.
 * These can be created by any admin-user in the verinice frontend itself.
 * 
 * Additionally, one initial user can be configured in applicationContext.xml itself,
 * as a backup administrative account and for initial setting up of the database.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class DbUserDetailsService extends UserLoader implements UserDetailsService {
	
    private final Logger log = Logger.getLogger(DbUserDetailsService.class);
	
	// injected by spring
	private String adminuser = "";

	// injected by spring
	private String adminpass = "";
	
	

	/* (non-Javadoc)
	 * @see org.springframework.security.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		if (adminuser.length() > 0 && adminpass.length() > 0 && username.equals(adminuser)) {
			return defaultUser();
		}
		
		ServerInitializer.inheritVeriniceContextState();
		List<Entity> entities = loadUserEntites(username);

		for (Entity entity : entities) {
			if (isUser(username, entity)) {
			    if (log.isDebugEnabled()) {
                    log.debug("User found: " + username);
                }
				return databaseUser(entity);
			}
		}
		if (log.isDebugEnabled()) {
            log.debug("User *NOT* found: " + username);
        }
		throw new UsernameNotFoundException(Messages.getString("DbUserDetailsService.4")); //$NON-NLS-1$
	}

	private UserDetails defaultUser() {
		VeriniceUserDetails user = new VeriniceUserDetails(adminuser, adminpass);
		user.addRole(ApplicationRoles.ROLE_ADMIN);
		user.addRole(ApplicationRoles.ROLE_USER);
        user.addRole(ApplicationRoles.ROLE_WEB);
		return user;
	}

	private UserDetails databaseUser(Entity entity) {	    
	    boolean scopeOnly = false;
	    Property p = entity.getProperties(Configuration.PROP_SCOPE).getProperty(0);
        if (p != null) {
            scopeOnly = Configuration.PROP_SCOPE_YES.equals(p.getPropertyValue());
        }

		VeriniceUserDetails userDetails = new VeriniceUserDetails(
		        entity.getSimpleValue(Configuration.PROP_USERNAME), 
		        entity.getSimpleValue(Configuration.PROP_PASSWORD),
		        scopeOnly);
		
		// All users without explicitly set Configuration.PROP_RCP==Configuration.PROP_RCP_NO
		// get ROLE_USER, user with ROLE_USER can access the RCP client 
		if (!entity.isSelected(Configuration.PROP_RCP, Configuration.PROP_RCP_NO)) {
		    userDetails.addRole(ApplicationRoles.ROLE_USER);
		}
		
		// if set in the entity, the user may also have the admin role:
		if (entity.isSelected(Configuration.PROP_ISADMIN, Configuration.PROP_ISADMIN_YES)) {
			userDetails.addRole(ApplicationRoles.ROLE_ADMIN);
		}
			
		// if set in the entity, the user may also have the admin role:
        if (!entity.isSelected(Configuration.PROP_WEB, Configuration.PROP_WEB_NO)) {
            userDetails.addRole(ApplicationRoles.ROLE_WEB);
        }
		
		return userDetails;
	}

	public static boolean isUser(String username, Entity entity) {
	    return entity.getSimpleValue(Configuration.PROP_USERNAME).equals(username);
	}

	public void setAdminuser(String adminuser) {
		this.adminuser = adminuser;
	}

	public void setAdminpass(String adminpass) {
		this.adminpass = adminpass;
	}

}
