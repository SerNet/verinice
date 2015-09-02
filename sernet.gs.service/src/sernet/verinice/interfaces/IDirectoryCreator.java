/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
 * Creates a directory
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IDirectoryCreator {

    /**
     * Creates a directory and returns it full path
     * 
     * @return Full path of the directory
     */
    String create();
    
    /**
     * Creates a directory and a sub directory in it.
     *  Returns the full path of the sub directory.
     * 
     * @return Full path of the sub directory
     */
    String create(String subDirectory);
}
