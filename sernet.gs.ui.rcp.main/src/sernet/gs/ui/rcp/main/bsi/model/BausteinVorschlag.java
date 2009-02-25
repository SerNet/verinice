package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.List;

public class BausteinVorschlag implements Serializable {

	private Integer dbId;
	
	private String bausteine;
	private String name;
	
	BausteinVorschlag() {
		// hibernate constructor
	}

	public BausteinVorschlag(String name, String list) {
		this.name = name;
		this.bausteine = list;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public String getBausteine() {
		return bausteine;
	}
	
	public String[] getSplitBausteine() {
		return bausteine.split(",\\s*");
	}

	public void setBausteine(String bausteine) {
		this.bausteine = bausteine;
	}



}
