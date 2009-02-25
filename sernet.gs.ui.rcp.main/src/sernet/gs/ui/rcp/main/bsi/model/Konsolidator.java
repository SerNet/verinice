package sernet.gs.ui.rcp.main.bsi.model;

import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * The Konsolidator copys values from one object to another,
 * filling in values that the user has already entered. 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class Konsolidator {

	/**
	 * Copy values for all Massnahmen from one BausteinUmsetzung to another.
	 * 
	 * @param source
	 * @param target
	 */
	public static void konsolidiereMassnahmen(BausteinUmsetzung source,
			BausteinUmsetzung target) {
		if (!source.getKapitel().equals(target.getKapitel()))
			return;
		
		for (MassnahmenUmsetzung mn: target.getMassnahmenUmsetzungen()) {
			MassnahmenUmsetzung sourceMn = source.getMassnahmenUmsetzung(mn.getUrl());
			if (sourceMn != null) {
				mn.getEntity().copyEntity(sourceMn.getEntity());
			}
		}
	}

	/**
	 * Copy values from one BausteinUmsetzung to another.
	 * @param source
	 * @param target
	 */
	public static void konsolidiereBaustein(BausteinUmsetzung source, 
			BausteinUmsetzung target) {
		target.getEntity().copyEntity(source.getEntity());
	}

}
