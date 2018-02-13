/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.bp.exceptions;

/**
 * This exception is thrown if a group could not be found in a scope. 
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class GroupNotFoundInScopeException extends RuntimeException {

    private static final long serialVersionUID = -227094558820619528L;

    public GroupNotFoundInScopeException() {
        super("A group could not be found in a scope.");
    }
    
    public GroupNotFoundInScopeException(String message) {
        super(message);
    }

}
