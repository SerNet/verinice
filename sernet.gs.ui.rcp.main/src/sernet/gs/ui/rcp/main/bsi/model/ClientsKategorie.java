package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class ClientsKategorie extends CnATreeElement 
	implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "clientskategorie"; //$NON-NLS-1$

	public ClientsKategorie(CnATreeElement parent) {
		super(parent);
	}
	
	protected ClientsKategorie() {
		
	}

	@Override
	public String getTitel() {
		return "IT-Systeme: Clients";
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof Client)
			return true;
		return false;
	}
}
