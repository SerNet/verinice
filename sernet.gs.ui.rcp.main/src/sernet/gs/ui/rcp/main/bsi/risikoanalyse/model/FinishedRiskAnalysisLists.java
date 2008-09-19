package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;

import sernet.gs.model.Gefaehrdung;

public class FinishedRiskAnalysisLists {
	
	private int dbId;
	private int finishedRiskAnalysisId;
	
	/*
	 * list of all Gefaehrdungen of type GefaehrdungsUmsetzung 
	 */
	private List<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen = new ArrayList<GefaehrdungsUmsetzung>();

	/*
	 * list of Gefaehrdungen associated to the chosen IT-system 
	 */
	private List<Gefaehrdung> associatedGefaehrdungen = new ArrayList<Gefaehrdung>();

	/*
	 * list of Gefaehrdungen, which need additional security measures 
	 * 
	 */
	private List<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen = new ArrayList<GefaehrdungsUmsetzung>();

	public FinishedRiskAnalysisLists(int analysisId,
			ArrayList<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen,
			ArrayList<Gefaehrdung> associatedGefaehrdungen,
			ArrayList<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen) {
		this.finishedRiskAnalysisId = analysisId;
		this.allGefaehrdungsUmsetzungen = allGefaehrdungsUmsetzungen;
		this.associatedGefaehrdungen = associatedGefaehrdungen;
		this.notOKGefaehrdungsUmsetzungen = notOKGefaehrdungsUmsetzungen;
	}
	
	public FinishedRiskAnalysisLists() {
		
	}

	public List<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
		return allGefaehrdungsUmsetzungen;
	}

	public List<Gefaehrdung> getAssociatedGefaehrdungen() {
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
			List<Gefaehrdung> associatedGefaehrdungen) {
		this.associatedGefaehrdungen = associatedGefaehrdungen;
	}

	public void setNotOKGefaehrdungsUmsetzungen(
			List<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen) {
		this.notOKGefaehrdungsUmsetzungen = notOKGefaehrdungsUmsetzungen;
	}
}
