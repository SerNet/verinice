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

import org.apache.log4j.Logger;
import org.aspectj.weaver.ast.Instanceof;
import org.hibernate.dialect.function.CastFunction;
import org.richfaces.iterator.ForEachIterator;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.basicauth.BasicProcessingFilterEntryPoint;
import org.springframework.security.userdetails.ldap.LdapUserDetails;

import sernet.gs.common.ApplicationRoles;
import sernet.verinice.interfaces.IAuthService;

/**
 * HTTP basic method authentication service.
 * Allows access to roles and name of user that is currently logged on.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class BasicAuthenticationService implements IAuthService {
    
    private BasicProcessingFilterEntryPoint entryPoint;
    private String guestUser = "";
    private String adminUsername;

    /**
     * @param guestUser the guestUser to set
     */
    public void setGuestUser(String guestUser) {
        this.guestUser = guestUser;
    }

    /**
     * @param entryPoint the entryPoint to set
     */
    public void setEntryPoint(BasicProcessingFilterEntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#getRoles()
     */
    @Override
    public String[] getRoles() {
        GrantedAuthority[] authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        String[] roles = new String[authority.length];
        for (int i=0;i<authority.length; i++) {
            roles[i] = authority[i].getAuthority();
        }
        return roles;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#getUsername()
     */
    @Override
    public String getUsername() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            GrantedAuthority[] authorities = authentication.getAuthorities();
            if (guestUser != null && guestUser.length()>0 && isGuestUser(authorities))
                return guestUser;
            else
                return authentication.getName();
        } catch (Exception e) {
            // do nothing, just return no user name
            Logger.getLogger( this.getClass() ).error( Messages.getString("AuthenticationService.1"), e ); //$NON-NLS-1$
        }
        // no user authenticated:
        return ""; //$NON-NLS-1$
    
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    /**
     * @param authorities
     * @return
     */
    private boolean isGuestUser(GrantedAuthority[] authorities) {
        for (GrantedAuthority auth : authorities) {
            if (auth.getAuthority().equals(ApplicationRoles.ROLE_GUEST)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#hashOwnPassword(java.lang.String, java.lang.String)
     */
    @Override
    public String hashOwnPassword(String username, String clearText) {
        // not implemented, user cannot change his passowrd using this service
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#hashPassword(java.lang.String, java.lang.String)
     */
    @Override
    public String hashPassword(String username, String clearText) {
        // not implemented, user cannot change his passowrd using this service
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#isPermissionHandlingNeeded()
     */
    @Override
    public boolean isPermissionHandlingNeeded() {
        return true;
    }

}


