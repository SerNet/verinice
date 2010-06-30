/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.iso27k;



/**
 * Tree structured collections of items.
 * Each item has a heading and a text 
 * and optionally a list of items as children.
 * 
 * To get the root item of the tree call getRoot.
 * 
 * @author Daniel Murygin<dm[at]sernet[dot]de>
 */
public interface ICatalog {
	
	/**
	 * Returns the name of the catalog
	 * 
	 * @return the name of the catalog
	 */
	String getName();
	
	/**
	 * Returns the description of the catalog
	 * 
	 * @return the description of the catalog
	 */
	String getDescription();
	
	/**
	 * Sets the description of the catalog
	 * 
	 * @param description
	 */
	void setDescription( String description );
	
	/**
	 * Returns the root item of the item tree
	 * 
	 * @return root of the item tree
	 */
	IItem getRoot();
	
}
