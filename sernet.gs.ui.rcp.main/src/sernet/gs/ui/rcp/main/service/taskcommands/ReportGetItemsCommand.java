package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.reports.IBSIReport;
import sernet.gs.ui.rcp.main.reports.Report;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;

public class ReportGetItemsCommand extends GenericCommand {

	private IBSIReport report;
	private ArrayList<CnATreeElement> items;

	public ReportGetItemsCommand(IBSIReport report) {
		this.report = report;
	}

	public void execute() {
		try {
			LoadBSIModel command = new LoadBSIModel();
			getCommandService().executeCommand(command);
			BSIModel model = command.getModel();
			((Report)report).setModel(model);
			
			items = report.getItems();
			for (CnATreeElement item : items) {
				IBaseDao<Object, Serializable> dao = getDaoFactory().getDAOForObject(item);
				HydratorUtil.hydrateElement(dao, item, false);
			}
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
		
		report = null;
	}

	public ArrayList<CnATreeElement> getItems() {
		return items;
	}

}
