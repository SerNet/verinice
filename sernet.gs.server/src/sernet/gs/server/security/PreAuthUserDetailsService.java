/*******************************************************************************
 * Copyright (c) 2014 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * Authenticates user against imported ldap user.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class PreAuthUserDetailsService extends DbUserDetailsService {

    private static final String REALM_SEPERATOR = "@";

    private String username;

    @Override
    public UserDetails loadUserByUsername(String principal) throws UsernameNotFoundException, DataAccessException {
        username = removeRealm(principal);
        return super.loadUserByUsername(username);
    }

    private String removeRealm(String principal) {
        if (principal != null && principal.contains(REALM_SEPERATOR))
            return principal.substring(0, principal.indexOf(REALM_SEPERATOR));
        return principal;
    }
}
