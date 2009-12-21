/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
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
 *     Daniel <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.io.Serializable;
import java.util.Collection;

/**
 * An item has a name and a description
 * and optionally a collection of child items
 * to create a tree structure.
 * 
 * @see ICatalog 
 * @author Daniel <dm@sernet.de>
 */
public interface IItem extends Serializable {
	
	static final int CONTROL = 0;
	
	static final int THREAD = 1;
	
	String getName();
	
	String getDescription();
	
	void setDescription( String description );
	
	int getTypeId();

	void setTypeId(int typeId);
	
	void addItem(IItem item);
	
	Collection<IItem> getItems();
}
