package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import sernet.gs.model.Gefaehrdung;

public class FinishedRiskAnalysisLists implements Serializable {
	
	private int dbId;
	private int finishedRiskAnalysisId;
	
	private String uuid;
	
	/*
	 * list of all Gefaehrdungen of type GefaehrdungsUmsetzung 
	 */
	private List<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen = new ArrayList<GefaehrdungsUmsetzung>();

	/*
	 * list of Gefaehrdungen associated to the chosen IT-system 
	 */
	private List<GefaehrdungsUmsetzung> associatedGefaehrdungen = new ArrayList<GefaehrdungsUmsetzung>();


	/*
	 * list of Gefaehrdungen, which need additional security measures 
	 * 
	 */
	private List<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen = new ArrayList<GefaehrdungsUmsetzung>();

	public FinishedRiskAnalysisLists(int analysisId,
			ArrayList<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen,
			ArrayList<GefaehrdungsUmsetzung> associatedGefaehrdungen,
			ArrayList<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen) {
		this();
		this.finishedRiskAnalysisId = analysisId;
		this.allGefaehrdungsUmsetzungen = allGefaehrdungsUmsetzungen;
		this.notOKGefaehrdungsUmsetzungen = notOKGefaehrdungsUmsetzungen;
		this.associatedGefaehrdungen = associatedGefaehrdungen;
	}
	
	public FinishedRiskAnalysisLists() {
		uuid = UUID.randomUUID().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj
				|| (obj instanceof FinishedRiskAnalysisLists
					&& this.uuid.equals(((FinishedRiskAnalysisLists)obj).getUuid())
					)
				);
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	public List<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
		return allGefaehrdungsUmsetzungen;
	}

	public List<GefaehrdungsUmsetzung> getAssociatedGefaehrdungen() {
		return associatedGefaehrdungen;
	}
	

	public List<GefaehrdungsUmsetzung> getNotOKGefaehrdungsUmsetzungen() {
		return notOKGefaehrdungsUmsetzungen;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	public int getFinishedRiskAnalysisId() {
		return finishedRiskAnalysisId;
	}

	public void setFinishedRiskAnalysisId(int finishedRiskAnalysisId) {
		this.finishedRiskAnalysisId = finishedRiskAnalysisId;
	}

	public void setAllGefaehrdungsUmsetzungen(
			List<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen) {
		this.allGefaehrdungsUmsetzungen = allGefaehrdungsUmsetzungen;
	}

	public void setAssociatedGefaehrdungen(
			List<GefaehrdungsUmsetzung> associatedGefaehrdungen) {
		this.associatedGefaehrdungen = associatedGefaehrdungen;
	}

	public void setNotOKGefaehrdungsUmsetzungen(
			List<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen) {
		this.notOKGefaehrdungsUmsetzungen = notOKGefaehrdungsUmsetzungen;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
