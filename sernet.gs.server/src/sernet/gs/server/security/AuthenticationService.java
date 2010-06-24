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

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.digestauth.DigestProcessingFilter;
import org.springframework.security.ui.digestauth.DigestProcessingFilterEntryPoint;

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
public final class AuthenticationService implements IAuthService {

	private DigestProcessingFilterEntryPoint entryPoint;
	
	public String[] getRoles() {
		 GrantedAuthority[] authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		 String[] roles = new String[authority.length];
		 for (int i=0;i<authority.length; i++) {
			 roles[i] = authority[i].getAuthority();
		 }
		 return roles;
	}

	public String hashPassword(String username, String pass) {
		return DigestProcessingFilter.encodePasswordInA1Format(username,
			getEntryPoint().getRealmName(), pass);
	}
	
	public DigestProcessingFilterEntryPoint getEntryPoint() {
		return entryPoint;
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
		}
		// no user authenticated:
		return "";
	}

	public boolean isPermissionHandlingNeeded()
	{
		return true;
	}
}
