package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class SonstigeITKategorie extends CnATreeElement 
	implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "sonstitkategorie"; //$NON-NLS-1$

	public SonstigeITKategorie(CnATreeElement parent) {
		super(parent);
	}
	
	private SonstigeITKategorie() {
		
	}

	@Override
	public String getTitle() {
		return "IT-Systeme: sonstige";
	}
	
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof SonstIT)
			return true;
		return false;
	}
}
