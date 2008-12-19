package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class CnAElementBuilder {
	private static CnAElementBuilder instance;

	private CnAElementBuilder() {
	}

	public static CnAElementBuilder getInstance() {
		if (null == instance) {
			instance = new CnAElementBuilder();
		}
		return instance;
	}

	public CnATreeElement buildAndSave(ITVerbund itverbund, String typeId) throws Exception {
		CnAElementFactory factory = CnAElementFactory.getInstance();
	
		if (typeId.equals(Anwendung.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(AnwendungenKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Anwendung.TYPE_ID, null);
		}
		
		else if (typeId.equals(Client.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(ClientsKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Client.TYPE_ID, null);
		} 
	
		else if (typeId.equals(Server.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(ServerKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Server.TYPE_ID, null);
		}
	
		else if (typeId.equals(Person.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(PersonenKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Person.TYPE_ID, null);
		
		}
	
		else if (typeId.equals(TelefonKomponente.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(TKKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, TelefonKomponente.TYPE_ID, null);
		}
		
		else if (typeId.equals(SonstIT.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(SonstigeITKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, SonstIT.TYPE_ID, null);
		} 
		
		else if (typeId.equals(NetzKomponente.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(NKKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, NetzKomponente.TYPE_ID, null);
		}
	
		else if (typeId.equals(Gebaeude.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(GebaeudeKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Gebaeude.TYPE_ID, null);
		} 
	
		else if (typeId.equals(Raum.TYPE_ID)) {
			CnATreeElement category = itverbund.getCategory(RaeumeKategorie.TYPE_ID);
			return CnAElementFactory.getInstance().saveNew(category, Raum.TYPE_ID, null);
		}
		
		// else, build nothing:
		return null;
		
	}
}
