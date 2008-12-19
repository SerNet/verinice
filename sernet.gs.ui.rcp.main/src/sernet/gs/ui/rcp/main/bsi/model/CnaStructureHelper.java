package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysis;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;

/**
 * Helper class to verify basic business logic rules for main tree,
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public final class CnaStructureHelper {

	public static boolean canContain(Object obj) {
		if (obj instanceof BausteinUmsetzung)
			return true;
		if (obj instanceof Baustein)
			return true;
		if (obj instanceof LinkKategorie)
			return true;
		if (obj instanceof FinishedRiskAnalysis)
			return true;
		return false;
	}
}
