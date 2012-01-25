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

import sernet.verinice.model.auth.Userprofile;

/**
 * Extends the {@link IRightsService} to the use
 * for clients.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IRightsServiceClient extends IRightsService {

    
    /**
     * Returns the Userprofile for the currently logged in user.
     * 
     * @return the Userprofile
     */
    List<Userprofile> getUserprofile();
    
    /**
     * Returns true if the action with id <code>actionId</code>
     * is enabled for the current user.
     * 
     * @param actionId The id of an action
     * @return True if an action is enabled for an user
     */
    boolean isEnabled(String actionId);
    
    void reload();
}
