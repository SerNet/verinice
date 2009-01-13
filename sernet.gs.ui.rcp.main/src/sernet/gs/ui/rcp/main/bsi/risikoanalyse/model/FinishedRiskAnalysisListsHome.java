package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRiskAnalysisListsByParentID;

public class FinishedRiskAnalysisListsHome {
	
	
	private static FinishedRiskAnalysisListsHome instance;

	private FinishedRiskAnalysisListsHome() {
	}
	
	public synchronized static FinishedRiskAnalysisListsHome getInstance() {
		if (instance == null)
			instance = new FinishedRiskAnalysisListsHome();
		return instance;
	}
	
	public void saveNew(FinishedRiskAnalysisLists list) throws Exception {
		SaveElement<FinishedRiskAnalysisLists> command = new SaveElement<FinishedRiskAnalysisLists>(list);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
	}

	public void update(FinishedRiskAnalysisLists list) throws Exception {
		SaveElement<FinishedRiskAnalysisLists> command = new SaveElement<FinishedRiskAnalysisLists>(list);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
	}
	
	public void remove(FinishedRiskAnalysisLists list) throws Exception {
		RemoveElement<FinishedRiskAnalysisLists> command = new RemoveElement<FinishedRiskAnalysisLists>(list);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
	}
	
	public FinishedRiskAnalysisLists loadById(int id) throws CommandException {
		FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(id);
		command = ServiceFactory.lookupCommandService().executeCommand(command);
		return command.getFoundLists();
	}
}
