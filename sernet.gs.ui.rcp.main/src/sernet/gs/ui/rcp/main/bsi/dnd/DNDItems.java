package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.List;

public class DNDItems {
	// TODO use system DND, (dragsource and droptarget)

	public static final String BAUSTEIN = "baustein";
	public static final Object BAUSTEINUMSETZUNG = "bausteinumsetzung";
	public static final Object CNAITEM = "cnaitem";
	
	private static List dndItems = new ArrayList();

	public static void setItems(List items) {
		dndItems = items; 
	}

	public static List getItems() {
		return dndItems;
	}

	public static void clear() {
		dndItems = null;
	}
	
	

}
