package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.ui.rcp.main.bsi.model.CnaStructureHelper;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

public class FinishedRiskAnalysis extends CnATreeElement{

	public static final String TYPE_ID = "riskanalysis";
	
	public FinishedRiskAnalysis(CnATreeElement cnaElement) {
		setParent(cnaElement);
	}

	@Override
	public String getTitle() {
		return "Risikoanalyse";
	}

	@Override
	public String getTypeId() {
		return this.TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof MassnahmenUmsetzung
				|| obj instanceof GefaehrdungsUmsetzung)
			return true;
		return CnaStructureHelper.canContain(obj);
	}

}
