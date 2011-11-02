/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
package sernet.verinice.interfaces;

import java.util.List;

import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;

/**
 * Service to read and change the authorization configuration of verinice.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IRightsService {

    /**
     * Returns the authorization configuration
     * which is defined in one or more 
     * verinice-auth xml documents.
     * 
     * See schema verinice-auth.xsd for details.
     * 
     * @return the authorization configuration
     */
    Auth getConfiguration();
    
    /**
     * Return the userprofiles of an user with name <i>username</i>.
     * A userprofile can belong to an user or an user group.
     * 
     * @param username The login name of an user
     * @return a {@link List} of userprofiles
     */
    List<Userprofile> getUserprofile(String username);
    
    /**
     * Returns all profiles of the authorization configuration
     * 
     * @return profiles of the authorization configuration
     */
    Profiles getProfiles();
}
