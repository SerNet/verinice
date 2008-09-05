package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class AnwendungenKategorie extends CnATreeElement
	implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "anwendungenkategorie"; //$NON-NLS-1$
	
	public AnwendungenKategorie(CnATreeElement parent) {
		super(parent);
		
	}
	
	private AnwendungenKategorie() {
		
	}
	
	@Override
	public String getTitel() {
		return Messages.AnwendungenKategorie_1;
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof Anwendung)
			return true;
		return false;
	}
	
	
}
