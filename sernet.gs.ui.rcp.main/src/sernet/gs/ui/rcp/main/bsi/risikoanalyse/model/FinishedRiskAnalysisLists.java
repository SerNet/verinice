package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;

import sernet.gs.model.Gefaehrdung;

public class FinishedRiskAnalysisLists {
	
	private int dbId;
	private int finishedRiskAnalysisId;
	
	/*
	 * list of all Gefaehrdungen of type GefaehrdungsUmsetzung 
	 */
	private ArrayList<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen = new ArrayList<GefaehrdungsUmsetzung>();

	/*
	 * list of Gefaehrdungen associated to the chosen IT-system 
	 */
	private ArrayList<Gefaehrdung> associatedGefaehrdungen = new ArrayList<Gefaehrdung>();

	/*
	 * list of Gefaehrdungen, which need further processing 
	 */
	private ArrayList<Gefaehrdung> notOKGefaehrdungen = new ArrayList<Gefaehrdung>();

	/*
	 * list of Gefaehrdungen, which need additional security measures 
	 * 
	 */
	private ArrayList<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen = new ArrayList<GefaehrdungsUmsetzung>();

	public FinishedRiskAnalysisLists(int analysisId,
			ArrayList<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen,
			ArrayList<Gefaehrdung> associatedGefaehrdungen,
			ArrayList<Gefaehrdung> notOKGefaehrdungen,
			ArrayList<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen) {
		this.finishedRiskAnalysisId = analysisId;
		this.allGefaehrdungsUmsetzungen = allGefaehrdungsUmsetzungen;
		this.associatedGefaehrdungen = associatedGefaehrdungen;
		this.notOKGefaehrdungen = notOKGefaehrdungen;
		this.notOKGefaehrdungsUmsetzungen = notOKGefaehrdungsUmsetzungen;
	}
	
	public FinishedRiskAnalysisLists() {
		
	}

	public ArrayList<GefaehrdungsUmsetzung> getAllGefaehrdungsUmsetzungen() {
		return allGefaehrdungsUmsetzungen;
	}

	public ArrayList<Gefaehrdung> getAssociatedGefaehrdungen() {
		return associatedGefaehrdungen;
	}

	public ArrayList<Gefaehrdung> getNotOKGefaehrdungen() {
		return notOKGefaehrdungen;
	}

	public ArrayList<GefaehrdungsUmsetzung> getNotOKGefaehrdungsUmsetzungen() {
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
			ArrayList<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen) {
		this.allGefaehrdungsUmsetzungen = allGefaehrdungsUmsetzungen;
	}

	public void setAssociatedGefaehrdungen(
			ArrayList<Gefaehrdung> associatedGefaehrdungen) {
		this.associatedGefaehrdungen = associatedGefaehrdungen;
	}

	public void setNotOKGefaehrdungen(ArrayList<Gefaehrdung> notOKGefaehrdungen) {
		this.notOKGefaehrdungen = notOKGefaehrdungen;
	}

	public void setNotOKGefaehrdungsUmsetzungen(
			ArrayList<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen) {
		this.notOKGefaehrdungsUmsetzungen = notOKGefaehrdungsUmsetzungen;
	}
}
