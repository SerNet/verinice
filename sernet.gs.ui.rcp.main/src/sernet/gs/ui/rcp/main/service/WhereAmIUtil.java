/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service;

/**
 * Find out if code is running on a client or the server.
 * Needed to find configuration files. Defaults to server.
 * 
 * Explanation: the command service is designed to be transparent
 * as to the execution of commands. It can be switched from remote
 * to local without the commands or application having to be aware of this switch.
 * 
 * For commands that do need to know where they are running, this utility class
 * can be queried. 
 *
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public abstract class WhereAmIUtil {

	public static final int LOCATION_CLIENT = 1;
	public static final int LOCATION_SERVER = 2;
	
	private static int location = LOCATION_SERVER;
	
	public static void setLocation(int loc) {
		location = loc;
	}

	public static boolean runningOnClient() {
		return (location == LOCATION_CLIENT);
	}

	public static boolean runningOnServer() {
		return (location == LOCATION_SERVER);
	}
	
	
	
	
	
}
