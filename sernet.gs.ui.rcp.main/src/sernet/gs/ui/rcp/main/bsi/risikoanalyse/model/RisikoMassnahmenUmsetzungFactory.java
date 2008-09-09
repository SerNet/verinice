package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.io.InputStream;

import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class RisikoMassnahmenUmsetzungFactory {

	public static RisikoMassnahmenUmsetzung buildFromRisikomassnahme(RisikoMassnahme draftMn,
			CnATreeElement superParent, 
			GefaehrdungsUmsetzung myParent) {
		
		RisikoMassnahmenUmsetzung umsetzung = new RisikoMassnahmenUmsetzung(superParent, myParent, draftMn);

		umsetzung.setNumber(draftMn.getNumber());
		umsetzung.setName(draftMn.getName());
		umsetzung.setUrl(draftMn.getUrl());
		umsetzung.setStand(draftMn.getStand());
		umsetzung.setStufe(draftMn.getSiegelstufe());
		umsetzung.setLebenszyklus(draftMn.getLZAsString());
		
		
		return umsetzung;
	}
	
	public static RisikoMassnahmenUmsetzung buildFromRisikomassnahmenUmsetzung(RisikoMassnahmenUmsetzung draftMnUms,
			CnATreeElement superParent, 
			GefaehrdungsUmsetzung myParent) {
		
		RisikoMassnahme massnahme = draftMnUms.getRisikoMassnahme();
		RisikoMassnahmenUmsetzung massnahmenUmsetzung = new RisikoMassnahmenUmsetzung(superParent, myParent, massnahme);

		
		massnahmenUmsetzung.setName(draftMnUms.getName());
		massnahmenUmsetzung.setNumber(massnahme.getNumber());
		massnahmenUmsetzung.setUrl(massnahme.getUrl());
		massnahmenUmsetzung.setStand(massnahme.getStand());
		massnahmenUmsetzung.setStufe(massnahme.getSiegelstufe());
		massnahmenUmsetzung.setLebenszyklus(massnahme.getLZAsString());
		
		return massnahmenUmsetzung;
	}
	
	public static RisikoMassnahmenUmsetzung buildFromMassnahmenUmsetzung(MassnahmenUmsetzung draftMnUms,
			CnATreeElement superParent,
			GefaehrdungsUmsetzung myParent) {

		
		RisikoMassnahmenUmsetzung massnahmenUmsetzung = new RisikoMassnahmenUmsetzung(superParent, myParent);
		
		massnahmenUmsetzung.setName(draftMnUms.getName());
		massnahmenUmsetzung.setNumber(draftMnUms.getKapitel());
		massnahmenUmsetzung.setUrl(draftMnUms.getUrl());
		massnahmenUmsetzung.setStand(draftMnUms.getStand());
		massnahmenUmsetzung.setStufe(draftMnUms.getStufe());
		
		return massnahmenUmsetzung;
	}

}
