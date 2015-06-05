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
package sernet.gs.ui.rcp.main.service;


public final class AuthenticationHelper {

	private String[] currentRoles = null;
	private static AuthenticationHelper instance = new AuthenticationHelper();

	private AuthenticationHelper() {
		
	}
	
	public boolean currentUserHasRole(String[] allowedRoles) {
		if (currentRoles == null) {
			try {
				currentRoles = ServiceFactory.lookupAuthService().getRoles();
			} catch (Exception e) {
				// no auth service available
				currentRoles = null;
			} 
		}
		
		// roles might still be uninitialized (authservice can also return null):
		if (currentRoles == null){
			return false;
		}
		for (String role : allowedRoles) {
			for (String userRole : currentRoles) {
				if (role.equals(userRole)){
					return true;
				}
			}
		}
		return false;
	}

	public static AuthenticationHelper getInstance() {
		return instance;
	}

}
