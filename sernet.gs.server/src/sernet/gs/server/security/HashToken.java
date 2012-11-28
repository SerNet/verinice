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

import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.digestauth.DigestProcessingFilter;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class HashToken extends UsernamePasswordAuthenticationToken {

    /**
     * @param name
     * @param principal
     * @param realmName
     */
    public HashToken(String name, Object password, String realmName) {     
        super(name,
              DigestProcessingFilter.encodePasswordInA1Format(name, realmName, (String) password));
        setAuthenticated(false);
    }

}
