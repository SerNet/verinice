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
package sernet.verinice.interfaces;

/**
 * Handler to read the authorization configuration on the server.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IRightsServerHandler {

    /**
     * Returns true if the action with id <code>actionId</code>
     * is enabled for user with <code>username</code>.
     * 
     * See class {@link ActionRightIDs} for all action ids.
     * 
     * @param username A name of a user
     * @param actionId The id of an action
     * @return True if an action is enabled for the user
     */
    boolean isEnabled(String username, String actionId);
    
    /**
     * Discards all data saved in the handler.
     * This method causes a reinitialization of the handler.
     */
    void discardData();
    
}
