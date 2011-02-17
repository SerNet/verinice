/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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

import javax.naming.ldap.InitialLdapContext;

import org.richfaces.iterator.ForEachIterator;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.AuthenticationProvider;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.ldap.LdapAuthenticator;

import sernet.gs.common.ApplicationRoles;

public class LdapAuthenticationProvider implements AuthenticationProvider {

    private LdapAuthenticator authenticator;
    public static final String ROLES_ATTRIBUTE = "attribute_roles";

    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        DirContextOperations authAdapter = authenticator.authenticate(auth);
        LdapUserToken ldapAuth = new LdapUserToken(auth, ApplicationRoles.ROLE_LDAPUSER);
        Object[] roles = authAdapter.getObjectAttributes(ROLES_ATTRIBUTE);
        for (Object role : roles) {
            ldapAuth.addAuthority((String)role);
        }
        return ldapAuth;
    }

    public boolean supports(Class clazz) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(clazz));
    }

    public LdapAuthenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(LdapAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

}
