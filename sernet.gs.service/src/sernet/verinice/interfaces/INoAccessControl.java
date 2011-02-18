/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
*/
package sernet.verinice.interfaces;



/**
 * Marker interface that can be used by {@link ICommand} implementations
 * which should work without access control restrictions.
 * 
 * <p>This is either neccessary because the command does not touch any
 * restricted resource or because there is a special demand for this.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public interface INoAccessControl {
}
