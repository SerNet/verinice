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

import java.io.Serializable;
import java.util.Collection;

import sernet.verinice.interfaces.iso27k.ICatalog;

/**
 * An {@link IItem} is an element of a tree structured
 * collection. Items are structured by a number string.
 * Number string format is: n[.n1..] e.g. 1.2 or 3.4.1 or simply 2
 * 
 * An item has a name and a description
 * and optionally a collection of child items
 * to create the tree structure.
 * 
 * @see ICatalog 
 * @author Daniel <dm[at]sernet[dot]de>
 */
public interface IItem extends Serializable {
	
	static final int CONTROL = 0;
	static final int THREAT = 1;
	static final int VULNERABILITY = 2;
	static final int ISA_TOPIC = 3;
	
	String getNumberString();
	
	String getName();
	
	String getDescription();
	
	void setDescription( String description );
	
	int getTypeId();

	void setTypeId(int typeId);
	
	void addItem(IItem item);
	
	Collection<IItem> getItems();

	boolean isMaturityLevelSupport();
	
	public String getMaturity();

	public String getWeight1();
	public String getWeight2();

	public String getThreshold1();
	public String getThreshold2();

}
