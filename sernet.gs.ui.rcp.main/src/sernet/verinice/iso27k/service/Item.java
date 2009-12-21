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

import java.util.Collection;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

/**
 * An item has a name and a description
 * and optionally a collection of child items
 * to create a tree structure.
 * 
 * This implementation stores child items in
 * a sorted map. Key of the map is the number of the child.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
@SuppressWarnings("serial")
public class Item implements IItem {
	
	private transient Logger log = Logger.getLogger(Item.class);
	
	
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(Item.class);
		}
		return log;
	}
	
	protected String name;
	
	protected String description;
	
	private String numberString;
	
	private int typeId = CONTROL;

	/**
	 * tree map to store and sort the items
	 * key is the number of child in this item
	 */
	protected SortedMap<Integer, IItem> itemMap;

	/**
	 * Creates an item without name
	 * Creates an empty tree map to store items
	 */
	public Item() {
		itemMap = new TreeMap<Integer,IItem>();
	}
	
	/**
	 * Creates an item with the given name
	 * Creates an empty tree map to store items
	 * 
	 * @param name name of the item
	 */
	public Item(String name) {
		this.name = name;
		itemMap = new TreeMap<Integer,IItem>();
	}
	
	/**
	 * Creates an item with the given name and type id.
	 * Creates an empty tree map to store items
	 * 
	 * @param name name of the item
	 */
	public Item(String name, String typeId) {
		this.name = name;
		if(typeId!=null && typeId.length()>0) {
			this.typeId = Integer.valueOf(typeId);
		}
		itemMap = new TreeMap<Integer,IItem>();
	}
	
	/**
	 * Adds the item as child if there is only one token left in numberTokens
	 * Theses last token is the child number.
	 * 
	 * If there is more than one token left, the item is passed to the child
	 * with the first token number.
	 * 
	 * @param item item to add
	 * @param numberTokens child numbers
	 */
	public void processItem(Item item, StringTokenizer numberTokens) {
		if(numberTokens.hasMoreTokens()) {
			int firstNumber = Integer.valueOf(numberTokens.nextToken());
			if(numberTokens.hasMoreTokens()) {
				Item child = (Item) getItemMap().get(firstNumber-1);
				if(child==null) {
					// item is missing, create dummy item
					String name = item.getNumberString();
					for (int i = 0; i < numberTokens.countTokens(); i++) {
						if(name.lastIndexOf(".")!=-1) {
							name = name.substring(0,name.lastIndexOf("."));
						}
					}
					child = new Item(name);
					getItemMap().put(firstNumber-1, child);
				}
				if (getLog().isDebugEnabled()) {
					getLog().debug(getName() + " - branch: " + child.getName() + " - child added: " + item.getName());
				}
				child.processItem(item, numberTokens);
			} else {
				// add item here
				getItemMap().put(firstNumber-1, item);
				if (getLog().isDebugEnabled()) {
					getLog().debug(getName() + " - new child: " + (firstNumber-1) + "-" + item.getName());
				}
			}
		}	
	}
	
	public SortedMap<Integer, IItem> getItemMap() {
		return itemMap;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IItem#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IItem#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IItem#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setNumberString(String numberString) {
		this.numberString = numberString;
	}

	public String getNumberString() {
		return numberString;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IFolder#addFolder(sernet.verinice.iso27k.service.IFolder)
	 */
	public void addItem(IItem item) {
		int i = 0;
		while(itemMap.get(i)!=null) {
			// nothing
		}
		itemMap.put(i, item);
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IFolder#getFolders()
	 */
	public Collection<IItem> getItems() {
		return itemMap.values();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		if(getItems().size()>0) {
			sb.append("\n |");
			for (IItem item : getItems()) {		
				sb.append("\n + ").append(item.toString());
			}
		}
		return sb.toString();
	}

}
