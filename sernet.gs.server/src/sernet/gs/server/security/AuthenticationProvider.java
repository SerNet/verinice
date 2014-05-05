/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.security;

import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.dao.DaoAuthenticationProvider;
import org.springframework.security.userdetails.UserDetails;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AuthenticationProvider extends DaoAuthenticationProvider {
    
    
    private String realmName;
    
    /* (non-Javadoc)
     * @see org.springframework.security.providers.dao.DaoAuthenticationProvider#additionalAuthenticationChecks(org.springframework.security.userdetails.UserDetails, org.springframework.security.providers.UsernamePasswordAuthenticationToken)
     */
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        Object principal = authentication.getPrincipal();
        // check if digest authentication is disabled
        if(principal instanceof String) {
            // "normal" web authentication
            if(userDetails instanceof VeriniceUserDetails) {
                ((VeriniceUserDetails)userDetails).setLogoutPossible(true);
            }
            // hash password
            authentication = new HashToken((String) authentication.getPrincipal(),authentication.getCredentials(),getRealmName());
        }
        super.additionalAuthenticationChecks(userDetails, authentication);
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }
}
