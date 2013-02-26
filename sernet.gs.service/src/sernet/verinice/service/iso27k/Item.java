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
package sernet.verinice.service.iso27k;

import java.util.Collection;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.iso27k.IItem;

/**
 * An item has a name and a description
 * and optionally a collection of child items
 * to create a tree structure.
 * 
 * This implementation stores child items in
 * a sorted map. Key of the map is the number of the child.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Item implements IItem {
	
	private transient Logger log = Logger.getLogger(Item.class);
	
	public static final String NUMBER_REGEX_PATTERN = "\\d+(\\.\\d+)*(\\.)?";
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(Item.class);
		}
		return log;
	}
	
	private String name;
	
	private String description;
	
	private String numberString;

	// controls can be simple "yes/no" type or numeric maturity levels:
	// imported maturity value from file
	private String maturity;
	// weight value for this control
	private String weight1;
	// possible second weight value
	private String weight2;
	// first threshold, i.e. minimum maturity for "yellow" range
	private String threshold1;
	// second threshold, i.e. mi nimum maturity for "green" range
	private String threshold2;
	
	private int typeId = CONTROL;

	/**
	 * tree map to store and sort the items
	 * key is the number of child in this item
	 */
	private SortedMap<Integer, IItem> itemMap;

	private boolean maturityLevelSupport = false;

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
	    if(name!=null) {
	        // replace all whitespace (e.g.: line breaks) with " "
            this.name = name.replaceAll("\\s", " ");
	    }
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
					String itemName = item.getNumberString();
					for (int i = 0; i < numberTokens.countTokens(); i++) {
						if(itemName.lastIndexOf('.')!=-1) {
							itemName = itemName.substring(0,itemName.lastIndexOf('.'));
						}
					}
					child = new Item(itemName);
					child.setNumberString(itemName);
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
	
	@Override
    public String getMaturity() {
		return maturity;
	}

	public void setMaturity(String maturity) {
		this.maturity = maturity;
	}

	@Override
    public String getWeight1() {
		return weight1;
	}

	public void setWeight1(String weight1) {
		this.weight1 = weight1;
	}

	@Override
    public String getWeight2() {
		return weight2;
	}

	public void setWeight2(String weight2) {
		this.weight2 = weight2;
	}

	@Override
    public String getThreshold1() {
		return threshold1;
	}

	public void setThreshold1(String threshold1) {
		this.threshold1 = threshold1;
	}

	@Override
    public String getThreshold2() {
		return threshold2;
	}

	public void setThreshold2(String threshold2) {
		this.threshold2 = threshold2;
	}

	public SortedMap<Integer, IItem> getItemMap() {
		return itemMap;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IItem#getName()
	 */
	@Override
    public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IItem#getDescription()
	 */
	@Override
    public String getDescription() {
		return description;
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IItem#setDescription(java.lang.String)
	 */
	@Override
    public void setDescription(String description) {
		this.description = description;
	}
	
	public void setNumberString(String numberString) {
		this.numberString = numberString;
	}

	@Override
    public String getNumberString() {
		return numberString;
	}

	@Override
    public int getTypeId() {
		return typeId;
	}

	@Override
    public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IFolder#addFolder(sernet.verinice.iso27k.service.IFolder)
	 */
	@Override
    public void addItem(IItem item) {
		int i = 0;
		itemMap.put(i, item);
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IFolder#getFolders()
	 */
	@Override
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((numberString == null) ? 0 : numberString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		Item other = (Item) obj;
		if (numberString == null) {
			if (other.numberString != null){
				return false;
			}
		} else if (!numberString.equals(other.numberString)){
			return false;
		}
		return true;
	}

	/**
	 * @param b
	 */
	public void setMaturityLevelSupport(boolean b) {
		this.maturityLevelSupport = true;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.IItem#isMaturityLevelSupport()
	 */
	@Override
    public boolean isMaturityLevelSupport() {
		return this.maturityLevelSupport;
	}
	
	

}
