package sernet.gs.ui.rcp.main.bsi.views;


import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


/**
 * Cache that is shared between a view's content and label provider and it's model update listener.
 * 
 * Used to allow lazy loading of tree elements, resolving already displayed
 * items when a new instance for an already displayed object is returned from the database.
 * 
 * @author koderman@sernet.de
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

	
}
