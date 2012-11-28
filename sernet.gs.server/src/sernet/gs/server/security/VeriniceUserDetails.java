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

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;

@SuppressWarnings("serial")
public class VeriniceUserDetails implements UserDetails {

	private String user;
	private String pass;
	private List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
	private boolean scopeOnly;
	private boolean logoutPossible = false;
	
	public VeriniceUserDetails(String user, String pass) {
        this(user,pass,false);
    }
	
	public VeriniceUserDetails(String user, String pass , boolean scopeOnly) {
        super();
        this.user = user;
        this.pass = pass;
        this.scopeOnly = scopeOnly;
    }
	
	public GrantedAuthority[] getAuthorities() {
		return (GrantedAuthority[]) roles.toArray(new GrantedAuthority[roles.size()]);
	}

	public String getPassword() {
		return pass;
	}

	public String getUsername() {
		return user;
	}

	/**
     * @return the scopeOnly
     */
    public boolean isScopeOnly() {
        return scopeOnly;
    }

    public boolean isLogoutPossible() {
        return logoutPossible;
    }

    public void setLogoutPossible(boolean logoutPossible) {
        this.logoutPossible = logoutPossible;
    }

    public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	public void addRole(String role) {
		roles.add(new GrantedAuthorityImpl(role));
	}
	
}
