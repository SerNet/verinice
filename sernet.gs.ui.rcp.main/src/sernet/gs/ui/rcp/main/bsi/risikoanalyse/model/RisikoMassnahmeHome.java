package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadElementByType;
import sernet.gs.ui.rcp.main.service.crudcommands.RemoveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.taskcommands.FindRisikomassnahmeByNumber;

public class RisikoMassnahmeHome {
	
	private static RisikoMassnahmeHome instance;
	private ICommandService commandService;

	private RisikoMassnahmeHome() {
		commandService = ServiceFactory.lookupCommandService();
	}
	
	public synchronized static RisikoMassnahmeHome getInstance() {
		if (instance == null)
			instance = new RisikoMassnahmeHome();
		return instance;
	}
	
	
	public void save(RisikoMassnahme mn) throws Exception {
		SaveElement<RisikoMassnahme> command = new SaveElement<RisikoMassnahme>(mn);
		commandService.executeCommand(command);
	}
	
	public void remove(RisikoMassnahme mn) throws Exception {
		RemoveElement<RisikoMassnahme> command = new RemoveElement<RisikoMassnahme>(mn);
		commandService.executeCommand(command);
	}
	
	public List<RisikoMassnahme> loadAll() throws RuntimeException {
		LoadElementByType<RisikoMassnahme> command = new LoadElementByType<RisikoMassnahme>(RisikoMassnahme.class);
		commandService.executeCommand(command);
		return command.getElements();
	}
	
	public RisikoMassnahme loadByNumber(String number) {
		FindRisikomassnahmeByNumber command = new FindRisikomassnahmeByNumber(number);
		commandService.executeCommand(command);
		return command.getMassnahme();
	}		
	
}
