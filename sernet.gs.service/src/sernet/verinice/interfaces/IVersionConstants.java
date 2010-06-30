/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public interface IVersionConstants {

    /**
     * Version number of DB that can be used:
     */
    public static final double COMPATIBLE_DB_VERSION = 0.97D;
    /**
     * Version number of client that can be used.
     * (Must be the same in client / server code of this class.)
     * 
     * This value is submitted by the client on every first connect in the
     * instance variable <code>clientVersion</code>. If this value differs from
     * the static field, the server throws an exception to prevent incompatible clients
     * from connecting.
     * 
     * If verinice runs standalone (just on a client without server), the version 
     * number will always be the same.
     */
    public static final double COMPATIBLE_CLIENT_VERSION = 0.97D;

}
