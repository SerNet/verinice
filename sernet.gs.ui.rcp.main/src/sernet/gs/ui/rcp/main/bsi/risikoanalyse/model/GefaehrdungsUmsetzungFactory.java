package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class GefaehrdungsUmsetzungFactory {

	public static GefaehrdungsUmsetzung build(
			CnATreeElement parent, Gefaehrdung source) {
		GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(parent);
		
		// FIXME set null parent first, then update only on wizarf finish
		
		gefaehrdungsUmsetzung.setId(source.getId());
		gefaehrdungsUmsetzung.setTitel(source.getTitel());
		gefaehrdungsUmsetzung.setAlternative(GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_C);
		gefaehrdungsUmsetzung.setOkay(true);
		gefaehrdungsUmsetzung.setUrl(source.getUrl());

		
		gefaehrdungsUmsetzung.setKategorie(source.getKategorieAsString());
		gefaehrdungsUmsetzung.setStand(source.getStand());

		if (source instanceof OwnGefaehrdung) {
			OwnGefaehrdung gefaehrdungSource = (OwnGefaehrdung) source;
			gefaehrdungsUmsetzung.setDescription(gefaehrdungSource.getBeschreibung());
		}
		return gefaehrdungsUmsetzung;
	}

}
