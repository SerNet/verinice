package sernet.gs.ui.rcp.main.bsi.dnd;

import java.util.ArrayList;
import java.util.List;

public class DNDItems {
	// TODO use system DND, (dragsource and droptarget)

	public static final String BAUSTEIN = "baustein"; //$NON-NLS-1$
	public static final Object BAUSTEINUMSETZUNG = "bausteinumsetzung"; //$NON-NLS-1$
	public static final Object CNAITEM = "cnaitem"; //$NON-NLS-1$
	public static final Object RISIKOMASSNAHMENUMSETZUNG = "risikomassnahmenumsetzung";
	
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
