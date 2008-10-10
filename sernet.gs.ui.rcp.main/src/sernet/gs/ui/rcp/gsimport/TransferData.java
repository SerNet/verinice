package sernet.gs.ui.rcp.gsimport;

import java.util.HashMap;
import java.util.Map;

import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.ui.rcp.main.bsi.model.Anwendung;
import sernet.gs.ui.rcp.main.bsi.model.AnwendungenKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Client;
import sernet.gs.ui.rcp.main.bsi.model.ClientsKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Gebaeude;
import sernet.gs.ui.rcp.main.bsi.model.GebaeudeKategorie;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
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
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class TransferData {
	
	
	public void transfer(ITVerbund itverbund, ZielobjektTypeResult result) throws Exception {
		NZielobjekt source = result.zielobjekt;
		itverbund.setTitel(source.getName());
		CnAElementHome.getInstance().update(itverbund);
	}

	public void transfer(CnATreeElement element, ZielobjektTypeResult result) {
			String typeId = element.getTypeId();
			if (typeId.equals(Anwendung.TYPE_ID)) {
				typedTransfer((Anwendung)element, result);
			}
			
			else if (typeId.equals(Client.TYPE_ID)) {
				typedTransfer((Client)element, result);
			} 
		
			else if (typeId.equals(Server.TYPE_ID)) {
				typedTransfer((Server)element, result);
			}
		
			else if (typeId.equals(Person.TYPE_ID)) {
				typedTransfer((Person)element, result);
			
			}

			else if (typeId.equals(TelefonKomponente.TYPE_ID)) {
				typedTransfer((TelefonKomponente)element, result);
			}
			
			else if (typeId.equals(SonstIT.TYPE_ID)) {
				typedTransfer((SonstIT)element, result);
			} 
			
			else if (typeId.equals(NetzKomponente.TYPE_ID)) {
				typedTransfer((NetzKomponente)element, result);
			}
		
			else if (typeId.equals(Gebaeude.TYPE_ID)) {
				typedTransfer((Gebaeude)element, result);
			} 
		
			else if (typeId.equals(Raum.TYPE_ID)) {
				typedTransfer((Raum)element, result);
			}
			
	}

	private void typedTransfer(Anwendung element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
	}

	private void typedTransfer(Client element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		}

	private void typedTransfer(Server element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
	}

	private void typedTransfer(Person element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
	}

	private void typedTransfer(TelefonKomponente element,
			ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
	}

	private void typedTransfer(SonstIT element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
	}

	private void typedTransfer(NetzKomponente element,
			ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
	}

	private void typedTransfer(Gebaeude element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
	}

	private void typedTransfer(Raum element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
	}

}
