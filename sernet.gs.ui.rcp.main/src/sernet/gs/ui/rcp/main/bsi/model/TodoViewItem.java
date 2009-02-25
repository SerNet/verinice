package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.Date;

import sernet.hui.common.connect.Entity;

public class TodoViewItem implements Serializable, IMassnahmeUmsetzung {
	
	private String titel;
	private String umsetzung;
	private Date umsetzungBis;
	private String umsetzungDurch;
	private char stufe;
	private String parentTitle;
	private String url;
	private String stand;
	private Integer dbId;
	private Date naechsteRevision;
	private String revisionDurch;

	

	public String getTitel() {
		return titel;
	}

	public String getUmsetzung() {
		return umsetzung;
	}

	public Date getUmsetzungBis() {
		return umsetzungBis;
	}

	public String getUmsetzungDurch() {
		return umsetzungDurch;
	}

	public char getStufe() {
		return stufe;
	}

	public String getParentTitle() {
		return parentTitle;
	}

	public void setTitel(String title) {
		this.titel = title;
	}

	public void setUmsetzung(String umsetzung) {
		this.umsetzung = umsetzung;
	}

	public void setUmsetzungBis(Date umsetzungBis) {
		this.umsetzungBis = umsetzungBis;
	}

	public void setUmsetzungDurch(String umsetzungDurch) {
		this.umsetzungDurch = umsetzungDurch;
	}

	public void setStufe(char stufe) {
		this.stufe = stufe;
	}

	public void setParentTitle(String parentTitle) {
		this.parentTitle = parentTitle;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setStand(String stand) {
		this.stand = stand;
	}

	public String getUrl() {
		return url;
	}

	public String getStand() {
		return stand;
	}

	public int getdbId() {
		return dbId;
	}

	public void setDbId(Integer dbId2) {
		this.dbId = dbId2;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj
				|| (obj instanceof TodoViewItem
					&& this.getParentTitle().equals(((TodoViewItem)obj).getParentTitle())
					&& this.titel.equals(((TodoViewItem)obj).getTitel())
					)
				);
	}
	
	@Override
	public int hashCode() {
		return dbId.hashCode() + url.hashCode() + titel.hashCode();
	}

	public void setNaechsteRevision(Date naechsteRevision) {
		this.naechsteRevision = naechsteRevision; 
	}

	public void setRevisionDurch(String revisionDurch) {
		this.revisionDurch = revisionDurch;
	}

	public String getRevisionDurch() {
		return this.revisionDurch;
	}

	public Date getNaechsteRevision() {
		return this.naechsteRevision;
	}

}
