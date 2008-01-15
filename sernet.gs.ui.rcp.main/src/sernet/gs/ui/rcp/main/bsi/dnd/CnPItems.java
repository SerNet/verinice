package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.List;

public class CnPItems {
	// TODO use system clipboard

	private static List copyPasteItems = new ArrayList();

	public static void setItems(List items) {
		copyPasteItems = items; 
	}

	public static List getItems() {
		return copyPasteItems;
	}

	public static void clear() {
		copyPasteItems = new ArrayList();
	}
	
	

}
