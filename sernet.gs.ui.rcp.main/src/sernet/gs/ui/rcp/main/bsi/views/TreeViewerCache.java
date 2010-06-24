/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views;


import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import sernet.verinice.model.common.CnATreeElement;


/**
 * Cache that is shared between a view's content and label provider and it's model update listener.
 * 
 * Used to allow lazy loading of tree elements, resolving already displayed
 * items when a new instance for an already displayed object is returned from the database.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class TreeViewerCache {

	private Map<Object, Object> cache;
	
	private static final Object PRESENT = new Object();
	
	public TreeViewerCache() {
		clear();
	}
	
	public void addObject(Object o) {
		this.cache.put(o, PRESENT);
	}
	
	public void clear() {
		this.cache = Collections.synchronizedMap(new WeakHashMap<Object, Object>());
	}
	
	public <T> T getCachedObject(T o) {
		synchronized(cache) {
			for (Object elmt : cache.keySet()) {
				if (elmt.equals(o))
					return (T) elmt;
			}
			return null;
		}
	}

	public void clear(Object oldElement) {
		this.cache.remove(oldElement);
	}

	/**
	 * @param id
	 * @return
	 */
	public CnATreeElement getCachedObjectById(Integer id) {
		synchronized(cache) {
			for (Object elmt : cache.keySet()) {
				if (elmt instanceof CnATreeElement) {
					CnATreeElement cnaElmt = (CnATreeElement) elmt;
					if (cnaElmt.getDbId().equals(id))
						return cnaElmt;
				}
			}
			return null;
		}
	}

	
}
