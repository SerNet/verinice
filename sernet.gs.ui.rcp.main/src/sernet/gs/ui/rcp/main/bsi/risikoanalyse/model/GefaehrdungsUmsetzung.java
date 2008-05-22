package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.model.Gefaehrdung;
import sernet.hui.common.connect.Entity;

public class GefaehrdungsUmsetzung extends Gefaehrdung {
	
	private int dbId;
	
	private String id;
	private String titel;
	private String kategorie;
	private Boolean okay;
	private String alternative;

	public static final String GEFAEHRDUNG_ALTERNATIVE_A = "A";
	public static final String GEFAEHRDUNG_ALTERNATIVE_B = "B";
	public static final String GEFAEHRDUNG_ALTERNATIVE_C = "C";
	public static final String GEFAEHRDUNG_ALTERNATIVE_D = "D";

	public static final String[] ALTERNATIVEN = {GEFAEHRDUNG_ALTERNATIVE_A,
		GEFAEHRDUNG_ALTERNATIVE_B,
		GEFAEHRDUNG_ALTERNATIVE_C,
		GEFAEHRDUNG_ALTERNATIVE_D,};
	
	// TODO eigener Entity-Typ für eigene Gefährundengen
	private Entity entity;
	
	public int getAlternativeIndex() {
		int i=-1;
		for (String alt : ALTERNATIVEN) {
			i++;
			if (alt.equals(alternative))
				return i;
		}
		return -1;
	}
	
	public GefaehrdungsUmsetzung(Gefaehrdung source) {
		this.id = source.getId();
		this.titel = source.getTitel();
		this.kategorie = source.getKategorieAsString();
		this.okay = true;
		this.alternative = GEFAEHRDUNG_ALTERNATIVE_C;
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
		return this.kategorie;
	}
	
	public void setKategorieAsString(String newKategorie) {
		this.kategorie = newKategorie;
	}
	
	public void setAlternative(String newAlternative) {
		this.alternative = newAlternative;
	}
	
	public String getAlternative() {
		return alternative;
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
	 * @return the okay
	 */
	public Boolean getOkay() {
		return okay;
	}

	/**
	 * @param okay the okay to set
	 */
	public void setOkay(Boolean newOkay) {
		this.okay = newOkay;
	}
}
