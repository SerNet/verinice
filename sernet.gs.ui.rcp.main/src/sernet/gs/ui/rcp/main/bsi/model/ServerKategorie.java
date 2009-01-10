package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class ServerKategorie extends CnATreeElement implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "serverkategorie"; //$NON-NLS-1$
	
	public ServerKategorie(CnATreeElement model) {
		super(model);
	}
	
	public ServerKategorie() {
		
	}
	
	@Override
	public String getTitel() {
		return "IT-Systeme: Server";
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof Server)
			return true;
		return false;
	}
}
