package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class TKKategorie extends CnATreeElement implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "tkkategorie"; //$NON-NLS-1$
	
	public TKKategorie(CnATreeElement model) {
		super(model);
	}
	
	
	private TKKategorie() {
		
	}
	
	@Override
	public String getTitel() {
		return "IT-Systeme: TK-Komponenten";
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof TelefonKomponente)
			return true;
		return false;
	}
}
