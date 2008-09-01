package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class FinishedRiskAnalysis extends CnATreeElement{

	public static final String TYPE_ID = "riskanalysis";
	
	@Override
	public String getTitle() {
		return "Risikoanalyse";
	}

	@Override
	public String getTypeId() {
		return this.TYPE_ID;
	}

}
