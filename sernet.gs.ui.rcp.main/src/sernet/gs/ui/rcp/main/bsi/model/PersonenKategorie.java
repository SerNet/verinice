package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class PersonenKategorie extends CnATreeElement implements IBSIStrukturKategorie {
	
	public static final String TYPE_ID = "personkategorie"; //$NON-NLS-1$

	public PersonenKategorie(CnATreeElement model) {
		super(model);
	}
	
	private PersonenKategorie() {
		
	}
	@Override
	public String getTitel() {
		return "Mitarbeiter";
	}
	
	
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof Person)
			return true;
		return false;
	}
}
