package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class RaeumeKategorie extends CnATreeElement implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "raeumekategorie"; //$NON-NLS-1$
	
	public RaeumeKategorie(CnATreeElement model) {
		super(model);
	}
	
	RaeumeKategorie() {
		
	}
	
	@Override
	public String getTitel() {
		return "Räume";
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof Raum)
			return true;
		return false;
	}
}
