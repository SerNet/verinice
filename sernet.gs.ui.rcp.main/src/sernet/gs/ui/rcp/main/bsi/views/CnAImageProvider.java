package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.swt.graphics.Image;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.ClientsKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.GebaeudeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.NKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.NetzKomponente;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.PersonenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.RaeumeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Raum;
import sernet.gs.ui.rcp.main.bsi.model.Server;
import sernet.gs.ui.rcp.main.bsi.model.ServerKategorie;
import sernet.gs.ui.rcp.main.bsi.model.SonstIT;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TKKategorie;
import sernet.gs.ui.rcp.main.bsi.model.TelefonKomponente;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnAPlaceholder;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;

public class CnAImageProvider {

	public static Image getImage(CnATreeElement elmt) {
		if (elmt instanceof MassnahmenUmsetzung)
			return getImage((MassnahmenUmsetzung)elmt);
		
		if (elmt instanceof GefaehrdungsUmsetzung)
			return ImageCache.getInstance().getImage(ImageCache.GEFAEHRDUNG);
		
		if (elmt instanceof BausteinUmsetzung)
			return ImageCache.getInstance().getImage(ImageCache.BAUSTEIN_UMSETZUNG);
		
		if (elmt instanceof Anwendung	|| elmt instanceof AnwendungenKategorie)
			return ImageCache.getInstance().getImage(ImageCache.ANWENDUNG);

		if (elmt instanceof Gebaeude || elmt instanceof GebaeudeKategorie)
			return ImageCache.getInstance().getImage(ImageCache.GEBAEUDE);

		if (elmt instanceof Person	|| elmt instanceof PersonenKategorie)
			return ImageCache.getInstance().getImage(ImageCache.PERSON);
		
		if (elmt instanceof Client || elmt instanceof ClientsKategorie)
			return ImageCache.getInstance().getImage(ImageCache.CLIENT);

		if (elmt instanceof SonstIT || elmt instanceof SonstigeITKategorie)
			return ImageCache.getInstance().getImage(ImageCache.SONSTIT);

		if (elmt instanceof Server || elmt instanceof ServerKategorie)
			return ImageCache.getInstance().getImage(ImageCache.SERVER);

		if (elmt instanceof TelefonKomponente || elmt instanceof TKKategorie)
			return ImageCache.getInstance().getImage(ImageCache.TELEFON);

		if (elmt instanceof NetzKomponente || elmt instanceof NKKategorie)
			return ImageCache.getInstance().getImage(ImageCache.NETWORK);
		
		if (elmt instanceof Raum || elmt instanceof RaeumeKategorie)
			return ImageCache.getInstance().getImage(ImageCache.RAUM);

		if (elmt instanceof ITVerbund || elmt instanceof CnAPlaceholder)
			return ImageCache.getInstance().getImage(ImageCache.EXPLORER);
		
		if (elmt instanceof IDatenschutzElement)
			return ImageCache.getInstance().getImage(ImageCache.SHIELD);
		
		if (elmt instanceof FinishedRiskAnalysis)
			return ImageCache.getInstance().getImage(ImageCache.RISIKO_MASSNAHMEN_UMSETZUNG);
		
		return ImageCache.getInstance().getImage(
				ImageCache.UNKNOWN);
		
	}
	
	private static Image getImage(MassnahmenUmsetzung mn) {
		String state = mn.getUmsetzung();
			
			if (state.equals(MassnahmenUmsetzung.P_UMSETZUNG_NEIN))
				return ImageCache.getInstance().getImage(
					ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
		
			if (state.equals(MassnahmenUmsetzung.P_UMSETZUNG_JA))
				return ImageCache.getInstance().getImage(
						ImageCache.MASSNAHMEN_UMSETZUNG_JA);
			
			if (state.equals(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE))
				return ImageCache.getInstance().getImage(
						ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
			
			if (state.equals(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH))
				return ImageCache.getInstance().getImage(
						ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
		//else:
		return ImageCache.getInstance().getImage(
					ImageCache.MASSNAHMEN_UMSETZUNG_UNBEARBEITET);
	}
	
	public static Image getImage(FinishedRiskAnalysis elmt) {
		if (elmt instanceof FinishedRiskAnalysis)
			return ImageCache.getInstance().getImage(ImageCache.RISIKO_MASSNAHMEN_UMSETZUNG);
		return ImageCache.getInstance().getImage(
				ImageCache.UNKNOWN);
		
	}
	
}
