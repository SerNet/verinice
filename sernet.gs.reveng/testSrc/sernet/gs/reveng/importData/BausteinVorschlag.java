package sernet.gs.reveng.importData;

import java.util.List;

public class BausteinVorschlag {

	private Integer dbId;
	
	private List<String> bausteine;
	private String name;

	public BausteinVorschlag(String name, List<String> list) {
		this.name = name;
		this.bausteine = list;
	}

	public List<String> getBausteine() {
		return bausteine;
	}

	public void setBausteine(List<String> bausteine) {
		this.bausteine = bausteine;
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

}
