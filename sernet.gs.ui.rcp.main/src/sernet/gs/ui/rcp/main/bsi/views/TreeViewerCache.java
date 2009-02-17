package sernet.gs.ui.rcp.main.bsi.views;


import java.util.Map;
import java.util.WeakHashMap;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class TreeViewerCache {

	private Map<Object, Object> cache;
	
	private static final Object PRESENT = new Object();
	
	public TreeViewerCache() {
		this.cache = new WeakHashMap<Object, Object>();
	}
	
	public void addObject(Object o) {
		this.cache.put(o, PRESENT);
	}
	
	public void clear() {
		this.cache = new WeakHashMap<Object, Object>();
	}
	
	public <T> T getCachedObject(T o) {
		for (Object elmt : cache.keySet()) {
			if (elmt.equals(o))
				return (T) elmt;
		}
		return null;
	}

	public void clear(Object oldElement) {
		this.cache.remove(oldElement);
	}

	
}
