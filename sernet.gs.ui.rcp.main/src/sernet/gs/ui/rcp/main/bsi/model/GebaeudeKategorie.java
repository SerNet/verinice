package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class GebaeudeKategorie extends CnATreeElement implements IBSIStrukturKategorie {
	public static final String TYPE_ID = "gebaeudekategorie"; //$NON-NLS-1$
	
	public GebaeudeKategorie(CnATreeElement parent) {
		super(parent);
		
	}
	
	private GebaeudeKategorie() {
		
	}
	
	@Override
	public String getTitle() {
		return Messages.GebaeudeKategorie_1;
	}
	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof Gebaeude)
			return true;
		return false;
	}
}
