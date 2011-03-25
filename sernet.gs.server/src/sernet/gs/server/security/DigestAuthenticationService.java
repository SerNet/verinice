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

import org.apache.log4j.Logger;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.digestauth.DigestProcessingFilter;
import org.springframework.security.ui.digestauth.DigestProcessingFilterEntryPoint;

import sernet.gs.service.SecurityException;
import sernet.verinice.interfaces.IAuthService;

/**
 * HTTP digest method authentication service.
 * Allows access to roles and name of user that is currently logged on.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public final class DigestAuthenticationService implements IAuthService {

	private DigestProcessingFilterEntryPoint entryPoint;
    private String adminUsername;
	
	public String[] getRoles() {
		 GrantedAuthority[] authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		 String[] roles = new String[authority.length];
		 for (int i=0;i<authority.length; i++) {
			 roles[i] = authority[i].getAuthority();
		 }
		 return roles;
	}

	public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    /**
	 * Create a password hash for given user and password string.
	 * Protected by Spring's security config, must have ROLE_ADMIN to use.
	 */
	public String hashPassword(String username, String pass) {
		return DigestProcessingFilter.encodePasswordInA1Format(username,
		        entryPoint.getRealmName(), pass);
	}

	
	/**
	 * Create a password hash for given user and password string.
	 * Additionally checks if user is the currently logged in user
	 * This method is availably to normal users, not protected by Spring's scecurity config.
	 * 
	 * @param username
	 * @param pass
	 * @return
	 */
	public String hashOwnPassword(String username, String pass) throws SecurityException {
	    if (!getUsername().equals(username)) {
	        throw new SecurityException(Messages.getString("AuthenticationService.0")); //$NON-NLS-1$
	    }
	    
        return DigestProcessingFilter.encodePasswordInA1Format(username,
                entryPoint.getRealmName(), pass);
    }
	
	public void setEntryPoint(DigestProcessingFilterEntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.IAuthService#getUsername()
	 */
	public String getUsername() {
		try {
			SecurityContext context = SecurityContextHolder.getContext();
			Authentication authentication = context.getAuthentication();
			Object principal = authentication.getPrincipal();
			if (principal instanceof VeriniceUserDetails) {
				VeriniceUserDetails details = (VeriniceUserDetails) principal;
				return details.getUsername();
			}
		} catch (Exception e) {
			// do nothing, just return no user name
			Logger.getLogger( this.getClass() ).error( Messages.getString("AuthenticationService.1"), e ); //$NON-NLS-1$
		}
		// no user authenticated:
		return ""; //$NON-NLS-1$
	}

	public boolean isPermissionHandlingNeeded()
	{
		return true;
	}
}
