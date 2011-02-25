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
package sernet.gs.common;

/**
 * Constants for all roles used by the application.
 * These are not the roles used for access controls on specific objects but more generic roles to determine
 * if a user is logged in at all, has administrator rights etc.
 * 
 * These roles are used to determine, what program <b>functions</b> the user can access, not which <b>objects</b>.
 * For the latter, see <code>Permission</code>.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public abstract class ApplicationRoles {
    /**
     * User is authenticated and can access general program functions
     * and the RCP client
     */
	public static final String ROLE_USER = "ROLE_USER";
	
	/**
	 * User is authenticated and can access more powerful and administrative functions, such as creating new users, setting 
	 * other people's passwords etc.
	 */
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	
	/**
     * User is authenticated and can access verinice.WEB the web frontend
     */
    public static final String ROLE_WEB = "ROLE_WEB";
	
	/**
	 * User was authenticated against a directory service. Therefore he may not change his own password inside verinice as usual
	 * and may be subject to other limitations. But he may also be able to query the directory for further information. 
	 */
	public static final String ROLE_LDAPUSER = "ROLE_LDAPUSER";

	/**
	 * LDAP users can use a generic guest account to access verinice, if one is set up. 
	 * On successfull authentication, the ROLE_GUEST role is added to the token.
	 */
    public static final String ROLE_GUEST = "ROLE_GUEST";
}
