package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;

import sernet.gs.model.Gefaehrdung;

public class FinishedRiskAnalysisLists {
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

	public FinishedRiskAnalysisLists(
			ArrayList<GefaehrdungsUmsetzung> allGefaehrdungsUmsetzungen,
			ArrayList<Gefaehrdung> associatedGefaehrdungen,
			ArrayList<Gefaehrdung> notOKGefaehrdungen,
			ArrayList<GefaehrdungsUmsetzung> notOKGefaehrdungsUmsetzungen) {
		this.allGefaehrdungsUmsetzungen = allGefaehrdungsUmsetzungen;
		this.associatedGefaehrdungen = associatedGefaehrdungen;
		this.notOKGefaehrdungen = notOKGefaehrdungen;
		this.notOKGefaehrdungsUmsetzungen = notOKGefaehrdungsUmsetzungen;
	}
	
	private FinishedRiskAnalysisLists() {
		// hibernate constructor
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
}
