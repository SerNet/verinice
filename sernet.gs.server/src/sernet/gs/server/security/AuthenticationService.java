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

import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.digestauth.DigestProcessingFilter;
import org.springframework.security.ui.digestauth.DigestProcessingFilterEntryPoint;

import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateDefaultConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveConfiguration;


public class AuthenticationService implements IAuthService {

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

}
