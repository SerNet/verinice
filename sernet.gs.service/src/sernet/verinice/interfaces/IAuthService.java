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
package sernet.verinice.interfaces;

public interface IAuthService {
    
	/**
	 * Get granted roles of currently active user.
	 * 
	 * @return
	 */
	public String[] getRoles();
	
	/**
	 * Get the name of the currently logged in user in the active context.
	 * @return
	 */
	public String getUsername();
	
	/**
	 * Encrypt the given cleartext password.
	 * 
	 * @param username
	 * @param clearText
	 * @return
	 */
	public String hashPassword(String username, String clearText);

	/**
	 * Only hash password if user is currently logged in.
	 * @param username
	 * @param clearText
	 * @return
	 */
	public String hashOwnPassword(String username, String clearText);

	/**
	 * Returns whether the creation and management of {@link Permission}
	 * objects is needed.
	 * 
	 * <p>By default permission handling is not needed for the standalone
	 * client with the integrated server.</p>
	 * 
	 * @return
	 */
	public boolean isPermissionHandlingNeeded();
	
	public String getAdminUsername();

}
