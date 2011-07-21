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
package sernet.verinice.service;

import sernet.verinice.interfaces.IAuthService;


/**
 * Authentication service to be used when no service is configured.
 * 
 * <p>This implementation is to be used in the standalone client and
 * the internal server. It requests that the application does not
 * {@link Permission} instances.</p>
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public final class NoAuthenticationService implements IAuthService {

	private static final String[] NO_ROLES = new String[0];

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.IAuthService#getRoles()
	 */
	public String[] getRoles() {
		return NO_ROLES;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.IAuthService#getUsername()
	 */
	public String getUsername() {
		return "internalAdmin";
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.IAuthService#hashPassword(java.lang.String, java.lang.String)
	 */
	public String hashPassword(String username, String clearText) {
		return null;
	}

	/**
	 * {@link Permission} instance handling not needed.
	 */
	public boolean isPermissionHandlingNeeded()
	{
		return false;
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#hashOwnPassword(java.lang.String, java.lang.String)
     */
    @Override
    public String hashOwnPassword(String username, String clearText) {
        return null;
    }

    @Override
    public String getAdminUsername() {
        return getUsername();
    }
}
