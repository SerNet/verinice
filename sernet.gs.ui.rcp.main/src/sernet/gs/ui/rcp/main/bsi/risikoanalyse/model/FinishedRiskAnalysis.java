package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.CnaStructureHelper;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class FinishedRiskAnalysis extends CnATreeElement  {

	public static final String TYPE_ID = "riskanalysis";

	public FinishedRiskAnalysis(CnATreeElement cnaElement) {
		setParent(cnaElement);
	}
	
	private FinishedRiskAnalysis() {
		// hibernate constructor
	}
	

	@Override
	public String getTitel() {
		return "Risikoanalyse";
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof GefaehrdungsUmsetzung)
			return true;
		return CnaStructureHelper.canContain(obj);
	}

}
