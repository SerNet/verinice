package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.List;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.FinishedRiskAnalysisLists;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahme;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class FindRiskAnalysisListsByParentID extends GenericCommand {

	
	private static final String QUERY_FIND_BY_PARENT_ID = "from "
		+ FinishedRiskAnalysisLists.class.getName() + " as element "
		+ "where element.finishedRiskAnalysisId = ?";
	
	
	private Integer id;

	private FinishedRiskAnalysisLists foundLists;

	public FindRiskAnalysisListsByParentID(Integer id) {
		this.id = id;
	}

	public void execute() {
		List list = getDaoFactory().getDAO(RisikoMassnahme.class).findByQuery(QUERY_FIND_BY_PARENT_ID,
				new Integer[] {id});
		for (Object object : list) {
			this.foundLists = (FinishedRiskAnalysisLists) object;
		}
	}

	public FinishedRiskAnalysisLists getFoundLists() {
		return foundLists;
	}

}
