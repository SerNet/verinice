package sernet.gs.ui.rcp.main.bsi.model;

import org.eclipse.core.internal.localstore.CopyVisitor;

import com.sun.star.text.SetVariableType;

import sernet.gs.model.Massnahme;

public class MassnahmenFactory {

	/**
	 * Create MassnahmenUmsetzung (control instance) and add to given BausteinUmsetzung (module instance).
	 * @param bu
	 * @param mn
	 */
	public void createMassnahmenUmsetzung(BausteinUmsetzung bu, Massnahme mn) {
		MassnahmenUmsetzung mu = new MassnahmenUmsetzung(bu);
		copyValues(mn, mu);
		bu.addChild(mu);
	}

	/**
	 * Creyte MassnahmenUmsetzung (control instance) from given Massnahme (control).
	 * 
	 * @param mn
	 * @return
	 */
	public MassnahmenUmsetzung createMassnahmenUmsetzung(Massnahme mn) {
		MassnahmenUmsetzung mu = new MassnahmenUmsetzung();
		copyValues(mn, mu);
		return mu;
	}
	
	private void copyValues(Massnahme mn, MassnahmenUmsetzung mu) {
		mu.setKapitel(mn.getId());
		mu.setUrl(mn.getUrl());
		mu.setName(mn.getTitel());
		mu.setLebenszyklus(mn.getLZAsString());
		mu.setStufe(mn.getSiegelstufe());
		mu.setStand(mn.getStand());
		mu.setVerantwortlicheRollenInitiierung(mn.getVerantwortlichInitiierung());
		mu.setVerantwortlicheRollenUmsetzung(mn.getVerantwortlichUmsetzung());
	}
	
	
}
