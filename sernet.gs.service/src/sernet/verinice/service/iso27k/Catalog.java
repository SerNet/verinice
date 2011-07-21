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

import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.iso27k.ICatalog;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.service.iso27k.Item;;

/**
 * Tree structured collections of items.
 * Each item has a heading and a text 
 * and optionally a list of items as children.
 * 
 * A Catalog is a special item itself with only one child item:
 * the root of the tree structure.
 * 
 * This implementation is for use with item instances of type {@link Item}.
 * To create a catalog buffer all items by calling bufferItem first 
 * and call processItemBuffer after that to create item tree.
 * 
 * @author Daniel Murygin<dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Catalog extends Item implements ICatalog {
	
	private transient Logger log = Logger.getLogger(Catalog.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(Catalog.class);
		}
		return log;
	}
	
	/**
	 * map to buffer and sort all items
	 * its only a buffer, so its transient
	 */
	transient SortedMap<String, Item> allItemMap;

	/**
	 * Creates an catalog without name
	 * and adds the root item.
	 */
	public Catalog() {
		super();
		Item root = new Item("root");
		getItemMap().put(0,root);
	}
	
	/**
	 * Creates an catalog with the given name
	 * and adds the root item.
	 */
	public Catalog(String name) {
		super(name);
		Item root = new Item("root");
		getItemMap().put(0,root);
	}
	
	/**
	 * Buffers an item i sorted map
	 * After buffering all items call processItemBuffer
	 * to create item tree.
	 * 
	 * @param catalogItem an item to buffer
	 */
	public void bufferItem(Item catalogItem) {
		String numberString = catalogItem.getNumberString();
		if(!Pattern.matches(Item.NUMBER_REGEX_PATTERN,numberString)) {
			getLog().error("Incorrect number: " + numberString);
			throw new IllegalArgumentException("Incorrect number: " + numberString);
		}
		getAllItemMap().put(numberString, catalogItem);
	}
	
	/**
	 * Iterates over all items in the buffer an creates the tree structure
	 */
	public void processItemBuffer() {
		for (String number : allItemMap.keySet()) {
			IItem item = allItemMap.get(number);
			if (getLog().isDebugEnabled()) {
				getLog().debug("processing: " + item.getName());
			}
			StringTokenizer numberTokens = new StringTokenizer(number,".");
			((Item) getRoot()).processItem((Item) item,numberTokens);
		}
	}
	
	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.service.ICatalog#getRoot()
	 */
	public IItem getRoot() {
		return getItems().iterator().next();
	}

	
	public SortedMap<String, Item> getAllItemMap() {
		if(allItemMap==null) {
			allItemMap = new TreeMap<String, Item>(new NumericStringComparator());
		}
		return allItemMap;
	}
	
	/**
	 * Returns the tree with all item names of the catalog
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		if(getItems().size()>0) {
			sb.append("\n|");
			for (IItem item : getItems()) {		
				sb.append("\n+ ");
				appendItemLog(item, sb, 1);
			}
		}
		return sb.toString();
	}
	
	public void appendItemLog(IItem item, StringBuilder sb, int level) {	
		sb.append(item.getName());
		if(item.getItems().size()>0) {
			sb.append("\n");
			for (int i = 0; i < level; i++) {
				sb.append(" |");
			}
			for (IItem child : item.getItems()) {		
				sb.append("\n");
				for (int i = 0; i < level-1; i++) {
					sb.append(" |");
				}
				sb.append(" + ");
				appendItemLog(child, sb, level+1);
			}
		}
	}

}
