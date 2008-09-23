package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.hui.common.connect.Entity;

public class OwnGefaehrdung extends Gefaehrdung {
	
	private String beschreibung;
	private String ownkategorie;

	
	
	// TODO eigener Entity-Type für eigene Gefährundengen
//	private Entity entity;
	
	
	public OwnGefaehrdung() {
		setTitel("");
		setId("");
		this.beschreibung = "";
		this.ownkategorie = "";
	}
	
	
	public String getKategorieAsString() {
		return this.ownkategorie;
	}
	
	public void setBeschreibung(String newDescr) {
		this.beschreibung =  newDescr;
	}
	
	public String getBeschreibung() {
		return this.beschreibung;
	}
	
	
	/**
	 * @return the ownkategorie
	 */
	public String getOwnkategorie() {
		return ownkategorie;
	}

	/**
	 * @param ownkategorie the ownkategorie to set
	 */
	public void setOwnkategorie(String ownkategorie) {
		this.ownkategorie = ownkategorie;
	}

	public String getUrl() {
		return getId();
	}
	
}
