package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.model.Gefaehrdung;
import sernet.hui.common.connect.Entity;

public class OwnGefaehrdung extends Gefaehrdung {
	
	private int dbId;
	
	private String id;
	private String titel;
	private String beschreibung;
	private String ownkategorie;

	
	
	// TODO eigener Entity-Type für eigene Gefährundengen
	private Entity entity;
	
	
	public OwnGefaehrdung() {
		super.setKategorie(super.KAT_UNDEF);
		this.id = "";
		this.titel = "";
		this.beschreibung = "";
		this.ownkategorie = "";
	}
	
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public void setId(String newId) {
		this.id = newId;
	}
	
	@Override
	public String getTitel() {
		return this.titel;
	}
	
	@Override
	public void setTitel(String newTitle) {
		this.titel = newTitle;
	}
	
	@Override
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
	 * @return the dbId
	 */
	public int getDbId() {
		return this.dbId;
	}

	/**
	 * @param dbId the dbId to set
	 */
	public void setDbId(int dbId) {
		this.dbId = dbId;
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
	
}
